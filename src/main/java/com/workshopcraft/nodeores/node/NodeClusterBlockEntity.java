package com.workshopcraft.nodeores.node;

import com.workshopcraft.nodeores.logic.Growth;
import com.workshopcraft.nodeores.logic.NodeSettings;
import com.workshopcraft.nodeores.registry.NodeContent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
//? if >=1.20.5
import net.minecraft.core.HolderLookup;
//? if >=1.21.6 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}

public class NodeClusterBlockEntity extends BlockEntity {
    private final NodeIo.Data data = new NodeIo.Data();
    private int clock;
    private boolean suppress;

    public NodeClusterBlockEntity(BlockPos pos, BlockState state) {
        super(NodeContent.CLUSTER_ENTITY, pos, state);
    }

    public void init(String ore, BlockPos anchor) {
        data.ore = ore == null ? "" : ore;
        data.ax = anchor.getX();
        data.ay = anchor.getY();
        data.az = anchor.getZ();
        data.ticks = 0;
        sync();
    }

    public String oreId() {
        return data.ore == null ? "" : data.ore;
    }

    public int ticks() {
        return data.ticks;
    }

    public void setTicks(int ticks) {
        data.ticks = Math.max(0, ticks);
    }

    public BlockPos anchorPos() {
        return new BlockPos(data.ax, data.ay, data.az);
    }

    public void suppress() {
        suppress = true;
    }

    public boolean suppressed() {
        return suppress;
    }

    public void tick() {
        if (level == null || level.isClientSide() || suppress) return;
        BlockPos pos = getBlockPos();
        if (!(level.getBlockState(pos.below()).getBlock() instanceof NodeBaseBlock)) {
            suppress = true;
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }
        if (++clock < 20) return;
        clock = 0;
        int total = NodeSettings.get().growthSeconds * 20;
        if (data.ticks < total) data.ticks = Math.min(total, data.ticks + 20);
        NodeStage stage = Growth.stage(data.ticks, total);
        boolean ready = Growth.harvestable(data.ticks, total);
        BlockState state = getBlockState();
        if (state.getBlock() instanceof NodeClusterBlock && (state.getValue(NodeClusterBlock.STAGE) != stage || state.getValue(NodeClusterBlock.READY) != ready)) {
            level.setBlock(pos, state.setValue(NodeClusterBlock.STAGE, stage).setValue(NodeClusterBlock.READY, ready), 3);
        }
        setChanged();
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
