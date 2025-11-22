package com.bnana;

import com.bnana.listeners.InventoryClickListener;
import com.bnana.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AvoidTPA extends JavaPlugin {

    private static AvoidTPA instance;
    private TPAStorage storage;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        storage = new TPAStorage(this);
        guiManager = new GUIManager(this);

        getCommand("tpa").setExecutor(new TPACommand(storage, this));
        getCommand("tpahere").setExecutor(new TPAHereCommand(storage, this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(storage));
        getCommand("tpdeny").setExecutor(new TPADenyCommand(storage));
        getCommand("tpaguitoggle").setExecutor(new TPAGUIToggleCommand(storage));

        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(storage, guiManager), this);

        getLogger().info("AvoidTPA enabled.");
    }

    @Override
    public void onDisable() {
        storage.savePrefs();
        getLogger().info("AvoidTPA disabled.");
    }

    public static AvoidTPA getInstance() {
        return instance;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
