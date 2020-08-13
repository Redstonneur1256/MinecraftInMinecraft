package fr.redstonneur1256;

import fr.redstonneur1256.redutilities.graphics.WindowMover;
import fr.redstonneur1256.redutilities.graphics.swing.PlaceHolderTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Frame extends JFrame {

    private MinecraftClient client;
    private PlaceHolderTextField addressField;
    private JButton connectButton;
    private JCheckBox sendData;
    private JButton sendOneFrame;
    private JSpinner updatesSecond;
    private JLabel infoLabel;
    public Frame(MinecraftClient client) {
        this.client = client;

        double ratio = 9.0 / 16.0;
        int width = 450; // Use custom width
        int height = (int) (width * ratio * 3 + 40);

        setUndecorated(true);
        setResizable(false);
        setSize(width, height);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        addressField = new PlaceHolderTextField();
        addressField.setPlaceHolder("Server address");
        addressField.setBounds(0, 0, 200, 20);
        add(addressField);

        connectButton = new JButton("Connect");
        connectButton.setBounds(200, 0, 100, 20);
        connectButton.addActionListener(event -> connect());
        add(connectButton);

        sendData = new JCheckBox("Send data");
        sendData.setBounds(0, 20, 100, 20);
        sendData.addChangeListener(event -> {
            if(sendData.isSelected()) {
                client.startSending();
            } else {
                client.stopSending();
            }
        });
        add(sendData);

        sendOneFrame = new JButton("Update now");
        sendOneFrame.setBounds(200, 20, 100, 20);
        sendOneFrame.addActionListener(event -> {
            try {
                client.sendOneFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        add(sendOneFrame);

        updatesSecond = new JSpinner();
        updatesSecond.setBounds(100, 20, 100, 20);
        updatesSecond.addChangeListener(e -> {
            int value = (int) updatesSecond.getValue();
            if(value > 20 || value < 1) {
                value = (value < 1 ? 1 : 20);
                updatesSecond.setValue(value);
            }
            client.setSendingSpeed(value);
        });
        updatesSecond.setValue(5);
        add(updatesSecond);

        JCheckBox useCache = new JCheckBox("Use color cache");
        useCache.setSelected(true);
        useCache.addChangeListener(event -> client.getPalette().useCache(useCache.isSelected()));
        useCache.setBounds(300, 20, 100, 20);
        add(useCache);

        JButton cacheInformation = new JButton("...");
        cacheInformation.setBorderPainted(false);
        cacheInformation.setBounds(400, 20, 20, 20);
        cacheInformation.addActionListener(event -> JOptionPane.showMessageDialog(this, "If selected, the application will use more memory but lower processor, else the application will have an high processor usage but low memory usage", "Information", JOptionPane.INFORMATION_MESSAGE));
        add(cacheInformation);


        infoLabel = new JLabel("");
        infoLabel.setBounds(300, 0, 200, 20);
        add(infoLabel);

        WindowMover mover = new WindowMover(this);
        mover.apply();

        enableFields(false);

        setVisible(true);
    }

    private void connect() {
        try {
            if (client.isConnected()) {
                client.disconnect();
                connectButton.setText("Connect");
                enableFields(false);
            } else {
                client.connect(addressField.getText(), 12346); // TODO: Add custom port configuration
                connectButton.setText("Disconnect");
                enableFields(true);

                client.setSendingSpeed(5);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void enableFields(boolean connected) {
        addressField.setEnabled(!connected);
        sendData.setEnabled(connected);
        sendOneFrame.setEnabled(connected);
        updatesSecond.setEnabled(connected);
    }

    public void update(double ratio, BufferedImage... images) {
        Graphics graphics = getGraphics();

        int width = getWidth();
        int height = (int) (width * ratio);

        for (int i = 0; i < images.length; i++) {
            int y = 40 + height * i;
            graphics.drawImage(images[i], 0, y, width, height, null);
        }

        graphics.dispose();
    }

    public void setInfoText(String text) {
        infoLabel.setText(text);
    }

    public MinecraftClient getClient() { return client; }
    public PlaceHolderTextField getAddressField() { return addressField; }
    public JButton getConnectButton() { return connectButton; }
    public JCheckBox getSendData() { return sendData; }
    public JButton getSendOneFrame() { return sendOneFrame; }
    public JSpinner getUpdatesSecond() { return updatesSecond; }
    public JLabel getInfoLabel() { return infoLabel; }

}
