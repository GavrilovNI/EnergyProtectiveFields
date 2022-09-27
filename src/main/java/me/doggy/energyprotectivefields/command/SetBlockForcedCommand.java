package me.doggy.energyprotectivefields.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.data.WorldFieldsBounds;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.function.Predicate;

public class SetBlockForcedCommand
{
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.setblock.failed"));
    
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("epf").then(Commands.literal("setblock").requires((p_138606_) -> {
            return p_138606_.hasPermission(2);
        }).then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("forced", BoolArgumentType.bool()).then(Commands.argument("block", BlockStateArgument.block()).executes((context) -> {
    
            boolean forced = BoolArgumentType.getBool(context, "forced");
            BlockPos blockPos = forced ? context.getArgument("pos", Coordinates.class).getBlockPos(context.getSource()) : BlockPosArgument.getLoadedBlockPos(context, "pos");
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            var mode = net.minecraft.server.commands.SetBlockCommand.Mode.REPLACE;
    
            return setBlock(context.getSource(), blockPos, blockInput, mode, null);
        }).then(Commands.literal("destroy").executes((context) -> {
    
            boolean forced = BoolArgumentType.getBool(context, "forced");
            BlockPos blockPos = forced ? context.getArgument("pos", Coordinates.class).getBlockPos(context.getSource()) : BlockPosArgument.getLoadedBlockPos(context, "pos");
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            var mode = net.minecraft.server.commands.SetBlockCommand.Mode.DESTROY;
    
            return setBlock(context.getSource(), blockPos,blockInput, mode, null);
        })).then(Commands.literal("keep").executes((context) -> {
    
            boolean forced = BoolArgumentType.getBool(context, "forced");
            BlockPos blockPos = forced ? context.getArgument("pos", Coordinates.class).getBlockPos(context.getSource()) : BlockPosArgument.getLoadedBlockPos(context, "pos");
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            var mode = net.minecraft.server.commands.SetBlockCommand.Mode.DESTROY;
    
            return setBlock(context.getSource(), blockPos, blockInput, mode, (p_180517_) -> {
                return p_180517_.getLevel().isEmptyBlock(p_180517_.getPos());
            });
        })).then(Commands.literal("replace").executes((context) -> {
    
            boolean forced = BoolArgumentType.getBool(context, "forced");
            BlockPos blockPos = forced ? context.getArgument("pos", Coordinates.class).getBlockPos(context.getSource()) : BlockPosArgument.getLoadedBlockPos(context, "pos");
            BlockInput blockInput = BlockStateArgument.getBlock(context, "block");
            var mode = net.minecraft.server.commands.SetBlockCommand.Mode.DESTROY;
    
            return setBlock(context.getSource(), blockPos, blockInput, mode, null);
        })))))));
    }
    
    private static int setBlock(CommandSourceStack pSource, BlockPos pPos, BlockInput pState, net.minecraft.server.commands.SetBlockCommand.Mode pMode, @Nullable Predicate<BlockInWorld> pPredicate) throws
            CommandSyntaxException
    {
        ServerLevel serverlevel = pSource.getLevel();
        if (pPredicate != null && !pPredicate.test(new BlockInWorld(serverlevel, pPos, true))) {
            throw ERROR_FAILED.create();
        } else {
            boolean flag;
            if (pMode == net.minecraft.server.commands.SetBlockCommand.Mode.DESTROY) {
                serverlevel.destroyBlock(pPos, true);
                flag = !pState.getState().isAir() || !serverlevel.getBlockState(pPos).isAir();
            } else {
                BlockEntity blockentity = serverlevel.getBlockEntity(pPos);
                Clearable.tryClear(blockentity);
                flag = true;
            }
            
            if (flag && !pState.place(serverlevel, pPos, 2)) {
                throw ERROR_FAILED.create();
            } else {
                serverlevel.blockUpdated(pPos, pState.getState().getBlock());
                pSource.sendSuccess(new TranslatableComponent("commands.setblock.success", pPos.getX(), pPos.getY(), pPos.getZ()), true);
                return 1;
            }
        }
    }
}