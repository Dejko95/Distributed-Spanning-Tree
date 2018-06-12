package bootstrap;

import java.awt.*;
import java.util.ArrayList;

public class NodeInfo {
    private String id;
    private String host;
    private int port;
    private ArrayList<NodeInfo> children = new ArrayList<>();

    private int x, y;
    private Color color;

    public static int width = 20, height = 20;

    public NodeInfo(String id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
        if (id.equals("root")) {
            color = Color.RED;
        } else {
            color = Color.DARK_GRAY;
        }
    }

    public void paintNode(Graphics2D g) {
        g.setColor(Color.white);
        g.fillOval(x - width, y - height, width, height);
        g.setColor(color);
        g.drawOval(x - width, y - height, width, height);
        g.drawString("" + id, x, y);
    }

    public void paintConnections(Graphics2D g) {
        g.setColor(Color.black);
        for (NodeInfo child: children) {
            g.drawLine(x, y, child.x, child.y);
        }
    }

    public void addChild(NodeInfo child) {
        children.add(child);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getId() {
        return id;
    }

    public ArrayList<NodeInfo> getChildren() {
        return children;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }
}
