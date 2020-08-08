package fr.redstonneur1256;

import fr.redstonneur1256.redutilities.Utils;
import fr.redstonneur1256.redutilities.graphics.ImageHelper;
import fr.redstonneur1256.redutilities.graphics.Palette;
import fr.redstonneur1256.utils.BlockData;
import fr.redstonneur1256.utils.Compression;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MinecraftClient {

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        MinecraftClient client = new MinecraftClient(args);
    }

    private Frame frame;
    private Robot robot;
    private Palette<BlockData> palette;
    private Rectangle screenRectangle;
    private int width, height;
    private double ratio;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private Thread receiveThread;
    private Thread sendingThread;
    private boolean sendingData;
    private long sendingRate;
    private long sendedBytes;

    private MinecraftClient(String[] args) throws Exception {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

        frame = new Frame(this);
        robot = new Robot();
        palette = new Palette<BlockData>().useCache(true);
        screenRectangle = new Rectangle(0, 0, size.width, size.height);

        PaletteLoader paletteLoader = new PaletteLoader(palette);
        paletteLoader.loadPalette(new File("defaultPack")); // TODO: Add option to allow users to choose pack
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void connect(String address, int port) throws Exception {
        if (isConnected())
            disconnect();
        socket = new Socket();
        socket.connect(new InetSocketAddress(address, port));
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());

        receiveThread = new Thread(this::receiveData);
        receiveThread.start();

        sendingThread = new Thread(this::sendData);
        sendingThread.start();
    }

    public void disconnect() throws Exception {
        if(!isConnected())
            return;
        socket.close();

        receiveThread.interrupt();
    }

    public void setSendingSpeed(int i) {
        sendingRate = 1_000_000_000L / i;
    }

    public boolean isSendingData() {
        return sendingThread != null && sendingThread.isAlive();
    }

    public void startSending() {
        sendingData = true;
    }

    public void stopSending() {
        sendingData = false;
    }

    public void sendOneFrame() throws IOException {
        if(width == 0 || height == 0)
            return;

        BufferedImage capture = robot.createScreenCapture(screenRectangle);
        BufferedImage image = ImageHelper.resize(capture, width, height);

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(byteOutput);

        dataOutput.writeInt(width);
        dataOutput.writeInt(height);

        Point location = MouseInfo.getPointerInfo().getLocation();
        location.x = location.x / (capture.getWidth() / width);
        location.y = location.y / (capture.getHeight() / height);

        BlockData[] type = new BlockData[width * height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                BlockData data;

                if(x == location.x && y == location.y) { // Its mouse location
                    data = PaletteLoader.RED;
                } else {
                    data = palette.matchColor(image.getRGB(x, y));
                }

                type[x + y * width] = data;

                dataOutput.writeInt(data.type);
                dataOutput.writeByte(data.data);
            }
        }

        BufferedImage minecraftPreview = palette.toImage(type, width, height);

        frame.update(ratio, capture, image, minecraftPreview);


        byte[] data = byteOutput.toByteArray();

        byte[] compressed = Compression.compress(data);

        output.writeInt(compressed.length);
        output.write(compressed);
        output.flush();

        sendedBytes += 4; // Compressed data length
        sendedBytes += compressed.length;
    }

    private void receiveData() {
        try {
            while (isConnected()) {
                byte command = input.readByte();
                switch (command) {
                    case 0:
                        width = input.readInt();
                        height = input.readInt();
                        ratio = (double) height / width;
                        break;
                    case 1:
                        String message = input.readUTF();
                        disconnect();
                        JOptionPane.showMessageDialog(frame, message, "Disconnected:", JOptionPane.INFORMATION_MESSAGE);
                        System.out.println("Server requested disconnect for reason: " + message);
                        frame.enableFields(false);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendData() {
        try {
            long lastUpdate = System.nanoTime();
            int ticks = 0;
            long timer = System.currentTimeMillis();

            while(!sendingThread.isInterrupted()) {
                long now = System.nanoTime();
                if(lastUpdate + sendingRate < now) {
                    lastUpdate += sendingRate;
                    if(sendingData)
                        sendOneFrame();
                    ticks++;
                } else {
                    Utils.sleep(1);
                }
                if(timer + 1000 < System.currentTimeMillis()) { // One second
                    timer += 1000;
                    frame.setInfoText(ticks + " FPS (" + Utils.sizeFormat(sendedBytes, "bps") + ")");
                    ticks = 0;
                    sendedBytes = 0;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public Palette<BlockData> getPalette() { return palette; }

}