package net.phroa.intercart;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageLoader {
    public static final int RAIL = 0xFF000000;
    public static final int BLANK = 0xFFFFFFFF;
    public static final int CACTUS = 0xFF00FF00;
    public static final int IRON = 0xFFC0C0C0;
    public static final int CHEST = 0xFFFFD800;

    private final BufferedImage image;

    public ImageLoader(String filepath) {
        BufferedImage i = null;
        try {
            i = ImageIO.read(new File(filepath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.image = i;
    }

    public List<Router> construct(Location topLeftCorner, UUID creator) {
        var routers = new ArrayList<Router>();
        topLeftCorner = topLeftCorner.toBlockLocation();
        // row 0, col 0, at the top left of the image, is the northwesternmost point.
        // south is +z, east is +x
        for (var row = 0; row < image.getHeight(); row++) {
            for (var col = 0; col < image.getWidth(); col++) {
                var pixel = image.getRGB(col, row);

                if (pixel == RAIL) {
                    // rail
                    offset(topLeftCorner, col, 0, row).setType(Material.POWERED_RAIL);
                    offset(topLeftCorner, col, -1, row).setType(Material.REDSTONE_BLOCK);

                    if (image.getRGB(col, row - 1) == RAIL && image.getRGB(col, row + 1) == BLANK
                            || image.getRGB(col, row + 1) == RAIL && image.getRGB(col, row - 1) == BLANK) {
                        // the current rail has a north connection but no south connection etc
                        offset(topLeftCorner, col, 0, row).setType(Material.RAIL);
                    } else if (image.getRGB(col - 1, row) == RAIL && image.getRGB(col + 1, row) == BLANK
                            || image.getRGB(col - 1, row) == RAIL && image.getRGB(col + 1, row) == BLANK) {
                        // the current rail has a west connection but no east connection etc
                        offset(topLeftCorner, col, 0, row).setType(Material.RAIL);
                    }
                } else if (pixel == CACTUS) {
                    // cactus
                    offset(topLeftCorner, col, 0, row).setType(Material.CACTUS);
                    offset(topLeftCorner, col, -1, row).setType(Material.SAND);
                } else if (pixel == IRON) {
                    // router base block
                    offset(topLeftCorner, col, 0, row).setType(Material.IRON_BLOCK);
                } else if (pixel == CHEST) {
                    // router chest block
                    var chest = offset(topLeftCorner, col, 1, row);
                    chest.setType(Material.CHEST);
                    offset(topLeftCorner, col, 0, row).setType(Material.IRON_BLOCK);
                    // now that a chest has been found, look for the interfaces leading to the router and build a router
                    var minCol = col;
                    var minRow = row;
                    var maxCol = col;
                    var maxRow = row;
                    while (image.getRGB(minCol - 1, minRow) == IRON) {
                        minCol--;
                    }
                    while (image.getRGB(minCol, minRow - 1) == IRON) {
                        minRow--;
                    }
                    while (image.getRGB(maxCol + 1, maxRow) == IRON) {
                        maxCol++;
                    }
                    while (image.getRGB(maxCol, maxRow + 1) == IRON) {
                        maxRow++;
                    }

                    var interfaces = new ArrayList<Location>();

                    for (var c = minCol; c <= maxCol; c++) {
                        if (image.getRGB(c, minRow - 1) == RAIL) {
                            var iface = offset(topLeftCorner, c, 0, minRow - 1).getLocation();
                            interfaces.add(iface);
                        }
                        if (image.getRGB(c, maxRow + 1) == RAIL) {
                            var iface = offset(topLeftCorner, c, 0, maxRow + 1).getLocation();
                            interfaces.add(iface);
                        }
                    }

                    for (var r = minRow; r <= maxRow; r++) {
                        if (image.getRGB(minCol - 1, r) == RAIL) {
                            var iface = offset(topLeftCorner, minCol - 1, 0, r).getLocation();
                            interfaces.add(iface);
                        }
                        if (image.getRGB(maxCol + 1, r) == RAIL) {
                            var iface = offset(topLeftCorner, maxCol + 1, 0, r).getLocation();
                            interfaces.add(iface);
                        }
                    }

                    routers.add(new Router(creator, chest.getLocation(), interfaces));
                }
            }
        }
        return routers;
    }

    private Block offset(Location base, int x, int y, int z) {
        return base.getWorld().getBlockAt(
                base.getBlockX() + x,
                base.getBlockY() + y,
                base.getBlockZ() + z
        );
    }
}
