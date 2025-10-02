import java.util.*;

/**
 * A Node class represents a single point on the grid for the A* search.
 * It stores its coordinates, its costs (g, h, f), and its parent for path reconstruction.
 */
class Node implements Comparable<Node> {
    int x, y;
    double g; // Cost from the start node to this node
    double h; // Heuristic: estimated cost from this node to the end node
    double f; // Total cost: f = g + h
    Node parent;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.f, other.f);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

/**
 * An implementation of the A* (A-Star) pathfinding algorithm on a 2D grid.
 */
public class AStarSearch {

    /**
     * Calculates the heuristic (Manhattan distance) between two nodes.
     */
    private double calculateHeuristic(Node a, Node b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /**
     * Reconstructs the path from the end node back to the start node.
     */
    private List<Node> reconstructPath(Node endNode) {
        List<Node> path = new ArrayList<>();
        Node current = endNode;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Finds the shortest path from a start to an end node on a grid.
     * @param grid The grid representation
