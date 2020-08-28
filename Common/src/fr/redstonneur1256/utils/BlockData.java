package fr.redstonneur1256.utils;

import fr.redstonneur1256.redutilities.graphics.Palette;

import java.awt.*;

public class BlockData extends Palette.ColorContainer {

    public int type;
    public byte data;

    public BlockData(int material, byte data) {
        this(material, data, Color.BLACK);
    }

    public BlockData(int material, byte data, Color color) {
        super(color);
        this.type = material;
        this.data = data;
    }

}