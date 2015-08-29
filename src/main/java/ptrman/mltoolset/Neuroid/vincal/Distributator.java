package ptrman.mltoolset.Neuroid.vincal;

import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.math.DistinctUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Connects the input neurons/hidden neurons in a random way which still fulfills the vicinal criteria.
 */
public class Distributator {
    public interface IConnectorService<Weighttype> {
        List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> createEdges(Weighttype weight, int minInputsPerHiddenNeuron, int maxInputsPerHiddenNeuron, double multipleConnectionsPropability, Random random);
    }

    /**
     *
     * connects them by this algorithm:
     * for each target neuron (which can be a output):
     *     choosing a random set of input-neuron-sets and connecting them to a other target neuron
     *
     */
    public static class ManyToOneConnectorService<Weighttype> implements IConnectorService<Weighttype> {
        private final int numberOfInputs;
        private final int inputWidth;
        private final int numberOfHiddenNeurons;

        public ManyToOneConnectorService(int inputWidth, int numberOfInputs, int numberOfHiddenNeurons) {
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

    public static class SupervisedStandardConnectionService<Weighttype> implements IConnectorService<Weighttype> {
        public SupervisedStandardConnectionService(final int inputneuronCount, final int numberOfRelayneurons, final int numberOfOutputNeurons, final Neuroid.IWeighttypeHelper<Weighttype> weighttypeHelper) {
            this.inputneuronCount = inputneuronCount;
            this.numberOfRelayneurons = numberOfRelayneurons;
            this.numberOfOutputNeurons = numberOfOutputNeurons;
            this.weighttypeHelper = weighttypeHelper;
        }

        @Override
        public List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> createEdges(Weighttype weight, int minInputsPerHiddenNeuron, int maxInputsPerHiddenNeuron, double multipleConnectionsPropability, Random random) {
            List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> resultConnections = new ArrayList<>();

            resultConnections.addAll(getConnectionsFromDirectInputToRelayNeurons());
            resultConnections.addAll(getConnectionsFromRelayNeuronsToOutputNeurons());

            return resultConnections;
        }

        private List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> getConnectionsFromDirectInputToRelayNeurons() {
            List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> directInputNeurons = getDirectInputNeurons();
            List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> hiddenRelayNeurons = getHiddenRelayNeurons();

            return DistribuatorHelper.forEachInputChooseRandomOutput(directInputNeurons, hiddenRelayNeurons, weighttypeHelper.getValueForObject(1), random);
        }

        // output neurons aren't the network output neurons
        private List<Neuroid.Helper.EdgeWeightTuple<Weighttype>> getConnectionsFromRelayNeuronsToOutputNeurons() {
            List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> hiddenRelayNeurons = getHiddenRelayNeurons();
            List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> outputNeurons = getOutputNeurons();

            return DistribuatorHelper.forEachInputChooseRandomOutput(hiddenRelayNeurons, outputNeurons, weighttypeHelper.getValueForObject(1), random);
        }

        private List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> getHiddenRelayNeurons() {
            List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> hiddenRelayNeurons = new ArrayList<>();

            // the from numberOfOutputNeurons hidden neurons are relay neurons

            for( int i = 0; i < numberOfRelayneurons; i++ ) {
                hiddenRelayNeurons.add(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(numberOfOutputNeurons + i, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN));
            }

            return hiddenRelayNeurons;
        }

        private List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> getDirectInputNeurons() {
            List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> directInputNeurons = new ArrayList<>();

            for( int inputNeuronIndex = 0; inputNeuronIndex < inputneuronCount; inputNeuronIndex++ ) {
                directInputNeurons.add(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(inputNeuronIndex, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.INPUT));
            }

            return directInputNeurons;
        }

        private List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> getOutputNeurons() {
            List<Neuroid.Helper.EdgeWeightTuple.NeuronAdress> outputNeurons = new ArrayList<>();

            for( int i = 0; i < numberOfRelayneurons; i++ ) {
                outputNeurons.add(new Neuroid.Helper.EdgeWeightTuple.NeuronAdress(i, Neuroid.Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN));
            }

            return outputNeurons;
        }

        private final int numberOfOutputNeurons;
        private final int numberOfRelayneurons;
        private final int inputneuronCount;

        private final Neuroid.IWeighttypeHelper<Weighttype> weighttypeHelper;

        private Random random = new Random();
    }
}
