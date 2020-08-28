package fr.redstonneur1256.utils;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class SocketAcceptThread extends Thread {
    private ServerSocket server;
    private Consumer<Socket> onConnect;

    public SocketAcceptThread(ServerSocket server, Consumer<Socket> onConnect) {
        this.server = server;
        this.onConnect = onConnect;
    }

    @Override
    public void run() {
        try {
            while(!isInterrupted()) {
                Socket socket = server.accept();
                onConnect.accept(socket);
            }
        }catch(Exception e) {
            if(!(e instanceof SocketException)) {
                e.printStackTrace();
            }
        }
    }

    public interface Consumer<T> {
        void accept(T t) throws Exception;
    }

}
