package com.workshopcraft.nodeores.node;

//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
*///?}

public final class NodeIo {
    private NodeIo() {}

    public static final class Data {
        public int ax;
        public int ay;
        public int az;
        public int ox = 1;
        public int oz = 1;
        public int ticks;
        public int wait;
        public boolean anchor;
        public String ore = "";
    }

    //? if >=1.21.6 {
    public static void read(Data data, ValueInput in) {
        data.ax = in.getIntOr("ax", 0);
        data.ay = in.getIntOr("ay", 0);
        data.az = in.getIntOr("az", 0);
        data.ox = in.getIntOr("ox", 1);
        data.oz = in.getIntOr("oz", 1);
        data.ticks = in.getIntOr("ticks", 0);
        data.wait = in.getIntOr("wait", 0);
        data.anchor = in.getBooleanOr("anchor", false);
        data.ore = in.getStringOr("ore", "");
    }

    public static void write(Data data, ValueOutput out) {
        out.putInt("ax", data.ax);
        out.putInt("ay", data.ay);
        out.putInt("az", data.az);
        out.putInt("ox", data.ox);
        out.putInt("oz", data.oz);
        out.putInt("ticks", data.ticks);
        out.putInt("wait", data.wait);
        out.putBoolean("anchor", data.anchor);
        out.putString("ore", data.ore == null ? "" : data.ore);
    }
    //?} else {
    /*public static void read(Data data, CompoundTag in) {
        data.ax = in.getInt("ax");
        data.ay = in.getInt("ay");
        data.az = in.getInt("az");
        data.ox = in.getInt("ox");
        data.oz = in.getInt("oz");
        data.ticks = in.getInt("ticks");
        data.wait = in.getInt("wait");
        data.anchor = in.getBoolean("anchor");
        data.ore = in.getString("ore");
    }

    public static void write(Data data, CompoundTag out) {
        out.putInt("ax", data.ax);
        out.putInt("ay", data.ay);
        out.putInt("az", data.az);
        out.putInt("ox", data.ox);
        out.putInt("oz", data.oz);
        out.putInt("ticks", data.ticks);
        out.putInt("wait", data.wait);
        out.putBoolean("anchor", data.anchor);
        out.putString("ore", data.ore == null ? "" : data.ore);
    }
    *///?}
}
