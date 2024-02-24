package com.lucaslower.trainmanager.Commands;

import com.lucaslower.trainmanager.Route;
import com.lucaslower.trainmanager.Train;
import com.lucaslower.trainmanager.Util.TrainManagerSaveData;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

import java.util.Map;
import java.util.function.Function;

public class Validation {

    protected static int ifTrainExists(CommandContext<CommandSource> cmd, Function<Train, Integer> f){
        String trainID = StringArgumentType.getString(cmd, "trainID");
        Map<String, Train> trains = TrainManagerSaveData.getTrains(cmd.getSource().getLevel());
        if(trains.containsKey(trainID)){
            return f.apply(trains.get(trainID));
        }
        else{
            cmd.getSource().sendFailure(new StringTextComponent("Error: train '" + trainID + "' does not exist."));
        }
        return 0;
    }

    public static int ifRouteExists(CommandContext<CommandSource> cmd, Function<Route, Integer> f){
        String routeName = StringArgumentType.getString(cmd, "routeName");
        Map<String, Route> routes = TrainManagerSaveData.getRoutes(cmd.getSource().getLevel());
        if(routes.containsKey(routeName)){
            return f.apply(routes.get(routeName));
        }
        else{
            cmd.getSource().sendFailure(new StringTextComponent("Error: route '" + routeName + "' does not exist."));
        }
        return 0;
    }

}
