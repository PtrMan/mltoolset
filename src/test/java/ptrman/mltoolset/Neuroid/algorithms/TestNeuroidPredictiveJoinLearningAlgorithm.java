package ptrman.mltoolset.Neuroid.algorithms;

import org.junit.Assert;
import org.junit.Test;
import ptrman.mltoolset.Neuroid.FloatWeightHelper;
import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.Neuroid.helper.EnumStandardNeuroidState;
import ptrman.mltoolset.Neuroid.vincal.INeuroidAllocator;
import ptrman.mltoolset.math.DistinctUtility;

import java.util.*;

/**
 *
 */
public class TestNeuroidPredictiveJoinLearningAlgorithm {
    private static class UnittestExposedNeuroidPredictiveJoinLearningAlgorithm extends NeuroidPredictiveJoinLearningAlgorithm {
        public UnittestExposedNeuroidPredictiveJoinLearningAlgorithm(INeuroidAllocator<Float, Integer> neuroidAllocator, int r, int k, float T, Neuroid<Float, Integer> network) {
            super(neuroidAllocator, r, k, T, network);
        }


        public final void joinEnhanced(final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> a, final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b) {
            super.joinEnhanced(a, b);
        }
    }

    private static class UnittestNeuroidAllocator implements INeuroidAllocator {
        public UnittestNeuroidAllocator(Neuroid<Float, Integer> network) {
            this.network = network;
        }

        @Override
        public Set<Neuroid.NeuroidGraph.NeuronNode> allocateNeuroidsWithConstraintsConnectedToBothInputsAndWithAtLeastNSynapsesToBoth(Set a, Set b, int numberOfResultNeuroids, int k) {
            Set<Neuroid.NeuroidGraph.NeuronNode> resultNeuroidSet = new HashSet<>();

            // we return the last 5 neurons for "z"

            for( int neuroidI = 5*2; neuroidI < 5*3; neuroidI++ ) {
                resultNeuroidSet.add(network.getGraph().neuronNodes[neuroidI]);
            }

            return resultNeuroidSet;
        }

        private final Neuroid<Float, Integer> network;

    }

    @Test
    public void testJoinEnhanced() {
        final int numberOfNeuronsInGroup = 5;

        final int r = numberOfNeuronsInGroup; // how many neuroids should be (roughtly?) allocated
        final int k = 2; // how many synapses should at least go from "a" and "b" to "z"?
        final float T = 1.0f; // standard threshold

        Random random = new Random(4223);

        Neuroid<Float, Integer> network = new Neuroid<>(new FloatWeightHelper());
        network.update = new NeuroidPredictiveJoinLearningAlgorithm.DummyUpdate();

        // first 5 neurons are "a", next are "b", next are "z"
        network.allocateNeurons(numberOfNeuronsInGroup + numberOfNeuronsInGroup + numberOfNeuronsInGroup, 0, 0);

        for( int neuronI = 0; neuronI < numberOfNeuronsInGroup*3; neuronI++ ) {
            network.getGraph().neuronNodes[neuronI].graphElement.threshold = T;
            // just a standard state
            network.getGraph().neuronNodes[neuronI].graphElement.state = EnumStandardNeuroidState.AvailableMemory.ordinal();
        }

        // we have to do this after allocating the neuroids
        UnittestNeuroidAllocator neuroidAllocator = new UnittestNeuroidAllocator(network);

        UnittestExposedNeuroidPredictiveJoinLearningAlgorithm predictiveJoinLearningAlgorithm = new UnittestExposedNeuroidPredictiveJoinLearningAlgorithm(neuroidAllocator, r, k, T, network);


        // generate edges
        final List<Neuroid.Helper.EdgeWeightTuple<Float>> networkEdges = generateEdgesForTestNetwork(random, numberOfNeuronsInGroup, k, T);
        network.addEdgeWeightTuples(networkEdges);

        network.initialize();

        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> a = getNeuroidsSet(network, 0, numberOfNeuronsInGroup, 0);
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b = getNeuroidsSet(network, 0, numberOfNeuronsInGroup, 1);

        // do actual test
        predictiveJoinLearningAlgorithm.joinEnhanced(a, b);

        // check for at least one neuron in "z" which is in the Operational state
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> z = getNeuroidsSet(network, 0, numberOfNeuronsInGroup, 2);

        boolean atLeastOneResultNeuroidInOperationalState = false;

        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationNeuroid : z ) {
            System.out.println(iterationNeuroid.graphElement.state);

            if( iterationNeuroid.graphElement.state == EnumStandardNeuroidState.JoinEnhancedOperational.ordinal() ) {
                atLeastOneResultNeuroidInOperationalState = true;
                break;
            }
        }

        Assert.assertTrue(atLeastOneResultNeuroidInOperationalState);
    }

    private static List<Neuroid.Helper.EdgeWeightTuple<Float>> generateEdgesForTestNetwork(Random random, int numberOfNeuronsInGroup, int k, float T) {
        List<Neuroid.Helper.EdgeWeightTuple<Float>> result = new ArrayList<>();

        // for each result neuron do this
        //  * search k neurons in A and B
        //  connect them

        for( int destinationNeuron = 0; destinationNeuron < numberOfNeuronsInGroup; destinationNeuron++ ) {
            final int destinationNeuronIndex = 2*numberOfNeuronsInGroup + destinationNeuron;

            List<Integer> neuronsInA = new ArrayList<>();
            List<Integer> neuronsInB = new ArrayList<>();

            neuronsInA = DistinctUtility.getNDisjunctIntegers(random, k, numberOfNeuronsInGroup);
            neuronsInB = DistinctUtility.getNDisjunctIntegers(random, k, numberOfNeuronsInGroup);

            /*
            int remainingSynapses = k;

            for(;;) {
                // TODO< equal randomisation of chosen group (if "a" or "b") >

                neuronsInA.addAll(DistinctUtility.getDisjuctNumbersTo(random, neuronsInA, 1, numberOfNeuronsInGroup));
                remainingSynapses--;
                if( remainingSynapses <= 0 ) {
                    break;
                }

                neuronsInB.addAll(DistinctUtility.getDisjuctNumbersTo(random, neuronsInB, 1, numberOfNeuronsInGroup));
                remainingSynapses--;
                if( remainingSynapses <= 0 ) {
                    break;
                }
            }
            */

            // make connections

            for( final int iterationNeuronInA : neuronsInA ) {
                final int sourceIndex = iterationNeuronInA + 0*numberOfNeuronsInGroup;
                result.add(new Neuroid.Helper.EdgeWeightTuple<>(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(sourceIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN), new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(destinationNeuronIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN), T/k));
            }

            for( final int iterationNeuronInB : neuronsInB ) {
                final int sourceIndex = iterationNeuronInB + 1*numberOfNeuronsInGroup;
                result.add(new Neuroid.Helper.EdgeWeightTuple<>(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(sourceIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN), new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(destinationNeuronIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN), T/k));
            }
        }

        return result;
    }

    private static Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> getNeuroidsSet(Neuroid<Float, Integer> network, int offset, int width, int setIndex) {
        Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> resultSet = new HashSet<>();

        for( int i = offset + width*setIndex; i < offset + width*(setIndex+1); i++ ) {
            resultSet.add(network.getGraph().neuronNodes[i]);
        }

        return resultSet;
    }
}
