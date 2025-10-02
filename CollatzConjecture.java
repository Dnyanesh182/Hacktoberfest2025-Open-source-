/**
 * A Java implementation to generate the sequence for the Collatz Conjecture.
 *
 * THE UNSOLVED MYSTERY: Can you prove that the `generateSequence` method will
 * always terminate (i.e., n will always reach 1) for any positive integer
 * you provide as a starting point? No one has been able to prove this.
 */
public class CollatzConjecture {

    /**
     * Generates and prints the Collatz sequence for a given starting number.
     * @param startNumber The positive integer to start the sequence with.
     */
    public static void generateSequence(long startNumber) {
        if (startNumber <= 0) {
            System.out.println("Please provide a positive integer.");
            return;
        }

        long n = startNumber;
        int steps = 0;
        long maxVal = n;

        System.out.print("Sequence for " + startNumber + ": " + n);

        // We add a step limit to prevent a true infinite loop if the conjecture is false.
        int stepLimit = 10000; 

        while (n != 1 && steps < stepLimit) {
            if (n % 2 == 0) {
                // If n is even, divide by 2
                n = n / 2;
            } else {
                // If n is odd, multiply by 3 and add 1
                n = 3 * n + 1;
            }
            
            if(n > maxVal) {
                maxVal = n;
            }

            System.out.print(" -> " + n);
            steps++;
        }
        System.out.println(); // New line for formatting

        if (steps >= stepLimit) {
             System.out.println("Warning: Reached step limit of " + stepLimit + ". The sequence is unusually long or the conjecture might be false for this number!");
        } else {
             System.out.println("Terminated in " + steps + " steps.");
             System.out.println("The highest number reached was " + maxVal + ".");
        }
    }

    public static void main(String[] args) {
        System.out.println("Exploring the Collatz Conjecture (3n + 1 Problem)...");
        System.out.println("The conjecture states that any positive integer will eventually reach 1.");
        System.out.println("------------------------------------------------------------------");

        System.out.println("Example 1: Starting with 6 (an easy one)");
        generateSequence(6);
        // Expected: 6 -> 3 -> 10 -> 5 -> 16 -> 8 -> 4 -> 2 -> 1

        System.out.println("\n------------------------------------------------------------------");
        System.out.println("Example 2: Starting with 27 (produces a very long, chaotic sequence)");
        generateSequence(27);
    }
}
