package net.phroa.intercart;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageLoader {
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

    public void construct(Location topLeftCorner) {
        topLeftCorner = topLeftCorner.toBlockLocation();
        var world = topLeftCorner.getWorld();
        // row 0, col 0, at the top left of the image, is the northwesternmost point.
        // south is +z, east is +x
        for (var row = 0; row < image.getHeight(); row++) {
            for (var col = 0; col < image.getWidth(); col++) {
                var pixel = image.getRGB(col, row) & 0xFFFFFF;

                if (pixel == 0x000000) {
                    // rail
                    offset(topLeftCorner, col, 0, row).setType(Material.POWERED_RAIL);
                    offset(topLeftCorner, col, -1, row).setType(Material.REDSTONE_BLOCK);

                    if ((image.getRGB(col, row - 1) & 0xFFFFFF) == 0x000000 && (image.getRGB(col, row + 1) & 0xFFFFFF) == 0xFFFFFF
                            || (image.getRGB(col, row + 1) & 0xFFFFFF) == 0x000000 && (image.getRGB(col, row - 1) & 0xFFFFFF) == 0xFFFFFF) {
                        // the current rail has a north connection but no south connection etc
                        offset(topLeftCorner, col, 0, row).setType(Material.RAIL);
                    } else if ((image.getRGB(col - 1, row) & 0xFFFFFF) == 0x000000 && (image.getRGB(col + 1, row) & 0xFFFFFF) == 0xFFFFFF
                            || (image.getRGB(col - 1, row) & 0xFFFFFF) == 0x000000 && (image.getRGB(col + 1, row) & 0xFFFFFF) == 0xFFFFFF) {
                        // the current rail has a west connection but no east connection etc
                        offset(topLeftCorner, col, 0, row).setType(Material.RAIL);
                    }
                } else if (pixel == 0x00FF00) {
                    // cactus
                    offset(topLeftCorner, col, 0, row).setType(Material.CACTUS);
                    offset(topLeftCorner, col, -1, row).setType(Material.SAND);
                } else if (pixel == 0xC0C0C0) {
                    // router base block
                    offset(topLeftCorner, col, 0, row).setType(Material.IRON_BLOCK);
                } else if (pixel == 0xFFD800) {
                    // router chest block
                    offset(topLeftCorner, col, 1, row).setType(Material.CHEST);
                    offset(topLeftCorner, col, 0, row).setType(Material.IRON_BLOCK);
                }
            }
        }
    }

    private Block offset(Location base, int x, int y, int z) {
        return base.getWorld().getBlockAt(
                base.getBlockX() + x,
                base.getBlockY() + y,
                base.getBlockZ() + z
        );
    }
}
