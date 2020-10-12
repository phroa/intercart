package net.phroa.intercart;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;

public class CartMoveListener implements Listener {
    private Intercart intercart;

    public CartMoveListener(Intercart intercart) {
        this.intercart = intercart;
    }

    @EventHandler
    public void onCollideBlock(VehicleBlockCollisionEvent e) {
        if (!(e.getVehicle() instanceof Minecart)) {
            return;
        }

        Minecart minecart = (Minecart) e.getVehicle();
        Block rail = minecart.getLocation().getBlock();
        intercart.meta.<Location>get(rail, Meta.META_ATTACHED_ROUTER).ifPresent(routerLocation -> {
            Block router = routerLocation.getBlock();
            intercart.meta.<RouterInfo>get(router, Meta.META_ROUTER_INFO).ifPresent(routerInfo -> {
                Integer destination = intercart.meta.<Integer>get(minecart, Meta.META_DESTINATION).orElse(0);

                Location out = routerInfo.getInterfaces().get(destination).add(0.5, 0, 0.5);
                minecart.teleport(out);
            });
        });

    }
}
