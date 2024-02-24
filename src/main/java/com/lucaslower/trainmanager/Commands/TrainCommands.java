package com.lucaslower.trainmanager.Commands;

import com.lucaslower.trainmanager.Train;
import com.lucaslower.trainmanager.Util.TrainManagerSaveData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;

import java.util.Map;

public class TrainCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(
            LiteralArgumentBuilder.<CommandSource>literal("trainmanager")
            .then(Commands.literal("trains")
                .executes(TrainCommands::listTrains)
                // CREATE TRAIN
                .then(Commands.literal("create")
                    .then(Commands.argument("trainID", StringArgumentType.word())
                    .then(Commands.argument("leadLoco", EntityArgument.entity())
                    .then(Commands.argument("routeName", StringArgumentType.word())
                        .executes(TrainCommands::createTrain)
                    )
                    )
                    )
                )
                .then(Commands.argument("trainID", StringArgumentType.word())
                    .then(Commands.literal("setLoco")
                        .then(Commands.argument("leadLoco", EntityArgument.entity())
                            .executes(TrainCommands::setLeadLoco)
                        )
                    )
                    .then(Commands.literal("setRoute")
                        .then(Commands.argument("routeName", StringArgumentType.word())
                            .executes(TrainCommands::setRouteName)
                        )
                    )
                    .then(Commands.literal("setNextTarget")
                        .then(Commands.argument("nextTargetNum", IntegerArgumentType.integer())
                            .executes(TrainCommands::setNextTarget)
                        )
                    )
                    .then(Commands.literal("enable")
                        .executes(TrainCommands::enableTrain)
                    )
                    .then(Commands.literal("disable")
                        .executes(TrainCommands::disableTrain)
                    )
                    .then(Commands.literal("broadcastUpdates")
                        .then(Commands.argument("truefalse", BoolArgumentType.bool())
                            .executes(TrainCommands::updateBroadcastState)
                        )
                    )
                )
            )
        );
    }

    private static int listTrains(CommandContext<CommandSource> cmd){
        CommandSource src = cmd.getSource();
        Map<String, Train> trains = TrainManagerSaveData.getTrains(src.getLevel());
        if(trains.isEmpty()){
            src.sendSuccess(new StringTextComponent("There are no registered trains."), true);
        }
        else{
            src.sendSuccess(new StringTextComponent("Registered trains:"), true);
            for(Train t : trains.values()){
                src.sendSuccess(new StringTextComponent("ID: " + t.getTrainID() + ", lead loco: " + t.getLeadLoco().getUUID().toString()), true);
            }
        }
        return 1;
    }

    private static int createTrain(CommandContext<CommandSource> cmd) {
        return Validation.ifRouteExists(cmd, (r) -> {
            final Entity leadLoco;
            try {
                leadLoco = EntityArgument.getEntity(cmd, "leadLoco");
            } catch (CommandSyntaxException e) {
                cmd.getSource().sendFailure(new StringTextComponent("Error: invalid entity given for lead locomotive."));
                return 0;
            }
            final String trainID = StringArgumentType.getString(cmd, "trainID");
            Train t = new Train(leadLoco.getUUID(), trainID, r.getRouteName());
            TrainManagerSaveData.saveTrain(t, cmd.getSource().getLevel());
            cmd.getSource().sendSuccess(new StringTextComponent("Train '" + t.getTrainID() + "' created, lead loco set to " + t.getLeadLoco().getUUID().toString()), true);
            return 1;
        });
    }

    private static int setLeadLoco(CommandContext<CommandSource> cmd) {
        return Validation.ifTrainExists(cmd, (t) -> {
            final Entity leadLoco;
            try {
                leadLoco = EntityArgument.getEntity(cmd, "leadLoco");
            } catch (CommandSyntaxException e) {
                cmd.getSource().sendFailure(new StringTextComponent("Error: invalid entity given for lead locomotive."));
                return 0;
            }
            t.setLeadLoco(leadLoco.getUUID());
            cmd.getSource().sendSuccess(new StringTextComponent("Lead loco set to " + t.getLeadLoco().getUUID().toString() + " for train " + t.getTrainID()), true);
            return 1;
        });
    }

    private static int setRouteName(CommandContext<CommandSource> cmd) {
        return Validation.ifTrainExists(cmd, (t) -> {
            String routeName = StringArgumentType.getString(cmd, "routeName");
            if(TrainManagerSaveData.getRoutes(cmd.getSource().getLevel()).containsKey(routeName)){
                t.setRouteName(routeName);
                cmd.getSource().sendSuccess(new StringTextComponent("Route name set to '" + routeName + "' for train "+ t.getTrainID()), true);
                return 1;
            }
            else{
                cmd.getSource().sendFailure(new StringTextComponent("Error: route '" + routeName + "' does not exist."));
            }
            return 0;
        });
    }

    private static int setNextTarget(CommandContext<CommandSource> cmd) {
        return Validation.ifTrainExists(cmd, (t) -> {
            int num = IntegerArgumentType.getInteger(cmd, "nextTargetNum");
            int trSize = t.getRoute().getTargets().size();
            if(num >= 0 && num < trSize){
                t.setNextTarget(num);
                cmd.getSource().sendSuccess(new StringTextComponent("Next target set to " + num + " for train "+ t.getTrainID()), true);
                return 1;
            }
            else{
                cmd.getSource().sendFailure(new StringTextComponent("Error: target number " + num + " out of bounds. Must be between 0 and " + (trSize - 1) + ", inclusive."));
            }
            return 0;
        });
    }

    private static int disableTrain(CommandContext<CommandSource> cmd) {
        return Validation.ifTrainExists(cmd, (t) -> {
            t.disable();
            cmd.getSource().sendSuccess(new StringTextComponent("Train "+ t.getTrainID() + " disabled."), true);
            return 1;
        });
    }

    private static int enableTrain(CommandContext<CommandSource> cmd) {
        return Validation.ifTrainExists(cmd, (t) -> {
            t.enable();
            cmd.getSource().sendSuccess(new StringTextComponent("Train "+ t.getTrainID() + " enabled."), true);
            return 1;
        });
    }

    private static int updateBroadcastState(CommandContext<CommandSource> cmd) {
        return Validation.ifTrainExists(cmd, (t) -> {
            boolean on = BoolArgumentType.getBool(cmd, "truefalse");
            t.setBroadcast(on);
            cmd.getSource().sendSuccess(new StringTextComponent("Train "+ t.getTrainID() + (on ? " broadcasting status updates." : " stopped broadcasting updates.")), true);
            return 1;
        });
    }


}
