package ptrman.mltoolset.Neuroid.vincal;

import ptrman.mltoolset.Neuroid.EdgeWeightTuple;
import ptrman.mltoolset.Neuroid.NeuronAdress;
import ptrman.mltoolset.misc.SetHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Contains algorithms for getting the connections by a specific (fixed) rule
 */
public class DistribuatorHelper {
    public static<Weighttype> List<EdgeWeightTuple<Weighttype>> forEachInputChooseRandomOutput(final Set<NeuronAdress> inputNeurons, final Set<NeuronAdress> outputNeurons, Weighttype weight, Random random) {
        List<EdgeWeightTuple<Weighttype>> resultConnections = new ArrayList<>();

        for( final NeuronAdress iterationInput : inputNeurons ) {
            NeuronAdress chosenDestinationNeuron = SetHelper.chooseRandomElement(outputNeurons, random);

            resultConnections.add(new EdgeWeightTuple<>(iterationInput, chosenDestinationNeuron, weight));
        }

        return resultConnections;
    }
}
