/**
 * An implementation of a Link/Cut Tree.
 *
 * This data structure maintains a dynamic forest of trees and supports operations
 * such as linking two trees, cutting an edge to split a tree, and finding the
 * root of a node's tree, all in O(log n) amortized time.
 *
 * It is a "data structure of data structures," representing the actual trees
 * via a preferred path decomposition, where each path is stored in an auxiliary
 * splay tree. The complexity comes from managing the pointers of the splay trees
 * and the "path-parent" pointers that link them together.
 */
public class LinkCutTree {

    /**
     * Represents a node in both the represented tree and the auxiliary splay tree.
     */
    static class Node {
        // Splay tree pointers
        Node parent = null;
        Node left = null;
        Node right = null;
        
        // Pointer to the parent in the represented tree, connecting different splay trees.
        Node pathParent = null;

        // For path operations, e.g., reversing a path to make a node the root.
        boolean isReversed = false;
        
        // For debugging
        final int id;
        Node(int id) { this.id = id; }
        
        /**
         * Checks if this node is the root of its auxiliary splay tree.
         * A node is a splay tree root if its parent pointer is null or
         * if its parent does not consider it a left/right child. This is
         * how we identify the top of a preferred path.
         */
        boolean isSplayRoot() {
            return parent == null || (parent.left != this && parent.right != this);
        }

        @Override
        public String toString() {
            return "Node " + id;
        }
    }

    // --- Core Splay Tree Operations ---
    // These operations manipulate the auxiliary trees.

    private void push(Node x) {
        if (!x.isReversed) return;
        Node temp = x.left;
        x.left = x.right;
        x.right = temp;
        if (x.left != null) x.left.isReversed = !x.left.isReversed;
        if (x.right != null) x.right.isReversed = !x.right.isReversed;
        x.isReversed = false;
    }

    private void connect(Node child, Node parent, boolean isLeftChild) {
        if (child != null) child.parent = parent;
        if (parent != null) {
            if (isLeftChild) parent.left = child;
            else parent.right = child;
        }
    }

    private void rotate(Node x) {
        Node p = x.parent;
        Node g = p.parent;
        boolean isRootP = p.isSplayRoot();
        boolean isLeftChildX = (x == p.left);

        connect(isLeftChildX ? x.right : x.left, p, isLeftChildX);
        if (!isRootP) connect(x, g, p == g.left);
        else x.parent = g;
        connect(p, x, !isLeftChildX);
        
        // The path parent link belongs to the splay tree root, so update it.
        if (!isRootP) x.pathParent = p.pathParent;
        p.pathParent = null;
    }

    private void splay(Node x) {
        // Push down reversals from ancestors to x before rotations
        while (!x.isSplayRoot()) {
             Node p = x.parent;
             Node g = p.parent;
             if (!p.isSplayRoot()) push(g);
             push(p);
             push(x);
             if (!p.isSplayRoot()) {
                 if ((p.left == x) == (g.left == p)) rotate(p);
                 else rotate(x);
             }
             rotate(x);
        }
        push(x);
    }
    
    // --- Link/Cut Tree Core Operations ---
    
    /**
     * The most important operation. Makes the path from x to the root of its
     * represented tree the new preferred path. After access(x), x becomes the
     * root of its auxiliary splay tree.
     * @return The root of the new splay tree (the previous root of x's tree).
     */
    private Node access(Node x) {
        splay(x);
        // Disconnect right child, as it represents nodes deeper than x
        x.right = null;

        Node last = x;
        while (x.pathParent != null) {
            Node y = x.pathParent;
            last = y;
            splay(y);
            // Switch y's preferred child to x's path
            y.right = x;
            splay(x);
        }
        return last;
    }

    /**
     * Makes node x the root of its represented tree.
     */
    public void makeRoot(Node x) {
        access(x);
        x.isReversed = !x.isReversed;
        push(x);
    }
    
    /**
     * Finds the root of the tree that node x belongs to.
     */
    public Node findRoot(Node x) {
        access(x);
        while (x.left != null) {
            x = x.left;
            push(x);
        }
        splay(x);
        return x;
    }
    
    /**
     * Creates a link from node u to node v.
     * Assumes u is a root and u and v are in different trees.
     */
    public void link(Node u, Node v) {
        makeRoot(u);
        access(v);
        u.pathParent = v;
    }

    /**
     * Cuts the edge between node v and its parent.
     */
    public void cut(Node v) {
        access(v);
        if (v.left != null) {
            v.left.parent = null;
            v.left.pathParent = v.pathParent;
            v.pathParent = null;
            v.left = null;
        }
    }

    public static void main(String[] args) {
        // Create 6 nodes
        Node[] nodes = new Node[6];
        for (int i = 0; i < 6; i++) {
            nodes[i] = new Node(i + 1);
        }

        LinkCutTree lct = new LinkCutTree();
        
        System.out.println("Building a path: 1-2-3 and a separate node 4");
        // Forest: {1}, {2}, {3}, {4}, {5}, {6}
        lct.link(nodes[0], nodes[1]); // 1-2
        lct.link(nodes[1], nodes[2]); // 1-2-3
        // Forest: {1,2,3}, {4}, {5}, {6}

        System.out.println("Root of 3 is: " + lct.findRoot(nodes[2])); // Expected: Node 1
        System.out.println("Root of 4 is: " + lct.findRoot(nodes[3])); // Expected: Node 4
        System.out.println("Are 1 and 3 connected? " + (lct.findRoot(nodes[0]) == lct.findRoot(nodes[2]))); // Expected: true
        System.out.println("Are 1 and 4 connected? " + (lct.findRoot(nodes[0]) == lct.findRoot(nodes[3]))); // Expected: false
        
        System.out.println("\nLinking 4 to 3...");
        lct.link(nodes[3], nodes[2]); // 1-2-3-4
        System.out.println("Root of 4 is now: " + lct.findRoot(nodes[3])); // Expected: Node 1
        
        System.out.println("\nCutting the edge from 2 to its parent (1)...");
        lct.cut(nodes[1]);
        // Forest: {1}, {2,3,4}
        System.out.println("Root of 3 is now: " + lct.findRoot(nodes[2])); // Expected: Node 2
        System.out.println("Root of 1 is now: " + lct.findRoot(nodes[0])); // Expected: Node 1
        System.out.println("Are 1 and 3 connected? " + (lct.findRoot(nodes[0]) == lct.findRoot(nodes[2]))); // Expected: false
        
        System.out.println("\nMaking 3 the root of its tree...");
        lct.makeRoot(nodes[2]);
        System.out.println("Root of 4 is now: " + lct.findRoot(nodes[3])); // Expected: Node 3
    }
}
