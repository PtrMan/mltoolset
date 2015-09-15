package ptrman.mltoolset.Neuroid.vincal;

import ptrman.mltoolset.Neuroid.EdgeWeightTuple;
import ptrman.mltoolset.Neuroid.IWeighttypeHelper;
import ptrman.mltoolset.Neuroid.NeuronAdress;
import ptrman.mltoolset.math.DistinctUtility;

import java.util.*;

/**
 * Connects the input neurons/hidden neurons in a random way which still fulfills the vicinal criteria.
 */
public class Distributator {
    public interface IConnectorService<Weighttype> {
        List<EdgeWeightTuple<Weighttype>> createEdges(Weighttype weight, int minInputsPerHiddenNeuron, int maxInputsPerHiddenNeuron, double multipleConnectionsPropability, Random random);
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
        public List<EdgeWeightTuple<Weighttype>> createEdges(Weighttype weight, int minInputsPerHiddenNeuron, int maxInputsPerHiddenNeuron, double multipleConnectionsPropability, Random random) {
            assert minInputsPerHiddenNeuron >= 1;
            assert maxInputsPerHiddenNeuron >= minInputsPerHiddenNeuron;
            assert multipleConnectionsPropability >= 0.0 && multipleConnectionsPropability <= 1.0;

            List<EdgeWeightTuple<Weighttype>> resultEdges = new ArrayList<>();

            for( int hiddenNeuronIndex = 0; hiddenNeuronIndex < numberOfHiddenNeurons; hiddenNeuronIndex++ ) {
                int numberOfInputsForThisHiddenNeuron = minInputsPerHiddenNeuron;
                if( random.nextDouble() < multipleConnectionsPropability ) {
                    numberOfInputsForThisHiddenNeuron = minInputsPerHiddenNeuron + random.nextInt(maxInputsPerHiddenNeuron - minInputsPerHiddenNeuron);
                }

                final List<Integer> sourceInputs = DistinctUtility.getNDisjunctIntegers(random, numberOfInputsForThisHiddenNeuron, numberOfInputs);
                for( int sourceInput : sourceInputs ) {
                    final int sourceInputIndexOffset = random.nextInt(inputWidth);
                    resultEdges.add(new EdgeWeightTuple<>(new NeuronAdress(sourceInput*inputWidth + sourceInputIndexOffset, NeuronAdress.EnumType.INPUT), new NeuronAdress(hiddenNeuronIndex, NeuronAdress.EnumType.HIDDEN), weight));
                }
            }

            return resultEdges;
        }
    }

    public static class SupervisedStandardConnectionService<Weighttype> implements IConnectorService<Weighttype> {
        public SupervisedStandardConnectionService(final int inputneuronCount, final int numberOfRelayneurons, final int numberOfOutputNeurons, final IWeighttypeHelper<Weighttype> weighttypeHelper) {
            this.inputneuronCount = inputneuronCount;
            this.numberOfRelayneurons = numberOfRelayneurons;
            this.numberOfOutputNeurons = numberOfOutputNeurons;
            this.weighttypeHelper = weighttypeHelper;
        }

        @Override
        public List<EdgeWeightTuple<Weighttype>> createEdges(Weighttype weight, int minInputsPerHiddenNeuron, int maxInputsPerHiddenNeuron, double multipleConnectionsPropability, Random random) {
            List<EdgeWeightTuple<Weighttype>> resultConnections = new ArrayList<>();

            resultConnections.addAll(getConnectionsFromDirectInputToRelayNeurons());
            resultConnections.addAll(getConnectionsFromRelayNeuronsToOutputNeurons());

            return resultConnections;
        }

        private List<EdgeWeightTuple<Weighttype>> getConnectionsFromDirectInputToRelayNeurons() {
            Set<NeuronAdress> directInputNeurons = getDirectInputNeurons();
            Set<NeuronAdress> hiddenRelayNeurons = getHiddenRelayNeurons();

            return DistribuatorHelper.forEachInputChooseRandomOutput(directInputNeurons, hiddenRelayNeurons, weighttypeHelper.getValueForObject(1), random);
        }

        // output neurons aren't the network output neurons
        private List<EdgeWeightTuple<Weighttype>> getConnectionsFromRelayNeuronsToOutputNeurons() {
            Set<NeuronAdress> hiddenRelayNeurons = getHiddenRelayNeurons();
            Set<NeuronAdress> outputNeurons = getOutputNeurons();

            return DistribuatorHelper.forEachInputChooseRandomOutput(hiddenRelayNeurons, outputNeurons, weighttypeHelper.getValueForObject(1), random);
        }

        private Set<NeuronAdress> getHiddenRelayNeurons() {
            Set<NeuronAdress> hiddenRelayNeurons = new HashSet<>();

            // the from numberOfOutputNeurons hidden neurons are relay neurons

            for( int i = 0; i < numberOfRelayneurons; i++ ) {
                hiddenRelayNeurons.add(new NeuronAdress(numberOfOutputNeurons + i, NeuronAdress.EnumType.HIDDEN));
            }

            return hiddenRelayNeurons;
        }

        private Set<NeuronAdress> getDirectInputNeurons() {
            Set<NeuronAdress> directInputNeurons = new HashSet<>();

            for( int inputNeuronIndex = 0; inputNeuronIndex < inputneuronCount; inputNeuronIndex++ ) {
                directInputNeurons.add(new NeuronAdress(inputNeuronIndex, NeuronAdress.EnumType.INPUT));
            }

            return directInputNeurons;
        }

        private Set<NeuronAdress> getOutputNeurons() {
            Set<NeuronAdress> outputNeurons = new HashSet<>();

            for( int i = 0; i < numberOfRelayneurons; i++ ) {
                outputNeurons.add(new NeuronAdress(i, NeuronAdress.EnumType.HIDDEN));
            }

            return outputNeurons;
        }

        private final int numberOfOutputNeurons;
        private final int numberOfRelayneurons;
        private final int inputneuronCount;

        private final IWeighttypeHelper<Weighttype> weighttypeHelper;

        private Random random = new Random();
    }
}
