package com.lucaslower.trainmanager;

import cam72cam.immersiverailroading.thirdparty.CommonAPI;
import cam72cam.mod.world.World;
import com.lucaslower.trainmanager.Events.ModEvents;
import com.lucaslower.trainmanager.Util.Chat;
import com.lucaslower.trainmanager.Util.TrainManagerSaveData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import cam72cam.immersiverailroading.entity.Locomotive;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

public class Train{

    private static final double MAX_ACCELERATION = 0.5;
    private static final double STOPPING_DECELERATION = -1;
    private static final double SLOWING_DECELERATION = -0.5;
    private static final double MAX_SPEED_MPH = 55.0;

    private Locomotive leadLoco;
    private CommonAPI trainAPI;
    private final String trainID;
    private String routeName;
    private boolean enabled = false;
    private int targetNum = 0;
    private double currentMaxSpeed = MAX_SPEED_MPH / 2.237;
    private double previousSpeed = 0.0;
    private double previousDistance = 0.0;
    private boolean stopping = false;
    private boolean slowing = false;

    private double currentThrottle = 0.0;
    private double currentBrake = 0.0;
    private double currentLocoBrake = 0.0;

    private boolean waitingAtStation = false;
    private int waitTicks = 600;
    private boolean broadcasting = false;

    public Train(UUID leadLocoUUID, String trainID, String routeName, int nextStop){
        this.trainID = trainID;
        setLeadLoco(leadLocoUUID);
        this.routeName = routeName;
        this.targetNum = nextStop;

        ModEvents.addTickUpdate(this::doUpdate);
    }
    public static Train fromNBT(CompoundNBT nbt) {
        return new Train(nbt.getUUID("leadLocoUUID"), nbt.getString("trainID"), nbt.getString("routeName"), nbt.getInt("nextStop"));
    }

    public CompoundNBT toNBT(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("leadLocoUUID", leadLoco.getUUID());
        nbt.putString("trainID", trainID);
        nbt.putString("routeName", routeName);
        nbt.putInt("nextStop", targetNum);
        return nbt;
    }

    public void setBroadcast(boolean on){
        broadcasting = on;
    }

    public Locomotive getLeadLoco(){
        return leadLoco;
    }

    public void setLeadLoco(UUID leadLocoUUID){
        List<Locomotive> locos = World.get(ServerLifecycleHooks.getCurrentServer().overworld()).getEntities(Locomotive.class);
        for(Locomotive loco : locos){
            if(loco.getUUID().equals(leadLocoUUID)){
                this.leadLoco = loco;
                this.trainAPI = new CommonAPI(loco);
            }
        }
    }

    public String getTrainID(){
        return trainID;
    }

    public Route getRoute(){
        return TrainManagerSaveData.getRoute(ServerLifecycleHooks.getCurrentServer().overworld(), routeName);
    }

    public void setRouteName(String name){
        this.routeName = name;
        TrainManagerSaveData.markDirty();
    }

    public void setNextTarget(int num){
        this.targetNum = num;
        TrainManagerSaveData.markDirty();
    }

    public void enable(){
        this.enabled = true;
        trainAPI.setIgnition(true);
        trainAPI.setReverser(1.0);
    }
    public void disable(){
        this.enabled = false;
        trainAPI.setThrottle(0.0);
        trainAPI.setTrainBrake(1.0);
        trainAPI.setReverser(0.0);
        trainAPI.setIgnition(false);
    }

    public void doUpdate(){
        if(enabled){
            if(waitingAtStation){
                if(waitTicks == 0){
                    waitingAtStation = false;
                    stopping = false;
                    targetNum += 1;
                    if(targetNum == getRoute().getTargets().size()){
                        targetNum = 0;
                    }
                    if(broadcasting) {
                        Chat.broadcastMessage("Train departing. Next target: " + getRoute().getTargets().get(targetNum).getTargetName());
                    }
                }
                else{
                    waitTicks--;
                }
            }
            else{

                // Get location for next action (stopping at station, throwing switch, changing speed, etc)
                Target currentTarget = getRoute().getTargets().get(targetNum);
                double targetX = currentTarget.getTargetX();
                double targetZ = currentTarget.getTargetZ();

                // Current train conditions
                double currentX = leadLoco.getPosition().x;
                double currentZ = leadLoco.getPosition().z;
                double currentSpeed = leadLoco.getCurrentSpeed().metersPerSecond();
                double acceleration = currentSpeed - previousSpeed;

                // Get trigger condition
                double speedToReach = currentTarget.getTargetSpeed() / 2.237;
                double targetDeceleration = currentTarget.getTargetType() == Target.TargetType.SPEED_CHANGE ? SLOWING_DECELERATION : STOPPING_DECELERATION;
                double triggerDistance = ((speedToReach * speedToReach) - (currentSpeed * currentSpeed)) / (2 * targetDeceleration);
                double targetDistance = Math.sqrt(Math.pow(targetX - currentX, 2) + Math.pow(targetZ - currentZ, 2));
                if(currentTarget.getTargetType() == Target.TargetType.SWITCH || (currentTarget.getTargetType() == Target.TargetType.SPEED_CHANGE && speedToReach >= currentMaxSpeed && !slowing)){
                    triggerDistance = 0.0;
                }

                // Check for trigger condition
                if(targetDistance < triggerDistance+4 && targetDistance > triggerDistance-4 && !stopping && !slowing){
                    if(currentTarget.getTargetType() == Target.TargetType.STATION_STOP){
                        stopping = true;
                        if(broadcasting) {
                            Chat.broadcastMessage("Arriving at " + currentTarget.getTargetName() + ". Train stopping.");
                        }
                    }
                    else if(currentTarget.getTargetType() == Target.TargetType.SPEED_CHANGE){
                        if(speedToReach < currentMaxSpeed){
                            slowing = true;
                            if(broadcasting) {
                                Chat.broadcastMessage("Slowing for speed limit of " + currentTarget.getTargetSpeed() + "mph.");
                            }
                        }
                        else{
                            targetNum += 1;
                            if(targetNum == getRoute().getTargets().size()){
                                targetNum = 0;
                            }
                            if(broadcasting) {
                                Chat.broadcastMessage("Speed limit changed to " + currentTarget.getTargetSpeed() + "mph. Next target: " + getRoute().getTargets().get(targetNum).getTargetName());
                            }
                        }
                        currentMaxSpeed = speedToReach;
                    }
                }

                // STOPPING
                if(stopping){
                    currentThrottle = 0.0;
                    // Make sure we stop completely
                    if (targetDistance < 2.0 || targetDistance > previousDistance) {
                        currentBrake = 1.0;
                    }
                    // SLOWING
                    if(currentSpeed > 0.0){
                        // SLOWING TOO SLOW
                        if(triggerDistance > targetDistance && currentBrake < 1.0){
                            currentBrake += 0.05;
                        }
                        // SLOWING TOO FAST
                        else if(triggerDistance < targetDistance && currentBrake > 0.0){
                            currentBrake -= 0.01;
                        }
                    }
                    // STOPPED
                    else{
                        waitingAtStation = true;
                        waitTicks = 600;
                        if(broadcasting) {
                            Chat.broadcastMessage("This stop is: " + currentTarget.getTargetName() + ". Waiting 30 seconds...");
                        }
                    }
                }
                // REACHING/MAINTAINING SPEED
                else{
                    double targetSpeed = currentMaxSpeed;
                    double speedDiff = targetSpeed - currentSpeed;
                    double targetAcceleration = (speedDiff/targetSpeed) * MAX_ACCELERATION;

                    // UNDERSPEED
                    if(speedDiff > 0){
                        if(slowing){
                            slowing = false;
                            targetNum += 1;
                            if(targetNum == getRoute().getTargets().size()){
                                targetNum = 0;
                            }
                            if(broadcasting) {
                                Chat.broadcastMessage("Speed limit reached. Next target: " + getRoute().getTargets().get(targetNum).getTargetName());
                            }
                        }
                        currentBrake = 0.0;
                        currentLocoBrake = 0.0;
                        // SPEEDING UP TOO SLOW
                        if(acceleration < targetAcceleration && currentThrottle < 1.0){
                            currentThrottle += 0.01;
                        }
                        // SPEEDING UP TOO FAST
                        else if(acceleration > targetAcceleration && currentThrottle > 0){
                            currentThrottle -= 0.1;
                        }
                    }
                    // OVERSPEED
                    else if(speedDiff < 0){
                        currentThrottle = 0.0;
                        if(slowing){
                            // SLOWING TOO SLOW
                            if(triggerDistance > targetDistance && currentLocoBrake < 1.0){
                                currentLocoBrake += 0.05;
                            }
                            // SLOWING TOO FAST
                            else if(triggerDistance < targetDistance && currentLocoBrake > 0.0){
                                currentLocoBrake -= 0.01;
                            }
                        }
                        else{
                            currentLocoBrake += 0.02;
                        }

                    }
                }

                trainAPI.setTrainBrake(currentBrake);
                trainAPI.setIndependentBrake(currentLocoBrake);
                trainAPI.setThrottle(currentThrottle);

                previousDistance = targetDistance;
                previousSpeed = currentSpeed;
            }
        }
    }

}