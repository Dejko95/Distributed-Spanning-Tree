package node;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class NeighbourNode {
    private String id;
    private String host;
    private int port;
    private Socket socket = null;
    private PrintWriter outWriter = null;

    public NeighbourNode(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getOutWriter() {
        return outWriter;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        System.out.println("set socket -> outwriter");
        try {
            this.outWriter = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
