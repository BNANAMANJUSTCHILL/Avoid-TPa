package com.bnana;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TPAStorage {

    private final JavaPlugin plugin;
    // pending requests: targetUUID -> Request object
    private final Map<UUID, Request> pending = new HashMap<>();

    // send cooldowns: sender UUID -> next allowed epoch millis
    private final Map<UUID, Long> sendCooldown = new HashMap<>();

    // gui prefs per-player (true = enabled)
    private final Map<UUID, Boolean> guiPrefs = new HashMap<>();

    private final File prefsFile;
    private final FileConfiguration prefsCfg;

    public TPAStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        prefsFile = new File(plugin.getDataFolder(), "players.yml");
        if (!prefsFile.exists()) {
            prefsFile.getParentFile().mkdirs();
            try { prefsFile.createNewFile(); } catch (IOException ignored) {}
        }
        prefsCfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(prefsFile);
        // Load prefs
        for (String k : prefsCfg.getKeys(false)) {
            try {
                guiPrefs.put(UUID.fromString(k), prefsCfg.getBoolean(k, true));
            } catch (Exception ignored) {}
        }
    }

    public boolean canSend(UUID sender) {
        long now = System.currentTimeMillis();
        return sendCooldown.getOrDefault(sender, 0L) <= now;
    }

    public void setSendCooldown(UUID sender, long seconds) {
        sendCooldown.put(sender, System.currentTimeMillis() + seconds * 1000L);
    }

    public void savePrefs() {
        for (Map.Entry<UUID, Boolean> e : guiPrefs.entrySet()) {
            prefsCfg.set(e.getKey().toString(), e.getValue());
        }
        try {
            prefsCfg.save(prefsFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isGuiEnabled(UUID player) {
        return guiPrefs.getOrDefault(player, true);
    }

    public void setGuiEnabled(UUID player, boolean enabled) {
        guiPrefs.put(player, enabled);
    }

    public void addRequest(UUID target, UUID requester) {
        Request r = new Request(target, requester, System.currentTimeMillis());
        pending.put(target, r);

        // schedule expiry
        long expireSec = plugin.getConfig().getLong("settings.request-expire-seconds", 30L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Request now = pending.get(target);
            if (now != null && now.equals(r)) {
                pending.remove(target);
                Player t = Bukkit.getPlayer(target);
                Player req = Bukkit.getPlayer(requester);
                if (t != null) t.sendMessage(prefix() + "§cThe TPA request has expired.");
                if (req != null) req.sendMessage(prefix() + "§cYour TPA request to " + (t!=null?t.getName():"(offline)") + " expired.");
            }
        }, expireSec * 20L);
    }

    public Request getRequest(UUID target) {
        return pending.get(target);
    }

    public void removeRequest(UUID target) {
        pending.remove(target);
    }

    private String prefix() {
        return "§5Avoid §9Tpa §7> §f";
    }

    public static class Request {
        public final UUID target;
        public final UUID requester;
        public final long createdAt;

        public Request(UUID target, UUID requester, long createdAt) {
            this.target = target;
            this.requester = requester;
            this.createdAt = createdAt;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Request)) return false;
            Request r = (Request)o;
            return Objects.equals(target, r.target) && Objects.equals(requester, r.requester) && createdAt==r.createdAt;
        }
    }
}
