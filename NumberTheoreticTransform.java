import java.util.Arrays;

/**
 * An implementation of the Number Theoretic Transform (NTT) for fast polynomial multiplication.
 *
 * This algorithm computes the convolution of two integer sequences (polynomials) in O(N log N)
 * time, which is significantly faster than the naive O(N^2) method.
 *
 * The complexity is mathematical, requiring a deep understanding of:
 * - Finite Field Arithmetic: All operations are modulo a specially chosen prime.
 * - Modular Inverse: Division is performed by multiplying with an inverse, found using
 * Fermat's Little Theorem.
 * - Primitive Roots of Unity: The algorithm replaces the complex roots of unity used in
 * a standard FFT with integer counterparts found within the finite field.
 */
public class NumberTheoreticTransform {

    // A common choice for NTT: a prime of the form k*2^m + 1
    private static final int MOD = 998244353;
    // A primitive root of MOD
    private static final int PRIMITIVE_ROOT = 3;

    /**
     * Computes modular exponentiation (base^exp % mod).
     */
    private static long power(long base, long exp) {
        long res = 1;
        base %= MOD;
        while (exp > 0) {
            if (exp % 2 == 1) res = (res * base) % MOD;
            base = (base * base) % MOD;
            exp /= 2;
        }
        return res;
    }

    /**
     * Computes the modular multiplicative inverse of n modulo MOD.
     * Uses Fermat's Little Theorem: a^(p-2) = a^-1 (mod p).
     */
    private static long modInverse(long n) {
        return power(n, MOD - 2);
    }

    /**
     * The core Number Theoretic Transform function.
     * @param a The polynomial (coefficient array) to transform.
     * @param invert True for inverse NTT, false for forward NTT.
     */
    public static void ntt(long[] a, boolean invert) {
        int n = a.length;

        // Bit-reversal permutation
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) {
                j ^= bit;
            }
            j ^= bit;
            if (i < j) {
                long temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }

        // Cooley-Tukey algorithm (iterative version)
        for (int len = 2; len <= n; len <<= 1) {
            long wlen = PRIMITIVE_ROOT;
            if (invert) {
                wlen = modInverse(wlen);
            }
            // Calculate the N-th root of unity for this level
            for (int i = len; i < (1 << 23); i <<= 1) {
                wlen = (wlen * wlen) % MOD;
            }

            for (int i = 0; i < n; i += len) {
                long w = 1;
                for (int j = 0; j < len / 2; j++) {
                    long u = a[i + j];
                    long v = (a[i + j + len / 2] * w) % MOD;
                    a[i + j] = (u + v) % MOD;
                    a[i + j + len / 2] = (u - v + MOD) % MOD;
                    w = (w * wlen) % MOD;
                }
            }
        }
        
        // If inverse, scale by 1/n
        if (invert) {
            long nInv = modInverse(n);
            for (int i = 0; i < n; i++) {
                a[i] = (a[i] * nInv) % MOD;
            }
        }
    }

    /**
     * Multiplies two polynomials A(x) and B(x) using NTT.
     * @param a Coefficients of polynomial A.
     * @param b Coefficients of polynomial B.
     * @return Coefficients of the resulting polynomial A(x) * B(x).
     */
    public static long[] multiply(long[] a, long[] b) {
        int resSize = a.length + b.length - 1;
        int n = 1;
        while (n < resSize) n <<= 1;

        long[] fa = Arrays.copyOf(a, n);
        long[] fb = Arrays.copyOf(b, n);
        
        ntt(fa, false); // Forward NTT on A
        ntt(fb, false); // Forward NTT on B
        
        // Point-wise multiplication in the frequency domain
        long[] c = new long[n];
        for (int i = 0; i < n; i++) {
            c[i] = (fa[i] * fb[i]) % MOD;
        }

        ntt(c, true); // Inverse NTT on the result

        return Arrays.copyOf(c, resSize);
    }

    public static void main(String[] args) {
        // Let A(x) = 1 + 2x + 3x^2
        long[] polyA = {1, 2, 3};
        // Let B(x) = 4 + 5x
        long[] polyB = {4, 5};

        // Expected result: A(x) * B(x) = (1 + 2x + 3x^2) * (4 + 5x)
        // = 4 + 5x + 8x + 10x^2 + 12x^2 + 15x^3
        // = 4 + 13x + 22x^2 + 15x^3
        // Coefficients: {4, 13, 22, 15}

        System.out.println("Polynomial A(x): " + Arrays.toString(polyA));
        System.out.println("Polynomial B(x): " + Arrays.toString(polyB));

        long[] result = multiply(polyA, polyB);
        
        System.out.println("Result of A(x) * B(x): " + Arrays.toString(result));
    }
}
