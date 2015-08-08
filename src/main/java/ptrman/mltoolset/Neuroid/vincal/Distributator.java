package ptrman.mltoolset.Neuroid.vincal;

import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.math.DistinctUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Connects the input neurons/hidden neurons in a random way which still fulfills the vincal criteria.
 */
public class Distributator {
    public interface IConnectorService<Weighttype> {
        List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> createEdges(Weighttype weight, int minInputsPerHiddenNeuron, int maxInputsPerHiddenNeuron, double multipleConnectionsPropability, Random random);
    }

    /**
     *
     * connects them by going from one input to a random Neuroid and going back to a neuron of the other input
     *
     */
    public static class BouncebackConnectorService<Weighttype> implements IConnectorService<Weighttype> {
        private final int numberOfInputs;
        private final int inputWidth;
        private final int numberOfHiddenNeurons;

        public BouncebackConnectorService(int inputWidth, int numberOfInputs, int numberOfHiddenNeurons) {
            this.inputWidth = inputWidth;
            this.numberOfInputs = numberOfInputs;
            this.numberOfHiddenNeurons = numberOfHiddenNeurons;
        }

        @Override
        public List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> createEdges(Weighttype weight, int minInputsPerHiddenNeuron, int maxInputsPerHiddenNeuron, double multipleConnectionsPropability, Random random) {
            assert minInputsPerHiddenNeuron >= 2;
            assert maxInputsPerHiddenNeuron >= minInputsPerHiddenNeuron;
            assert multipleConnectionsPropability >= 0.0 && multipleConnectionsPropability <= 1.0;

            // TODO< multiple connections >

            List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> resultEdges = new ArrayList<>();

            List<Integer> remainingHiddenNeuroids = new ArrayList<>();
            for( int hiddenNeuroidIndex = 0; hiddenNeuroidIndex < numberOfHiddenNeurons; hiddenNeuroidIndex++ ) {
                remainingHiddenNeuroids.add(hiddenNeuroidIndex);
            }

            for( int hiddenNeuronCounter = 0; hiddenNeuronCounter < numberOfHiddenNeurons; hiddenNeuronCounter++ ) {
                final int sourceInputAIndexOffset = random.nextInt(inputWidth);
                final int sourceInputBIndexOffset = random.nextInt(inputWidth);

                final int hiddenIndexIndex = random.nextInt(remainingHiddenNeuroids.size());
                final int hiddenIndex = remainingHiddenNeuroids.get(hiddenIndexIndex);
                remainingHiddenNeuroids.remove(hiddenIndexIndex);

                final List<Integer> sourceInputs = DistinctUtility.getTwoDisjunctNumbers(random, numberOfInputs);
                final int sourceInputA = sourceInputs.get(0);
                final int sourceInputB = sourceInputs.get(1);

                resultEdges.add(new Neuroid.Helper.EdgeWeightTuple<>(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(sourceInputA*inputWidth + sourceInputAIndexOffset, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.INPUT), new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(hiddenIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN), weight));
                resultEdges.add(new Neuroid.Helper.EdgeWeightTuple<>(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(sourceInputB*inputWidth + sourceInputBIndexOffset, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.INPUT), new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(hiddenIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN), weight));
            }

            return resultEdges;
        }
    }
}
