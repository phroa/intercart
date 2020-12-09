package net.phroa.intercart;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.stream.Collectors;

public class RouteTableListener implements CommandExecutor {
    private final Intercart intercart;

    public RouteTableListener(Intercart intercart) {
        this.intercart = intercart;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || args.length < 1) {
            return false;
        }

        var player = (Player) sender;
        var block = player.getTargetBlock(10);
        var router = intercart.routersByLocation.get(block.getLocation());
        if (router == null) {
            router = intercart.routersByInterfaceLocation.get(block.getLocation());
            if (router == null) {
                return false;
            }
        }

        var op = args[0];
        switch (op) {
            case "print" -> {
                var table = router.routingTable.entrySet()
                        .stream()
                        .sorted(Comparator.comparing((a) -> -a.getKey().getMask()))
                        .collect(Collectors.toList());
                for (int i = 0; i < table.size(); i++) {
                    var route = table.get(i);
                    player.sendMessage("%03d - %s via %d".formatted(i, route.getKey(), route.getValue()));
                }
            }
            case "add" -> {
                var cidr = CIDR.parse(args[1]);
                var iface = Integer.parseInt(args[2]);
                router.routingTable.put(cidr, iface);
            }
            case "get" -> {
                var dest = router.route(new Destination(args[1]));
                if (dest == null) {
                    player.sendMessage("Network unreachable");
                } else {
                    var iface = router.getInterfaces().indexOf(dest);
                    player.sendMessage("Route via interface " + iface);
                }
            }
        }

        return true;
    }
}
