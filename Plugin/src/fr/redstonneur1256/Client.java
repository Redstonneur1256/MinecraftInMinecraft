package fr.redstonneur1256;

import fr.redstonneur1256.utils.BlockData;
import fr.redstonneur1256.utils.Compression;
import fr.redstonneur1256.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {

    private Socket socket;
    private DataOutputStream writer;
    private DataInputStream reader;
    public Client(Socket socket) throws Exception {
        this.socket = socket;
        this.writer = new DataOutputStream(socket.getOutputStream());
        this.reader = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        MinecraftInMinecraft plugin = JavaPlugin.getPlugin(MinecraftInMinecraft.class);
        try {

            while(!socket.isClosed() && !isInterrupted()) {
                int amount = reader.readInt();
                while(reader.available() < amount) {
                    Utils.sleep(15);
                }
                byte[] data = new byte[amount];

                int count = reader.read(data);
                if (count != amount) {
                    System.out.printf("Expected %s bytes of data, found only %s%n", amount, count);
                    continue;
                }

                byte[] decompressed = Compression.decompress(data);

                DataInputStream input = new DataInputStream(new ByteArrayInputStream(decompressed));

                BlockData[] blockData = plugin.getBlockData();

                int xMax = input.readInt();
                int yMax = input.readInt();
                for (int x = 0; x < xMax; x++) {
                    for (int y = 0; y < yMax; y++) {
                        int index = x + y * plugin.getWidth();

                        int type = input.readInt();
                        byte bData = input.readByte();

                        BlockData block = blockData[index];
                        block.type = type;
                        block.data = bData;
                    }
                }

                Bukkit.getScheduler().runTask(plugin, plugin::updateBlocks);
            }


        }catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void disconnect(String message) throws IOException {
        if(socket.isClosed())
            return;

        writer.writeByte(1); // Disconnect code
        writer.writeUTF(message);
        writer.flush();
        socket.close();
        interrupt();
    }

    public void changeSize(int width, int height) throws IOException {
        if(socket.isClosed())
            return;

        writer.writeByte(0); // Change size code.
        writer.writeInt(width);
        writer.writeInt(height);
        writer.flush();
    }
}
