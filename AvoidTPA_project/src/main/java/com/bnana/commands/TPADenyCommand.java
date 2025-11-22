package com.bnana.commands;

import com.bnana.TPAStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPADenyCommand implements CommandExecutor {

    private final TPAStorage storage;

    public TPADenyCommand(TPAStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player target = (Player) sender;
        TPAStorage.Request req = storage.getRequest(target.getUniqueId());
        if (req == null) { target.sendMessage(prefix() + "You have no pending requests."); return true; }
        Player requester = Bukkit.getPlayer(req.requester);
        if (requester != null) requester.sendMessage(prefix() + target.getName() + " denied your request."); 
        target.sendMessage(prefix() + "You denied the request."); 
        storage.removeRequest(target.getUniqueId());
        return true;
    }

    private String prefix() { return "§5Avoid §9Tpa §7> §f"; }
}
