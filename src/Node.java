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

}
