package node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionReceiver implements Runnable {
    private Node node;

    public ConnectionReceiver(Node node) {
        this.node = node;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(node.getPort());
            int connected = 0;
            int bootstrapConnections = 2;
            while (connected < node.getNeighbours().size() + bootstrapConnections) {
                Socket clientSocket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(node, clientSocket);
                Thread thread = new Thread(serverThread);
                thread.start();
                connected++;
            }
            System.out.println("finished with waiting for connections");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("receiver finished");
    }
}
