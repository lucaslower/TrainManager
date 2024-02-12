package com.lucaslower.trainmanager.Events;

import com.lucaslower.trainmanager.TrainManagerMain;
import com.lucaslower.trainmanager.Commands.TrainCommands;
import com.lucaslower.trainmanager.Commands.RouteCommands;
import com.lucaslower.trainmanager.Util.TrainManagerSaveData;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber(modid = TrainManagerMain.MOD_ID)
public class ModEvents {

    private static final List<Runnable> updates = new ArrayList<>();
    private static final List<PrintWriter> writers = new ArrayList<>();

    public static void addTickUpdate(Runnable fn){
        updates.add(fn);
    }

    public static void addWriter(PrintWriter w){
        writers.add(w);
    }

    public static void removeWriter(PrintWriter w){
        writers.remove(w);
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event){
        TrainCommands.register(event.getDispatcher());
        RouteCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event){
        if(event.phase == TickEvent.Phase.END){
            updates.forEach(Runnable::run);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(FMLServerStoppingEvent event){
        writers.forEach(PrintWriter::close);
        TrainManagerSaveData.markDirty();
    }

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartedEvent event){
        event.getServer().overworld().getDataStorage().computeIfAbsent(TrainManagerSaveData::new, TrainManagerSaveData.NAME);
    }
}
