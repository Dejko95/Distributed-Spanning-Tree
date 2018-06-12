package node;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

public class Node {
    private String id;
    private String host;
    private int port;

    private ArrayList<NeighbourNode> neighbours = new ArrayList<>();
    private Vector<NeighbourNode> children = new Vector<>();
    private Vector<NeighbourNode> unrelated = new Vector<>();
    //private NeighbourNode parent;
    private AtomicReference<NeighbourNode> parent = new AtomicReference<>();

    public Node(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
        parent.set(null);
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

    public ArrayList<NeighbourNode> getNeighbours() {
        return neighbours;
    }


    public Vector<NeighbourNode> getChildren() {
        return children;
    }

//    public NeighbourNode getParent() {
//        return parent;
//    }

    public Vector<NeighbourNode> getUnrelated() {
        return unrelated;
    }

//    public void setParent(NeighbourNode parent) {
//        this.parent = parent;
//    }

    public AtomicReference<NeighbourNode> getParent() {
        return parent;
    }
}
