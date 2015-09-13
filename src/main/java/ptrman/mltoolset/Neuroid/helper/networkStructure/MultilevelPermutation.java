package ptrman.mltoolset.Neuroid.helper.networkStructure;

import ptrman.mltoolset.math.TruncatedFisherYades;

import java.util.List;
import java.util.Random;

/**
 * Helper for calculating the index/adress of the permutation result over many permutations which are chained.
 * can be queried in both directions
 *
 * GPU: should fit into cache and be superfast because its just another "bitshifting" algorithm
 */
public class MultilevelPermutation {
    private static class TruncatedFisherYadesGenerator implements TruncatedFisherYades.IGenerator<Integer> {
        @Override
        public Integer generate(int index) {
            return index;
        }
    }

    public static class Permutation {
        public int forward[];
        public int backward[];

        public int size;

        public Permutation(int size) {
            this.size = size;
            this.forward = new int[size];
            this.backward = new int[size];
        }
    }

    public static Permutation createPermutation(int[] forwardIndices) {
        Permutation result = new Permutation(forwardIndices.length);
        result.forward = forwardIndices;

        for( int i = 0; i < forwardIndices.length; i++ ) {
            result.backward[forwardIndices[i]] = i;
        }

        return result;
    }

    public static Permutation createPermutationFromRng(Random random, final int size) {
        int[] forwardPermutation = new int[size];

        TruncatedFisherYades<Integer> fisherYades = new TruncatedFisherYades(size, new TruncatedFisherYadesGenerator());

        for( int i = 0; i < size; i++ ) {
            forwardPermutation[i] = fisherYades.takeOne(random);
        }
        
        return createPermutation(forwardPermutation);
    }

    public static int getMultilevelPermutationForward(final int index, final List<Permutation> permutationChain) {
        int currentAbsoluteIndex = index;

        for( final Permutation iterationPermutation : permutationChain ) {
            final int permutationRemainder = currentAbsoluteIndex % iterationPermutation.size;
            final int permutationBlockIndex = (currentAbsoluteIndex / iterationPermutation.size);
            final int permutationOffset = permutationBlockIndex * iterationPermutation.size;

            final int permutationResult = iterationPermutation.forward[permutationRemainder];
            currentAbsoluteIndex = permutationOffset + permutationResult;
        }

        return currentAbsoluteIndex;
    }

    public static int getMultilevelPermutationBackward(final int index, final List<Permutation> permutationChain) {
        int currentAbsoluteIndex = index;

        for( int permutationChainIndex = permutationChain.size()-1; permutationChainIndex >= 0; permutationChainIndex-- ) {
            final Permutation iterationPermutation = permutationChain.get(permutationChainIndex);

            final int permutationRemainder = currentAbsoluteIndex % iterationPermutation.size;
            final int permutationBlockIndex = (currentAbsoluteIndex / iterationPermutation.size);
            final int permutationOffset = permutationBlockIndex * iterationPermutation.size;

            final int permutationResult = iterationPermutation.backward[permutationRemainder];
            currentAbsoluteIndex = permutationOffset + permutationResult;
        }

        return currentAbsoluteIndex;
    }
}
