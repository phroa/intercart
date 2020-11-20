package net.phroa.intercart;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;

public class RouterBuildingListener implements Listener, CommandExecutor {

    private final Intercart intercart;

    public RouterBuildingListener(Intercart intercart) {
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
            Optional.ofNullable(intercart.routersByLocation.get(click))
                    .map(Router::toString)
                    .ifPresent(player::sendMessage);
            Optional.ofNullable(intercart.routersByInterfaceLocation.get(click))
                    .ifPresent(router -> {
                        var index = router.interfaces.indexOf(click);
                        player.sendMessage("Clicked on interface " + index);
                        player.sendMessage("Router: " + router);
                    });
        }

        var currentState = intercart.playerBuildStates.getOrDefault(uuid, BuildState.NONE);
        switch (currentState) {
            case NONE:
                return;

            case SELECT_ROUTER:

                if (!(clickedBlock.getState() instanceof Chest)) {
                    return;
                }

                player.sendMessage("Selected router at " + click);
                var router = intercart.routersByLocation.get(click);
                if (router != null) {
                    player.sendMessage("This is already a router.  Selecting it to add interfaces to.");
                } else {
                    router = new Router(uuid, click, new ArrayList<>());
                    intercart.addRouter(router);
                    player.sendMessage("Built a new router.");
                }
                intercart.playerBuildRouters.put(uuid, router);
                player.sendMessage("Click a powered rail");

                intercart.playerBuildStates.put(uuid, BuildState.ADD_INTERFACE);
                break;

            case ADD_INTERFACE:
                if (clickedBlock.getType() != Material.POWERED_RAIL) {
                    return;
                }

                var routerInProgress = intercart.playerBuildRouters.get(uuid);
                player.sendMessage("Selected interface at " + click);

                var existingRouter = intercart.routersByInterfaceLocation.get(click);
                if (existingRouter != null) {
                    player.sendMessage("This is already an interface to a router: " + existingRouter);
                    return;
                }

                intercart.addInterface(routerInProgress, click);

                player.sendMessage("Attached to router at " + routerInProgress);
                player.sendMessage("\"/ic-build done\" to stop, or click another powered rail.");

                intercart.playerBuildStates.put(uuid, BuildState.ADD_INTERFACE);
                break;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        if (command.getName().equalsIgnoreCase("ic-build")) {
            var player = (Player) sender;
            var uuid = player.getUniqueId();

            if (args.length > 0 && args[0].equalsIgnoreCase("done")) {
                intercart.playerBuildStates.remove(uuid);
                intercart.playerBuildRouters.remove(uuid);
                player.sendMessage("Done.");
                return true;
            }

            var currentState = intercart.playerBuildStates.get(uuid);
            if (currentState != null) {
                player.sendMessage("You're currently in state " + currentState);
                return true;
            }

            intercart.playerBuildStates.put(uuid, BuildState.SELECT_ROUTER);
            player.sendMessage("Select a chest to be a router.");
            return true;
        }
        return false;
    }
}
