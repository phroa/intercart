package net.phroa.intercart;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class RouterClickListener implements Listener {

    private final Intercart intercart;

    public RouterClickListener(Intercart intercart) {
        this.intercart = intercart;
    }

    @EventHandler
    public void clickListener(PlayerInteractEvent e) {
        var clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        var player = e.getPlayer();
        var uuid = player.getUniqueId();

        // 2 events are fired per click on an interface
        var debounce = intercart.playerInteractDebounce.get(uuid);
        var now = Instant.now();
        if (debounce != null) {
            var delta = ChronoUnit.SECONDS.between(debounce, now);
            if (delta < 1) {
                return;
            }
        }
        intercart.playerInteractDebounce.put(uuid, now);

        var click = clickedBlock.getLocation();

        if (e.getItem() != null && e.getItem().getType() == Material.DIAMOND_HOE) {
            var router = intercart.routersByLocation.get(click);
            if (router == null) {
                return;
            }

            for (Map.Entry<CIDR, Integer> entry : router.routingTable.entrySet()) {
                player.sendMessage(entry.getKey().toString() + " = " + entry.getValue());
            }
        }
    }
}
