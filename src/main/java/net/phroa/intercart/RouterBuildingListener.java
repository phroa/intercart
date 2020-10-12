package net.phroa.intercart;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RouterBuildingListener implements Listener, CommandExecutor {

    private final Intercart intercart;

    public RouterBuildingListener(Intercart intercart) {
        this.intercart = intercart;
    }

    @EventHandler
    public void clickListener(PlayerInteractEvent e) {
        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = e.getPlayer();

        if (e.getItem() != null && e.getItem().getType() == Material.DIAMOND_HOE) {
            intercart.meta.<String>get(clickedBlock, Meta.META_ROUTER_INFO).ifPresent(player::sendMessage);
            intercart.meta.<String>get(clickedBlock, Meta.META_ATTACHED_ROUTER).ifPresent(player::sendMessage);
        }

        BuildState currentState = intercart.meta.<BuildState>get(player, Meta.META_BUILD_STATE).orElse(BuildState.NONE);

        switch (currentState) {
            case NONE:
                return;

            case SELECT_ROUTER:

                if (!(clickedBlock.getState() instanceof Chest)) {
                    return;
                }

                player.sendMessage("Selected router at " + clickedBlock.getLocation());
                if (clickedBlock.hasMetadata(Meta.META_ROUTER_INFO)) {
                    player.sendMessage("This is already a router.  Selecting it to add interfaces to.");
                } else {
                    RouterInfo routerInfo = new RouterInfo(player.getUniqueId(), new ArrayList<>());
                    clickedBlock.setMetadata(Meta.META_ROUTER_INFO, new FixedMetadataValue(intercart, routerInfo));
                    player.sendMessage("Built a new router.");
                }
                intercart.meta.set(player, Meta.META_BUILD_STATE, BuildState.ADD_INTERFACE);
                intercart.meta.set(player, Meta.META_CURRENT_ROUTER, clickedBlock.getLocation());
                player.sendMessage("Click a powered rail");
                break;

            case ADD_INTERFACE:
                if (clickedBlock.getType() != Material.POWERED_RAIL) {
                    return;
                }

                Location router = intercart.meta.<Location>get(player, Meta.META_CURRENT_ROUTER).orElseThrow();

                player.sendMessage("Selected interface at " + clickedBlock.getLocation());

                Optional<Location> existingRouterLocation = intercart.meta.get(clickedBlock, Meta.META_ATTACHED_ROUTER);
                if (existingRouterLocation.isPresent()) {
                    player.sendMessage("This is already an interface with a router at " + existingRouterLocation.get());
                    return;
                }

                Block routerBlock = e.getClickedBlock().getWorld().getBlockAt(router);
                intercart.meta.<RouterInfo, RouterInfo>map(routerBlock, Meta.META_ROUTER_INFO, info -> {
                    RouterInfo updated = new RouterInfo(info);
                    updated.addInterface(clickedBlock.getLocation());
                    return updated;
                });

                intercart.meta.set(clickedBlock, Meta.META_ATTACHED_ROUTER, router);
                player.sendMessage("Attached to router at " + router);

                intercart.meta.set(player, Meta.META_BUILD_STATE, BuildState.ADD_INTERFACE);
                player.sendMessage("\"/ic-build done\" to stop, or click another powered rail.");
                break;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        if (command.getName().equalsIgnoreCase("ic-build")) {
            Player player = (Player) sender;

            if (args.length > 0 && args[0].equalsIgnoreCase("done")) {
                intercart.meta.remove(player, Meta.META_BUILD_STATE);
                intercart.meta.remove(player, Meta.META_CURRENT_ROUTER);
                player.sendMessage("Done.");
                return true;
            }

            Optional<BuildState> currentState = intercart.meta.get(player, Meta.META_BUILD_STATE);
            if (currentState.isPresent()) {
                player.sendMessage("You're currently in state " + currentState.get());
                return true;
            }

            intercart.meta.set(player, Meta.META_BUILD_STATE, BuildState.SELECT_ROUTER);
            player.sendMessage("Select a chest to be a router.");
            return true;
        }
        return false;
    }
}
