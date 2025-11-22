package com.bnana.commands;

import com.bnana.TPAStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TPAGUIToggleCommand implements CommandExecutor {

    private final TPAStorage storage;

    public TPAGUIToggleCommand(TPAStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        UUID id = p.getUniqueId();
        boolean enabled = storage.isGuiEnabled(id);
        storage.setGuiEnabled(id, !enabled);
        p.sendMessage("§5Avoid §9Tpa §7> §fGUI is now: " + (!enabled ? "§aENABLED" : "§cDISABLED"));
        return true;
    }
}
