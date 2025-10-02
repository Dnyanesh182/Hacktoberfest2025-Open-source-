import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * A simplified implementation of the RSA public-key encryption algorithm.
 *
 * The security of RSA relies on the computational difficulty of the integer
 * factorization problem. While multiplying two large primes is easy, factoring
 * their product is considered computationally infeasible. This code demonstrates
 * the key generation, encryption, and decryption steps.
 */
public class RSA {

    private BigInteger privateKey;
    private BigInteger publicKey;
    private BigInteger modulus;

    // Bit length for the prime numbers. In practice, this should be 2048 or higher.
    private static final int BIT_LENGTH = 512;

    /**
     * Generates the public and private keys.
     */
    public void generateKeys() {
        SecureRandom rand = new SecureRandom();

        // 1. Choose two large distinct prime numbers, p and q.
        BigInteger p = BigInteger.probablePrime(BIT_LENGTH, rand);
        BigInteger q = BigInteger.probablePrime(BIT_LENGTH, rand);

        // 2. Calculate n = p * q. This is the modulus for both keys.
        modulus = p.multiply(q);

        // 3. Calculate phi(n) = (p-1) * (q-1).
        // This is Euler's totient function.
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // 4. Choose an integer e such that 1 < e < phi(n) and gcd(e, phi(n)) = 1.
        // e is the public key exponent. 65537 is a common choice.
        publicKey = new BigInteger("65537");

        // 5. Compute d, the private key exponent.
        // d is the modular multiplicative inverse of e mod phi(n).
        // d * e ≡ 1 (mod phi(n))
        privateKey = publicKey.modInverse(phi);
    }

    /**
     * Encrypts a message using the public key.
     * @param message The message to be encrypted (as a BigInteger).
     * @return The encrypted ciphertext (as a BigInteger).
     */
    public BigInteger encrypt(BigInteger message) {
        // Ciphertext C = M^e mod n
        return message.modPow(publicKey, modulus);
    }

    /**
     * Decrypts a ciphertext using the private key.
     * @param ciphertext The encrypted message to be decrypted.
     * @return The original message.
     */
    public BigInteger decrypt(BigInteger ciphertext) {
        // Message M = C^d mod n
        return ciphertext.modPow(privateKey, modulus);
    }

    public static void main(String[] args) {
        RSA rsa = new RSA();
        rsa.generateKeys();

        System.out.println("Generating RSA keys of bit length: " + BIT_LENGTH);
        System.out.println("--------------------------------------------------");

        // Create a message to encrypt.
        // Note: The message must be smaller than the modulus 'n'.
        String originalMessageText = "This is a secret message protected by RSA.";
        BigInteger originalMessage = new BigInteger(originalMessageText.getBytes());
        
        System.out.println("Original Message (as BigInteger): " + originalMessage);

        // Encrypt the message
        BigInteger encryptedMessage = rsa.encrypt(originalMessage);
        System.out.println("Encrypted Ciphertext: " + encryptedMessage);

        // Decrypt the message
        BigInteger decryptedMessage = rsa.decrypt(encryptedMessage);
        System.out.println("Decrypted Message (as BigInteger): " + decryptedMessage);
        
        // Convert the decrypted BigInteger back to text to verify
        String restoredMessageText = new String(decryptedMessage.toByteArray());
        System.out.println("\nRestored Message Text: \"" + restoredMessageText + "\"");

        if (originalMessage.equals(decryptedMessage)) {
            System.out.println("\nSuccess: The original message was restored correctly!");
        } else {
            System.out.println("\nFailure: The message could not be restored.");
        }
    }
}
