package bootstrap;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class BootstrapServer {
    private ServerSocket serverSocket;
    private int port;
    private AtomicInteger finishedSetupConnections = new AtomicInteger(0);
    private HashMap<String, NodeInfo> nodeMap = new HashMap<>();

    public AtomicInteger getFinishedSetupConnections() {
        return finishedSetupConnections;
    }

    private void waitNodesForConnections() throws IOException {

        //read nodes from config
        Scanner sc = new Scanner(new File("config2.txt"));
        String bootstrapInfo[] = sc.nextLine().split(" ");
        port = Integer.parseInt(bootstrapInfo[2]);
        String line = sc.nextLine();
        while (line.length() > 0) {
            String nodeInfo[] = line.split(" ");
            NodeInfo node = new NodeInfo(nodeInfo[0], nodeInfo[1], Integer.parseInt(nodeInfo[2]));
            nodeMap.put(node.getId(), node);
            line = sc.nextLine();
        }

        serverSocket = new ServerSocket(port);
        int connected = 0;
        while (connected < nodeMap.size()) {
            Socket socket = serverSocket.accept();
            Thread thread = new Thread(new BootstrapServerThread(this, socket));
            thread.start();
            connected++;
        }

        serverSocket.close();
    }

    private void pingAndWait(String nodeId) throws IOException {
        System.out.println(nodeId + " pinged");
        Socket socket = new Socket(nodeMap.get(nodeId).getHost(), nodeMap.get(nodeId).getPort());

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
        out.println("bootstrap");
        out.println(nodeId.equals("root") ? "start" : "wait");
        String message = in.readLine(); //expected "finished"
        System.out.println("message response: " + message);
        if (!message.equals("finished")) {
            System.out.println("Algorithm not finished successfully.");
            throw new IOException("Algorithm not finished successfully.");
        }
        socket.close();
    }

    private void waitForTerminations() {
        for (NodeInfo node: nodeMap.values()) {
            if (node.getId().equals("root")) {
                continue;
            }
            try {
                pingAndWait(node.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void drawGraph() {
        new GraphDrawer(nodeMap);
    }


    private void getTreeStructure() throws IOException {
        System.out.println("size:" + nodeMap.size());
        System.out.println(1);
        for (NodeInfo node: nodeMap.values()) {
            Socket socket = new Socket(node.getHost(), node.getPort());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
            out.println("bootstrap");
            out.println("get_children");
            String responseMessage = in.readLine();
            socket.close();
            System.out.println("----------");
            System.out.println(node.getId() + ": " + responseMessage);

            //parse message
            if (responseMessage.length() == 0) continue;
            String childrenIDs[] = responseMessage.split(",");
            for (String childID: childrenIDs) {
                node.addChild(nodeMap.get(childID));
            }
        }
        System.out.println(2);
    }

    public static void main(String[] args) {
        BootstrapServer bootstrapServer = new BootstrapServer();
        try {
            bootstrapServer.waitNodesForConnections();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Port not available");
            return;
        }

        //wait for all nodes to connect themselves
        while (bootstrapServer.finishedSetupConnections.get() < bootstrapServer.nodeMap.size()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //call root node to start algorithm
        try {
            bootstrapServer.pingAndWait("root");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to root node");
            return;
        }

        //wait other nodes to also finish algorithm
        bootstrapServer.waitForTerminations();

        //ask each node for their neighbours in a spanning tree
        try {
            System.out.println("getting children from nodes");
            bootstrapServer.getTreeStructure();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot connect to some node");
            return;
        }

        //draw spanning tree
        for (NodeInfo node: bootstrapServer.nodeMap.values()) {
            System.out.print(node.getId() + ": ");
            for (NodeInfo child: node.getChildren()) {
                System.out.print(child.getId() + ", ");
            }
            System.out.println();
        }
        bootstrapServer.drawGraph();

    }
}
