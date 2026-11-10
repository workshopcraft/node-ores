package com.workshopcraft.nodeores.node;

import com.workshopcraft.nodeores.registry.NodeContent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
//? if >=1.20.5
import net.minecraft.core.HolderLookup;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

public class NodeBaseBlockEntity extends BlockEntity {
    private final NodeIo.Data data = new NodeIo.Data();
    private int clock;

    public NodeBaseBlockEntity(BlockPos pos, BlockState state) {
        super(NodeContent.BASE_ENTITY, pos, state);
    }

    public void configure(BlockPos anchor, int ox, int oz, boolean anchorFlag, int wait) {
        data.ax = anchor.getX();
        data.ay = anchor.getY();
        data.az = anchor.getZ();
        data.ox = ox == 0 ? 1 : Integer.signum(ox);
        data.oz = oz == 0 ? 1 : Integer.signum(oz);
        data.anchor = anchorFlag;
        data.wait = wait;
        sync();
    }

    public BlockPos anchorPos() {
        return new BlockPos(data.ax, data.ay, data.az);
    }

    public boolean isAnchor() {
        return data.anchor;
    }

    public int stepX() {
        return data.ox == 0 ? 1 : data.ox;
    }

    public int stepZ() {
        return data.oz == 0 ? 1 : data.oz;
    }

    public int spawnWait() {
        return data.wait;
    }

    public void setSpawnWait(int wait) {
        data.wait = wait;
        setChanged();
    }

    public void tick() {
        if (level == null || level.isClientSide() || !data.anchor) return;
        if (++clock < 20) return;
        clock = 0;
        com.workshopcraft.nodeores.node.NodeLogic.pulse(this);
    }

    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    //? if >=1.21.6 {
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        NodeIo.read(data, input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        NodeIo.write(data, output);
    }
    //?} else if >=1.20.5 {
    /*@Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        NodeIo.read(data, tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        NodeIo.write(data, tag);
    }
    *///?} else {
    /*@Override
    public void load(CompoundTag tag) {
        super.load(tag);
        NodeIo.read(data, tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        NodeIo.write(data, tag);
    }
    *///?}

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    //? if >=1.20.5 {
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }
    //?} else {
    /*@Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
    *///?}
}
