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
import java.util.UUID;

public class RouterBuildingListener implements Listener, CommandExecutor {
    private static final String META_BUILD_STATE = "ic-build-state";
    private static final String META_ROUTER_INFO = "ic-router-info";
    private static final String META_ATTACHED_ROUTER = "ic-attached-router";
    private static final String META_CURRENT_ROUTER = "ic-current-router";

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

        Player player = e.getPlayer();

        if (e.getItem() != null && e.getItem().getType() == Material.DIAMOND_HOE) {
            for (MetadataValue value : e.getClickedBlock().getMetadata(META_ROUTER_INFO)) {
                player.sendMessage(value.value().toString());
            }
            for (MetadataValue value : e.getClickedBlock().getMetadata(META_ATTACHED_ROUTER)) {
                player.sendMessage(value.value().toString());
            }
        }

        if (!player.hasMetadata(META_BUILD_STATE)) {
            return;
        }

        BuildState currentState = BuildState.NONE;
        List<MetadataValue> metadata = player.getMetadata(META_BUILD_STATE);
        for (MetadataValue data : metadata) {
            if (data.getOwningPlugin() == intercart && data.value() instanceof BuildState) {
                currentState = (BuildState) data.value();
                if (currentState == null) {
                    currentState = BuildState.NONE;
                }
            }
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        switch (currentState) {
            case SELECT_ROUTER:

                if (!(clickedBlock.getState() instanceof Chest)) {
                    return;
                }

                player.sendMessage("Selected router at " + clickedBlock.getLocation());
                if (clickedBlock.hasMetadata(META_ROUTER_INFO)) {
                    player.sendMessage("This is already a router.  Selecting it to add interfaces to.");
                } else {
                    RouterInfo routerInfo = new RouterInfo(player.getUniqueId(), new ArrayList<>());
                    clickedBlock.setMetadata(META_ROUTER_INFO, new FixedMetadataValue(intercart, routerInfo));
                    player.sendMessage("Built a new router.");
                }
                player.setMetadata(META_BUILD_STATE, new FixedMetadataValue(intercart, BuildState.ADD_INTERFACE));
                player.setMetadata(META_CURRENT_ROUTER, new FixedMetadataValue(intercart, clickedBlock.getLocation()));
                player.sendMessage("Click a powered rail");
                break;

            case ADD_INTERFACE:
                if (clickedBlock.getType() != Material.POWERED_RAIL) {
                    return;
                }

                if (!player.hasMetadata(META_CURRENT_ROUTER)) {
                    player.sendMessage("You're not building a router.  What?");
                    return;
                }

                Location router = null;
                for (MetadataValue data : player.getMetadata(META_CURRENT_ROUTER)) {
                    if (data.getOwningPlugin() == intercart && data.value() instanceof Location) {
                        router = (Location) data.value();
                    }
                }

                if (router == null) {
                    player.sendMessage("The router you were building has since disappeared.");
                    return;
                }

                player.sendMessage("Selected interface at " + clickedBlock.getLocation());
                if (clickedBlock.hasMetadata(META_ATTACHED_ROUTER)) {
                    player.sendMessage("This is already a router.  Selecting it to add interfaces to.");
                } else {
                    clickedBlock.setMetadata(META_ATTACHED_ROUTER, new FixedMetadataValue(intercart, router));
                    player.sendMessage("Attached to router at " + router);
                }
                player.setMetadata(META_BUILD_STATE, new FixedMetadataValue(intercart, BuildState.ADD_INTERFACE));
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
                player.removeMetadata(META_BUILD_STATE, intercart);
                player.removeMetadata(META_CURRENT_ROUTER, intercart);
                player.sendMessage("Done.");
                return true;
            }

            if (player.hasMetadata("ic-build-state")) {
                List<MetadataValue> metadata = player.getMetadata(META_BUILD_STATE);
                for (MetadataValue data : metadata) {
                    if (data.getOwningPlugin() == intercart && data.value() instanceof BuildState) {
                        player.sendMessage("You're currently in state " + data.value());
                        return true;
                    }
                }

            }

            player.setMetadata(META_BUILD_STATE, new FixedMetadataValue(intercart, BuildState.SELECT_ROUTER));
            player.sendMessage("Select a chest to be a router.");
            return true;
        }
        return false;
    }
}
