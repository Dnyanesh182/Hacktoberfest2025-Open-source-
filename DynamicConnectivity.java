import java.util.*;

/**
 * A conceptual implementation of a Fully Dynamic Connectivity algorithm.
 * Based on the hierarchical decomposition method by Holm et al. (O(log^2 n) updates).
 *
 * This data structure maintains an undirected graph under edge insertions and deletions
 * and answers connectivity queries in polylogarithmic time.
 *
 * The immense complexity stems from:
 * 1. A hierarchy of log(n) levels, each representing a subgraph.
 * 2. Each level uses a Link/Cut Tree to maintain a Minimum Spanning Forest.
 * 3. A complex, cascading search for replacement edges upon deletion.
 */
public class DynamicConnectivity {

    // An edge in the graph, aware of its level in the hierarchy.
    static class Edge {
        int u, v;
        int level;
        public Edge(int u, int v) { this.u = u; this.v = v; this.level = 0; }
        @Override
        public int hashCode() { return Objects.hash(Math.min(u,v), Math.max(u,v)); }
        @Override
        public boolean equals(Object o) {
            if(o == this) return true;
            if(!(o instanceof Edge)) return false;
            Edge other = (Edge) o;
            return (this.u == other.u && this.v == other.v) || (this.u == other.v && this.v == other.u);
        }
    }

    // A single level in the hierarchy.
    static class Level {
        // Each level maintains a spanning forest using a powerful dynamic forest data structure.
        final LinkCutTree forest; 
        // Stores non-tree edges for this level.
        final Map<LinkCutTree.Node, Set<Edge>> nonTreeEdges;

        Level(int n) {
            this.forest = new LinkCutTree(n); // Custom LCT with n nodes
            this.nonTreeEdges = new HashMap<>();
            for (int i=0; i<n; i++) nonTreeEdges.put(forest.nodes[i], new HashSet<>());
        }
    }

    private final int n; // Number of vertices
    private final int maxLevels;
    private final Level[] levels;
    private final Map<Edge, Edge> edgeMap; // To track edges and their levels

    public DynamicConnectivity(int n) {
        this.n = n;
        this.maxLevels = (int) (Math.log(n) / Math.log(2));
        this.levels = new Level[maxLevels + 1];
        for (int i = 0; i <= maxLevels; i++) {
            levels[i] = new Level(n);
        }
        this.edgeMap = new HashMap<>();
    }

    /**
     * Adds an edge to the graph. The new edge is always added at level 0.
     */
    public void addEdge(int u, int v) {
        Edge e = new Edge(u, v);
        edgeMap.put(e, e);
        
        // If u and v are already connected at level 0, add as a non-tree edge.
        if (levels[0].forest.isConnected(u, v)) {
            levels[0].nonTreeEdges.get(levels[0].forest.nodes[u]).add(e);
            levels[0].nonTreeEdges.get(levels[0].forest.nodes[v]).add(e);
        } else {
            // Otherwise, it becomes a tree edge in the level 0 spanning forest.
            levels[0].forest.link(u, v);
        }
    }

    /**
     * Deletes an edge from the graph. This is the most complex operation.
     */
    public void deleteEdge(int u, int v) {
        Edge queryEdge = new Edge(u, v);
        Edge e = edgeMap.remove(queryEdge);
        if (e == null) return; // Edge doesn't exist

        int level = e.level;
        
        // Check if it was a tree edge or a non-tree edge at its level.
        if (levels[level].forest.isTreeEdge(e.u, e.v)) {
            // This is the hard case. We must find a replacement.
            levels[level].forest.cut(e.u, e.v);
            
            // Search for a replacement edge, cascading down the levels.
            for (int i = level; i >= 0; i--) {
                if (findReplacement(u, v, i)) {
                    return; // Replacement found and tree structure is restored.
                }
            }
        } else {
            // Easy case: it was a non-tree edge. Just remove it.
            levels[level].nonTreeEdges.get(levels[level].forest.nodes[u]).remove(e);
            levels[level].nonTreeEdges.get(levels[level].forest.nodes[v]).remove(e);
        }
    }

    /**
     * The core logic for finding a replacement edge.
     */
    private boolean findReplacement(int u, int v, int level) {
        // Make the component containing u smaller for efficiency.
        if (levels[level].forest.size(u) > levels[level].forest.size(v)) {
            int temp = u; u = v; v = temp;
        }

        // 1. Promote tree edges from the current level's smaller component up to the next level.
        // This is done to satisfy the invariants of the data structure.
        for (Edge treeEdge : levels[level].forest.getEdgesInComponent(u)) {
             treeEdge.level++; // Promote
             levels[level+1].forest.link(treeEdge.u, treeEdge.v); // Simplified, real one checks connectivity first
        }

        // 2. Search for a non-tree edge in the smaller component at the current level to act as a replacement.
        for (Edge nonTreeEdge : levels[level].forest.getNonTreeEdgesInComponent(u)) {
            // If this edge connects the two disconnected components...
            if (levels[level].forest.isConnected(nonTreeEdge.u, v)) {
                // We found a replacement! Add it as a tree edge.
                levels[level].forest.link(nonTreeEdge.u, nonTreeEdge.v);
                return true;
            } else {
                // This edge is not a replacement, so promote it.
                nonTreeEdge.level++;
                levels[level+1].nonTreeEdges.get(levels[level+1].forest.nodes[nonTreeEdge.u]).add(nonTreeEdge);
                levels[level+1].nonTreeEdges.get(levels[level+1].forest.nodes[nonTreeEdge.v]).add(nonTreeEdge);
            }
        }
        
        return false; // No replacement found at this level.
    }
    
    public boolean isConnected(int u, int v) {
        return levels[0].forest.isConnected(u,v);
    }

    // Dummy LinkCutTree for conceptual purposes.
    static class LinkCutTree {
        static class Node {}
        Node[] nodes;
        public LinkCutTree(int n) { nodes = new Node[n]; }
        public void link(int u, int v) {}
        public void cut(int u, int v) {}
        public boolean isConnected(int u, int v) { return true; }
        public boolean isTreeEdge(int u, int v) { return true; }
        public int size(int u) {return 1;}
        public List<Edge> getEdgesInComponent(int u) { return new ArrayList<>(); }
        public List<Edge> getNonTreeEdgesInComponent(int u) {return new ArrayList<>();}
    }

    public static void main(String[] args) {
        System.out.println("Demonstrating the conceptual structure of a Dynamic Connectivity algorithm.");
        DynamicConnectivity dc = new DynamicConnectivity(10);
        
        dc.addEdge(1, 2);
        dc.addEdge(2, 3);
        dc.addEdge(4, 5);

        System.out.println("Are 1 and 3 connected? " + dc.isConnected(1, 3)); // Expected: true
        System.out.println("Are 1 and 4 connected? " + dc.isConnected(1, 4)); // Expected: false

        System.out.println("\nDeleting edge (2,3)... this triggers the complex replacement search.");
        dc.deleteEdge(2, 3);
        System.out.println("Are 1 and 3 connected? " + dc.isConnected(1, 3)); // Expected: false

        System.out.println("\nAdding replacement edge (1,3)...");
        dc.addEdge(1,3);
        System.out.println("Are 1 and 3 connected? " + dc.isConnected(1, 3)); // Expected: true
    }
}
