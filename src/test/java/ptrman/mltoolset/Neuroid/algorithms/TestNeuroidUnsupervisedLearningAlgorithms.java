package ptrman.mltoolset.Neuroid.algorithms;

import org.junit.Assert;
import org.junit.Test;
import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.Neuroid.vincal.Distributator;

import java.util.List;
import java.util.Random;

/**
 *
 */
public class TestNeuroidUnsupervisedLearningAlgorithms {
    @Test
    public void twoInputs() {
        Distributator.IConnectorService<Integer> connectorService = new Distributator.BouncebackConnectorService<>(5, 2, 5);

        List<Neuroid.Helper.EdgeWeightTuple<Integer>> graphEdges = connectorService.createEdges(1, 2, 2, 0.0, new Random());

        Neuroid<Integer, Integer> neuroid = new Neuroid<>(new ptrman.mltoolset.Neuroid.IntegerWeightHelper());

        neuroid.update = new NeuroidUnsupervisedLearningAlgorithms.Update();

        neuroid.allocateNeurons(5, 5 * 2, 0);

        for( int neuronI = 0; neuronI < 5; neuronI++ ) {
            neuroid.getGraph().neuronNodes[neuronI].graphElement.threshold = 1;
        }

        neuroid.addEdgeWeightTuples(graphEdges);
        neuroid.initialize();

        // stimulate input A
        for( int i = 0; i < 5*2; i++ ) {
            neuroid.setActivationOfInputNeuron(i, false);
        }
        for( int i = 0; i < 5; i++ ) {
            neuroid.setActivationOfInputNeuron(i, true);
        }

        neuroid.timestep();

        // stimulate input B
        for( int i = 0; i < 5*2; i++ ) {
            neuroid.setActivationOfInputNeuron(i, false);
        }
        for( int i = 5; i < 10; i++ ) {
            neuroid.setActivationOfInputNeuron(i, true);
        }

        neuroid.timestep();

        for( int i = 0; i < 5*2; i++ ) {
            neuroid.setActivationOfInputNeuron(i, false);
        }
        // stimulate A and B
        for( int i = 0; i < 5; i++ ) {
            neuroid.setActivationOfInputNeuron(i, true);
        }
        for( int i = 5; i < 10; i++ ) {
            neuroid.setActivationOfInputNeuron(i, true);
        }

        // let it settle in
        neuroid.timestep();

        neuroid.timestep();

        // check that at least one neuron is active
        boolean atLeastOneActive = false;

        final boolean[] activityOfNeurons = neuroid.getActiviationOfNeurons();
        for( int neuronI = 0; neuronI < 5*2; neuronI++ ) {
            if( activityOfNeurons[neuronI] ) {
                atLeastOneActive = true;
                break;
            }
        }

        Assert.assertTrue(atLeastOneActive);
    }
}
