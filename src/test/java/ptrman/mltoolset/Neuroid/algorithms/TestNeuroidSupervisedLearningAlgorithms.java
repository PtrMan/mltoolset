package ptrman.mltoolset.Neuroid.algorithms;

import org.junit.Assert;
import org.junit.Test;
import ptrman.mltoolset.Neuroid.IntegerWeightHelper;
import ptrman.mltoolset.Neuroid.Neuroid;
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
        Neuroid.IWeighttypeHelper weighttypeHelper = new IntegerWeightHelper();

        Distributator.IConnectorService<Integer> connectorService = new Distributator.SupervisedStandardConnectionService<>(inputneuronCount, numberOfRelayneurons, numberOfOutputNeurons, weighttypeHelper);

        List<Neuroid.Helper.EdgeWeightTuple<Integer>> graphEdges = connectorService.createEdges(0, 0, 0, 0.0, new Random());

        Neuroid<Integer, Integer> neuroid = new Neuroid<>(weighttypeHelper);

        neuroid.update = new NeuroidSupervisedLearningAlgorithms.Update();

        neuroid.allocateNeurons(numberOfRelayneurons + numberOfOutputNeurons, inputneuronCount, 0);

        for( int neuronI = 0; neuronI < numberOfRelayneurons; neuronI++ ) {
            neuroid.getGraph().neuronNodes[neuronI].graphElement.threshold = 0xffff; // inf
            neuroid.getGraph().neuronNodes[neuronI].graphElement.state = EnumStandardNeuroidState.UnsupervisedMemory.ordinal();
        }

        for( int neuronI = numberOfRelayneurons; neuronI < numberOfRelayneurons+numberOfOutputNeurons; neuronI++ ) {
            neuroid.getGraph().neuronNodes[neuronI].graphElement.threshold = 1;
            neuroid.getGraph().neuronNodes[neuronI].graphElement.state = EnumStandardNeuroidState.Relay.ordinal();
        }

        neuroid.addEdgeWeightTuples(graphEdges);
        neuroid.initialize();

        boolean[] input = new boolean[inputneuronCount];
        boolean[] teachVector = new boolean[numberOfOutputNeurons];
        input[0] = true;
        teachVector[0] = true;

        testPattern(input, teachVector, neuroid);
    }

    @Test
    public void testMultipleSupervision() {
        final int inputneuronCount = 5;
        final int numberOfRelayneurons = 7;
        final int numberOfOutputNeurons = 3;
        Neuroid.IWeighttypeHelper weighttypeHelper = new IntegerWeightHelper();

        Distributator.IConnectorService<Integer> connectorService = new Distributator.SupervisedStandardConnectionService<>(inputneuronCount, numberOfRelayneurons, numberOfOutputNeurons, weighttypeHelper);

        List<Neuroid.Helper.EdgeWeightTuple<Integer>> graphEdges = connectorService.createEdges(0, 0, 0, 0.0, new Random());

        Neuroid<Integer, Integer> neuroid = new Neuroid<>(weighttypeHelper);

        neuroid.update = new NeuroidSupervisedLearningAlgorithms.Update();

        neuroid.allocateNeurons(numberOfRelayneurons + numberOfOutputNeurons, inputneuronCount, 0);

        for( int neuronI = 0; neuronI < numberOfRelayneurons; neuronI++ ) {
            neuroid.getGraph().neuronNodes[neuronI].graphElement.threshold = 0xffff; // inf
            neuroid.getGraph().neuronNodes[neuronI].graphElement.state = EnumStandardNeuroidState.UnsupervisedMemory.ordinal();
        }

        for( int neuronI = numberOfRelayneurons; neuronI < numberOfRelayneurons+numberOfOutputNeurons; neuronI++ ) {
            neuroid.getGraph().neuronNodes[neuronI].graphElement.threshold = 1;
            neuroid.getGraph().neuronNodes[neuronI].graphElement.state = EnumStandardNeuroidState.Relay.ordinal();
        }

        neuroid.addEdgeWeightTuples(graphEdges);
        neuroid.initialize();

        boolean[] input = new boolean[inputneuronCount];
        boolean[] teachVector = new boolean[numberOfOutputNeurons];
        input[0] = true;
        teachVector[0] = true;

        testPattern(input, teachVector, neuroid);

        input = new boolean[inputneuronCount];
        teachVector = new boolean[numberOfOutputNeurons];
        input[0] = true;
        input[1] = true;
        teachVector[1] = true;

        testPattern(input, teachVector, neuroid);
    }

    private void testPattern(final boolean[] input, final boolean[] teachVector, Neuroid<Integer, Integer> neuroid) {
        for( int i = 0; i < input.length; i++ ) {
            neuroid.setActivationOfInputNeuron(i, input[i]);
        }
        neuroid.setActivationOfInputNeuron(0, true);

        // two because the relay neuron delays one step
        neuroid.timestep();
        neuroid.timestep();

        for( int i = 0; i < teachVector.length; i++ ) {
            if( teachVector[i] ) {
                neuroid.getGraph().neuronNodes[i].graphElement.state = EnumStandardNeuroidState.UnsuperviseMemoryFired.ordinal();
            }
        }

        neuroid.timestep();

        // now it should have learned it...test if its the case

        for( int i = 0; i < input.length; i++ ) {
            neuroid.setActivationOfInputNeuron(i, false);
        }

        neuroid.timestep();
        neuroid.timestep();
        neuroid.timestep();

        for( int i = 0; i < input.length; i++ ) {
            neuroid.setActivationOfInputNeuron(i, input[i]);
        }

        neuroid.timestep();
        neuroid.timestep();
        neuroid.timestep();

        // check if its set
        for( int i = 0; i < teachVector.length; i++ ) {
            if( teachVector[i] ) {
                Assert.assertTrue(neuroid.getGraph().neuronNodes[i].graphElement.firing);
            }
        }
    }
}
