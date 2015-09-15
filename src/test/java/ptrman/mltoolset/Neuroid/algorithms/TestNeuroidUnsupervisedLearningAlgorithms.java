package ptrman.mltoolset.Neuroid.algorithms;

import org.junit.Assert;
import org.junit.Test;
import ptrman.mltoolset.Neuroid.DannNetworkAccessor;
import ptrman.mltoolset.Neuroid.EdgeWeightTuple;
import ptrman.mltoolset.Neuroid.Network;
import ptrman.mltoolset.Neuroid.NeuronAdress;
import ptrman.mltoolset.Neuroid.vincal.Distributator;

import java.util.List;
import java.util.Random;

/**
 *
 */
public class TestNeuroidUnsupervisedLearningAlgorithms {
    @Test
    public void twoInputs() {
        Distributator.IConnectorService<Integer> connectorService = new Distributator.ManyToOneConnectorService<>(5, 2, 5);

        List<EdgeWeightTuple<Integer>> graphEdges = connectorService.createEdges(1, 2, 2, 0.0, new Random());

        Network<Integer, Integer> network = new Network<>(new ptrman.mltoolset.Neuroid.IntegerWeightHelper(), new DannNetworkAccessor<>());

        network.update = new NeuroidUnsupervisedLearningAlgorithms.Update();

        network.allocateNeurons(5, 5 * 2, 0);

        for( int neuronI = 0; neuronI < 5; neuronI++ ) {
            network.getNetworkAccessor().getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN)).setThreshold(0xffff); // inf
        }

        network.addEdgeWeightTuples(graphEdges);
        network.initialize();

        // stimulate input A
        for( int i = 0; i < 5*2; i++ ) {
            network.setActivationOfInputNeuron(i, false);
        }
        for( int i = 0; i < 5; i++ ) {
            network.setActivationOfInputNeuron(i, true);
        }

        network.timestep();

        // stimulate input B
        for( int i = 0; i < 5*2; i++ ) {
            network.setActivationOfInputNeuron(i, false);
        }
        for( int i = 5; i < 10; i++ ) {
            network.setActivationOfInputNeuron(i, true);
        }

        network.timestep();

        for( int i = 0; i < 5*2; i++ ) {
            network.setActivationOfInputNeuron(i, false);
        }
        // stimulate A and B
        for( int i = 0; i < 5; i++ ) {
            network.setActivationOfInputNeuron(i, true);
        }
        for( int i = 5; i < 10; i++ ) {
            network.setActivationOfInputNeuron(i, true);
        }

        // let it settle in
        network.timestep();

        network.timestep();

        // check that at least one neuron is active
        boolean atLeastOneActive = false;

        final boolean[] activityOfHiddenNeurons = network.getActiviationOfHiddenNeurons();
        for( int neuronI = 0; neuronI < activityOfHiddenNeurons.length; neuronI++ ) {
            if( activityOfHiddenNeurons[neuronI] ) {
                atLeastOneActive = true;
                break;
            }
        }

        Assert.assertTrue(atLeastOneActive);
    }
}
