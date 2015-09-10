package ptrman.mltoolset.Neuroid.helper.networkStructure;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MultiLevelPermutationGraphTest {
    public static void main(String[] args) {
        int connectionFactor = 3;

        int n = 5*100;//15;

        int offset = 0;

        List<MultilevelPermutation.Permutation> permutationChain = new ArrayList<>();

        permutationChain.add(new MultilevelPermutation.Permutation(5));
        permutationChain.get(0).forward = new int[5];
        permutationChain.get(0).forward[0] = 2;
        permutationChain.get(0).forward[1] = 0;
        permutationChain.get(0).forward[2] = 3;
        permutationChain.get(0).forward[3] = 1;
        permutationChain.get(0).forward[4] = 4;

        permutationChain.add(new MultilevelPermutation.Permutation(5));
        permutationChain.get(1).forward = new int[9];
        permutationChain.get(1).forward[0] = 8;
        permutationChain.get(1).forward[1] = 3;
        permutationChain.get(1).forward[2] = 4;
        permutationChain.get(1).forward[3] = 7;
        permutationChain.get(1).forward[4] = 0;
        permutationChain.get(1).forward[5] = 6;
        permutationChain.get(1).forward[6] = 2;
        permutationChain.get(1).forward[7] = 5;
        permutationChain.get(1).forward[8] = 1;

        List<Connection> connections = calculateExplicitConnections(connectionFactor, n, offset, permutationChain);


        System.out.println("digraph connections {");

        for( final Connection iterationConnection : connections ) {
            System.out.format("%d -> %d; ", iterationConnection.sourceIndex, iterationConnection.destinationIndex);
            //System.out.println();
        }

        System.out.println("}");

        System.out.println("digraph concepts {");

        for( final Connection iterationConnection : connections ) {
            System.out.format("%d -> %d; ", iterationConnection.sourceIndex/5, iterationConnection.destinationIndex/5);
            //System.out.println();
        }

        System.out.println("}");

    }

    public static class Connection {
        public Connection(int sourceIndex, int destinationIndex) {
            this.sourceIndex = sourceIndex;
            this.destinationIndex = destinationIndex;
        }

        int sourceIndex;
        int destinationIndex;
    }

    private static List<Connection> calculateExplicitConnections(int connectionFactor, int n, int offset, final List<MultilevelPermutation.Permutation> permutationChain) {
        List<Connection> connections = new ArrayList<>();

        for( int i = 0; i < n * connectionFactor; i++ ) {
            final int destinationIndex = MultilevelPermutation.getMultilevelPermutationBackward(i + offset, permutationChain);
            final int sourceIndex = i/connectionFactor;

            if( /* redudant sourceIndex < n &&*/ destinationIndex < n ) {
                connections.add(new Connection(sourceIndex, destinationIndex));
            }
        }

        return connections;
    }
}
