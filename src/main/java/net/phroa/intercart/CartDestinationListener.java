package net.phroa.intercart;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class CartDestinationListener implements CommandExecutor, Listener {
    private final Intercart intercart;

    public CartDestinationListener(Intercart intercart) {
        this.intercart = intercart;
    }

    @EventHandler
    public void onClickEntity(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Minecart)) {
            return;
        }

        Player player = e.getPlayer();
        Minecart minecart = (Minecart) e.getRightClicked();

        intercart.meta.<Destination>get(player, Meta.META_DESTINATION).ifPresent(destination -> {
            intercart.meta.set(minecart, Meta.META_DESTINATION, destination);
            player.sendMessage("Set destination to " + destination);
            intercart.meta.remove(player, Meta.META_DESTINATION);
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (!command.getName().equalsIgnoreCase("ic-go")) {
            return false;
        }

        if (args.length == 0) {
            for (int i = 0; i < intercart.routers.size(); i++) {
                player.sendMessage("Router " + i);
                player.sendMessage(intercart.routers.get(i).toString());
            }
            return true;
        }
        Integer dest = Integer.parseInt(args[0]);
        player.sendMessage("Click a minecart");
        intercart.meta.set(player, Meta.META_DESTINATION, new Destination(dest));
        return false;
    }
}
