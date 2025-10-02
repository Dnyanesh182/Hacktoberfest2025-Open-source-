import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of a Suffix Tree using Ukkonen's online O(N) algorithm.
 * A suffix tree stores all suffixes of a given string in a way that allows for
 * highly efficient string searching and other complex stringology tasks.
 *
 * The complexity of this implementation stems from:
 * - The management of the "active point" (activeNode, activeEdge, activeLength).
 * - The use of "suffix links" to achieve linear-time construction.
 * - The subtle rules for extending the tree by splitting edges or adding leaves.
 */
public class SuffixTree {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz$";
    private final String text;
    private final Node root;
    
    // State variables for the online construction algorithm
    private Node activeNode;
    private int activeEdge;   // Represents the starting character of the edge
    private int activeLength;
    private int remainingSuffixes;

    // A sentinel node to handle certain edge cases with suffix links
    private static final Node DUMMY_NODE = new Node(-1, -1, null);

    // Inner class representing a node in the suffix tree
    private static class Node {
        // [start, end) represents the edge label leading to this node
        int start;
        int end;
        Node suffixLink;
        Map<Character, Node> children = new HashMap<>();

        Node(int start, int end, Node parent) {
            this.start = start;
            this.end = end;
            this.suffixLink = DUMMY_NODE; // Initialize to dummy by default
        }

        int edgeLength(int currentPosition) {
            return Math.min(end, currentPosition + 1) - start;
        }
    }

    public SuffixTree(String text) {
        // Append a unique terminal character if not present
        this.text = text.endsWith("$") ? text : text + "$";
        this.root = new Node(-1, -1, null);
        this.root.suffixLink = root; // Root's suffix link points to itself
        
        this.activeNode = root;
        this.activeEdge = -1;
        this.activeLength = 0;
        this.remainingSuffixes = 0;

        build();
    }

    private void build() {
        for (int i = 0; i < text.length(); i++) {
            extend(i);
        }
    }

    // The core of Ukkonen's algorithm
    private void extend(int position) {
        char currentChar = text.charAt(position);
        remainingSuffixes++;
        Node lastNewNode = null;

        while (remainingSuffixes > 0) {
            // If activeLength is 0, we are at a node.
            if (activeLength == 0) {
                activeEdge = position;
            }

            // Check if a path for the current character exists from the active node.
            if (!activeNode.children.containsKey(text.charAt(activeEdge))) {
                // Rule 2: No path exists. Create a new leaf.
                activeNode.children.put(text.charAt(activeEdge), new Node(position, text.length(), activeNode));
                if (lastNewNode != null) {
                    lastNewNode.suffixLink = activeNode;
                }
                lastNewNode = null;
            } else {
                Node nextNode = activeNode.children.get(text.charAt(activeEdge));
                int edgeLen = nextNode.edgeLength(position);

                // Walk down if the activeLength is greater than the edge length
                if (activeLength >= edgeLen) {
                    activeEdge += edgeLen;
                    activeLength -= edgeLen;
                    activeNode = nextNode;
                    continue; // Re-evaluate from the new active node
                }

                // Rule 3: Path exists, but we need to split the edge.
                if (text.charAt(nextNode.start + activeLength) == currentChar) {
                    // Observation 3: The character is already on the edge.
                    // We just update the active length and do nothing.
                    activeLength++;
                    if (lastNewNode != null) {
                        lastNewNode.suffixLink = activeNode;
                    }
                    break;
                }

                // We must split the edge.
                Node splitNode = new Node(nextNode.start, nextNode.start + activeLength, activeNode);
                activeNode.children.put(text.charAt(activeEdge), splitNode);
                
                // New leaf for the current character
                splitNode.children.put(currentChar, new Node(position, text.length(), splitNode));
                
                // The old path becomes a child of the new split node
                nextNode.start += activeLength;
                splitNode.children.put(text.charAt(nextNode.start), nextNode);

                if (lastNewNode != null) {
                    lastNewNode.suffixLink = splitNode;
                }
                lastNewNode = splitNode;
            }

            remainingSuffixes--;
            
            // Update the active point for the next extension
            if (activeNode == root && activeLength > 0) {
                activeLength--;
                activeEdge = position - remainingSuffixes + 1;
            } else if (activeNode != root){
                activeNode = activeNode.suffixLink;
            }
        }
    }

    // Method to visualize the tree (for debugging/demonstration)
    public void printTree() {
        System.out.println("Suffix Tree for: '" + text + "'");
        printRecursive(root, "");
    }

    private void printRecursive(Node node, String indent) {
        if (node != root) {
            String edgeLabel = text.substring(node.start, Math.min(node.end, text.length()));
            System.out.println(indent + "-> [" + edgeLabel + "] (" + node.start + "," + node.end + ")");
        }

        for (Node child : node.children.values()) {
            printRecursive(child, indent + "  ");
        }
    }

    public static void main(String[] args) {
        String testString = "mississippi";
        System.out.println("Building Suffix Tree for: " + testString);
        SuffixTree st = new SuffixTree(testString);
        st.printTree();
        
        System.out.println("\n----------------------------------\n");
        
        testString = "bananas";
        System.out.println("Building Suffix Tree for: " + testString);
        st = new SuffixTree(testString);
        st.printTree();
    }
}
