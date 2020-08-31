package fr.redstonneur1256;

import fr.redstonneur1256.commands.ConfigCommand;
import fr.redstonneur1256.redutilities.io.compression.Compression;
import fr.redstonneur1256.utils.BlockData;
import fr.redstonneur1256.utils.SocketAcceptThread;
import fr.redstonneur1256.utils.Vars;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MinecraftInMinecraft extends JavaPlugin {

    private int width, height;
    private List<Point> updateOrder;
    private ServerSocket server;
    private Client client;
    private BlockData[] blockData;

    @Override
    public void onEnable() {
        Compression.setMethod(Compression.Method.zLib);
        Compression.setThreadSafe(false);
        Compression.setBufferSize(8129);

        saveDefaultConfig();
        FileConfiguration config = getConfig();

        reloadConfig();

        getCommand("config").setExecutor(new ConfigCommand());

        try {
            server = new ServerSocket(config.getInt("port", Vars.defaultPort));
            SocketAcceptThread thread = new SocketAcceptThread(server, this::setSocket);
            thread.start();
        }catch(Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDisable() {
        try {
            server.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        clearBlocks();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        FileConfiguration config = getConfig();
        width = config.getInt("width");
        height = config.getInt("height");

        blockData = new BlockData[width * height];
        for(int i = 0; i < blockData.length; i++) {
            blockData[i] = new BlockData(0, (byte) 0);
        }

        updateOrder = new ArrayList<>();
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                updateOrder.add(new Point(x, y));
            }
        }

        Point mapCenter = new Point(width / 2, height / 2);
        updateOrder.sort((pointOne, pointTwo) -> {
            double distanceOne = pointOne.distance(mapCenter);
            double distanceTwo = pointTwo.distance(mapCenter);
            return (int) (distanceOne - distanceTwo);
        });

        if(client != null) {
            try {
                client.changeSize(width, height);
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setSocket(Socket socket) throws Exception {
        if(client != null) {
            try {
                client.disconnect("Another socket connected");
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
        client = new Client(socket);
        client.changeSize(width, height); // Send him the current display size
        client.start();
    }

    public void clearBlocks() {
        World world = Bukkit.getWorld("world");

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                world.getBlockAt(x, 1, y).setType(Material.AIR);
            }
        }
    }

    public void updateBlocks() {
        World world = Bukkit.getWorld("world");

        for(Point point : updateOrder) {
            setBlock(world, point.x, point.y);
        }
    }

    private void setBlock(World world, int x, int y) {
        int index = x + y * width;
        if(index < 0 || index > blockData.length)
            return;

        Block block = world.getBlockAt(x, 1, y);

        BlockData data = blockData[index];

        if(block.getTypeId() != data.type)
            block.setTypeId(data.type);
        if(block.getData() != data.data)
            block.setData(data.data);
    }

    public void setBlocks(BlockData[] blockData) {
        this.blockData = blockData;
    }

    public BlockData[] getBlockData() {
        return blockData;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


}
