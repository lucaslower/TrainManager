package com.lucaslower.trainmanager.Util;

import com.lucaslower.trainmanager.Route;
import com.lucaslower.trainmanager.Target;
import com.lucaslower.trainmanager.Train;
import com.lucaslower.trainmanager.TrainManagerMain;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import java.util.*;

public class TrainManagerSaveData extends WorldSavedData {

    public static final String NAME = TrainManagerMain.MOD_ID + "-SaveData";

    private final Map<String, Train> TRAINS_DATA = new HashMap<>();
    private final Map<String, Route> ROUTES_DATA = new HashMap<>();

    public TrainManagerSaveData(String p){
        super(p);
    }

    public TrainManagerSaveData(){
        this(NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {
        CompoundNBT routes = nbt.getCompound("routes");
        for(int i=0;routes.contains("route"+i);i++){
            Route r = Route.fromNBT(routes.getCompound("route" + i));
            ROUTES_DATA.put(r.getRouteName(), r);
        }

        CompoundNBT trains = nbt.getCompound("trains");
        for(int i=0;trains.contains("train"+i);i++){
            Train t = Train.fromNBT(trains.getCompound("train" + i));
            TRAINS_DATA.put(t.getTrainID(), t);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        CompoundNBT trains = new CompoundNBT();
        int i = 0;
        for (Train train : TRAINS_DATA.values()) {
            trains.put("train" + i++, train.toNBT());
        }
        nbt.put("trains", trains);

        CompoundNBT routes = new CompoundNBT();
        int j = 0;
        for (Route route : ROUTES_DATA.values()) {
            routes.put("route" + j++, route.toNBT());
        }
        nbt.put("routes", routes);

        return nbt;
    }

    public static void markDirty(){
        ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(TrainManagerSaveData::new, TrainManagerSaveData.NAME).setDirty();
    }

    // TRAINS
    public static void saveTrain(Train t, ServerWorld world){
        TrainManagerSaveData data = world.getDataStorage().computeIfAbsent(TrainManagerSaveData::new, TrainManagerSaveData.NAME);
        data.TRAINS_DATA.put(t.getTrainID(), t);
        data.setDirty();
    }
    public static Map<String, Train> getTrains(ServerWorld world){
        return world.getDataStorage().computeIfAbsent(TrainManagerSaveData::new, TrainManagerSaveData.NAME).TRAINS_DATA;
    }
    public static Train getTrain(ServerWorld world, String trainID){
        return getTrains(world).get(trainID);
    }

    // ROUTES
    public static void saveRoute(Route r, ServerWorld world){
        TrainManagerSaveData data = world.getDataStorage().computeIfAbsent(TrainManagerSaveData::new, TrainManagerSaveData.NAME);
        data.ROUTES_DATA.put(r.getRouteName(), r);
        data.setDirty();
    }
    public static Map<String, Route> getRoutes(ServerWorld world){
        return world.getDataStorage().computeIfAbsent(TrainManagerSaveData::new, TrainManagerSaveData.NAME).ROUTES_DATA;
    }
    public static Route getRoute(ServerWorld world, String routeName){
        return getRoutes(world).get(routeName);
    }

}
