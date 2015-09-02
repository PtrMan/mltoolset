package ptrman.mltoolset.Neuroid.vincal;

import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.misc.SetHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Contains algorithms for getting the connections by a specific (fixed) rule
 */
public class DistribuatorHelper {
    public static<Weighttype> List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> forEachInputChooseRandomOutput(final Set<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> inputNeurons, final Set<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> outputNeurons, Weighttype weight, Random random) {
        List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> resultConnections = new ArrayList<>();

        for( Neuroid.Helper.EdgeWeightTuple.NeuronAdress iterationInput : inputNeurons ) {
            Neuroid.Helper.EdgeWeightTuple.NeuronAdress chosenDestinationNeuron = SetHelper.chooseRandomElement(outputNeurons, random);

            resultConnections.add(new Neuroid.Helper.EdgeWeightTuple<Weighttype>(iterationInput, chosenDestinationNeuron, weight));
        }

        return resultConnections;
    }
}
