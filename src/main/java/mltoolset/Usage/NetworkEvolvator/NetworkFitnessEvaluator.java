package mltoolset.Usage.NetworkEvolvator;

import mltoolset.Neuroid.Neuroid;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.util.List;
import java.util.Random;

import static java.lang.Math.max;

public class NetworkFitnessEvaluator implements FitnessEvaluator<NetworkGeneticExpression> {
    private static class Update implements Neuroid.IUpdate<Float, Integer> {
        private final int latencyAfterActivation;
        private final float randomFiringPropability;

        public Update(final int latencyAfterActivation, final float randomFiringPropability) {
            this.latencyAfterActivation = latencyAfterActivation;
            this.randomFiringPropability = randomFiringPropability;
        }

        @Override
        public void calculateUpdateFunction(int neuronIndex, Neuroid.NeuroidGraphElement neuroid, List<Integer> updatedMode, List<Float> updatedWeights, Neuroid.IWeighttypeHelper<Float> weighttypeHelper) {
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

    public double getFitness(NetworkGeneticExpression networkGeneticExpression, List<? extends NetworkGeneticExpression> list) {
        final float CONNECTION_PENELIZE = 0.05f; // how much does a connection cost?

        final int numberOfInputNeurons = 1;

        final int latencyAfterActivation = 2;
        final float randomFiringPropability = 0.0f;

        float fitness = 50.0f;

        // evaluate how many times the output neuron (neuron 0) got stimulated

        Neuroid<Float, Integer> neuroid = new Neuroid<>(new Neuroid.FloatWeighttypeHelper());
        neuroid.update = new Update(latencyAfterActivation, randomFiringPropability);

        neuroid.allocateNeurons(networkGeneticExpression.neuronCandidatesActive.length, numberOfInputNeurons);
        neuroid.input = new boolean[numberOfInputNeurons];

        for( int neuronI = 0; neuronI < networkGeneticExpression.neuronCandidatesActive.length; neuronI++ ) {
            neuroid.getGraph().elements.get(neuronI).content.threshold = new Float(0.4f);
        }

        neuroid.addConnections(networkGeneticExpression.connectionsWithWeights);

        neuroid.initialize();

        // simulate network
        for( int timestep = 0; timestep < 50; timestep++ ) {
            // stimulate

            if( timestep < 5 ) {
                neuroid.input[0] = true;
            }

            neuroid.timestep();

            // read out result and rate

            final boolean[] neuronActivation = neuroid.getActiviationOfNeurons();

            if( neuronActivation[0] ) {
                //fitness += 10.0f;
            }
            if( neuronActivation[1 + (timestep % 5)] ) {
                fitness += 100.0f;
            }
            //if( neuronActivation[2] ) {
            //    fitness += 10.0f;
            //}

        }

        //System.out.println(networkGeneticExpression.connectionsWithWeights.size());

        fitness -= ((float)networkGeneticExpression.connectionsWithWeights.size() * CONNECTION_PENELIZE);

        // TODO< penelize for active neurons? >

        fitness = max(fitness, 0.0f);

        return fitness;
    }

    public boolean isNatural() {
        return true;
    }
}
