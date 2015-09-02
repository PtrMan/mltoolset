package ptrman.mltoolset.Neuroid.algorithms;

import org.junit.Assert;
import org.junit.Test;
import ptrman.mltoolset.Neuroid.FloatWeightHelper;
import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.Neuroid.helper.EnumStandardNeuroidState;
import ptrman.mltoolset.Neuroid.vincal.DistribuatorHelper;
import ptrman.mltoolset.Neuroid.vincal.INeuroidAllocator;
import ptrman.mltoolset.math.DistinctUtility;

import java.util.*;

/**
 *
 */
public class TestNeuroidPredictiveJoinLearningAlgorithm {
    private static class UnittestExposedNeuroidPredictiveJoinLearningAlgorithm extends NeuroidPredictiveJoinLearningAlgorithm {
        public UnittestExposedNeuroidPredictiveJoinLearningAlgorithm(INeuroidAllocator<Float, Integer> neuroidAllocator, int r, int joinK, int linkK, float T, Neuroid<Float, Integer> network) {
            super(neuroidAllocator, r, joinK, linkK, T, network);
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

    private static class JoinContext {
        Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> a;
        Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b;

        Neuroid<Float, Integer> network;
    }


    private static class LinkContext {
        Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> a;
        Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b;

        Neuroid<Float, Integer> network;
    }




    @Test
    public void testJoinEnhancedCState() {
        final int numberOfNeuronsInGroup = 5;

        final int r = numberOfNeuronsInGroup; // how many neuroids should be (roughtly?) allocated
        final int joinK = 2; // how many synapses should at least go from "a" and "b" to "z"?
        final int linkK = 1;
        final float T = 1.0f; // standard threshold

        JoinContext joinContext = joinCreateNetworkAndDoJoinOperation(numberOfNeuronsInGroup, r, joinK, linkK, T);


        // check for at least one neuron in "z" which is in the Operational state
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> z = getNeuroidsSet(joinContext.network, 0, numberOfNeuronsInGroup, 2);

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

    @Test
    public void testJoinEnhancedCorrectActivationAActiveBActive() {
        final int numberOfNeuronsInGroup = 5;

        final int r = numberOfNeuronsInGroup; // how many neuroids should be (roughtly?) allocated
        final int joinK = 2; // how many synapses should at least go from "a" and "b" to "z"?
        final int linkK = 1;
        final float T = 1.0f; // standard threshold

        JoinContext joinContext = joinCreateNetworkAndDoJoinOperation(numberOfNeuronsInGroup, r, joinK, linkK, T);

        // neurons in z should be in Operational state, is not checked

        // let all inputs fire and look if the result fires
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : joinContext.a ) {
            iterationANeuroid.graphElement.nextFiring = true;
        }
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : joinContext.b ) {
            iterationANeuroid.graphElement.nextFiring = true;
        }

        joinContext.network.timestep();

        // at least one neuron should fire

        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> z = getNeuroidsSet(joinContext.network, 0, numberOfNeuronsInGroup, 2);

        boolean atLeastOneNeuroidInZFiring = false;

        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationNeuroid : z ) {
            if( iterationNeuroid.graphElement.nextFiring ) {
                atLeastOneNeuroidInZFiring = true;
                break;
            }
        }

        Assert.assertTrue(atLeastOneNeuroidInZFiring);
    }

    @Test
    public void testJoinEnhancedCorrectActivationAActiveBInactive() {
        final int numberOfNeuronsInGroup = 5;

        final int r = numberOfNeuronsInGroup; // how many neuroids should be (roughtly?) allocated
        final int joinK = 2; // how many synapses should at least go from each of "a", "b" to "z"?
        final int linkK = 1;
        final float T = 1.0f; // standard threshold

        JoinContext joinContext = joinCreateNetworkAndDoJoinOperation(numberOfNeuronsInGroup, r, joinK, linkK, T);

        // neurons in z should be in Operational state, is not checked

        // let all inputs fire and look if the result fires
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : joinContext.a ) {
            iterationANeuroid.graphElement.nextFiring = true;
        }
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : joinContext.b ) {
            iterationANeuroid.graphElement.nextFiring = false;
        }

        System.out.print("---");
        System.out.println();

        joinContext.network.timestep();

        // all neurons in z shouldn't fire

        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> z = getNeuroidsSet(joinContext.network, 0, numberOfNeuronsInGroup, 2);

        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationNeuroid : z ) {
            Assert.assertFalse(iterationNeuroid.graphElement.nextFiring);
        }
    }

    @Test
    public void testJoinEnhancedCorrectActivationAInactiveBActive() {
        final int numberOfNeuronsInGroup = 5;

        final int r = numberOfNeuronsInGroup; // how many neuroids should be (roughtly?) allocated
        final int joinK = 2; // how many synapses should at least go from each of "a", "b" to "z"?
        final int linkK = 1;
        final float T = 1.0f; // standard threshold

        JoinContext joinContext = joinCreateNetworkAndDoJoinOperation(numberOfNeuronsInGroup, r, joinK, linkK, T);

        // neurons in z should be in Operational state, is not checked

        // let all inputs fire and look if the result fires
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : joinContext.a ) {
            iterationANeuroid.graphElement.nextFiring = false;
        }
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : joinContext.b ) {
            iterationANeuroid.graphElement.nextFiring = true;
        }

        System.out.print("---");
        System.out.println();

        joinContext.network.timestep();

        // all neurons in z shouldn't fire

        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> z = getNeuroidsSet(joinContext.network, 0, numberOfNeuronsInGroup, 2);

        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationNeuroid : z ) {
            Assert.assertFalse(iterationNeuroid.graphElement.nextFiring);
        }
    }

    @Test
    public void testJoinEnhancedCorrectActivationAInactiveBInactive() {
        final int numberOfNeuronsInGroup = 5;

        final int r = numberOfNeuronsInGroup; // how many neuroids should be (roughtly?) allocated
        final int joinK = 2; // how many synapses should at least go from each of "a", "b" to "z"?
        final int linkK = 1;
        final float T = 1.0f; // standard threshold

        JoinContext joinContext = joinCreateNetworkAndDoJoinOperation(numberOfNeuronsInGroup, r, joinK, linkK, T);

        // neurons in z should be in Operational state, is not checked

        // let all inputs fire and look if the result fires
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : joinContext.a ) {
            iterationANeuroid.graphElement.nextFiring = false;
        }
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : joinContext.b ) {
            iterationANeuroid.graphElement.nextFiring = false;
        }

        System.out.print("---");
        System.out.println();

        joinContext.network.timestep();

        // all neurons in z shouldn't fire

        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> z = getNeuroidsSet(joinContext.network, 0, numberOfNeuronsInGroup, 2);

        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationNeuroid : z ) {
            Assert.assertFalse(iterationNeuroid.graphElement.nextFiring);
        }
    }

    @Test
    public void testLinkSimple() {
        final int numberOfNeuronsInGroup = 5;
        final int numberOfRelayNeuroids = 5;
        final int r = 1; // ignored
        final int joinK = 2; // ignored
        final int linkK = 1; // required
        final float T = 1.0f;

        LinkContext linkContext = linkCreateNetworkAndDoLinkOperation(numberOfNeuronsInGroup, numberOfRelayNeuroids, r, joinK, linkK, T);

        // let all input fire and look if the result fires
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : linkContext.a ) {
            iterationANeuroid.graphElement.nextFiring = true;
        }

        linkContext.network.timestep();
        linkContext.network.timestep();

        // at least one neuron should fire

        // TODO< adressing is wrong, but works fine if the number of numberOfNeuronsInGroup is equal to the number of relay neurons >
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b = getNeuroidsSet(linkContext.network, 0, numberOfNeuronsInGroup, 2);

        boolean atLeastOneNeuroidInBFiring = false;

        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationNeuroid : b ) {
            if( iterationNeuroid.graphElement.nextFiring ) {
                atLeastOneNeuroidInBFiring = true;
                break;
            }
        }

        Assert.assertTrue(atLeastOneNeuroidInBFiring);

    }

    private static JoinContext joinCreateNetworkAndDoJoinOperation(int numberOfNeuronsInGroup, int r, int joinK, int linkK, float T) {
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

        UnittestExposedNeuroidPredictiveJoinLearningAlgorithm predictiveJoinLearningAlgorithm = new UnittestExposedNeuroidPredictiveJoinLearningAlgorithm(neuroidAllocator, r, joinK, linkK, T, network);


        // generate edges
        final List<Neuroid.Helper.EdgeWeightTuple<Float>> networkEdges = generateEdgesForJoinTestNetwork(random, numberOfNeuronsInGroup, joinK, T);
        network.addEdgeWeightTuples(networkEdges);

        network.initialize();

        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> a = getNeuroidsSet(network, 0, numberOfNeuronsInGroup, 0);
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b = getNeuroidsSet(network, 0, numberOfNeuronsInGroup, 1);

        // do actual test
        predictiveJoinLearningAlgorithm.joinEnhanced(a, b);

        JoinContext resultJoinContext = new JoinContext();
        resultJoinContext.a = a;
        resultJoinContext.b = b;
        resultJoinContext.network = network;

        return resultJoinContext;
    }

    private static LinkContext linkCreateNetworkAndDoLinkOperation(int numberOfNeuronsInGroup, int numberOfRelayNeuroids, int r, int joinK, int linkK, float T) {
        Random random = new Random(4223);

        Neuroid<Float, Integer> network = new Neuroid<>(new FloatWeightHelper());
        network.update = new NeuroidPredictiveJoinLearningAlgorithm.DummyUpdate();

        // first 5 neurons are "a", next are "relay", next are "b"
        network.allocateNeurons(numberOfNeuronsInGroup + numberOfRelayNeuroids + numberOfNeuronsInGroup, 0, 0);

        for( int neuronI = 0; neuronI < numberOfNeuronsInGroup; neuronI++ ) {
            network.getGraph().neuronNodes[neuronI].graphElement.threshold = T;
            // just a standard state
            network.getGraph().neuronNodes[neuronI].graphElement.state = EnumStandardNeuroidState.AvailableMemory.ordinal();
        }

        for( int neuronI = numberOfNeuronsInGroup; neuronI < numberOfNeuronsInGroup + numberOfRelayNeuroids; neuronI++ ) {
            network.getGraph().neuronNodes[neuronI].graphElement.threshold = T;
            network.getGraph().neuronNodes[neuronI].graphElement.state = EnumStandardNeuroidState.Relay.ordinal();
        }

        for( int neuronI = numberOfNeuronsInGroup + numberOfRelayNeuroids; neuronI < numberOfNeuronsInGroup + numberOfRelayNeuroids + numberOfNeuronsInGroup; neuronI++ ) {
            network.getGraph().neuronNodes[neuronI].graphElement.threshold = T;
            // just a standard state
            network.getGraph().neuronNodes[neuronI].graphElement.state = EnumStandardNeuroidState.AvailableMemory.ordinal();
        }

        // required for link functionality!
        network.resizeFiringHistory(1);


        // we have to do this after allocating the neuroids
        UnittestNeuroidAllocator neuroidAllocator = new UnittestNeuroidAllocator(network);

        UnittestExposedNeuroidPredictiveJoinLearningAlgorithm predictiveJoinLearningAlgorithm = new UnittestExposedNeuroidPredictiveJoinLearningAlgorithm(neuroidAllocator, r, joinK, linkK, T, network);


        Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> globalRelayNeuroids = new HashSet<>();

        // fill relay neuroid set
        for( int neuronI = numberOfNeuronsInGroup; neuronI < numberOfNeuronsInGroup + numberOfRelayNeuroids; neuronI++ ) {
            globalRelayNeuroids.add(network.getGraph().neuronNodes[neuronI]);
        }

        predictiveJoinLearningAlgorithm.setGlobalRelayNeuroids(globalRelayNeuroids);



        // generate edges
        final List<Neuroid.Helper.EdgeWeightTuple<Float>> networkEdges = generateEdgesForLinkTestNetwork(random, network, numberOfNeuronsInGroup, numberOfRelayNeuroids, T);
        network.addEdgeWeightTuples(networkEdges);

        network.initialize();

        // TODO< adressing is wrong, but works fine if the number of numberOfNeuronsInGroup is equal to the number of relay neurons >
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> a = getNeuroidsSet(network, 0, numberOfNeuronsInGroup, 0);
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b = getNeuroidsSet(network, 0, numberOfNeuronsInGroup, 2);

        // do actual test
        predictiveJoinLearningAlgorithm.linkEnhanced(a, b);

        LinkContext resultLinkContext = new LinkContext();
        resultLinkContext.a = a;
        resultLinkContext.b = b;
        resultLinkContext.network = network;

        return resultLinkContext;

    }

    private static List<Neuroid.Helper.EdgeWeightTuple<Float>> generateEdgesForLinkTestNetwork(Random random, Neuroid<Float, Integer> network, int numberOfNeuronsInGroup, int numberOfRelayNeuroids, float T) {
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> a = getNeuroidsSet(network, 0, numberOfNeuronsInGroup, 0);
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> relay = getNeuroidsSet(network, numberOfNeuronsInGroup, numberOfRelayNeuroids, 0);
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b = getNeuroidsSet(network, numberOfNeuronsInGroup + numberOfRelayNeuroids, numberOfNeuronsInGroup, 0);

        final Set<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> aNeuronAdresses = translateNeuronsToNeuronAddresses(a, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN);
        final Set<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> relayNeuronAdresses = translateNeuronsToNeuronAddresses(relay, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN);
        final Set<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> bNeuronAdresses = translateNeuronsToNeuronAddresses(b, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN);

        List<Neuroid.Helper.EdgeWeightTuple<Float>> edges = new ArrayList<>();

        edges.addAll(DistribuatorHelper.forEachInputChooseRandomOutput(aNeuronAdresses, relayNeuronAdresses, T, random));
        edges.addAll(DistribuatorHelper.forEachInputChooseRandomOutput(relayNeuronAdresses, bNeuronAdresses, T, random));

        return edges;
    }


    private static<Weighttype, ModeType> Set<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> translateNeuronsToNeuronAddresses(final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> neurons, final Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType neuronType ) {
        Set<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> result = new HashSet<>();

        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationNeuron : neurons ) {
            // dirty because it only works for hidden neurons
            // this is no problem for PJOIN because the neurons which do the work are all hidden
            result.add(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(iterationNeuron.index, neuronType));
        }

        return result;
    }

    private static List<Neuroid.Helper.EdgeWeightTuple<Float>> generateEdgesForJoinTestNetwork(Random random, int numberOfNeuronsInGroup, int k, float T) {
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

            int remainingSynapses = k * 2;

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
            }*/


            // make connections

            for( final int iterationNeuronInA : neuronsInA ) {
                final int sourceIndex = iterationNeuronInA + 0*numberOfNeuronsInGroup;
                final float threshold = T; // must be T because else the learning for the enhanced join mechanism doesn't work at all
                result.add(new Neuroid.Helper.EdgeWeightTuple<>(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(sourceIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN), new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(destinationNeuronIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN), threshold));
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
