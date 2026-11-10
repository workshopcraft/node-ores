package com.workshopcraft.nodeores.world;

import com.workshopcraft.nodeores.logic.NodeSettings;
import com.workshopcraft.nodeores.node.NodeLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
//? if <26.3 {
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
//?} else {
/*import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
*///?}

//? if <26.3 {
public class OreNodeFeature extends Feature<NoneFeatureConfiguration> {
    public OreNodeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return place(context.level(), context.random(), context.origin());
    }
//?} else {
/*public class OreNodeFeature implements Feature {
    public static final MapCodec<OreNodeFeature> CODEC = MapCodec.unit(OreNodeFeature::new);

    public OreNodeFeature() {}

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return place(level, random, origin);
    }
*///?}

    private static boolean place(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int oneIn = NodeSettings.get().worldgenOneIn;
        if (oneIn > 1 && random.nextInt(oneIn) != 0) return false;
        BlockPos spot = origin;
        if (!level.getBlockState(spot).isAir()) spot = spot.above();
        return NodeLogic.place(level, spot, random, Block.UPDATE_CLIENTS, true);
    }
}
