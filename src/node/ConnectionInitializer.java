package node;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionInitializer implements Runnable{
    NeighbourNode neighbourNode;
    AtomicInteger connectionsCount;
    Node node;

    ConnectionInitializer(NeighbourNode neighbourNode, AtomicInteger connectionsCount, Node node) {
        this.neighbourNode = neighbourNode;
        this.connectionsCount = connectionsCount;
        this.node = node;
    }

    @Override
    public void run() {
        boolean connected = false;
        Socket socket;
        while (!connected) {
            try {
                socket = new Socket(neighbourNode.getHost(), neighbourNode.getPort());
                connected = true;
                neighbourNode.setSocket(socket);
                neighbourNode.getOutWriter().println(node.getId());
                connectionsCount.incrementAndGet();
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
