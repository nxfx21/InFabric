package com.nxfx21.infabric.commands;

import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ClearEffects implements Listener, CommandExecutor {
    private final DataManager dataManager;

    public ClearEffects(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("cleareffects")) return false;
        
        if (args.length != 1) {
            sender.sendMessage(new Message(MessageType.CLEAREFFECTS_USAGE).toComponent());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            dataManager.removeEffect(target.getUniqueId(), "1");
            dataManager.removeEffect(target.getUniqueId(), "2");
        }
        
        return true;
    }
}