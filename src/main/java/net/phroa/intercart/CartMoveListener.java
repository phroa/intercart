package net.phroa.intercart;

import org.bukkit.Location;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.util.Vector;

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
        var router = intercart.routersByInterfaceLocation.get(rail.getLocation().toBlockLocation());
        var destination = intercart.meta.<Destination>get(minecart, Meta.META_DESTINATION).orElse(new Destination("0.0.0.0"));

        var out = router.route(destination);
        if (out != null) {
            minecart.teleport(computeDestination(minecart, out));
            minecart.setVelocity(computeVelocity(minecart, out));
        }
    }

    private Location computeDestination(Minecart minecart, Location interfaceLocation) {
        return interfaceLocation.toCenterLocation();
    }

    private Vector computeVelocity(Minecart minecart, Location interfaceLocation) {
        var railDirection = ((Rail) interfaceLocation.getBlock().getState().getBlockData()).getShape();
        switch (railDirection) {
            case NORTH_SOUTH -> {
                var northBlock = interfaceLocation.add(0, 0, -1).getBlock();
                if (northBlock.getState().getBlockData() instanceof Rail) {
                    // send cart to the north
                    return new Vector(0, 0, -minecart.getMaxSpeed());
                }
                var southBlock = interfaceLocation.add(0, 0, 1).getBlock();
                if (southBlock.getState().getBlockData() instanceof Rail) {
                    // send cart to the south
                    return new Vector(0, 0, minecart.getMaxSpeed());
                }
            }
            case EAST_WEST -> {
                var eastBlock = interfaceLocation.add(1, 0, 0).getBlock();
                if (eastBlock.getState().getBlockData() instanceof Rail) {
                    // send cart to the east
                    return new Vector(minecart.getMaxSpeed(), 0, 0);
                }
                var westBlock = interfaceLocation.add(-1, 0, 0).getBlock();
                if (westBlock.getState().getBlockData() instanceof Rail) {
                    // send cart to the west
                    return new Vector(-minecart.getMaxSpeed(), 0, 0);
                }
            }
        }
        // Don't adjust velocity, hope the minecart can detect it is next to a block and needs to push itself
        return new Vector();
    }
}
