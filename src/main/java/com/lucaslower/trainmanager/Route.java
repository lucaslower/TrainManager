package com.lucaslower.trainmanager;

import com.lucaslower.trainmanager.Util.TrainManagerSaveData;
import net.minecraft.nbt.CompoundNBT;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Route {
    private final LinkedList<Target> targets = new LinkedList<>();
    private final String routeName;

    public Route(String name){
        this.routeName = name;
    }

    public static Route fromNBT(CompoundNBT nbt){
        Route r = new Route(nbt.getString("routeName"));
        CompoundNBT savetargets = nbt.getCompound("targets");
        for(int i=0;savetargets.contains("target"+i);i++){
            Target target = Target.fromNBT(savetargets.getCompound("target" + i));
            r.targets.add(target);
        }
        return r;
    }

    public CompoundNBT toNBT(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("routeName", this.routeName);

        CompoundNBT savetargets = new CompoundNBT();
        int i = 0;
        for (Target target : getTargets()) {
            savetargets.put("target" + i++, target.toNBT());
        }
        nbt.put("targets", savetargets);
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
