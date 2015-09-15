package ptrman.mltoolset.Neuroid.algorithms;

import org.junit.Assert;
import org.junit.Test;
import ptrman.mltoolset.Neuroid.*;
import ptrman.mltoolset.Neuroid.helper.EnumStandardNeuroidState;
import ptrman.mltoolset.Neuroid.vincal.Distributator;

import java.util.List;
import java.util.Random;

/**
 *
 */
public class TestNeuroidSupervisedLearningAlgorithms {
    @Test
    public void testSingleSupervision() {
        final int inputneuronCount = 5;
        final int numberOfRelayneurons = 7;
        final int numberOfOutputNeurons = 3;
        IWeighttypeHelper weighttypeHelper = new IntegerWeightHelper();

        Distributator.IConnectorService<Integer> connectorService = new Distributator.SupervisedStandardConnectionService<>(inputneuronCount, numberOfRelayneurons, numberOfOutputNeurons, weighttypeHelper);

        List<EdgeWeightTuple<Integer>> graphEdges = connectorService.createEdges(0, 0, 0, 0.0, new Random());

        Network<Integer, Integer> network = new Network<>(weighttypeHelper, new DannNetworkAccessor<>());

        network.update = new NeuroidSupervisedLearningAlgorithms.Update();

        network.allocateNeurons(numberOfRelayneurons + numberOfOutputNeurons, inputneuronCount, 0);

        INetworkAccessor<Integer, Integer> networkAccessor = network.getNetworkAccessor();

        for( int neuronI = 0; neuronI < numberOfRelayneurons; neuronI++ ) {
            INeuronAccessor<Integer, Integer> neuroidAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN));

            neuroidAccessor.setThreshold(0xffff); // inf
            neuroidAccessor.setState(EnumStandardNeuroidState.UnsupervisedMemory.ordinal());
        }

        for( int neuronI = numberOfRelayneurons; neuronI < numberOfRelayneurons+numberOfOutputNeurons; neuronI++ ) {
            INeuronAccessor<Integer, Integer> neuroidAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN));

            neuroidAccessor.setThreshold(1);
            neuroidAccessor.setState(EnumStandardNeuroidState.Relay.ordinal());
        }

        network.addEdgeWeightTuples(graphEdges);
        network.initialize();

        boolean[] input = new boolean[inputneuronCount];
        boolean[] teachVector = new boolean[numberOfOutputNeurons];
        input[0] = true;
        teachVector[0] = true;

        testPattern(input, teachVector, network);
    }

    @Test
    public void testMultipleSupervision() {
        final int inputneuronCount = 5;
        final int numberOfRelayneurons = 7;
        final int numberOfOutputNeurons = 3;
        IWeighttypeHelper<Integer> weighttypeHelper = new IntegerWeightHelper();

        Distributator.IConnectorService<Integer> connectorService = new Distributator.SupervisedStandardConnectionService<>(inputneuronCount, numberOfRelayneurons, numberOfOutputNeurons, weighttypeHelper);

        List<EdgeWeightTuple<Integer>> graphEdges = connectorService.createEdges(0, 0, 0, 0.0, new Random());

        Network<Integer, Integer> network = new Network<>(weighttypeHelper, new DannNetworkAccessor<>());

        network.update = new NeuroidSupervisedLearningAlgorithms.Update();

        network.allocateNeurons(numberOfRelayneurons + numberOfOutputNeurons, inputneuronCount, 0);

        INetworkAccessor<Integer, Integer> networkAccessor = network.getNetworkAccessor();

        for( int neuronI = 0; neuronI < numberOfRelayneurons; neuronI++ ) {
            INeuronAccessor<Integer, Integer> neuroidAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN));

            neuroidAccessor.setThreshold(0xffff); // inf
            neuroidAccessor.setState(EnumStandardNeuroidState.UnsupervisedMemory.ordinal());
        }

        for( int neuronI = numberOfRelayneurons; neuronI < numberOfRelayneurons+numberOfOutputNeurons; neuronI++ ) {
            INeuronAccessor<Integer, Integer> neuroidAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN));

            neuroidAccessor.setThreshold(1);
            neuroidAccessor.setState(EnumStandardNeuroidState.Relay.ordinal());
        }

        network.addEdgeWeightTuples(graphEdges);
        network.initialize();

        boolean[] input = new boolean[inputneuronCount];
        boolean[] teachVector = new boolean[numberOfOutputNeurons];
        input[0] = true;
        teachVector[0] = true;

        testPattern(input, teachVector, network);

        input = new boolean[inputneuronCount];
        teachVector = new boolean[numberOfOutputNeurons];
        input[0] = true;
        input[1] = true;
        teachVector[1] = true;

        testPattern(input, teachVector, network);
    }

    private void testPattern(final boolean[] input, final boolean[] teachVector, Network<Integer, Integer> network) {
        INetworkAccessor<Integer, Integer> networkAccessor = network.getNetworkAccessor();

        for( int i = 0; i < input.length; i++ ) {
            network.setActivationOfInputNeuron(i, input[i]);
        }
        network.setActivationOfInputNeuron(0, true);

        // two because the relay neuron delays one step
        network.timestep();
        network.timestep();

        for( int i = 0; i < teachVector.length; i++ ) {
            if( teachVector[i] ) {
                INeuronAccessor<Integer, Integer> neuroidAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(i, NeuronAdress.EnumType.HIDDEN));
                neuroidAccessor.setState(EnumStandardNeuroidState.UnsuperviseMemoryFired.ordinal());
            }
        }

        network.timestep();

        // now it should have learned it...test if its the case

        for( int i = 0; i < input.length; i++ ) {
            network.setActivationOfInputNeuron(i, false);
        }

        network.timestep();
        network.timestep();
        network.timestep();

        for( int i = 0; i < input.length; i++ ) {
            network.setActivationOfInputNeuron(i, input[i]);
        }

        network.timestep();
        network.timestep();
        network.timestep();

        // check if its set
        for( int i = 0; i < teachVector.length; i++ ) {
            if( teachVector[i] ) {
                INeuronAccessor<Integer, Integer> neuroidAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(i, NeuronAdress.EnumType.HIDDEN));
                Assert.assertTrue(neuroidAccessor.getFiring());
            }
        }
    }
}
