package net.phroa.intercart;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.*;

@SuppressWarnings("unused")
public final class Intercart extends JavaPlugin {

    final Meta meta = new Meta(this);

    final List<Router> routers = new ArrayList<>();
    final Map<Location, Router> routersByLocation = new HashMap<>();
    final Map<Location, Router> routersByInterfaceLocation = new HashMap<>();

    final Map<UUID, Instant> playerInteractDebounce = new HashMap<>();
    final Map<UUID, BuildState> playerBuildStates = new HashMap<>();
    final Map<UUID, Router> playerBuildRouters = new HashMap<>();

    private final CartMoveListener cartMoveListener = new CartMoveListener(this);
    private final CartDestinationListener cartDestinationListener = new CartDestinationListener(this);
    private final RouterBuildingListener routerBuildingListener = new RouterBuildingListener(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(cartDestinationListener, this);
        Bukkit.getPluginManager().registerEvents(cartMoveListener, this);
        Bukkit.getPluginManager().registerEvents(routerBuildingListener, this);
        getCommand("ic-build").setExecutor(routerBuildingListener);
        getCommand("ic-go").setExecutor(cartDestinationListener);

        ConfigurationSerialization.registerClass(Destination.class, "intercart-destination");
        ConfigurationSerialization.registerClass(Router.class, "intercart-router");

        var configRouters = (List<Router>) getConfig().getList("routers");
        if (configRouters != null) {
            configRouters.forEach(this::addRouter);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving " + routers.size() + " routers to config");
        getConfig().set("routers", routers);
        saveConfig();
    }

    public void addRouter(Router router) {
        if (!routers.contains(router)) {
            routers.add(router);
        }
        routersByLocation.put(router.location, router);
        for (Location i : router.interfaces) {
            routersByInterfaceLocation.put(i, router);
        }
    }

    public void addInterface(Router router, Location iface) {
        router.addInterface(iface);
        routersByInterfaceLocation.put(iface, router);
    }
}
