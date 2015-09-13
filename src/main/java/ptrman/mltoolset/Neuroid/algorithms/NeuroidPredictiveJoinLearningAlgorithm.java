package ptrman.mltoolset.Neuroid.algorithms;

import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.Neuroid.helper.EnumStandardNeuroidState;
import ptrman.mltoolset.Neuroid.helper.Query;
import ptrman.mltoolset.Neuroid.vincal.INeuroidAllocator;

import java.util.Arrays;
import java.util.HashSet;
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
    public static class DummyUpdate implements Neuroid.IUpdate<Float, MetaType> {
        @Override
        public void calculateUpdateFunction(Neuroid.NeuroidGraph<Float, MetaType> graph, Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> neuroid, Neuroid.IWeighttypeHelper<Float> weighttypeHelper) {
            neuroid.graphElement.nextFiring = weighttypeHelper.greaterEqual(neuroid.graphElement.sumOfIncommingWeights, neuroid.graphElement.threshold);
        }

        @Override
        public void initialize(Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> neuroid, List<Float> updatedWeights) {

        }
    }

    public static class MetaType {
        public boolean isPredictive; // part of the PJOIN algorithm, is chosen for all (nonrelay) neurons with proability half
                                     // can later on the gpu be calculated based on the neuron index
    }


    public NeuroidPredictiveJoinLearningAlgorithm(INeuroidAllocator<Float, MetaType> neuroidAllocator, int r, int joinK, int linkK, float T, Neuroid<Float, MetaType> network) {
        this.neuroidAllocator = neuroidAllocator;
        this.r = r;
        this.joinK = joinK;
        this.linkK = linkK;
        this.T = T;
        this.network = network;
    }

    public void setGlobalRelayNeuroids(Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> globalRelayNeuroids) {
        this.globalRelayNeuroids = globalRelayNeuroids;
    }

    private void pjoin(final Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> a, final Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> b) {
        Query.FilterEdgeByCondition<Float, MetaType> filterForRelayNeuronsWhichHadFired = new Query.FilterEdgeByCondition<Float, MetaType>() {

            @Override
            public boolean query(Neuroid.NeuroidGraph.Edge<Float, MetaType> edge) {
                return globalRelayNeuroids.contains(edge.getSourceNode()) && edge.getSourceNode().graphElement.firingHistory[0] == true;
            }
        };



        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> c = joinEnhanced(a, b);

        Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> cp = new HashSet<>();

        // filter for cp
        for(  Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationNeuroid : c ) {
            if( iterationNeuroid.graphElement.meta.isPredictive ) {
                cp.add(iterationNeuroid);
            }
        }

        // let the neurons in c which are not in cp enter the state OPERATIONAL for join
        for(  Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationNeuroid : c ) {
            // optimized< neuroids in cp are predictive neuroids, so neuroids not in cp are not predictive neuroids >
            if( !iterationNeuroid.graphElement.meta.isPredictive ) {
                iterationNeuroid.graphElement.state = EnumStandardNeuroidState.JoinEnhancedOperational.ordinal();
            }
        }

        // ======
        // step 3

        Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> ap = new HashSet<>();
        Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> bp = new HashSet<>();

        // filter for ap
        for(  Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationNeuroid : a ) {
            if( iterationNeuroid.graphElement.meta.isPredictive ) {
                ap.add(iterationNeuroid);
            }
        }

        // filter for bp
        for(  Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationNeuroid : b ) {
            if( iterationNeuroid.graphElement.meta.isPredictive ) {
                bp.add(iterationNeuroid);
            }
        }






        linkEnhanced(cp, ap);

        // MODIFY SYNAPSES
        // set synapse state of relay synapses to ap to PARENT(which is .state = 1)
        // TODO< this should be done after the parallel link operations >
        {
            for( Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationNeuroidFromAp : ap ) {
                List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
                        new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationNeuroidFromAp.index),
                        filterForRelayNeuronsWhichHadFired
                );
                final Query.QueryResult<Float, MetaType> queryResultForEdgesFromRelay = Query.query(queryCommands, network);

                Set<Neuroid.NeuroidGraph.Edge<Float, MetaType>> edgeSetToDoubleAndSetToParent = queryResultForEdgesFromRelay.edgesSet;

                for( Neuroid.NeuroidGraph.Edge<Float, MetaType> iterationEdge : edgeSetToDoubleAndSetToParent ) {
                    iterationEdge.weight = iterationEdge.weight.floatValue() * 2.0f;
                    iterationEdge.state = 1; // set to PARENT state
                }

            }
        }


        linkEnhanced(cp, bp);

        // MODIFY SYNAPSES
        // set synapse state of relay synapses to ap to PARENT(which is .state = 1)
        // TODO< this should be done after the parallel link operations >
        {
            for( Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationNeuroidFromBp : bp ) {
                List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
                        new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationNeuroidFromBp.index),
                        filterForRelayNeuronsWhichHadFired
                );
                final Query.QueryResult<Float, MetaType> queryResultForEdgesFromRelay = Query.query(queryCommands, network);

                Set<Neuroid.NeuroidGraph.Edge<Float, MetaType>> edgeSetToDoubleAndSetToParent = queryResultForEdgesFromRelay.edgesSet;

                for( Neuroid.NeuroidGraph.Edge<Float, MetaType> iterationEdge : edgeSetToDoubleAndSetToParent ) {
                    iterationEdge.weight = iterationEdge.weight.floatValue() * 2.0f;
                    iterationEdge.state = 1; // set to PARENT state
                }

            }
        }

        // ======
        // step 4
        // * cp enters operation state P-operational
        for(  Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationNeuroid : cp ) {
            iterationNeuroid.graphElement.state = EnumStandardNeuroidState.JoinPOperational.ordinal();
        }

        // double strength from synapses from A and B to Cp
        for( Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationNeuroidFromCp : cp ) {
            // we assume that A and B can overlap, so we have to merge the edge/synapse set of a and b
            // if this is not needed the edge strength doubling can be done without the set edgeSetToDouble

            List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
                    new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationNeuroidFromCp.index),
                    new Query.FilterEdgeSourceQueryCommand(a)
            );
            Query.QueryResult<Float, MetaType> queryResultForEdgesFromA = Query.query(queryCommands, network);

            Set<Neuroid.NeuroidGraph.Edge<Float, MetaType>> edgeSetToDouble = queryResultForEdgesFromA.edgesSet;

            queryCommands = Arrays.asList(
                    new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationNeuroidFromCp.index),
                    new Query.FilterEdgeSourceQueryCommand(b)
            );
            Query.QueryResult<Float, MetaType> queryResultForEdgesFromB = Query.query(queryCommands, network);

            edgeSetToDouble.addAll(queryResultForEdgesFromB.edgesSet);

            // double strengths of edges
            for( Neuroid.NeuroidGraph.Edge<Float, MetaType> iterationEdge : edgeSetToDouble ) {
                iterationEdge.weight = iterationEdge.weight.floatValue() * 2.0f;
            }
        }
    }

    // protected for access for unittesting
    /**
     * join operation as described in the paper, is a specialisation of Valiants version in the book "circuits of the mind"
     *
     * @param a neuroids of the group which represents a
     * @param b neuroids of the group which represents b
     */
    protected Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> joinEnhanced(final Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> a, final Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> b) {
        final Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> z = neuroidAllocator.allocateNeuroidsWithConstraintsConnectedToBothInputsAndWithAtLeastNSynapsesToBoth(a, b, r, joinK);

        // TODO< call interface for feedback for visualisation and/or debugging, unittesting, etc >

        // ALGORITHM STEP
        // let "a" fire and update incomming weights of neuroids in z (in the timestep)

        for( Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationANeuroid : a ) {
            iterationANeuroid.graphElement.nextFiring = true;
        }

        network.timestep();

        // in the paper is described that we should look in z if a neuroid fires, instead we look at nextFiring to save one whole timestep inside the neuroid network



        for( Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationZNeuroid : z ) {
            int numberOfSynapsesFromA = 0;
            for( Neuroid.NeuroidGraph.Edge<Float, MetaType> iterationEdge : network.getGraph().graph.getInEdges(iterationZNeuroid) ) {
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

        for( Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationANeuroid : b ) {
            iterationANeuroid.graphElement.nextFiring = true;
        }

        network.timestep();

        // update neurons
        for( Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationZNeuroid : z ) {
            if( iterationZNeuroid.graphElement.state == EnumStandardNeuroidState.JoinEnhancedPoised.ordinal() ) {
                int numberOfSynapsesFromB = 0;
                for( Neuroid.NeuroidGraph.Edge<Float, MetaType> iterationEdge : network.getGraph().graph.getInEdges(iterationZNeuroid) ) {
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

        return z;
    }

    protected void linkEnhanced(final Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> a, final Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> b) {
        Query.FilterEdgeByCondition<Float, MetaType> filterForRelayNeuronsWhichHadFired = new Query.FilterEdgeByCondition<Float, MetaType>() {

            @Override
            public boolean query(Neuroid.NeuroidGraph.Edge<Float, MetaType> edge) {
                return globalRelayNeuroids.contains(edge.getSourceNode()) && edge.getSourceNode().graphElement.firingHistory[0] == true;
            }
        };

        // TODO< call interface for feedback for visualisation and/or debugging, unittesting, etc >

        // set "b" neurons to state prepared
        for (Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationBNeuroid : b) {
            iterationBNeuroid.graphElement.state = EnumStandardNeuroidState.LinkEnhancedPrepared.ordinal();
        }

        // ALGORITHM STEP
        // let "a" fire

        for (Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationANeuroid : a) {
            iterationANeuroid.graphElement.nextFiring = true;
        }

        network.timestep();

        // let the relay neurons fire
        network.timestep();

        // update neurons
        for (Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> iterationBNeuroid : b) {
            if (iterationBNeuroid.graphElement.nextFiring && iterationBNeuroid.graphElement.state == EnumStandardNeuroidState.LinkEnhancedPrepared.ordinal()) {
                iterationBNeuroid.graphElement.state = EnumStandardNeuroidState.LinkEnhancedLOperational.ordinal();

                // query edges of fired relay neurons to the iterationBNeuroid
                final List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
                        new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationBNeuroid.index),
                        filterForRelayNeuronsWhichHadFired
                );
                Query.QueryResult<Float, MetaType> queryResult = Query.query(queryCommands, network);

                // change weights
                for( Neuroid.NeuroidGraph.Edge<Float, MetaType> iterationEdgeSynapseOfQuery : queryResult.edgesSet ) {
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
    private void adjustWeightsOfSynapsesCommingFromNeuroidSetWhichFiresForNeuroid(Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> sourceNeuroidsWhichAreCheckedForFiring, Neuroid.NeuroidGraph.NeuronNode<Float, MetaType> destinationNeuroid, float targetWeight) {
        final List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
            new Query.GetInEdgesByNeuroidIndexQueryCommand(destinationNeuroid.index),
            new Query.FilterEdgeSourceQueryCommand(sourceNeuroidsWhichAreCheckedForFiring)
        );

        final Query.QueryResult<Float, MetaType> queryResult = Query.query(queryCommands, network);
        for( Neuroid.NeuroidGraph.Edge<Float, MetaType> iterationEdge : queryResult.edgesSet ) {
            iterationEdge.weight = targetWeight;
        }
    }


    protected final float T; // standard threshold
    protected final int r; // how many neuroids should be (roughtly?) allocated
    protected final int joinK; // how many synapses should go for the join operation for all input neurons to the result neuron?
    protected final int linkK;
    protected final INeuroidAllocator<Float, MetaType> neuroidAllocator;

    // TODO< method to set this >
    // TODO< Adress notion for easier rewrite for edgeless networks >
    private Set<Neuroid.NeuroidGraph.NeuronNode<Float, MetaType>> globalRelayNeuroids;

    private Neuroid<Float, MetaType> network;
}
