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
            assert minInputsPerHiddenNeuron >= 1;
            assert maxInputsPerHiddenNeuron >= minInputsPerHiddenNeuron;
            assert multipleConnectionsPropability >= 0.0 && multipleConnectionsPropability <= 1.0;

            List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> resultEdges = new ArrayList<>();

            for( int hiddenNeuronIndex = 0; hiddenNeuronIndex < numberOfHiddenNeurons; hiddenNeuronIndex++ ) {
                int numberOfInputsForThisHiddenNeuron = minInputsPerHiddenNeuron;
                if( random.nextDouble() < multipleConnectionsPropability ) {
                    numberOfInputsForThisHiddenNeuron = minInputsPerHiddenNeuron + random.nextInt(maxInputsPerHiddenNeuron - minInputsPerHiddenNeuron);
                }

                final List<Integer> sourceInputs = DistinctUtility.getNDisjunctIntegers(random, numberOfInputsForThisHiddenNeuron, numberOfInputs);
                for( int sourceInput : sourceInputs ) {
                    final int sourceInputIndexOffset = random.nextInt(inputWidth);
                    resultEdges.add(new Neuroid.Helper.EdgeWeightTuple<>(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(sourceInput*inputWidth + sourceInputIndexOffset, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.INPUT), new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(hiddenNeuronIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN), weight));
                }
            }

            return resultEdges;
        }
    }
}
