package node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable {
    private Node node;
    private Socket socket;
    private NeighbourNode neighbourNode = null;

    public ServerThread(Node node, Socket socket) {
        this.node = node;
        this.socket = socket;
    }

    static int counter = 1;

    @Override
    public void run() {
        int x = counter++;
        System.out.println("Thread started-------------" + x);
        try {
            PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String messageID = in.readLine();
            System.out.println("messageID: " + messageID);
            for (NeighbourNode neighbour: node.getNeighbours()) {
                if (neighbour.getId().equals(messageID)) {
                    neighbourNode = neighbour;
                }
            }


                String message = in.readLine();
                System.out.println("from " + (neighbourNode != null ? neighbourNode.getId() : "bootstrap") + ": " + message);
                if (message.equals("start")) {
                    node.getParent().set(new NeighbourNode(node.getId(), node.getHost(), node.getPort()));
                    queryNeighbours();
                    waitForTermination();
                    System.out.println("finished");
                    out.println("finished");
                    System.out.println("Finish thread " + (neighbourNode != null ? neighbourNode.getId() : "bootstrap"));
                } else if (message.equals("wait")) {
                    waitForTermination();
                    System.out.println("finished");
                    out.println("finished");
                } else if (message.equals("query")) {
                    if (node.getParent().compareAndSet(null, neighbourNode)) {
                        accept();
                        queryNeighbours();
                    } else {
                        node.getUnrelated().add(neighbourNode);
                        reject();
                    }
                } else if (message.equals("accept")) {
                    node.getChildren().add(neighbourNode);
                } else if (message.equals("reject")) {
                    node.getUnrelated().add(neighbourNode);
                } else if (message.equals("get_children")) {
                    StringBuilder response = new StringBuilder("");
                    for (NeighbourNode child: node.getChildren()) {
                        response.append(child.getId() + ",");
                    }
                    System.out.println(response.toString());
                    out.println(response.toString());
                    System.out.println("Finish thread " + (neighbourNode != null ? neighbourNode.getId() : "bootstrap"));
                }
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Thread finished-------------" + x);
    }

    private void waitForTermination() {
        while (node.getChildren().size() + node.getUnrelated().size() < node.getNeighbours().size() - 1 || node.getParent().get() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("children: " + node.getChildren().size() + ", unrelated: " + node.getUnrelated().size());
        }
        System.out.println("termination");
    }

    private void accept() {
        neighbourNode.getOutWriter().println("accept");
        try {
            neighbourNode.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reject() {
        neighbourNode.getOutWriter().println("reject");
        try {
            neighbourNode.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void queryNeighbours() {
        for (NeighbourNode neighbour: node.getNeighbours()) {
            if (!neighbour.equals(node.getParent().get())) {
                neighbour.getOutWriter().println("query");
                try {
                    neighbour.getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
