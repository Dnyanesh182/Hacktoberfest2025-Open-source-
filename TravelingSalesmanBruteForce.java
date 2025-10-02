import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A demonstration of the Traveling Salesman Problem (TSP) using a brute-force approach.
 *
 * THE UNSOLVED PART: There is no known algorithm that can solve this problem
 * efficiently (in polynomial time) for all possible inputs. An efficient solution
 * would prove P=NP and revolutionize computing, logistics, and science.
 */
public class TravelingSalesmanBruteForce {

    static int V = 4; // Number of vertices (cities)
    static int bestPathCost = Integer.MAX_VALUE;
    static List<Integer> bestPath = new ArrayList<>();

    /**
     * Solves the TSP using a brute-force recursive approach.
     * It explores every possible permutation of cities.
     *
     * @param graph The adjacency matrix representing distances between cities.
     * @param currentPath The path taken so far.
     * @param visited A boolean array to mark visited cities.
     * @param currentCost The cost of the path taken so far.
     */
    public static void solveTSP(int[][] graph, List<Integer> currentPath, boolean[] visited, int currentCost) {
        // If all cities have been visited
        if (currentPath.size() == V) {
            // Add the cost to return to the starting city
            int totalCost = currentCost + graph[currentPath.get(currentPath.size() - 1)][currentPath.get(0)];

            // If this path is the best one found so far, save it
            if (totalCost < bestPathCost) {
                bestPathCost = totalCost;
                bestPath = new ArrayList<>(currentPath);
                // Add the starting city to complete the loop for printing
                bestPath.add(currentPath.get(0));
            }
            return;
        }

        // Explore the next city to visit
        for (int i = 0; i < V; i++) {
            if (!visited[i]) {
                int lastCity = currentPath.get(currentPath.size() - 1);

                // Mark the city as visited
                visited[i] = true;
                currentPath.add(i);

                // Recurse for the next level
                solveTSP(graph, currentPath, visited, currentCost + graph[lastCity][i]);

                // Backtrack: Un-mark the city and remove it from the path
                visited[i] = false;
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }

    public static void main(String[] args) {
        // Adjacency matrix for the distances between 4 cities.
        // graph[i][j] = distance from city i to city j
        int[][] graph = {
            {0, 10, 15, 20},
            {10, 0, 35, 25},
            {15, 35, 0, 30},
            {20, 25, 30, 0}
        };

        // Initial setup to start the search from city 0
        boolean[] visited = new boolean[V];
        List<Integer> currentPath = new ArrayList<>();
        
        visited[0] = true;
        currentPath.add(0);

        System.out.println("Solving TSP for " + V + " cities...");
        System.out.println("This has a time complexity of O(N!), which is infeasible for large N.");

        solveTSP(graph, currentPath, visited, 0);

        System.out.println("\nThe shortest possible route has a cost of: " + bestPathCost);
        System.out.print("The path is: ");
        for (int i = 0; i < bestPath.size(); i++) {
            System.out.print(bestPath.get(i) + (i == bestPath.size() - 1 ? "" : " -> "));
        }
        System.out.println();
    }
}
