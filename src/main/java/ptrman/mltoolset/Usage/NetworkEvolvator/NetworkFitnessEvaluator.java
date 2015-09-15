package ptrman.mltoolset.Usage.NetworkEvolvator;

import org.uncommons.watchmaker.framework.FitnessEvaluator;
import ptrman.mltoolset.Neuroid.*;

import java.util.List;
import java.util.Random;

import static java.lang.Math.max;

public class NetworkFitnessEvaluator implements FitnessEvaluator<NetworkGeneticExpression> {
    private static class Update implements IUpdate<Float, Integer> {
        private final int latencyAfterActivation;
        private final float randomFiringPropability;

        public Update(final int latencyAfterActivation, final float randomFiringPropability) {
            this.latencyAfterActivation = latencyAfterActivation;
            this.randomFiringPropability = randomFiringPropability;
        }

        @Override
        public void calculateUpdateFunction(INetworkAccessor<Float, Integer> neuroidNetworkAccessor, NeuronAdress neuronAdress, IWeighttypeHelper<Float> weighttypeHelper) {
            INeuronAccessor<Float, Integer> neuronAccessor = neuroidNetworkAccessor.getNeuroidAccessorByAdress(neuronAdress);

            neuronAccessor.setNextFiring(neuronAccessor.isStimulated(weighttypeHelper));

            if (neuronAccessor.getNextFiring()) {
                neuronAccessor.setRemainingLatency(latencyAfterActivation);
            }
            else {
                boolean isFiring = (float)random.nextFloat() < randomFiringPropability;

                neuronAccessor.setNextFiring(isFiring);
            }

        }

        @Override
        public void initialize(INetworkAccessor<Float, Integer> neuroidAccessor, NeuronAdress neuronAdress, List<Float> updatedWeights) {

        }

        private Random random = new Random();
    }

    public double getFitness(NetworkGeneticExpression networkGeneticExpression, List<? extends NetworkGeneticExpression> list) {
        final float CONNECTION_PENELIZE = 0.08f; // how much does a connection cost?
        final float NEURON_PENELIZE = 0.8f; // how much does a neuron cost?

        final int numberOfInputNeurons = 1;

        final int latencyAfterActivation = 2;
        final float randomFiringPropability = 0.0f;

        float fitness = 5000.0f;

        // evaluate how many times the output neuron (neuron 0) got stimulated

        Network<Float, Integer> network = new Network<>(new ptrman.mltoolset.Neuroid.FloatWeightHelper(), new DannNetworkAccessor<>());
        network.update = new Update(latencyAfterActivation, randomFiringPropability);

        INetworkAccessor<Float, Integer> neuroidNetworkAccessor = network.getNetworkAccessor();

        network.allocateNeurons(networkGeneticExpression.neuronCandidatesActive.length, numberOfInputNeurons, 1);

        for( int neuronI = 0; neuronI < networkGeneticExpression.neuronCandidatesActive.length; neuronI++ ) {
            neuroidNetworkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(neuronI, NeuronAdress.EnumType.HIDDEN)).setThreshold(0.4f);
        }

        network.addEdgeWeightTuples(networkGeneticExpression.connectionsWithWeights);

        network.initialize();

        // simulate network
        for( int timestep = 0; timestep < 80; timestep++ ) {
            // stimulate

            if( (timestep % 5) == 0 ) {
                network.setActivationOfInputNeuron(0, true);
            }

            network.timestep();

            // read out result and rate

            final boolean[] neuronActivation = network.getActiviationOfHiddenNeurons();

            if( neuronActivation[0] ) {
                //fitness += 10.0f;
            }



            for( int timestepNeuron = 0; timestepNeuron < 5; timestepNeuron++ ) {
                final float fitnessDelta;

                if( timestepNeuron == timestep ) {
                    fitnessDelta = 100.0f;
                }
                else {
                    fitnessDelta = -1.0f;
                }

                if( neuronActivation[1 + timestepNeuron] ) {
                    fitness += fitnessDelta;
                }
            }

            //if( neuronActivation[1 + (timestep % 5)] ) {
            //    fitness += 100.0f;
            //}
            //if( neuronActivation[2] ) {
            //    fitness += 10.0f;
            //}

        }

        //System.out.println(networkGeneticExpression.connectionsWithWeights.size());

        fitness -= ((float)networkGeneticExpression.connectionsWithWeights.size() * CONNECTION_PENELIZE);

        fitness -= ((float)getNumberOfActiveNeurons(networkGeneticExpression) * NEURON_PENELIZE);

        fitness = max(fitness, 0.0f);

        return fitness;
    }

    public boolean isNatural() {
        return true;
    }

    private static int getNumberOfActiveNeurons(NetworkGeneticExpression networkGeneticExpression) {
        final boolean[] neuronsActivate = networkGeneticExpression.neuronCandidatesActive;

        int activeNeurons = 0;

        for( int i = 0; i < neuronsActivate.length; i++ ) {
            if( neuronsActivate[i] ) {
                activeNeurons++;
            }
        }

        return activeNeurons;
    }
}
