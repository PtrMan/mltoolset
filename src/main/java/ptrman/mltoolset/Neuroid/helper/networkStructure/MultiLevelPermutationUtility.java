package ptrman.mltoolset.Neuroid.helper.networkStructure;

import ptrman.mltoolset.math.Primes;

public class MultiLevelPermutationUtility {
    public static int[] getFirstPrimesOfWhichTheProductIsEqualOrAbove(final int number, final int numberOfPrimes) {
        for( int primeArrayIndex = 0; primeArrayIndex <= Primes.primes.length-numberOfPrimes; primeArrayIndex++ ) {
            final int[] primes = getPrimes(primeArrayIndex, numberOfPrimes);
            final int product = multiply(primes);
            if( product >= number ) {
                return primes;
            }
        }

        return new int[0];
    }

    private static int[] getPrimes(final int primeArrayIndex, final int numberOfPrimes) {
        int[] primes = new int[numberOfPrimes];

        for( int primeIndex = 0; primeIndex < numberOfPrimes; primeIndex++ ) {
            primes[primeIndex] = Primes.primes[primeArrayIndex + primeIndex];
        }

        return primes;
    }

    private static int multiply(final int[] numbers) {
        int result = 1;

        for( int i = 0; i < numbers.length; i++ ) {
            result *= numbers[i];
        }

        return result;
    }
}
