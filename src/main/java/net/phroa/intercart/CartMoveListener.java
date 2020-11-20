package net.phroa.intercart;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;

public class CartMoveListener implements Listener {
    private final Intercart intercart;

    public CartMoveListener(Intercart intercart) {
        this.intercart = intercart;
    }

    @EventHandler
    public void onCollideBlock(VehicleBlockCollisionEvent e) {
        if (!(e.getVehicle() instanceof Minecart)) {
            return;
        }

        var minecart = (Minecart) e.getVehicle();
        var rail = minecart.getLocation().getBlock();
        var router = intercart.routersByInterfaceLocation.get(rail.getLocation());
        var destination = intercart.meta.<Destination>get(minecart, Meta.META_DESTINATION).orElseThrow();

        var out = router.getInterfaces().get(destination.destination).add(0.5, 0, 0.5);
        minecart.teleport(out);

    }
}
