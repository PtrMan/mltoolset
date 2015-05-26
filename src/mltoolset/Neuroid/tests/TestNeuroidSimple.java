package mltoolset.Neuroid.tests;

import mltoolset.Neuroid.Neuroid;

import java.util.List;
import java.util.Random;

/**
 * Simple test case for standard neuroid impl.
 */
public class TestNeuroidSimple {
    private static class WeighttypeHelper implements Neuroid.IWeighttypeHelper {
        @Override
        public Object getValueForZero() {
            return 0.0f;
        }

        @Override
        public boolean greater(Object left, Object right) {
            final float leftAsFloat = (Float)left;
            final float rightAsFloat = (Float)right;

            return leftAsFloat > rightAsFloat;
        }

        @Override
        public boolean greaterEqual(Object left, Object right) {
            final float leftAsFloat = (Float)left;
            final float rightAsFloat = (Float)right;

            return leftAsFloat >= rightAsFloat;
        }

        @Override
        public Object add(Object left, Object right) {
            final float leftAsFloat = (Float)left;
            final float rightAsFloat = (Float)right;

            return new Float(leftAsFloat + rightAsFloat);
        }
    }

    private static class Update implements Neuroid.IUpdate<Float, Integer> {
        private final int latencyAfterActivation;
        private final float randomFiringPropability;

        public Update(final int latencyAfterActivation, final float randomFiringPropability) {
                this.latencyAfterActivation = latencyAfterActivation;
                this.randomFiringPropability = randomFiringPropability;
        }

        @Override
        public void calculateUpdateFunction(Neuroid.NeuroidGraphElement neuroid, List<Integer> updatedMode, List<Float> updatedWeights, Neuroid.IWeighttypeHelper<Float> weighttypeHelper) {
            neuroid.nextFiring = neuroid.isStimulated(weighttypeHelper);

            if (neuroid.nextFiring) {
                neuroid.remainingLatency = latencyAfterActivation;
            }
            else {
                boolean isFiring = (float)random.nextFloat() < randomFiringPropability;

                neuroid.nextFiring = isFiring;
            }
        }

        @Override
        public void initialize(Neuroid.NeuroidGraphElement neuroid, List<Integer> parentIndices, List<Integer> updatedMode, List<Float> updatedWeights) {
        }

        private Random random = new Random();
    }

    public static void main(String[] args) {
        final int latencyAfterActivation = 3;
        final float randomFiringPropability =.1f;


        Neuroid<Float, Integer> neuroid = new Neuroid<>(new WeighttypeHelper());
        neuroid.update = new Update(latencyAfterActivation, randomFiringPropability);

        neuroid.allocateNeurons(6, 3);
        neuroid.input = new boolean[3];

        neuroid.getGraph().elements.get(3).content.threshold = new Float(0.5f);
        neuroid.getGraph().elements.get(4).content.threshold = new Float(0.5f);
        neuroid.getGraph().elements.get(5).content.threshold = new Float(0.5f);

        neuroid.addTwoWayConnection(3, 4, 0.9f);
        neuroid.addTwoWayConnection(4, 5, 0.9f);

        neuroid.initialize();

        for( int timestep = 0; timestep < 100; timestep++ ) {
            neuroid.debugAllNeurons();

            neuroid.timestep();

        }

        int debug = 0;


        // TODO< test >
    }

}
