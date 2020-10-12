package net.phroa.intercart;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public final class Intercart extends JavaPlugin {

    final Meta meta = new Meta(this);

    private final CartMoveListener cartMoveListener = new CartMoveListener(this);
    private final CartDestinationListener cartDestinationListener = new CartDestinationListener(this);
    private final RouterBuildingListener routerBuildingListener = new RouterBuildingListener(this);

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(cartDestinationListener, this);
        Bukkit.getPluginManager().registerEvents(cartMoveListener, this);
        Bukkit.getPluginManager().registerEvents(routerBuildingListener, this);
        getCommand("ic-build").setExecutor(routerBuildingListener);
        getCommand("ic-go").setExecutor(cartDestinationListener);
    }

}
