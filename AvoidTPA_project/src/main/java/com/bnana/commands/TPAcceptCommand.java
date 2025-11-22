package com.bnana.commands;

import com.bnana.TPAStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TPAcceptCommand implements CommandExecutor {

    private final TPAStorage storage;

    public TPAcceptCommand(TPAStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player target = (Player) sender;
        TPAStorage.Request req = storage.getRequest(target.getUniqueId());
        if (req == null) { target.sendMessage(prefix() + "You have no pending requests."); return true; }

        Player requester = Bukkit.getPlayer(req.requester);
        if (requester == null) { target.sendMessage(prefix() + "Requester is offline."); storage.removeRequest(target.getUniqueId()); return true; }

        long delay = Bukkit.getPluginManager().getPlugin("AvoidTPA").getConfig().getLong("settings.teleport-delay-seconds",5L);
        target.sendMessage(prefix() + "Accepted. Teleporting in " + delay + "s..."); 
        requester.sendMessage(prefix() + target.getName() + " accepted your request. Teleporting in " + delay + "s..."); 

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("AvoidTPA"), () -> {
            // re-check still valid
            TPAStorage.Request now = storage.getRequest(target.getUniqueId());
            if (now == null || now.requester==null) return;
            if (!now.equals(req)) return;
            Player t = Bukkit.getPlayer(target.getUniqueId());
            Player r = Bukkit.getPlayer(req.requester);
            if (t == null || r == null) return;
            // teleport requester to target
            r.teleport(t.getLocation());
            r.sendMessage(prefix() + "Teleported to " + t.getName() + "!"); 
            t.sendMessage(prefix() + "Teleport complete."); 
            storage.removeRequest(target.getUniqueId());
        }, delay * 20L);

        return true;
    }

    private String prefix() { return "§5Avoid §9Tpa §7> §f"; }
}
