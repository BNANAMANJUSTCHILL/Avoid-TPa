package com.bnana.listeners;

import com.bnana.AvoidTPA;
import com.bnana.GUIManager;
import com.bnana.TPAStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final TPAStorage storage;
    private final GUIManager gui;

    public InventoryClickListener(TPAStorage storage, GUIManager gui) {
        this.storage = storage;
        this.gui = gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle() == null) return;
        if (!e.getView().getTitle().startsWith(ChatColor.DARK_PURPLE + AvoidTPA.getInstance().getConfig().getString("settings.gui-title", "Avoid Tpa")))
            return;

        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        UUID targetUUID = clicker.getUniqueId();
        TPAStorage.Request req = storage.getRequest(targetUUID);
        if (req == null) {
            clicker.closeInventory();
            return;
        }

        String display = item.getItemMeta().getDisplayName();
        if (display == null) return;
        display = ChatColor.stripColor(display);

        // Confirm
        if (display.equalsIgnoreCase("Confirm")) {
            Player requester = Bukkit.getPlayer(req.requester);
            if (requester == null) {
                clicker.sendMessage(prefix() + ChatColor.RED + "Requester is no longer online.");
                storage.removeRequest(targetUUID);
                clicker.closeInventory();
                return;
            }
            long delay = AvoidTPA.getInstance().getConfig().getLong("settings.teleport-delay-seconds", 5L);
            clicker.sendMessage(prefix() + ChatColor.GREEN + "Accepted. Teleporting in " + delay + "s..."); 
            requester.sendMessage(prefix() + ChatColor.GREEN + clicker.getName() + " accepted your request. Teleporting in " + delay + "s..."); 

            // schedule teleport after delay seconds (ensure both still online and request still valid)
            Bukkit.getScheduler().runTaskLater(AvoidTPA.getInstance(), () -> {
                TPAStorage.Request now = storage.getRequest(targetUUID);
                if (now == null) return;
                if (!now.equals(req)) return;
                Player t = Bukkit.getPlayer(targetUUID);
                Player r = Bukkit.getPlayer(req.requester);
                if (t == null || r == null) return;
                // determine type: requester -> target (normal tpa) or target -> requester (tpahere)
                // In storage we saved targetUUID as target for /tpa (target is the one being requested to accept)
                // For /tpahere we also saved target as target and requester as requester, and we will teleport appropriately on accept
                // Here we teleport requester to target
                r.teleport(t.getLocation());
                r.sendMessage(prefix() + ChatColor.GREEN + "Teleported to " + t.getName() + "!"); 
                t.sendMessage(prefix() + ChatColor.GREEN + "Teleport complete."); 
                storage.removeRequest(targetUUID);
            }, delay * 20L);
            clicker.closeInventory();
            return;
        }

        // Cancel
        if (display.equalsIgnoreCase("Cancel") || display.equalsIgnoreCase("Deny")) {
            Player requester = Bukkit.getPlayer(req.requester);
            if (requester != null) requester.sendMessage(prefix() + ChatColor.RED + clicker.getName() + " denied your request.");
            clicker.sendMessage(prefix() + ChatColor.YELLOW + "You denied the request.");
            storage.removeRequest(targetUUID);
            clicker.closeInventory();
            return;
        }
    }

    private String prefix() {
        return "§5Avoid §9Tpa §7> §f";
    }
}
