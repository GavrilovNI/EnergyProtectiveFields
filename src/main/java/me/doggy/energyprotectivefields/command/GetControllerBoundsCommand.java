package me.doggy.energyprotectivefields.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.data.WorldFieldsBounds;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;

public class GetControllerBoundsCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("epf").then(Commands.literal("get-controller-bounds").executes(GetControllerBoundsCommand::execute)));
    }
    
    public static int execute(CommandContext<CommandSourceStack> context)
    {
        Entity sourceEntity = context.getSource().getEntity();
    
        if(sourceEntity instanceof ServerPlayer == false)
            return -1;
        ServerPlayer player = (ServerPlayer)sourceEntity;
    
        BlockPos controllerPos = player.blockPosition().below(1);
        var level = player.getLevel();
        
        if(level.getBlockEntity(controllerPos) instanceof FieldControllerBlockEntity controller)
        {
            player.sendMessage(new TextComponent("Controller found on pos: " + controllerPos.toShortString()), Util.NIL_UUID);
            var bounds = controller.getShapeBounds();
            if(bounds == null)
            {
                player.sendMessage(new TextComponent("Controller's bounds are empty."), Util.NIL_UUID);
            }
            else
            {
                var min = new Vec3i(bounds.minX(), bounds.minY(), bounds.minZ());
                var max = new Vec3i(bounds.maxX(), bounds.maxY(), bounds.maxZ());
                player.sendMessage(new TextComponent("Controller's bound's min: " + min.toShortString()), Util.NIL_UUID);
                player.sendMessage(new TextComponent("Controller's bound's max: " + max.toShortString()), Util.NIL_UUID);
            }
        }
        else
        {
            player.sendMessage(new TextComponent("No controller found under you."), Util.NIL_UUID);
        }
        return 1;
    }
}
