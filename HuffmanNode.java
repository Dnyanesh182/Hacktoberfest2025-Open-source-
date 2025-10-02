import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

// A node in the Huffman Tree.
// It must be comparable to be used in the PriorityQueue.
class HuffmanNode implements Comparable<HuffmanNode> {
    char character;
    int frequency;
    HuffmanNode left;
    HuffmanNode right;

    @Override
    public int compareTo(HuffmanNode other) {
        return this.frequency - other.frequency;
    }
}

/**
 * A complete implementation of the Huffman Coding algorithm for lossless data compression.
 *
 * This class demonstrates the key steps:
 * 1. Building a frequency map of characters.
 * 2. Constructing the Huffman Tree using a greedy approach with a PriorityQueue.
 * 3. Generating prefix-free codes by traversing the tree.
 * 4. Encoding an input string to its compressed binary representation.
 * 5. Decoding the binary string back to the original text.
 */
public class HuffmanCoding {

    private HuffmanNode root;
    private final Map<Character, String> charToCodeMap = new HashMap<>();

    /**
     * Builds the Huffman tree and generates codes for a given text.
     * @param text The input text to be compressed.
     */
    public HuffmanCoding(String text) {
        // 1. Build the frequency map.
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : text.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }

        // 2. Initialize a priority queue with leaf nodes.
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            HuffmanNode node = new HuffmanNode();
            node.character = entry.getKey();
            node.frequency = entry.getValue();
            pq.add(node);
        }

        // 3. Build the Huffman Tree by merging nodes.
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();

            HuffmanNode parent = new HuffmanNode();
            parent.frequency = left.frequency + right.frequency;
            // Use a special character for internal nodes
            parent.character = '-'; 
            parent.left = left;
            parent.right = right;

            pq.add(parent);
        }

        // The root of the tree is the last item in the queue.
        this.root = pq.poll();

        // 4. Generate the codes for each character by traversing the tree.
        generateCodes(this.root, "");
    }

    /**
     * Recursively traverses the tree to generate the binary codes.
     */
    private void generateCodes(HuffmanNode node, String code) {
        if (node == null) {
            return;
        }
        // If it's a leaf node, it represents a character.
        if (node.left == null && node.right == null) {
            charToCodeMap.put(node.character, code);
            return;
        }
        generateCodes(node.left, code + "0");
        generateCodes(node.right, code + "1");
    }

    /**
     * Encodes a string using the generated Huffman codes.
     * @param text The string to encode.
     * @return The compressed binary string representation.
     */
    public String encode(String text) {
        StringBuilder encodedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            encodedText.append(charToCodeMap.get(c));
        }
        return encodedText.toString();
    }

    /**
     * Decodes a binary string using the Huffman tree.
     * @param encodedText The compressed binary string.
     * @return The original, decoded string.
     */
    public String decode(String encodedText) {
        StringBuilder decodedText = new StringBuilder();
        HuffmanNode current = root;
        for (char bit : encodedText.toCharArray()) {
            if (bit == '0') {
                current = current.left;
            } else {
                current = current.right;
            }

            // If we've reached a leaf node, we've found a character.
            if (current.left == null && current.right == null) {
                decodedText.append(current.character);
                // Return to the root to start decoding the next character.
                current = root;
            }
        }
        return decodedText.toString();
    }
    
    public void printCodes() {
        System.out.println("Huffman Codes:");
        for (Map.Entry<Character, String> entry : charToCodeMap.entrySet()) {
            System.out.println("'" + entry.getKey() + "': " + entry.getValue());
        }
    }

    public static void main(String[] args) {
        String text = "huffman coding is a fun algorithm";
        System.out.println("Original Text: \"" + text + "\"");
        System.out.println("----------------------------------------");

        // Create the HuffmanCoding object, which builds the tree and codes.
        HuffmanCoding huffman = new HuffmanCoding(text);

        // Print the generated codes.
        huffman.printCodes();
        System.out.println("----------------------------------------");
        
        // Encode the text.
        String encoded = huffman.encode(text);
        System.out.println("Encoded (Binary String): " + encoded);

        // Decode the text.
        String decoded = huffman.decode(encoded);
        System.out.println("Decoded Text: \"" + decoded + "\"");
        System.out.println("----------------------------------------");
        
        // Show compression statistics.
        int originalBits = text.length() * 8; // Assuming 8 bits/char for ASCII
        int compressedBits = encoded.length();
        System.out.println("Original size (bits): " + originalBits);
        System.out.println("Compressed size (bits): " + compressedBits);
        double ratio = (double) compressedBits / originalBits * 100;
        System.out.printf("Compression Ratio: %.2f%%\n", ratio);
    }
}
