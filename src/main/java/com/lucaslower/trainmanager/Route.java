package com.lucaslower.trainmanager;

import com.lucaslower.trainmanager.Util.TrainManagerSaveData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class Route {
    private LinkedList<Target> targets = new LinkedList<>();
    private final String routeName;

    public Route(String name){
        this.routeName = name;
    }

    public static Route fromNBT(CompoundNBT nbt){
        Route r = new Route(nbt.getString("routeName"));
        r.targets = nbt.getList("targets", Constants.NBT.TAG_COMPOUND).stream().map((tag) -> Target.fromNBT((CompoundNBT) tag)).collect(Collectors.toCollection(LinkedList::new));
        return r;
    }

    public CompoundNBT toNBT(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("routeName", this.routeName);
        ListNBT targetList = targets.stream().map(Target::toNBT).collect(Collectors.toCollection(ListNBT::new));
        nbt.put("targets", targetList);
        return nbt;
    }

    public String getRouteName(){
        return this.routeName;
    }

    public LinkedList<Target> getTargets(){ return targets; }

    public LinkedList<Target> getTargets(Target.TargetType type){ return targets.stream().filter((tgt) -> tgt.getTargetType() == type).collect(Collectors.toCollection(LinkedList::new)); }

    public void addTarget(Target target){
        targets.add(target);
        TrainManagerSaveData.markDirty();
    }
}
