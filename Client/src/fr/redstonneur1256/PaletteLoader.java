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

    public static final BlockData air;
    public static final BlockData red;
    private static final List<String> blockColors;
    private static final int badColor;

    static {
        air = new BlockData(Material.AIR.getId(), (byte) 0, Color.WHITE);
        red = new BlockData(Material.CONCRETE.getId(), (byte) 14, Color.RED);

        blockColors = Arrays.asList("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "silver", "cyan", "purple", "blue", "brown", "green", "red", "black");
        badColor = 0x80000000;
    }
    
    private File minecraftFolder;
    private Palette<BlockData> palette;

    public PaletteLoader(File minecraftFolder, Palette<BlockData> palette) {
        this.minecraftFolder = minecraftFolder;
        this.palette = palette;
    }

    public void loadPalette() {
        System.out.println("Loading blocks color list...");
        long start = System.currentTimeMillis();

        List<Material> ignoreTypes = Arrays.asList(Material.BEACON, Material.SEA_LANTERN, Material.REDSTONE_LAMP_OFF,
                Material.REDSTONE_LAMP_ON, Material.TRAP_DOOR, Material.IRON_TRAPDOOR, Material.STRUCTURE_BLOCK,
                Material.DRAGON_EGG, Material.BREWING_STAND, Material.MOB_SPAWNER, Material.GLASS, Material.STAINED_GLASS,
                Material.STAINED_GLASS_PANE, Material.SOUL_SAND, Material.OBSERVER, Material.BONE_BLOCK, Material.GRASS_PATH,
                Material.GRASS);

        List<String> ignoreContains = Arrays.asList("SEED", "ORE", "CHORUS", "DOOR", "ICE", "TERRACOTTA", "SHULKER_BOX",
                "COMMAND", "SLAB", "STAIR", "FENCE");
        List<Material> colored = Arrays.asList(Material.CONCRETE, Material.CONCRETE_POWDER, Material.STAINED_CLAY, Material.WOOL);

        for(Material material : Material.values()) {
            if(!material.isBlock() ||
                    material.isTransparent() ||
                    !material.isSolid() ||
                    ignoreTypes.contains(material)) {
                continue;
            }
            String name = material.name();
            if(ignoreContains.stream().anyMatch(name::contains)) {
                continue;
            }

            if(colored.contains(material)) {
                addColoredMaterial(material);
            }else {
                addMaterial(material);
            }
        }

        addColoredMaterial(Material.CONCRETE);
        addColoredMaterial(Material.CONCRETE_POWDER);
        addColoredMaterial(Material.STAINED_CLAY, "hardened_clay_stained");
        addColoredMaterial(Material.WOOL, "wool_colored");

        long end = System.currentTimeMillis();
        long time = end - start;

        long milliseconds = time % 1000;
        long seconds = time / 1000;

        String timeFormat = (seconds > 0 ? seconds + " s " : "") + (milliseconds > 0 ? milliseconds + " ms" : "");

        System.out.printf("Loaded %s block/color materials from %s in %s%n", palette.getColors().size(), minecraftFolder, timeFormat);
    }

    private void addMaterial(Material material) {
        addMaterial(material, 0, material.name() + ".png");
    }

    private void addMaterial(Material material, int data, String textureFile) {
        File file = new File(minecraftFolder, "blocks/" + textureFile);
        if(!file.exists())
            return;
        int color = readTexture(file);
        if(color == badColor)
            return;
        palette.addColor(new BlockData(material.getId(), (byte) data, new Color(color)));
    }

    private void addColoredMaterial(Material material) {
        addColoredMaterial(material, material.name().toLowerCase());
    }

    private void addColoredMaterial(Material material, String name) {
        for(int i = 0; i < blockColors.size(); i++) {
            String textureFile = name + "_" + blockColors.get(i) + ".png";
            addMaterial(material, i, textureFile);
        }
    }

    private static int readTexture(File file) {
        int color = badColor;
        try {
            InputStream reader = new FileInputStream(file);
            BufferedImage image = ImageIO.read(reader);

            int red = 0;
            int green = 0;
            int blue = 0;

            for(int x = 0; x < image.getWidth(); x++) {
                for(int y = 0; y < image.getHeight(); y++) {
                    int rgb = image.getRGB(x, y);
                    int a = (rgb >> 24) & 0xFF;
                    if(a != 255) { // Avoid materials like glass because color result will be depend on block under him
                        System.out.println("Skipping " + file + " because it contains a non alpha 255 pixel");
                        return badColor;
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

        }catch(Exception e) {
            //System.out.println("Failed to read " + textureFile + " -> " + e);
        }
        return color;
    }

}