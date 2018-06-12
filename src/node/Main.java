package node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {
        String id = args[0];

        //get node info
        Scanner sc = null;
        try {
            sc = new Scanner(new File("config.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = sc.nextLine();
        Node node = null;
        HashMap<String, NeighbourNode> allNodes = new HashMap<>();
        while (line.length() > 0) {
            String nodeInfo[] = line.split(" ");
            if (nodeInfo[0].equals(id)) {
                node = new Node(nodeInfo[0], nodeInfo[1], Integer.parseInt(nodeInfo[2]));
            } else {
                allNodes.put(nodeInfo[0], new NeighbourNode(nodeInfo[0], nodeInfo[1], Integer.parseInt(nodeInfo[2])));
            }
            line = sc.nextLine();
        }

        //read connections
        line = sc.nextLine();
        while (line.length() > 0) {
            String edge[] = line.split(" ");
            if (edge[0].equals(node.getId())) {
                node.getNeighbours().add(allNodes.get(edge[1]));
            } else if (edge[1].equals(node.getId())) {
                node.getNeighbours().add(allNodes.get(edge[0]));
            }
            line = sc.nextLine();
        }

        //set up server socket for receiving connections
        {
            Thread thread = new Thread(new ConnectionReceiver(node));
            thread.start();
        }

        //obtain connections with neighbours
        AtomicInteger connectionsCount = new AtomicInteger(0);
        for (NeighbourNode neighbourNode: node.getNeighbours()) {
            Thread thread = new Thread(new ConnectionInitializer(neighbourNode, connectionsCount, node));
            thread.start();
        }
        //wait to connect with all neighbours
        while (connectionsCount.get() < node.getNeighbours().size()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //notify bootstrap
        NeighbourNode bootstrap = allNodes.get("bootstrap");
        try {
            Socket socket = new Socket(bootstrap.getHost(), bootstrap.getPort());
            PrintWriter out=new PrintWriter(socket.getOutputStream(),true);
            out.println("finished_setup");
            System.out.println("finished setup");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("cant connect to bootstrap");
            return;
        }
    }
}
//terminate zapravo treba da pogasi sve, a ne samo taj tred