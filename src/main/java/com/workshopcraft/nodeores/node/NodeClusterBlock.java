package com.workshopcraft.nodeores.node;

import com.mojang.serialization.MapCodec;
import com.workshopcraft.nodeores.registry.NodeContent;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NodeClusterBlock extends BaseEntityBlock {
    public static final EnumProperty<NodeKind> KIND = EnumProperty.create("kind", NodeKind.class);
    public static final EnumProperty<NodeStage> STAGE = EnumProperty.create("stage", NodeStage.class);
    public static final BooleanProperty READY = BooleanProperty.create("ready");
    //? if >=1.20.5 && <26.3
    public static final MapCodec<NodeClusterBlock> CODEC = simpleCodec(NodeClusterBlock::new);

    public NodeClusterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(KIND, NodeKind.ROCKY).setValue(STAGE, NodeStage.NUB).setValue(READY, false));
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
        builder.add(KIND, STAGE, READY);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return NodeShapes.cluster(state.getValue(KIND), state.getValue(STAGE));
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return OreLoot.progress(state, player, level, pos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeClusterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, NodeContent.CLUSTER_ENTITY, (lvl, blockPos, blockState, be) -> be.tick());
    }

    private void harvest(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (blockEntity instanceof NodeClusterBlockEntity cluster && !cluster.suppressed() && state.getValue(READY)) {
            OreLoot.drop(level, pos, player, tool, cluster.oreId());
        }
    }

    //? if >=26.3 {
    /*@Override
    public void playerDestroy(net.minecraft.server.level.ServerLevel level, net.minecraft.server.level.ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        harvest(level, player, pos, state, blockEntity, tool);
    }
    *///?} else {
    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        harvest(level, player, pos, state, blockEntity, tool);
    }
    //?}

    //? if >=26.1 {
    @Override
    public void wasExploded(net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
        explode(level, pos);
    }
    //?} else {
    /*@Override
    public void wasExploded(Level level, BlockPos pos, net.minecraft.world.level.Explosion explosion) {
        explode(level, pos);
        super.wasExploded(level, pos, explosion);
    }
    *///?}

    private void explode(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof NodeClusterBlock)) return;
        if (!(level.getBlockEntity(pos) instanceof NodeClusterBlockEntity cluster) || cluster.suppressed()) return;
        if (state.getValue(READY)) OreLoot.drop(level, pos, null, ItemStack.EMPTY, cluster.oreId());
    }
}
