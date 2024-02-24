package com.lucaslower.trainmanager.Events;

import com.lucaslower.trainmanager.Train;
import com.lucaslower.trainmanager.TrainManagerMain;
import com.lucaslower.trainmanager.Commands.TrainCommands;
import com.lucaslower.trainmanager.Commands.RouteCommands;
import com.lucaslower.trainmanager.Util.TrainManagerSaveData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
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
    public static void onRegisterCommands(RegisterCommandsEvent event){
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
    public static void onFMLServerStopping(FMLServerStoppingEvent event){
        writers.forEach(PrintWriter::close);
        TrainManagerSaveData.markDirty();
    }

    @SubscribeEvent
    public static void onFMLServerStarted(FMLServerStartedEvent event){
        event.getServer().overworld().getDataStorage().computeIfAbsent(TrainManagerSaveData::new, TrainManagerSaveData.NAME);
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event){
        if(event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
            Collection<Train> trains = TrainManagerSaveData.getTrains(ServerLifecycleHooks.getCurrentServer().overworld()).values();
            int curY = 2;
            FontRenderer fr = Minecraft.getInstance().font;
            for(Train train : trains){
                fr.draw(event.getMatrixStack(), train.toString(), 2, curY, 0xFFFFFFFF);
                curY += fr.lineHeight + 2;
            }

        }
    }
}
