package com.workshopcraft.nodeores.node;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.nodeores.logic.NodeSettings;
import com.workshopcraft.nodeores.registry.NodeContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NodeBaseBlock extends BaseEntityBlock {
    public static final EnumProperty<NodeCorner> CORNER = EnumProperty.create("corner", NodeCorner.class);
    //? if >=1.20.5 && <26.3
    public static final MapCodec<NodeBaseBlock> CODEC = simpleCodec(NodeBaseBlock::new);

    public NodeBaseBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CORNER, NodeCorner.NORTH_WEST));
    }

    //? if >=1.20.5 && <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    //? if <1.21.2 {
    /*@Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }
    *///?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CORNER);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return NodeShapes.base(state.getValue(CORNER));
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float hardness = NodeSettings.get().baseHardness;
        if (!Float.isFinite(hardness) || hardness <= 0.0F) return 0.0F;
        int divisor = player.hasCorrectToolForDrops(state) ? 30 : 100;
        return player.getDestroySpeed(state) / hardness / (float) divisor;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeBaseBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, NodeContent.BASE_ENTITY, (lvl, pos, blockState, be) -> be.tick());
    }

    //? if >=1.20.5 {
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof NodeBaseBlockEntity be) {
            NodeLogic.destroy(level, be, player.hasCorrectToolForDrops(state));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
    //?} else {
    /*@Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof NodeBaseBlockEntity be) {
            NodeLogic.destroy(level, be, player.hasCorrectToolForDrops(state));
        }
        super.playerWillDestroy(level, pos, state, player);
    }
    *///?}

    //? if >=26.1 {
    @Override
    public void wasExploded(net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof NodeBaseBlockEntity be) NodeLogic.destroy(level, be, true);
    }
    //?} else {
    /*@Override
    public void wasExploded(Level level, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof NodeBaseBlockEntity be) NodeLogic.destroy(level, be, true);
        super.wasExploded(level, pos, explosion);
    }
    *///?}
}
