package fr.redstonneur1256;

import fr.redstonneur1256.redutilities.graphics.Palette;
import fr.redstonneur1256.utils.BlockData;
import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class PaletteLoader {

    private static final List<String> BLOCK_COLORS;
    public static final BlockData AIR;
    public static final BlockData BLACK;
    public static final BlockData RED;
    static {
        AIR = new BlockData(Material.AIR.getId(), (byte) 0, Color.WHITE);
        BLACK = new BlockData(Material.CONCRETE.getId(), (byte) 15, Color.BLACK);
        RED = new BlockData(Material.CONCRETE.getId(), (byte) 14, Color.RED);

        BLOCK_COLORS = Arrays.asList("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "silver", "cyan", "purple", "blue", "brown", "green", "red", "black");
    }

    private Palette<BlockData> palette;
    public PaletteLoader(Palette<BlockData> palette) {
        this.palette = palette;
    }

    public void loadPalette(File minecraftFolder) {
        System.out.println("Loading blocks color list...");
        long start = System.currentTimeMillis();

        // TODO: Clean code
        List<Material> ignoreTypes = Arrays.asList(Material.BEACON, Material.SEA_LANTERN, Material.REDSTONE_LAMP_OFF,
                Material.REDSTONE_LAMP_ON, Material.TRAP_DOOR, Material.IRON_TRAPDOOR, Material.STRUCTURE_BLOCK,
                Material.DRAGON_EGG, Material.BREWING_STAND, Material.MOB_SPAWNER, Material.GLASS, Material.STAINED_GLASS,
                Material.STAINED_GLASS_PANE, Material.SOUL_SAND, Material.OBSERVER, Material.BONE_BLOCK, Material.GRASS_PATH,
                Material.GRASS);

        List<String> ignoreContains = Arrays.asList("SEED", "ORE", "CHORUS", "DOOR", "ICE", "TERRACOTTA", "SHULKER_BOX",
                "COMMAND", "SLAB", "STAIR", "FENCE");
        List<Material> colored = Arrays.asList(Material.CONCRETE, Material.CONCRETE_POWDER, Material.STAINED_CLAY, Material.WOOL);

        for (Material material : Material.values()) {
            if(!material.isBlock()) {
                // Skip it because its not a block.
                continue;
            }
            if(material.isTransparent() || !material.isSolid()) {
                continue;
            }
            if(ignoreTypes.contains(material)) {
                continue;
            }
            String name = material.name();
            if (ignoreContains.stream().anyMatch(name::contains)) {
                continue;
            }

            if(colored.contains(material)) {
                addColoredMaterial(material, minecraftFolder);
            } else {
                addMaterial(material, minecraftFolder);
            }
        }

        addColoredMaterial(Material.CONCRETE, minecraftFolder);
        addColoredMaterial(Material.CONCRETE_POWDER, minecraftFolder);
        addColoredMaterial(Material.STAINED_CLAY, "hardened_clay_stained", minecraftFolder);
        addColoredMaterial(Material.WOOL, "wool_colored", minecraftFolder);

        long end = System.currentTimeMillis();
        long time = end - start;

        long milliseconds = time % 1000;
        long seconds = time / 1000;

        String timeFormat = (seconds > 0 ? seconds + " s " : "") + (milliseconds > 0 ? milliseconds + " ms" : "");

        System.out.printf("Loaded %s block/color materials from %s in %s%n", palette.getColors().size(), minecraftFolder, timeFormat);
    }

    private void addMaterial(Material material, File minecraftFolder) {
        addMaterial(material, 0, material.name() + ".png", minecraftFolder);
    }

    private void addMaterial(Material material, int data, String textureFile, File minecraftFolder) {
        int color = readTexture(textureFile, minecraftFolder);
        palette.addColor(new BlockData(material.getId(), (byte) data, new Color(color)));
    }

    private void addColoredMaterial(Material material, File minecraftFolder) {
        addColoredMaterial(material, material.name().toLowerCase(), minecraftFolder);
    }

    private void addColoredMaterial(Material material, String name, File minecraftFolder) {
        for (int i = 0; i < BLOCK_COLORS.size(); i++) {
            String textureFile = name + "_" + BLOCK_COLORS.get(i) + ".png";
            addMaterial(material, i, textureFile, minecraftFolder);
        }
    }

    private static int readTexture(String textureFile, File minecraftFolder) {
        textureFile = textureFile.toLowerCase();

        int color = Color.BLACK.getRGB();
        try {
            InputStream reader = new FileInputStream(new File(minecraftFolder, "blocks/" + textureFile));
            BufferedImage image = ImageIO.read(reader);

            int red = 0;
            int green = 0;
            int blue = 0;

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int rgb = image.getRGB(x, y);
                    int a = (rgb >> 24) & 0xFF;
                    if(a != 255) {
                        System.out.println("Skipping " + textureFile + " because it contains a non alpha 255 pixel");
                        return Color.WHITE.getRGB();
                    }

                    red += (rgb >> 16) & 0xFF;
                    green += (rgb >> 8) & 0xFF;
                    blue += rgb & 0xFF;

                }
            }

            float size = (image.getWidth() * image.getHeight());

            red = (int) Math.floor(red / size);
            green = (int) Math.floor(green / size);
            blue = (int) Math.floor(blue / size);

            color = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);

        } catch (Exception e) {
            //System.out.println("Failed to read " + textureFile + " -> " + e);
        }
        return color;
    }

}