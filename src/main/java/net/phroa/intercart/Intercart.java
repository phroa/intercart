package net.phroa.intercart;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public final class Intercart extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        RouterBuildingListener routerBuildingListener = new RouterBuildingListener(this);
        Bukkit.getPluginManager().registerEvents(routerBuildingListener, this);

        getCommand("ic-build").setExecutor(routerBuildingListener);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
