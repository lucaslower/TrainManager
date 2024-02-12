package com.lucaslower.trainmanager;

import net.minecraft.nbt.CompoundNBT;

public class Target {
    private final double targetX;
    private final double targetZ;
    private final String targetName;
    private final double speed;
    private final TargetType type;

    public Target(TargetType type, String name, double x, double z, double targetSpeed) {
        this.type = type;
        this.targetName = name;
        this.targetX = x;
        this.targetZ = z;
        this.speed = targetSpeed;
    }

    public static Target newStation(String name, double x, double z){
        return new Target(TargetType.STATION_STOP, name, x, z, 0.0);
    }

    public static Target newSpeedChange(double speed, double x, double z){
        return new Target(TargetType.SPEED_CHANGE, "SpeedLimit"+speed, x, z, speed);
    }

    public static Target fromNBT(CompoundNBT nbt) {
        TargetType tt = TargetType.valueOf(nbt.getString("type"));
        Target t = null;
        switch(tt){
            case STATION_STOP:
                t = newStation(nbt.getString("name"), nbt.getDouble("x"), nbt.getDouble("z"));
                break;
            case SPEED_CHANGE:
                t = newSpeedChange(nbt.getDouble("speed"), nbt.getDouble("x"), nbt.getDouble("z"));
                break;
            case SWITCH:

                break;
        }
        return t;
    }

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("type", type.name());
        nbt.putDouble("x", this.targetX);
        nbt.putDouble("z", this.targetZ);
        switch(type){
            case STATION_STOP: nbt.putString("name", this.targetName); break;
            case SPEED_CHANGE: nbt.putDouble("speed", this.speed); break;
            case SWITCH: break;
        }
        return nbt;
    }

    public TargetType getTargetType(){ return this.type; }

    public String getTargetName(){ return this.targetName; }

    public double getTargetX(){ return this.targetX; }

    public double getTargetZ() { return this.targetZ; }

    public double getTargetSpeed(){ return this.speed; }

    public enum TargetType{
        STATION_STOP, SPEED_CHANGE, SWITCH;
    }
}
