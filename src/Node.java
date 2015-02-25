package src;

import java.util.ArrayList;
import java.awt.Point;

/**
 * Created by Andrew on 2/25/2015.
 */
public class Node {
    private String symbol;
    private ArrayList<Node> neighbors;
    private Point position;

    public Node (String sym, Point pos) {
        this.symbol = sym;
        this.position = pos;
        this.neighbors = new ArrayList<Node>();
    }

    public String get_symbol() {
        return this.symbol;
    }

    public Point get_position() {
        return this.position;
    }

    public ArrayList<Node> get_neighbors() {
        return this.neighbors;
    }

    public boolean contains(Node n) {
        return this.neighbors.contains(n);
    }

    public void add_neighbor(Node candidate) {
        if (!this.contains(candidate)) {
            neighbors.add(candidate);
        }
        if (!candidate.contains(this)) {
            candidate.add_neighbor(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (!position.equals(node.position)) return false;
        if (!symbol.equals(node.symbol)) return false;

        return true;
    }

    public static int manhattan_distance (Node a, Node b) {
        return Math.abs(a.get_position().x - b.get_position().x) + Math.abs(a.get_position().y - b.get_position().y);
    }


    public static int euclidian_distance (Node a, Node b) {
        double dy = a.get_position().y - b.get_position().y;
        double dx = a.get_position().x - b.get_position().x;
        double res = dx*dx + dy*dy;
        return (int) Math.round(Math.sqrt(res));
    }

    public static int manhat_euclid_avg(Node a, Node b) {
        return (int) Math.round(euclidian_distance(a,b) + manhattan_distance(a,b) / 2.0);
    }
}