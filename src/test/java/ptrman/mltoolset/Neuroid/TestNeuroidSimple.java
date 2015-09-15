package ptrman.mltoolset.Neuroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple test case for standard neuroid impl.
 */
public class TestNeuroidSimple {
    private static class Update implements IUpdate<Float, Integer> {
        private final int latencyAfterActivation;
        private final float randomFiringPropability;

        public Update(final int latencyAfterActivation, final float randomFiringPropability) {
                this.latencyAfterActivation = latencyAfterActivation;
                this.randomFiringPropability = randomFiringPropability;
        }

        @Override
        public void calculateUpdateFunction(INetworkAccessor<Float, Integer> neuroidAccessor, NeuronAdress neuronAdress, IWeighttypeHelper<Float> weighttypeHelper) {
            //
            INeuronAccessor<Float, Integer> currentNeuroidAccessor = neuroidAccessor.getNeuroidAccessorByAdress(neuronAdress);

            currentNeuroidAccessor.setNextFiring(currentNeuroidAccessor.isStimulated(weighttypeHelper));

            if (currentNeuroidAccessor.getNextFiring()) {
                currentNeuroidAccessor.setRemainingLatency(latencyAfterActivation);
            }
            else {
                boolean isFiring = (float)random.nextFloat() < randomFiringPropability;

                currentNeuroidAccessor.setNextFiring(isFiring);
            }
        }

        @Override
        public void initialize(INetworkAccessor<Float, Integer> neuroidAccessor, NeuronAdress neuronAdress, List<Float> updatedWeights) {

        }

        private Random random = new Random();
    }

    public static void main(String[] args) {
        final int latencyAfterActivation = 3;
        final float randomFiringPropability =.0f;

        Network<Float, Integer> network = new Network<>(new FloatWeightHelper(), new DannNetworkAccessor<>());
        network.update = new Update(latencyAfterActivation, randomFiringPropability);

        network.allocateNeurons(3, 3, 0);

        network.getNetworkAccessor().getNeuroidAccessorByAdress(new NeuronAdress(0, NeuronAdress.EnumType.HIDDEN)).setThreshold(0.5f);
        network.getNetworkAccessor().getNeuroidAccessorByAdress(new NeuronAdress(1, NeuronAdress.EnumType.HIDDEN)).setThreshold(0.5f);
        network.getNetworkAccessor().getNeuroidAccessorByAdress(new NeuronAdress(2, NeuronAdress.EnumType.HIDDEN)).setThreshold(0.5f);

        List<EdgeWeightTuple<Float>> edgeWeightTuples = new ArrayList<>();
        edgeWeightTuples.add(new EdgeWeightTuple<>(new NeuronAdress(2, NeuronAdress.EnumType.INPUT), new NeuronAdress(0, NeuronAdress.EnumType.HIDDEN), 0.9f));
        edgeWeightTuples.add(new EdgeWeightTuple<>(new NeuronAdress(0, NeuronAdress.EnumType.HIDDEN), new NeuronAdress(1, NeuronAdress.EnumType.HIDDEN), 0.9f));
        network.addEdgeWeightTuples(edgeWeightTuples);

        network.initialize();

        for( int timestep = 0; timestep < 5; timestep++ ) {
            System.out.println("=A=A=A=A");

            //network.debugAllNeurons();

            // stimulate
            network.setActivationOfInputNeuron(2, true);

            network.timestep();

            //network.debugAllNeurons();

        }

        int debug = 0;
    }

}
