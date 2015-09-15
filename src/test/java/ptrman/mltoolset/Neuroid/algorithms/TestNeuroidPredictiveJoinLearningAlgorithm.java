package ptrman.mltoolset.Neuroid.algorithms;

import org.junit.Assert;
import org.junit.Test;
import ptrman.mltoolset.Neuroid.*;
import ptrman.mltoolset.Neuroid.algorithms.NeuroidPredictiveJoinLearningAlgorithm.MetaType;
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
        public UnittestExposedNeuroidPredictiveJoinLearningAlgorithm(INeuroidAllocator<Float, MetaType> neuroidAllocator, int r, int joinK, int linkK, float T, Network<Float, MetaType> network) {
            super(neuroidAllocator, r, joinK, linkK, T, network);
        }


        public final Set<NeuronAdress> joinEnhanced(final Set<NeuronAdress> a, final Set<NeuronAdress> b) {
            return super.joinEnhanced(a, b);
        }
    }

    private static class UnittestNeuroidAllocator implements INeuroidAllocator {
        public UnittestNeuroidAllocator(Network<Float, MetaType> network) {
            this.network = network;
        }

        @Override
        public Set<NeuronAdress> allocateNeuroidsWithConstraintsConnectedToBothInputsAndWithAtLeastNSynapsesToBoth(Set a, Set b, int numberOfResultNeuroids, int k) {
            Set<NeuronAdress> resultNeuroidSet = new HashSet<>();

            // we return the last 5 neurons for "z"

            for( int neuroidI = 5*2; neuroidI < 5*3; neuroidI++ ) {
                resultNeuroidSet.add(new NeuronAdress(neuroidI, NeuronAdress.EnumType.HIDDEN));
            }

            return resultNeuroidSet;
        }

        private final Network<Float, MetaType> network;

    }

    /*
    private static class SparseIncrementalTryNeuroidAllocator implements INeuroidAllocator<Float, MetaType> {

    }
    */


    private static class JoinContext {
        Set<NeuronAdress> a;
        Set<NeuronAdress> b;

        Network<Float, MetaType> network;
    }


    private static class LinkContext {
        Set<NeuronAdress> a;
        Set<NeuronAdress> b;

        Network<Float, MetaType> network;
    }




    @Test
    public void testJoinEnhancedCState() {
        final int numberOfNeuronsInGroup = 5;

        final int r = numberOfNeuronsInGroup; // how many neuroids should be (roughtly?) allocated
        final int joinK = 2; // how many synapses should at least go from "a" and "b" to "z"?
        final int linkK = 1;
        final float T = 1.0f; // standard threshold

        JoinContext joinContext = joinCreateNetworkAndDoJoinOperation(numberOfNeuronsInGroup, r, joinK, linkK, T);

        INetworkAccessor<Float, MetaType> networkAccessor = joinContext.network.getNetworkAccessor();

        // check for at least one neuron in "z" which is in the Operational state
        final Set<NeuronAdress> z = getNeuroidsSet(0, numberOfNeuronsInGroup, 2);

        boolean atLeastOneResultNeuroidInOperationalState = false;

        for( final NeuronAdress iterationNeuroidAdress : z ) {
            INeuronAccessor<Float, MetaType> neuronAccessor = networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress);

            System.out.println(neuronAccessor.getState());

            if( neuronAccessor.getState() == EnumStandardNeuroidState.JoinEnhancedOperational.ordinal() ) {
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

        INetworkAccessor<Float, MetaType> networkAccessor = joinContext.network.getNetworkAccessor();

        // neurons in z should be in Operational state, is not checked

        // let all inputs fire and look if the result fires
        for( final NeuronAdress iterationANeuroidAdress : joinContext.a ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroidAdress).setNextFiring(true);
        }
        for( final NeuronAdress iterationANeuroidAdress : joinContext.b ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroidAdress).setNextFiring(true);
        }

        joinContext.network.timestep();

        // at least one neuron should fire

        final Set<NeuronAdress> z = getNeuroidsSet(0, numberOfNeuronsInGroup, 2);

        boolean atLeastOneNeuroidInZFiring = false;

        for( final NeuronAdress iterationNeuroidAdress : z ) {
            if( networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress).getNextFiring() ) {
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

        INetworkAccessor<Float, MetaType> networkAccessor = joinContext.network.getNetworkAccessor();

        // neurons in z should be in Operational state, is not checked

        for( final NeuronAdress iterationANeuroidAdress : joinContext.a ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroidAdress).setNextFiring(true);
        }
        for( final NeuronAdress iterationANeuroidAdress : joinContext.b ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroidAdress).setNextFiring(false);
        }

        System.out.print("---");
        System.out.println();

        joinContext.network.timestep();

        // all neurons in z shouldn't fire

        final Set<NeuronAdress> z = getNeuroidsSet(0, numberOfNeuronsInGroup, 2);

        for( final NeuronAdress iterationNeuroidAdress : z ) {
            Assert.assertFalse(networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress).getNextFiring());
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

        INetworkAccessor<Float, MetaType> networkAccessor = joinContext.network.getNetworkAccessor();

        // neurons in z should be in Operational state, is not checked

        for( final NeuronAdress iterationANeuroidAdress : joinContext.a ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroidAdress).setNextFiring(false);
        }
        for( final NeuronAdress iterationANeuroidAdress : joinContext.b ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroidAdress).setNextFiring(true);
        }

        System.out.print("---");
        System.out.println();

        joinContext.network.timestep();

        // all neurons in z shouldn't fire

        final Set<NeuronAdress> z = getNeuroidsSet(0, numberOfNeuronsInGroup, 2);

        for( final NeuronAdress iterationNeuroidAdress : z ) {
            Assert.assertFalse(networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress).getNextFiring());
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

        INetworkAccessor<Float, MetaType> networkAccessor = joinContext.network.getNetworkAccessor();

        // neurons in z should be in Operational state, is not checked

        for( final NeuronAdress iterationANeuroidAdress : joinContext.a ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroidAdress).setNextFiring(false);
        }
        for( final NeuronAdress iterationANeuroidAdress : joinContext.b ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroidAdress).setNextFiring(false);
        }

        System.out.print("---");
        System.out.println();

        joinContext.network.timestep();

        // all neurons in z shouldn't fire

        final Set<NeuronAdress> z = getNeuroidsSet(0, numberOfNeuronsInGroup, 2);

        for( final NeuronAdress iterationNeuroidAdress : z ) {
            Assert.assertFalse(networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress).getNextFiring());
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

        INetworkAccessor<Float, MetaType> networkAccessor = linkContext.network.getNetworkAccessor();

        // let all input fire and look if the result fires
        for( final NeuronAdress iterationANeuroidAdress : linkContext.a ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroidAdress).setNextFiring(true);
        }

        linkContext.network.timestep();
        linkContext.network.timestep();

        // at least one neuron should fire

        // TODO< adressing is wrong, but works fine if the number of numberOfNeuronsInGroup is equal to the number of relay neurons >
        final Set<NeuronAdress> b = getNeuroidsSet(0, numberOfNeuronsInGroup, 2);

        boolean atLeastOneNeuroidInBFiring = false;

        for( final NeuronAdress iterationNeuroid : b ) {
            if( networkAccessor.getNeuroidAccessorByAdress(iterationNeuroid).getNextFiring() ) {
                atLeastOneNeuroidInBFiring = true;
                break;
            }
        }

        Assert.assertTrue(atLeastOneNeuroidInBFiring);

    }

    private static JoinContext joinCreateNetworkAndDoJoinOperation(int numberOfNeuronsInGroup, int r, int joinK, int linkK, float T) {
        Random random = new Random(4223);

        Network<Float, MetaType> network = new Network<>(new FloatWeightHelper(), new DannNetworkAccessor<>());
        network.update = new NeuroidPredictiveJoinLearningAlgorithm.DummyUpdate();

        INetworkAccessor<Float, MetaType> networkAccessor = network.getNetworkAccessor();

        // first 5 neurons are "a", next are "b", next are "z"
        network.allocateNeurons(numberOfNeuronsInGroup + numberOfNeuronsInGroup + numberOfNeuronsInGroup, 0, 0);

        for( int neuronI = 0; neuronI < numberOfNeuronsInGroup*3; neuronI++ ) {
            INeuronAccessor<Float, MetaType> neuronAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN));

            neuronAccessor.setThreshold(T);
            // just a standard state
            neuronAccessor.setState(EnumStandardNeuroidState.AvailableMemory.ordinal());
        }

        // we have to do this after allocating the neuroids
        UnittestNeuroidAllocator neuroidAllocator = new UnittestNeuroidAllocator(network);

        UnittestExposedNeuroidPredictiveJoinLearningAlgorithm predictiveJoinLearningAlgorithm = new UnittestExposedNeuroidPredictiveJoinLearningAlgorithm(neuroidAllocator, r, joinK, linkK, T, network);


        // generate edges
        final List<EdgeWeightTuple<Float>> networkEdges = generateEdgesForJoinTestNetwork(random, numberOfNeuronsInGroup, joinK, T);
        network.addEdgeWeightTuples(networkEdges);

        network.initialize();

        final Set<NeuronAdress> a = getNeuroidsSet(0, numberOfNeuronsInGroup, 0);
        final Set<NeuronAdress> b = getNeuroidsSet(0, numberOfNeuronsInGroup, 1);

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

        Network<Float, MetaType> network = new Network<>(new FloatWeightHelper(), new DannNetworkAccessor<>());
        network.update = new NeuroidPredictiveJoinLearningAlgorithm.DummyUpdate();

        INetworkAccessor<Float, MetaType> networkAccessor = network.getNetworkAccessor();

        // first 5 neurons are "a", next are "relay", next are "b"
        network.allocateNeurons(numberOfNeuronsInGroup + numberOfRelayNeuroids + numberOfNeuronsInGroup, 0, 0);

        for( int neuronI = 0; neuronI < numberOfNeuronsInGroup; neuronI++ ) {
            INeuronAccessor<Float, MetaType> neuronAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN));

            neuronAccessor.setThreshold(T);
            // just a standard state
            neuronAccessor.setState(EnumStandardNeuroidState.AvailableMemory.ordinal());
        }

        for( int neuronI = numberOfNeuronsInGroup; neuronI < numberOfNeuronsInGroup + numberOfRelayNeuroids; neuronI++ ) {
            INeuronAccessor<Float, MetaType> neuronAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN));

            neuronAccessor.setThreshold(T);
            // just a standard state
            neuronAccessor.setState(EnumStandardNeuroidState.Relay.ordinal());
        }

        for( int neuronI = numberOfNeuronsInGroup + numberOfRelayNeuroids; neuronI < numberOfNeuronsInGroup + numberOfRelayNeuroids + numberOfNeuronsInGroup; neuronI++ ) {
            INeuronAccessor<Float, MetaType> neuronAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN));

            neuronAccessor.setThreshold(T);
            // just a standard state
            neuronAccessor.setState(EnumStandardNeuroidState.AvailableMemory.ordinal());
        }

        // required for link functionality!
        network.resizeFiringHistory(1);


        // we have to do this after allocating the neuroids
        UnittestNeuroidAllocator neuroidAllocator = new UnittestNeuroidAllocator(network);

        UnittestExposedNeuroidPredictiveJoinLearningAlgorithm predictiveJoinLearningAlgorithm = new UnittestExposedNeuroidPredictiveJoinLearningAlgorithm(neuroidAllocator, r, joinK, linkK, T, network);


        Set<NeuronAdress> globalRelayNeuroids = new HashSet<>();

        // fill relay neuroid set
        for( int neuronI = numberOfNeuronsInGroup; neuronI < numberOfNeuronsInGroup + numberOfRelayNeuroids; neuronI++ ) {
            globalRelayNeuroids.add(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN));
        }

        predictiveJoinLearningAlgorithm.setGlobalRelayNeuroids(globalRelayNeuroids);



        // generate edges
        final List<EdgeWeightTuple<Float>> networkEdges = generateEdgesForLinkTestNetwork(random, network, numberOfNeuronsInGroup, numberOfRelayNeuroids, T);
        network.addEdgeWeightTuples(networkEdges);

        network.initialize();

        // TODO< adressing is wrong, but works fine if the number of numberOfNeuronsInGroup is equal to the number of relay neurons >
        final Set<NeuronAdress> a = getNeuroidsSet(0, numberOfNeuronsInGroup, 0);
        final Set<NeuronAdress> b = getNeuroidsSet(0, numberOfNeuronsInGroup, 2);

        // do actual test
        predictiveJoinLearningAlgorithm.linkEnhanced(a, b);

        LinkContext resultLinkContext = new LinkContext();
        resultLinkContext.a = a;
        resultLinkContext.b = b;
        resultLinkContext.network = network;

        return resultLinkContext;

    }

    private static List<EdgeWeightTuple<Float>> generateEdgesForLinkTestNetwork(Random random, Network<Float, MetaType> network, int numberOfNeuronsInGroup, int numberOfRelayNeuroids, float T) {
        final Set<NeuronAdress> aNeuronAdresses = getNeuroidsSet(0, numberOfNeuronsInGroup, 0);
        final Set<NeuronAdress> relayNeuronAdresses = getNeuroidsSet(numberOfNeuronsInGroup, numberOfRelayNeuroids, 0);
        final Set<NeuronAdress> bNeuronAdresses = getNeuroidsSet(numberOfNeuronsInGroup + numberOfRelayNeuroids, numberOfNeuronsInGroup, 0);

        List<EdgeWeightTuple<Float>> edges = new ArrayList<>();

        edges.addAll(DistribuatorHelper.forEachInputChooseRandomOutput(aNeuronAdresses, relayNeuronAdresses, T, random));
        edges.addAll(DistribuatorHelper.forEachInputChooseRandomOutput(relayNeuronAdresses, bNeuronAdresses, T, random));

        return edges;
    }

    private static List<EdgeWeightTuple<Float>> generateEdgesForJoinTestNetwork(Random random, int numberOfNeuronsInGroup, int k, float T) {
        List<EdgeWeightTuple<Float>> result = new ArrayList<>();

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
                result.add(new EdgeWeightTuple<>(new NeuronAdress(sourceIndex, NeuronAdress.EnumType.HIDDEN), new NeuronAdress(destinationNeuronIndex, NeuronAdress.EnumType.HIDDEN), threshold));
            }

            for( final int iterationNeuronInB : neuronsInB ) {
                final int sourceIndex = iterationNeuronInB + 1*numberOfNeuronsInGroup;
                result.add(new EdgeWeightTuple<>(new NeuronAdress(sourceIndex, NeuronAdress.EnumType.HIDDEN), new NeuronAdress(destinationNeuronIndex, NeuronAdress.EnumType.HIDDEN), T/k));
            }
        }

        return result;
    }

    private static Set<NeuronAdress> getNeuroidsSet(int offset, int width, int setIndex) {
        Set<NeuronAdress> resultSet = new HashSet<>();

        for( int i = offset + width*setIndex; i < offset + width*(setIndex+1); i++ ) {
            resultSet.add(new NeuronAdress(i, NeuronAdress.EnumType.HIDDEN));
        }

        return resultSet;
    }
}
