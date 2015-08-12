package ptrman.mltoolset.Neuroid.vincal;

import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.misc.ListHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contains algorithms for getting the connections by a specific (fixed) rule
 */
public class DistribuatorHelper {
    public static<Weighttype> List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> forEachInputChooseRandomOutput(final List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> inputNeurons, final List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> outputNeurons, Weighttype weight, Random random) {
        List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> resultConnections = new ArrayList<>();

        for( Neuroid.Helper.EdgeWeightTuple.NeuronAdress iterationInput : inputNeurons ) {
            Neuroid.Helper.EdgeWeightTuple.NeuronAdress chosenDestinationNeuron = ListHelper.chooseRandomElement(outputNeurons, random);

            resultConnections.add(new Neuroid.Helper.EdgeWeightTuple<Weighttype>(iterationInput, chosenDestinationNeuron, weight));
        }

        return resultConnections;
    }
}
