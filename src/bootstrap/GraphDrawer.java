package bootstrap;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class GraphDrawer extends JFrame {
    HashMap<String, NodeInfo> nodeMap;

    public GraphDrawer(HashMap<String, NodeInfo> nodeMap) {
        this.nodeMap = nodeMap;
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(new DrawPanel());
        pack();
    }

    private class DrawPanel extends JPanel {
        public DrawPanel() {
            setVisible(true);
            setPreferredSize(new Dimension(600, 600));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2));

            int nodeCount = nodeMap.size();
            double angle = 2 * Math.PI / nodeCount;
            double currAngle = Math.PI;
            System.out.println(angle);
            for (NodeInfo node: nodeMap.values()) {
                int x = 300 + (int)(Math.sin(currAngle) * 250);
                int y = 300 + (int)(Math.cos(currAngle) * 250);
                System.out.println(x + " " + y);
                node.setX(x);
                node.setY(y);
                currAngle += angle;
            }

            g.setColor(Color.DARK_GRAY);
            //draw edges
            for (NodeInfo node: nodeMap.values()) {
                for (NodeInfo child: node.getChildren()) {
                    g.drawLine(node.getX(), node.getY(), child.getX(), child.getY());
                }
            }

            //draw nodes
            for (NodeInfo node: nodeMap.values()) {
                g2.setColor(Color.WHITE);
                g.fillOval(node.getX() - NodeInfo.width, node.getY() - NodeInfo.height, 2 * NodeInfo.width, 2 * NodeInfo.height);
                g2.setColor(node.getColor());
                g2.drawOval(node.getX() - NodeInfo.width, node.getY() - NodeInfo.height, 2 * NodeInfo.width, 2 * NodeInfo.height);
                g2.drawString(node.getId() + " (" + node.getHost() + "," + node.getPort() + ")", node.getX() - NodeInfo.width, node.getY() - NodeInfo.height - 2);
            }
        }
    }
}
