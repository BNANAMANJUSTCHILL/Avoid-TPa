package com.bnana.commands;

import com.bnana.AvoidTPA;
import com.bnana.TPAStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TPACommand implements CommandExecutor {

    private final TPAStorage storage;
    private final AvoidTPA plugin;

    public TPACommand(TPAStorage storage, AvoidTPA plugin) {
        this.storage = storage;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        if (args.length != 1) {
            p.sendMessage(prefix() + "Usage: /tpa <player>"); return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) { p.sendMessage(prefix() + "Player not found."); return true; }
        if (target.equals(p)) { p.sendMessage(prefix() + "You cannot request yourself."); return true; }

        if (!storage.canSend(p.getUniqueId())) {
            p.sendMessage(prefix() + "You must wait before sending another request."); return true;
        }

        // store and set cooldown
        storage.addRequest(target.getUniqueId(), p.getUniqueId());
        long cooldown = plugin.getConfig().getLong("settings.send-cooldown-seconds", 30L);
        storage.setSendCooldown(p.getUniqueId(), cooldown);

        p.sendMessage(prefix() + "Sent TPA request to " + target.getName() + "."); 
        target.sendMessage(prefix() + p.getName() + " wants to teleport to you!");

        if (storage.isGuiEnabled(target.getUniqueId())) {
            target.openInventory(plugin.getGuiManager().createTPAGui(target, p, false));
        } else {
            target.sendMessage(prefix() + "Type /tpaccept to accept or /tpdeny to deny. (Request expires in " + plugin.getConfig().getLong("settings.request-expire-seconds",30L) + "s)") ;
        }

        return true;
    }

    private String prefix() { return "§5Avoid §9Tpa §7> §f"; }
}
