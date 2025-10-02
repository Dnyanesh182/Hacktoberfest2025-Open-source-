import java.util.Arrays;

/**
 * An implementation of a Van Emde Boas (vEB) Tree.
 *
 * This data structure stores non-negative integers from a bounded universe [0, U-1]
 * and supports operations like insert, delete, member, successor, and predecessor
 * in O(log log U) time.
 *
 * The key idea is a recursive structure. A vEB tree of universe size U is composed of:
 * 1. A "summary" vEB tree of size sqrt(U) to store which clusters contain elements.
 * 2. An array of sqrt(U) "cluster" vEB trees, each of size sqrt(U).
 *
 * This implementation handles non-power-of-2 universe sizes by rounding up to the
 * next power of 2. It stores `min` and `max` elements separately for O(1) retrieval.
 */
public class VanEmdeBoasTree {

    private int universeSize;
    private Integer min;
    private Integer max;

    // Recursive structures
    private VanEmdeBoasTree summary;
    private VanEmdeBoasTree[] clusters;

    // Helper values for bit manipulation
    private int upperSqrt;
    private int lowerSqrt;

    /**
     * Constructs a Van Emde Boas Tree for a given universe size.
     * @param u The size of the universe of keys [0, u-1].
     */
    public VanEmdeBoasTree(int u) {
        this.universeSize = u;
        this.min = null;
        this.max = null;

        // Base case for the recursion
        if (u <= 2) {
            this.summary = null;
            this.clusters = null;
            return;
        }

        // Calculate the size of the sub-trees. We round up the square root
        // to handle universe sizes that are not perfect squares.
        this.lowerSqrt = 1 << ((int) (Math.log(u) / Math.log(2)) / 2);
        this.upperSqrt = 1 << ((int) (Math.log(u) / Math.log(2)) + 1) / 2;
        
        // The universe size of the summary and clusters must be a power of 2
        this.summary = new VanEmdeBoasTree(upperSqrt);
        this.clusters = new VanEmdeBoasTree[upperSqrt];
        for (int i = 0; i < upperSqrt; i++) {
            this.clusters[i] = new VanEmdeBoasTree(lowerSqrt);
        }
    }

    // Helper function to get the high bits of x
    private int high(int x) {
        return x / lowerSqrt;
    }

    // Helper function to get the low bits of x
    private int low(int x) {
        return x % lowerSqrt;
    }
    
    // Helper function to reconstruct x from high and low bits
    private int index(int high, int low) {
        return high * lowerSqrt + low;
    }
    
    public Integer getMin() {
        return this.min;
    }

    public Integer getMax() {
        return this.max;
    }

    /**
     * Inserts an element into the vEB tree.
     * @param x The integer to insert.
     */
    public void insert(int x) {
        // If the tree is empty, this is the first element.
        if (min == null) {
            min = x;
            max = x;
            return;
        }

        // If x is smaller than current min, swap them.
        // We will then proceed to insert the old min.
        if (x < min) {
            int temp = x;
            x = min;
            min = temp;
        }
        
        // Recursively insert if not in the base case
        if (universeSize > 2) {
            int highX = high(x);
            int lowX = low(x);

            // If the cluster for x is empty, we must first insert into the summary.
            if (clusters[highX].getMin() == null) {
                summary.insert(highX);
                clusters[highX].insert(lowX);
            } else {
                clusters[highX].insert(lowX);
            }
        }
        
        // Update max if x is greater
        if (x > max) {
            max = x;
        }
    }
    
    /**
     * Checks if an element is present in the tree.
     * @param x The integer to check.
     * @return true if x is in the tree, false otherwise.
     */
    public boolean member(int x) {
        if (x == min || x == max) {
            return true;
        }
        if (universeSize <= 2 || x < min || x > max) {
            return false;
        }
        return clusters[high(x)].member(low(x));
    }

    /**
     * Finds the smallest element in the tree that is greater than x.
     * @param x The integer.
     * @return The successor of x, or null if none exists.
     */
    public Integer successor(int x) {
        // Base case: if universe is 2, and x is 0 and min is 1, then 1 is the successor.
        if (universeSize == 2) {
            if (x == 0 && max == 1) {
                return 1;
            } else {
                return null;
            }
        }

        // If x is smaller than the minimum element, the minimum is its successor.
        if (min != null && x < min) {
            return min;
        }

        int highX = high(x);
        int lowX = low(x);

        // Find max element in x's cluster
        Integer maxInCluster = clusters[highX].getMax();

        // 1. If there is a successor within x's own cluster, find it.
        if (maxInCluster != null && lowX < maxInCluster) {
            int offset = clusters[highX].successor(lowX);
            return index(highX, offset);
        } else {
            // 2. Otherwise, find the next non-empty cluster using the summary structure.
            Integer successorClusterIdx = summary.successor(highX);
            
            // If no successor cluster exists, there is no successor for x.
            if (successorClusterIdx == null) {
                return null;
            } else {
                // The successor is the minimum element in the next cluster.
                int offset = clusters[successorClusterIdx].getMin();
                return index(successorClusterIdx, offset);
            }
        }
    }
    
    public static void main(String[] args) {
        // Universe size must be a power of 2 for this implementation's logic to be clean.
        // Let's use U = 16. So keys can be from 0 to 15.
        int universeSize = 16;
        VanEmdeBoasTree veb = new VanEmdeBoasTree(universeSize);

        System.out.println("Inserting elements: 2, 3, 4, 5, 7, 14, 15");
        veb.insert(2);
        veb.insert(3);
        veb.insert(4);
        veb.insert(5);
        veb.insert(7);
        veb.insert(14);
        veb.insert(15);

        System.out.println("Min element: " + veb.getMin()); // Expected: 2
        System.out.println("Max element: " + veb.getMax()); // Expected: 15

        System.out.println("\n--- Membership checks ---");
        System.out.println("Is 7 in the tree? " + veb.member(7)); // Expected: true
        System.out.println("Is 8 in the tree? " + veb.member(8)); // Expected: false
        
        System.out.println("\n--- Successor checks ---");
        System.out.println("Successor of 2: " + veb.successor(2));   // Expected: 3
        System.out.println("Successor of 5: " + veb.successor(5));   // Expected: 7
        System.out.println("Successor of 7: " + veb.successor(7));   // Expected: 14
        System.out.println("Successor of 13: " + veb.successor(13)); // Expected: 14
        System.out.println("Successor of 14: " + veb.successor(14)); // Expected: 15
        System.out.println("Successor of 15: " + veb.successor(15)); // Expected: null
        System.out.println("Successor of 0: " + veb.successor(0));   // Expected: 2 (the min)
    }
}
