package com.bnana;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final AvoidTPA plugin;
    public GUIManager(AvoidTPA plugin) { this.plugin = plugin; }

    public Inventory createTPAGui(Player target, Player requester, boolean isHere) {
        FileConfiguration cfg = plugin.getConfig();
        String title = cfg.getString("settings.gui-title", "Avoid Tpa");
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + title);

        // leave edges empty as requested (use air) but fill non-used slots with gray pane for clearer look
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i=0;i<27;i++) inv.setItem(i, filler);

        // slots per your layout:
        // we will place world icon at slot 11, player head at 13, accept at 15, deny at 17 (matching your visual)
        int worldSlot = 11;
        int headSlot = 13;
        int acceptSlot = 15;
        int denySlot = 17;

        // World icon block with display name = world name
        Material mat = pickMaterialForWorld(target.getWorld().getName());
        ItemStack world = new ItemStack(mat);
        ItemMeta wm = world.getItemMeta();
        wm.setDisplayName(ChatColor.AQUA + target.getWorld().getName());
        wm.setLore(List.of(ChatColor.GRAY + "World icon"));
        wm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        world.setItemMeta(wm);

        // Player head centered, display name = requester name
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        sm.setOwningPlayer(requester);
        sm.setDisplayName(ChatColor.BLUE + requester.getName());
        sm.setLore(List.of(ChatColor.GRAY + (isHere ? "TPA-HERE request" : "TPA request")));
        head.setItemMeta(sm);

        // Accept button - green concrete with 'Confirm' text
        ItemStack accept = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta am = accept.getItemMeta();
        am.setDisplayName(ChatColor.GREEN + "Confirm");
        am.setLore(List.of(ChatColor.GRAY + "Accept the request (teleports after delay)."));
        accept.setItemMeta(am);

        // Deny button - red concrete with 'Cancel' text
        ItemStack deny = new ItemStack(Material.RED_CONCRETE);
        ItemMeta dm = deny.getItemMeta();
        dm.setDisplayName(ChatColor.RED + "Cancel");
        dm.setLore(List.of(ChatColor.GRAY + "Deny the request."));
        deny.setItemMeta(dm);

        inv.setItem(worldSlot, world);
        inv.setItem(headSlot, head);
        inv.setItem(acceptSlot, accept);
        inv.setItem(denySlot, deny);

        return inv;
    }

    private Material pickMaterialForWorld(String worldName) {
        String key = worldName;
        FileConfiguration cfg = plugin.getConfig();
        String matName = cfg.getString("world-icons." + key, null);
        if (matName != null) {
            try { return Material.valueOf(matName.toUpperCase()); } catch (Exception ignored) {}
        }
        // fallback matches
        switch (worldName.toLowerCase()) {
            case "world": return Material.GRASS_BLOCK;
            case "world_the_end": case "the_end": return Material.END_STONE;
            case "world_nether": case "the_nether": return Material.NETHERRACK;
            case "spawn": return Material.AMETHYST_BLOCK;
            default: return Material.BOOK;
        }
    }
}
