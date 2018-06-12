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

    @Override
    public void run() {
        try {
            PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            while (true) {
//                String message = in.readLine();
//                System.out.println("msg: " + message);
//                if (message.length() > 1000) break;
//            }
            String messageID = in.readLine();
            System.out.println("messageID: " + messageID);
            for (NeighbourNode neighbour: node.getNeighbours()) {
                if (neighbour.getId().equals(messageID)) {
                    neighbourNode = neighbour;
                }
            }


            while (true) {
                String message = in.readLine();
                System.out.println("from " + (neighbourNode != null ? neighbourNode.getId() : "bootstrap") + ": " + message);
                if (message.equals("start")) {
                    node.getParent().set(new NeighbourNode(node.getId(), node.getHost(), node.getPort()));
                    queryNeighbours();
                    while (node.getChildren().size() + node.getUnrelated().size() < node.getNeighbours().size() - 1) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("children: " + node.getChildren().size() + ", unrelated: " + node.getUnrelated().size());
                    }
                    System.out.println("finished");
                    out.println("finished");
                    socket.close();
                    System.out.println("Finish thread " + (neighbourNode != null ? neighbourNode.getId() : "bootstrap"));
                    return;
                } else if (message.equals("query")) {
                    //compare and set parent
                    if (node.getParent().compareAndSet(null, neighbourNode)) {
                        accept();
                        queryNeighbours();
                        if (node.getChildren().size() + node.getUnrelated().size() == node.getNeighbours().size() - 1) {
                            socket.close();
                            System.out.println("Finish thread " + (neighbourNode != null ? neighbourNode.getId() : "bootstrap"));
                            return;
                        }
                    } else {
                        reject();
                    }
                } else if (message.equals("accept")) {
                    node.getChildren().add(neighbourNode);
                    if (node.getChildren().size() + node.getUnrelated().size() == node.getNeighbours().size() - 1) {
                        socket.close();
                        System.out.println("Finish thread " + (neighbourNode != null ? neighbourNode.getId() : "bootstrap"));
                        return;
                    }
                } else if (message.equals("reject")) {
                    node.getUnrelated().add(neighbourNode);
                    if (node.getChildren().size() + node.getUnrelated().size() == node.getNeighbours().size() - 1) {
                        socket.close();
                        System.out.println("Finish thread " + (neighbourNode != null ? neighbourNode.getId() : "bootstrap"));
                        return;
                    }
                } else if (message.equals("get_children")) {
                    StringBuilder response = new StringBuilder("");
                    for (NeighbourNode child: node.getChildren()) {
                        response.append(child.getId() + ",");
                    }
                    System.out.println(response.toString());
                    out.println(response.toString());
                    socket.close();
                    System.out.println("Finish thread " + (neighbourNode != null ? neighbourNode.getId() : "bootstrap"));
                    return;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void accept() {
        neighbourNode.getOutWriter().println("accept");
    }

    private void reject() {
        neighbourNode.getOutWriter().println("reject");
    }

    private void queryNeighbours() {
        for (NeighbourNode neighbour: node.getNeighbours()) {
            if (!neighbour.equals(node.getParent().get())) {
                neighbour.getOutWriter().println("query");
            }
        }
    }
}
