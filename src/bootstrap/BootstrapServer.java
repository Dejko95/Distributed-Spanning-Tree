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
        Scanner sc = new Scanner(new File("config.txt"));
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

    private void startAlgorithmFromRootNode() throws IOException {
        Socket socket = new Socket(nodeMap.get("root").getHost(), nodeMap.get("root").getPort());
        //we don't send anything, only wait for algorithm to finish ?
        //maybe to send-----------------------------------------

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
        out.println("bootstrap");
        out.println("start");
        String message = in.readLine(); //expected "finished"
        if (!message.equals("finished")) {
            System.out.println("Algorithm not finished successfully.");
            throw new IOException("Algorithm not finished successfully.");
        }
        socket.close();
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
        HashMap<String, NodeInfo> nodeMap = new HashMap<>();
        for (int i=0; i<6; i++) {
            NodeInfo node = new NodeInfo(i == 0 ? "root" : "n",",",1);
            nodeMap.put("" + i, node);
        }
        nodeMap.get("0").addChild(nodeMap.get("1"));
        nodeMap.get("0").addChild(nodeMap.get("4"));
        nodeMap.get("1").addChild(nodeMap.get("4"));
        nodeMap.get("1").addChild(nodeMap.get("5"));
        nodeMap.get("2").addChild(nodeMap.get("3"));
        nodeMap.get("2").addChild(nodeMap.get("4"));
        nodeMap.get("4").addChild(nodeMap.get("5"));
        new GraphDrawer(nodeMap);
        return;
        //wait for all nodes to connect to bootstrap
//        BootstrapServer bootstrapServer = new BootstrapServer();
//        try {
//            bootstrapServer.waitNodesForConnections();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Port not available");
//            return;
//        }
//
//        //wait for all nodes to connect themselves
//        while (bootstrapServer.finishedSetupConnections.get() < bootstrapServer.nodeMap.size()) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        //call root node to start algorithm
//        try {
//            System.out.println("Root notified to start algorithm");
//            bootstrapServer.startAlgorithmFromRootNode();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Cannot connect to root node");
//            return;
//        }
//
//        //ask each node for their neighbours in a spanning tree
//        try {
//            System.out.println("getting children from nodes");
//            bootstrapServer.getTreeStructure();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Cannot connect to some node");
//            return;
//        }
//
//        //draw spanning tree
//        for (NodeInfo node: bootstrapServer.nodeMap.values()) {
//            System.out.print(node.getId() + ": ");
//            for (NodeInfo child: node.getChildren()) {
//                System.out.print(child.getId() + ", ");
//            }
//            System.out.println();
//        }
    }

}
