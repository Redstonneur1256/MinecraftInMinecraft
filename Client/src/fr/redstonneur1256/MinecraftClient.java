package fr.redstonneur1256;

import fr.redstonneur1256.redutilities.Utils;
import fr.redstonneur1256.redutilities.graphics.ImageHelper;
import fr.redstonneur1256.redutilities.graphics.Palette;
import fr.redstonneur1256.redutilities.io.compression.Compression;
import fr.redstonneur1256.utils.BlockData;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MinecraftClient {

    private Frame frame;
    private Robot robot;
    private Palette<BlockData> palette;
    private Rectangle screenRectangle;
    private int[] oldColors;
    private BlockData[] oldData;
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

        frame = new Frame(this, size.width, size.height);
        robot = new Robot();
        palette = new Palette<>(PaletteLoader.air).useCache(true);
        screenRectangle = new Rectangle(0, 0, size.width, size.height);
        oldColors = new int[0];
        oldData = new BlockData[0];

        PaletteLoader paletteLoader = new PaletteLoader(new File("defaultPack"), palette);
        paletteLoader.loadPalette();
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Compression.setMethod(Compression.Method.zLib);
        Compression.setThreadSafe(false);
        Compression.setBufferSize(8129);

        MinecraftClient client = new MinecraftClient(args);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void connect(String address, int port) throws Exception {
        if(isConnected())
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

    public void sendOneFrame() throws Exception {
        if(width == 0 || height == 0)
            return;

        BufferedImage capture = robot.createScreenCapture(screenRectangle);
        BufferedImage image = ImageHelper.resize(capture, width, height);

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(byteOutput);

        dataOutput.writeInt(width);
        dataOutput.writeInt(height);

        Point location = MouseInfo.getPointerInfo().getLocation();
        location.x /= ((double) capture.getWidth() / width);
        location.y /= ((double) capture.getHeight() / height);

        int count = width * height;
        if(oldColors.length != count) {
            oldColors = new int[count];
        }
        if(oldData.length != count) {
            oldData = new BlockData[count];
        }

        BlockData[] type = new BlockData[count];
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                BlockData data;
                int index = x + y * width;

                if(x == location.x && y == location.y) { // Its mouse location
                    data = PaletteLoader.red;
                }else {
                    int rgb = image.getRGB(x, y);
                    int oldRgb = oldColors[index];
                    if(rgb == oldRgb && oldData[index] != null) {
                        data = oldData[index];
                    }else {
                        data = palette.matchColor(image.getRGB(x, y));
                        oldData[index] = data;
                        oldColors[index] = rgb;
                    }
                }

                type[index] = data;

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
            while(isConnected()) {
                byte command = input.readByte();
                switch(command) {
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
                        frame.getConnectButton().setText("Connect");
                        frame.setInfoText("");
                        break;
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void sendData() {
        try {
            long lastUpdate = System.nanoTime();
            int frames = 0;
            long timer = System.currentTimeMillis();

            while(!sendingThread.isInterrupted()) {
                long now = System.nanoTime();
                if(lastUpdate + sendingRate < now) {
                    lastUpdate += sendingRate;
                    if(sendingData) {
                        sendOneFrame();
                        frames++;
                    }
                }else {
                    Utils.sleep(1);
                }
                if(timer + 1000 < System.currentTimeMillis()) { // Every second
                    timer += 1000;
                    frame.setInfoText(frames + " FPS (" + Utils.sizeFormat(sendedBytes, "Bps") + ")");
                    frames = 0;
                    sendedBytes = 0;
                }
            }
        }catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    public Palette<BlockData> getPalette() {
        return palette;
    }

}