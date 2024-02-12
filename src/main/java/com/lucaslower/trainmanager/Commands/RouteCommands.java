package com.lucaslower.trainmanager.Commands;

import com.lucaslower.trainmanager.Route;
import com.lucaslower.trainmanager.Target;
import com.lucaslower.trainmanager.Target.TargetType;
import com.lucaslower.trainmanager.Util.TrainManagerSaveData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

import java.util.LinkedList;
import java.util.Map;

public class RouteCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            LiteralArgumentBuilder.<CommandSource>literal("trainmanager")
            .then(Commands.literal("routes")
                .executes(RouteCommands::listRoutes)
                .then(Commands.literal("create")
                    .then(Commands.argument("routeName", StringArgumentType.word())
                        .executes(RouteCommands::createRoute)
                    )
                )
                .then(Commands.argument("routeName", StringArgumentType.word())
                    .then(Commands.literal("addStop")
                        .then(Commands.argument("stationName", StringArgumentType.word())
                            .executes(RouteCommands::createRouteStop)
                        )
                    )
                    .then(Commands.literal("addSpeedChange")
                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                            .executes(RouteCommands::createRouteSpeedChange)
                        )
                    )
                )
            )
        );
    }

    private static int createRouteStop(CommandContext<CommandSource> cmd) {
        String routeName = StringArgumentType.getString(cmd, "routeName");
        String stationName = StringArgumentType.getString(cmd, "stationName");

        if(TrainManagerSaveData.getRoutes(cmd.getSource().getLevel()).containsKey(routeName)){
            Target newTarget = Target.newStation(stationName, cmd.getSource().getPosition().x, cmd.getSource().getPosition().z);
            TrainManagerSaveData.createRouteTarget(newTarget, routeName, cmd.getSource().getLevel());
            cmd.getSource().sendSuccess(new StringTextComponent("Stop '" + newTarget.getTargetName() + "' created at present location for route " + routeName), true);
        }
        else{
            cmd.getSource().sendFailure(new StringTextComponent("Error: route '" + routeName + "' does not exist."));
        }
        return 1;
    }

    private static int createRouteSpeedChange(CommandContext<CommandSource> cmd) {
        String routeName = StringArgumentType.getString(cmd, "routeName");
        double speed = DoubleArgumentType.getDouble(cmd, "speed");

        if(TrainManagerSaveData.getRoutes(cmd.getSource().getLevel()).containsKey(routeName)){
            Target newTarget = Target.newSpeedChange(speed, cmd.getSource().getPosition().x, cmd.getSource().getPosition().z);
            TrainManagerSaveData.createRouteTarget(newTarget, routeName, cmd.getSource().getLevel());
            cmd.getSource().sendSuccess(new StringTextComponent("Speed Limit of " + speed + "mph created at present location for route " + routeName), true);
        }
        else{
            cmd.getSource().sendFailure(new StringTextComponent("Error: route '" + routeName + "' does not exist."));
        }


        return 1;
    }

    private static int createRoute(CommandContext<CommandSource> cmd) {
        String routeName = StringArgumentType.getString(cmd, "routeName");
        Route r = new Route(routeName);
        TrainManagerSaveData.createRoute(r, cmd.getSource().getLevel());
        cmd.getSource().sendSuccess(new StringTextComponent("Route '" + r.getRouteName() + "' created, please add stops in order from route start to end"), true);
        return 1;
    }

    private static int listRoutes(CommandContext<CommandSource> cmd) {
        CommandSource src = cmd.getSource();
        Map<String, Route> routes = TrainManagerSaveData.getRoutes(src.getLevel());
        if(routes.isEmpty()){
            src.sendSuccess(new StringTextComponent("There are no registered routes."), true);
        }
        else{
            src.sendSuccess(new StringTextComponent("Registered routes:"), true);
            for(Route r : routes.values()){
                LinkedList<Target> targets = r.getTargets(TargetType.STATION_STOP);
                src.sendSuccess(new StringTextComponent("Name: " + r.getRouteName() + ", first stop: " + targets.getFirst().getTargetName() + ", last stop: " + targets.getLast().getTargetName()), true);
            }
        }
        return 1;
    }
}
