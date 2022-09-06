package me.doggy.energyprotectivefields.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.data.WorldFieldsBounds;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;

public class FindControllersTestCommand
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("epf").then(Commands.literal("find-controllers").executes(FindControllersTestCommand::execute)));
    }
    
    public static int execute(CommandContext<CommandSourceStack> context)
    {
        Entity sourceEntity = context.getSource().getEntity();
    
        if(sourceEntity instanceof ServerPlayer == false)
            return -1;
        ServerPlayer player = (ServerPlayer)sourceEntity;
    
        BlockPos playerBlockPos = player.blockPosition();
    
        var level = player.getLevel();
        var controllersInChunk = WorldFieldsBounds.get(level).getControllersByChunk(level.getChunk(playerBlockPos).getPos());
    
        player.sendMessage(new TextComponent("Your pos: " + playerBlockPos.toShortString()), Util.NIL_UUID);
        
        player.sendMessage(new TextComponent("Found " + controllersInChunk.size() + " controllers in chunk:"), Util.NIL_UUID);
        for(var controllerPos : controllersInChunk)
            player.sendMessage(new TextComponent("    " + controllerPos.toShortString()), Util.NIL_UUID);
    
        HashSet<BlockPos> controllersByBounds = new HashSet<>();
        HashSet<BlockPos> controllersByPosition = new HashSet<>();
        
        for(var controllerPos : controllersInChunk)
        {
            var controller = (FieldControllerBlockEntity)level.getBlockEntity(controllerPos);
            var shapeBounds = controller.getShapeBounds();
            if(shapeBounds.isInside(playerBlockPos))
                controllersByBounds.add(controllerPos);
            if(controller.isInsideField(playerBlockPos))
                controllersByPosition.add(controllerPos);
        }
    
        player.sendMessage(new TextComponent("Found " + controllersByBounds.size() + " controllers with bounds in your pos:"), Util.NIL_UUID);
        for(var controllerPos : controllersByBounds)
            player.sendMessage(new TextComponent("    " + controllerPos.toShortString()), Util.NIL_UUID);
        
        player.sendMessage(new TextComponent("Found " + controllersByPosition.size() + " controllers in your pos:"), Util.NIL_UUID);
        for(var controllerPos : controllersByPosition)
            player.sendMessage(new TextComponent("    " + controllerPos.toShortString()), Util.NIL_UUID);
        
        return 1;
    }
}
