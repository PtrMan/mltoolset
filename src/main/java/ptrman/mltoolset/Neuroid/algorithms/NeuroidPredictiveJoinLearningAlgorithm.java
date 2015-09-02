package ptrman.mltoolset.Neuroid.algorithms;

import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.Neuroid.helper.EnumStandardNeuroidState;
import ptrman.mltoolset.Neuroid.helper.Query;
import ptrman.mltoolset.Neuroid.vincal.INeuroidAllocator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Predictive join vicinal learning algorithm
 *
 * Algorithm "specification":
 *   after the paper
 *   "Cortical Learning via Prediction"
 *   written by
 *      Christos H. Papadimitriou  CHRISTOS@CS.BERKELEY.EDU
 *      Santosh S. Vempala         VEMPALA@GATECH.EDU
 *
 * written by SquareOfTwo, Github PtrMan, robertw89@googlemail.com
 */
public class NeuroidPredictiveJoinLearningAlgorithm {
    public static class DummyUpdate implements Neuroid.IUpdate<Float, Integer> {
        @Override
        public void calculateUpdateFunction(Neuroid.NeuroidGraph<Float, Integer> graph, Neuroid.NeuroidGraph.NeuronNode<Float, Integer> neuroid, Neuroid.IWeighttypeHelper<Float> weighttypeHelper) {
            if( neuroid.index >= 10 ) {
                System.out.format("threshold %f", neuroid.graphElement.threshold.floatValue());
                System.out.println();
            }

            neuroid.graphElement.nextFiring = weighttypeHelper.greaterEqual(neuroid.graphElement.sumOfIncommingWeights, neuroid.graphElement.threshold);
        }

        @Override
        public void initialize(Neuroid.NeuroidGraph.NeuronNode<Float, Integer> neuroid, List<Integer> updatedMode, List<Float> updatedWeights) {

        }
    }


    public NeuroidPredictiveJoinLearningAlgorithm(INeuroidAllocator<Float, Integer> neuroidAllocator, int r, int joinK, int linkK, float T, Neuroid<Float, Integer> network) {
        this.neuroidAllocator = neuroidAllocator;
        this.r = r;
        this.joinK = joinK;
        this.linkK = linkK;
        this.T = T;
        this.network = network;
    }

    public void setGlobalRelayNeuroids(Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> globalRelayNeuroids) {
        this.globalRelayNeuroids = globalRelayNeuroids;
    }

    /*private void pjoin(TODO parameters) {

    }*/

    // protected for access for unittesting
    /**
     * join operation as described in the paper, is a specialisation of Valiants version in the book "circuits of the mind"
     *
     * @param a neuroids of the group which represents a
     * @param b neuroids of the group which represents b
     */
    protected void joinEnhanced(final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> a, final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b) {
        // TODO< call interface for feedback for visualisation and/or debugging, unittesting, etc >

        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> z = neuroidAllocator.allocateNeuroidsWithConstraintsConnectedToBothInputsAndWithAtLeastNSynapsesToBoth(a, b, r, joinK);

        // ALGORITHM STEP
        // let "a" fire and update incomming weights of neuroids in z (in the timestep)

        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : a ) {
            iterationANeuroid.graphElement.nextFiring = true;
        }

        network.timestep();

        // in the paper is described that we should look in z if a neuroid fires, instead we look at nextFiring to save one whole timestep inside the neuroid network



        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationZNeuroid : z ) {
            int numberOfSynapsesFromA = 0;
            for( Neuroid.NeuroidGraph.Edge<Float, Integer> iterationEdge : network.getGraph().graph.getInEdges(iterationZNeuroid) ) {
                if( a.contains(iterationEdge.getSourceNode()) ) {
                    numberOfSynapsesFromA++;
                }
            }

            final float weightsForSynapsesFromFiringNeurons = (T / 2.0f) * (1.0f / (float)numberOfSynapsesFromA);


            if( iterationZNeuroid.graphElement.nextFiring ) {
                adjustWeightsOfSynapsesCommingFromNeuroidSetWhichFiresForNeuroid(a, iterationZNeuroid, weightsForSynapsesFromFiringNeurons);

                iterationZNeuroid.graphElement.state = EnumStandardNeuroidState.JoinEnhancedPoised.ordinal();

            } else {
                iterationZNeuroid.graphElement.state = EnumStandardNeuroidState.JoinEnhancedDismissed.ordinal();
                iterationZNeuroid.graphElement.threshold = 0.0f;
            }
        }

        // ALGORITHM STEP
        // let "b" fire

        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : b ) {
            iterationANeuroid.graphElement.nextFiring = true;
        }

        network.timestep();

        // update neurons
        for( Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationZNeuroid : z ) {
            if( iterationZNeuroid.graphElement.state == EnumStandardNeuroidState.JoinEnhancedPoised.ordinal() ) {
                int numberOfSynapsesFromB = 0;
                for( Neuroid.NeuroidGraph.Edge<Float, Integer> iterationEdge : network.getGraph().graph.getInEdges(iterationZNeuroid) ) {
                    if (b.contains(iterationEdge.getSourceNode())) {
                        numberOfSynapsesFromB++;
                    }
                }

                final float weightsForSynapsesFromFiringNeurons = (T / 2.0f) * (1.0f / (float)numberOfSynapsesFromB);


                if( iterationZNeuroid.graphElement.nextFiring ) {
                    adjustWeightsOfSynapsesCommingFromNeuroidSetWhichFiresForNeuroid(b, iterationZNeuroid, weightsForSynapsesFromFiringNeurons);

                    iterationZNeuroid.graphElement.state = EnumStandardNeuroidState.JoinEnhancedOperational.ordinal();
                }
                else {
                    iterationZNeuroid.graphElement.state = EnumStandardNeuroidState.JoinEnhancedDismissed.ordinal();
                }
            }
        }
    }

    protected void linkEnhanced(final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> a, final Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> b) {
        Query.FilterEdgeByCondition<Float, Integer> filterForRelayNeuronsWhichHadFired = new Query.FilterEdgeByCondition<Float, Integer>() {

            @Override
            public boolean query(Neuroid.NeuroidGraph.Edge<Float, Integer> edge) {
                return globalRelayNeuroids.contains(edge.getSourceNode()) && edge.getSourceNode().graphElement.firingHistory[0] == true;
            }
        };

        // TODO< call interface for feedback for visualisation and/or debugging, unittesting, etc >

        // set "b" neurons to state prepared
        for (Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationBNeuroid : b) {
            iterationBNeuroid.graphElement.state = EnumStandardNeuroidState.LinkEnhancedPrepared.ordinal();
        }

        // ALGORITHM STEP
        // let "a" fire

        for (Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationANeuroid : a) {
            iterationANeuroid.graphElement.nextFiring = true;
        }

        network.timestep();

        // let the relay neurons fire
        network.timestep();

        // update neurons
        for (Neuroid.NeuroidGraph.NeuronNode<Float, Integer> iterationBNeuroid : b) {
            if (iterationBNeuroid.graphElement.nextFiring && iterationBNeuroid.graphElement.state == EnumStandardNeuroidState.LinkEnhancedPrepared.ordinal()) {
                iterationBNeuroid.graphElement.state = EnumStandardNeuroidState.LinkEnhancedLOperational.ordinal();

                // query edges of fired relay neurons to the iterationBNeuroid
                final List<Query.QueryCommand<Float, Integer>> queryCommands = Arrays.asList(
                        new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationBNeuroid.index),
                        filterForRelayNeuronsWhichHadFired
                );
                Query.QueryResult<Float, Integer> queryResult = Query.query(queryCommands, network);

                // change weights
                for( Neuroid.NeuroidGraph.Edge<Float, Integer> iterationEdgeSynapseOfQuery : queryResult.edgesSet ) {
                    iterationEdgeSynapseOfQuery.weight = T/(float)linkK;
                }
            }
        }
    }

    /**
     * sets the weights of the firing neuroids (which do have connections to destinationNeuroid) in sourceNeuroidsWhichAreCheckedForFiring to targetWeight
     *
     * @param sourceNeuroidsWhichAreCheckedForFiring
     * @param destinationNeuroid
     * @param targetWeight
     */
    private void adjustWeightsOfSynapsesCommingFromNeuroidSetWhichFiresForNeuroid(Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> sourceNeuroidsWhichAreCheckedForFiring, Neuroid.NeuroidGraph.NeuronNode<Float, Integer> destinationNeuroid, float targetWeight) {
        final List<Query.QueryCommand<Float, Integer>> queryCommands = Arrays.asList(
            new Query.GetInEdgesByNeuroidIndexQueryCommand(destinationNeuroid.index),
            new Query.FilterEdgeSourceQueryCommand(sourceNeuroidsWhichAreCheckedForFiring)
        );

        final Query.QueryResult<Float, Integer> queryResult = Query.query(queryCommands, network);
        for( Neuroid.NeuroidGraph.Edge<Float, Integer> iterationEdge : queryResult.edgesSet ) {
            iterationEdge.weight = targetWeight;
        }
    }


    private final float T; // standard threshold
    private final int r; // how many neuroids should be (roughtly?) allocated
    private final int joinK; // how many synapses should go for the join operation for all input neurons to the result neuron?
    private final int linkK;
    private final INeuroidAllocator<Float, Integer> neuroidAllocator;

    // TODO< method to set this >
    // TODO< Adress notion for easier rewrite for edgeless networks >
    private Set<Neuroid.NeuroidGraph.NeuronNode<Float, Integer>> globalRelayNeuroids;

    private Neuroid<Float, Integer> network;
}
