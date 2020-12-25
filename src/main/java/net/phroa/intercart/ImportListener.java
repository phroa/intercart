package net.phroa.intercart;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.nio.file.Paths;

public class ImportListener implements CommandExecutor {
    private final Intercart intercart;

    public ImportListener(Intercart intercart) {
        this.intercart = intercart;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        if (args.length != 1) {
            return false;
        }

        var i = new ImageLoader(Paths.get(intercart.getDataFolder().getPath(), args[0]).toString());
        i.construct(((Player) sender).getTargetBlock(10).getLocation().add(0, 1, 0));

        return true;
    }
}
