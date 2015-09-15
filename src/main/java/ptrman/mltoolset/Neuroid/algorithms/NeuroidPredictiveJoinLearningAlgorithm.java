package ptrman.mltoolset.Neuroid.algorithms;

import ptrman.mltoolset.Neuroid.*;
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
    public static class DummyUpdate implements IUpdate<Float, MetaType> {
        @Override
        public void calculateUpdateFunction(INetworkAccessor<Float, MetaType> neuroidNetworkAccessor, final NeuronAdress neuronAdress, IWeighttypeHelper<Float> weighttypeHelper) {
            INeuronAccessor<Float, MetaType> neuron = neuroidNetworkAccessor.getNeuroidAccessorByAdress(neuronAdress);

            neuron.setNextFiring(weighttypeHelper.greaterEqual(neuron.getSumOfIncommingWeights(), neuron.getThreshold()));
        }

        @Override
        public void initialize(INetworkAccessor<Float, MetaType> neuroidAccessor, NeuronAdress neuronAdress, List<Float> updatedWeights) {

        }
    }

    public static class MetaType {
        public boolean isPredictive; // part of the PJOIN algorithm, is chosen for all (nonrelay) neurons with proability half
                                     // can later on the gpu be calculated based on the neuron index
    }


    public NeuroidPredictiveJoinLearningAlgorithm(INeuroidAllocator<Float, MetaType> neuroidAllocator, int r, int joinK, int linkK, float T, Network<Float, MetaType> network) {
        this.neuroidAllocator = neuroidAllocator;
        this.r = r;
        this.joinK = joinK;
        this.linkK = linkK;
        this.T = T;
        this.network = network;
    }

    public void setGlobalRelayNeuroids(Set<NeuronAdress> globalRelayNeuroids) {
        this.globalRelayNeuroids = globalRelayNeuroids;
    }

    private void pjoin(final Set<NeuronAdress> a, final Set<NeuronAdress> b) {
        Query.FilterEdgeByCondition<Float, MetaType> filterForRelayNeuronsWhichHadFired = new Query.FilterEdgeByCondition<Float, MetaType>() {
            @Override
            public boolean query(IEdge edge) {
                return globalRelayNeuroids.contains(edge.getSourceAdress()) && network.getNetworkAccessor().getNeuroidAccessorByAdress(edge.getSourceAdress()).getFiringHistory(0) == true;
            }
        };


        INetworkAccessor<Float, MetaType> networkAccessor = network.getNetworkAccessor();


        final Set<NeuronAdress> c = joinEnhanced(a, b);

        Set<NeuronAdress> cp = new HashSet<>();

        // filter for cp
        for( final NeuronAdress iterationNeuroidAdress : c ) {
            if( networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress).getMeta().isPredictive ) {
                cp.add(iterationNeuroidAdress);
            }
        }

        // let the neurons in c which are not in cp enter the state OPERATIONAL for join
        for( final NeuronAdress iterationNeuroidAdress : c ) {
            // optimized< neuroids in cp are predictive neuroids, so neuroids not in cp are not predictive neuroids >
            if( !networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress).getMeta().isPredictive ) {
                networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress).setState(EnumStandardNeuroidState.JoinEnhancedOperational.ordinal());
            }
        }

        // ======
        // step 3

        Set<NeuronAdress> ap = new HashSet<>();
        Set<NeuronAdress> bp = new HashSet<>();

        // filter for ap
        for( final NeuronAdress iterationNeuroidAdress : a ) {
            if( networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress).getMeta().isPredictive ) {
                ap.add(iterationNeuroidAdress);
            }
        }

        // filter for bp
        for( final NeuronAdress iterationNeuroidAdress : b ) {
            if( networkAccessor.getNeuroidAccessorByAdress(iterationNeuroidAdress).getMeta().isPredictive ) {
                bp.add(iterationNeuroidAdress);
            }
        }






        linkEnhanced(cp, ap);

        // MODIFY SYNAPSES
        // set synapse state of relay synapses to ap to PARENT(which is .state = 1)
        // TODO< this should be done after the parallel link operations >
        {
            for( final NeuronAdress iterationNeuroidFromAp : ap ) {
                List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
                        new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationNeuroidFromAp.index),
                        filterForRelayNeuronsWhichHadFired
                );
                final Query.QueryResult<Float, MetaType> queryResultForEdgesFromRelay = Query.query(queryCommands, network);

                Set<IEdge<Float>> edgeSetToDoubleAndSetToParent = queryResultForEdgesFromRelay.edgesSet;

                for( IEdge<Float> iterationEdge : edgeSetToDoubleAndSetToParent ) {
                    iterationEdge.setWeight(iterationEdge.getWeight().floatValue() * 2.0f);
                    iterationEdge.setState(1); // set to PARENT state
                    networkAccessor.updateEdge(iterationEdge);
                }

            }
        }


        linkEnhanced(cp, bp);

        // MODIFY SYNAPSES
        // set synapse state of relay synapses to ap to PARENT(which is .state = 1)
        // TODO< this should be done after the parallel link operations >
        {
            for( final NeuronAdress iterationNeuroidFromBp : bp ) {
                List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
                        new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationNeuroidFromBp.index),
                        filterForRelayNeuronsWhichHadFired
                );
                final Query.QueryResult<Float, MetaType> queryResultForEdgesFromRelay = Query.query(queryCommands, network);

                Set<IEdge<Float>> edgeSetToDoubleAndSetToParent = queryResultForEdgesFromRelay.edgesSet;

                for( IEdge<Float> iterationEdge : edgeSetToDoubleAndSetToParent ) {
                    iterationEdge.setWeight(iterationEdge.getWeight().floatValue() * 2.0f);
                    iterationEdge.setState(1); // set to PARENT state
                    networkAccessor.updateEdge(iterationEdge);
                }

            }
        }

        // ======
        // step 4
        // * cp enters operation state P-operational
        for( final NeuronAdress iterationNeuroid : cp ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationNeuroid).setState(EnumStandardNeuroidState.JoinPOperational.ordinal());
        }

        // double strength from synapses from A and B to Cp
        for( final NeuronAdress iterationNeuroidFromCp : cp ) {
            // we assume that A and B can overlap, so we have to merge the edge/synapse set of a and b
            // if this is not needed the edge strength doubling can be done without the set edgeSetToDouble

            List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
                    new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationNeuroidFromCp.index),
                    new Query.FilterEdgeSourceQueryCommand(a)
            );
            Query.QueryResult<Float, MetaType> queryResultForEdgesFromA = Query.query(queryCommands, network);

            Set<IEdge<Float>> edgeSetToDouble = queryResultForEdgesFromA.edgesSet;

            queryCommands = Arrays.asList(
                    new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationNeuroidFromCp.index),
                    new Query.FilterEdgeSourceQueryCommand(b)
            );
            Query.QueryResult<Float, MetaType> queryResultForEdgesFromB = Query.query(queryCommands, network);

            edgeSetToDouble.addAll(queryResultForEdgesFromB.edgesSet);

            // double strengths of edges
            for( IEdge<Float> iterationEdge : edgeSetToDouble ) {
                iterationEdge.setWeight(iterationEdge.getWeight().floatValue() * 2.0f);
                networkAccessor.updateEdge(iterationEdge);
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
    protected Set<NeuronAdress> joinEnhanced(final Set<NeuronAdress> a, final Set<NeuronAdress> b) {
        // TODO< call interface for feedback for visualisation and/or debugging, unittesting, etc >

        INetworkAccessor<Float, MetaType> networkAccessor = network.getNetworkAccessor();

        final Set<NeuronAdress> z = neuroidAllocator.allocateNeuroidsWithConstraintsConnectedToBothInputsAndWithAtLeastNSynapsesToBoth(a, b, r, joinK);


        // ALGORITHM STEP
        // let "a" fire and update incomming weights of neuroids in z (in the timestep)

        for( final NeuronAdress iterationANeuroid : a ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroid).setNextFiring(true);
        }

        network.timestep();

        // in the paper is described that we should look in z if a neuroid fires, instead we look at nextFiring to save one whole timestep inside the neuroid network



        for( final NeuronAdress iterationZNeuroidAdress : z ) {
            INeuronAccessor<Float, MetaType> iterationZNeuroid = networkAccessor.getNeuroidAccessorByAdress(iterationZNeuroidAdress);

            IEdgesAccessor<Float, MetaType> inEdgesAccessor = iterationZNeuroid.getInEdgesAccessor();

            int numberOfSynapsesFromA = 0;

            for( int edgeIndex = 0; edgeIndex < inEdgesAccessor.getNumberOfEdges(); edgeIndex++ ) {
                IEdge<Float> iterationEdge = inEdgesAccessor.getEdge(edgeIndex);

                if( a.contains(iterationEdge.getSourceAdress()) ) {
                    numberOfSynapsesFromA++;
                }
            }

            final float weightsForSynapsesFromFiringNeurons = (T / 2.0f) * (1.0f / (float)numberOfSynapsesFromA);


            if( iterationZNeuroid.getNextFiring() ) {
                adjustWeightsOfSynapsesCommingFromNeuroidSetWhichFiresForNeuroid(a, iterationZNeuroidAdress, weightsForSynapsesFromFiringNeurons);

                iterationZNeuroid.setState(EnumStandardNeuroidState.JoinEnhancedPoised.ordinal());

            } else {
                iterationZNeuroid.setState(EnumStandardNeuroidState.JoinEnhancedDismissed.ordinal());
                iterationZNeuroid.setThreshold(0.0f);
            }
        }

        // ALGORITHM STEP
        // let "b" fire

        for( final NeuronAdress iterationANeuroid : b ) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroid).setNextFiring(true);
        }

        network.timestep();

        // update neurons
        for( final NeuronAdress iterationZNeuroidAdress : z ) {
            INeuronAccessor<Float, MetaType> iterationZNeuroid = networkAccessor.getNeuroidAccessorByAdress(iterationZNeuroidAdress);

            IEdgesAccessor<Float, MetaType> inEdgesAccessor = iterationZNeuroid.getInEdgesAccessor();

            if( iterationZNeuroid.getState() == EnumStandardNeuroidState.JoinEnhancedPoised.ordinal() ) {
                int numberOfSynapsesFromB = 0;
                for( int edgeIndex = 0; edgeIndex < inEdgesAccessor.getNumberOfEdges(); edgeIndex++ ) {
                    IEdge<Float> iterationEdge = inEdgesAccessor.getEdge(edgeIndex);
                    if( b.contains(iterationEdge.getSourceAdress()) ) {
                        numberOfSynapsesFromB++;
                    }
                }

                final float weightsForSynapsesFromFiringNeurons = (T / 2.0f) * (1.0f / (float)numberOfSynapsesFromB);


                if( iterationZNeuroid.getNextFiring() ) {
                    adjustWeightsOfSynapsesCommingFromNeuroidSetWhichFiresForNeuroid(b, iterationZNeuroidAdress, weightsForSynapsesFromFiringNeurons);

                    iterationZNeuroid.setState(EnumStandardNeuroidState.JoinEnhancedOperational.ordinal());
                }
                else {
                    iterationZNeuroid.setState(EnumStandardNeuroidState.JoinEnhancedDismissed.ordinal());
                }
            }
        }

        return z;
    }

    protected void linkEnhanced(final Set<NeuronAdress> a, final Set<NeuronAdress> b) {
        Query.FilterEdgeByCondition<Float, MetaType> filterForRelayNeuronsWhichHadFired = new Query.FilterEdgeByCondition<Float, MetaType>() {
            @Override
            public boolean query(IEdge edge) {
                return globalRelayNeuroids.contains(edge.getSourceAdress()) && network.getNetworkAccessor().getNeuroidAccessorByAdress(edge.getSourceAdress()).getFiringHistory(0) == true;
            }
        };

        // TODO< call interface for feedback for visualisation and/or debugging, unittesting, etc >

        INetworkAccessor<Float, MetaType> networkAccessor = network.getNetworkAccessor();

        // set "b" neurons to state prepared
        for (NeuronAdress iterationBNeuroid : b) {
            networkAccessor.getNeuroidAccessorByAdress(iterationBNeuroid).setState(EnumStandardNeuroidState.LinkEnhancedPrepared.ordinal());
        }

        // ALGORITHM STEP
        // let "a" fire

        for (NeuronAdress iterationANeuroid : a) {
            networkAccessor.getNeuroidAccessorByAdress(iterationANeuroid).setNextFiring(true);
        }

        network.timestep();

        // let the relay neurons fire
        network.timestep();

        // update neurons
        for (NeuronAdress iterationBNeuroid : b) {
            INeuronAccessor<Float, MetaType> neuroidAccessor = networkAccessor.getNeuroidAccessorByAdress(iterationBNeuroid);

            if (neuroidAccessor.getNextFiring() && neuroidAccessor.getState() == EnumStandardNeuroidState.LinkEnhancedPrepared.ordinal()) {
                neuroidAccessor.setState(EnumStandardNeuroidState.LinkEnhancedLOperational.ordinal());

                // query edges of fired relay neurons to the iterationBNeuroid
                final List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
                        new Query.GetInEdgesByNeuroidIndexQueryCommand(iterationBNeuroid.index),
                        filterForRelayNeuronsWhichHadFired
                );
                Query.QueryResult<Float, MetaType> queryResult = Query.query(queryCommands, network);

                // change weights
                for( IEdge<Float> iterationEdgeSynapseOfQuery : queryResult.edgesSet ) {
                    iterationEdgeSynapseOfQuery.setWeight(T/(float)linkK);
                    networkAccessor.updateEdge(iterationEdgeSynapseOfQuery);
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
    private void adjustWeightsOfSynapsesCommingFromNeuroidSetWhichFiresForNeuroid(Set<NeuronAdress> sourceNeuroidsWhichAreCheckedForFiring, NeuronAdress destinationNeuroid, float targetWeight) {
        final List<Query.QueryCommand<Float, MetaType>> queryCommands = Arrays.asList(
            new Query.GetInEdgesByNeuroidIndexQueryCommand(destinationNeuroid.index),
            new Query.FilterEdgeSourceQueryCommand(sourceNeuroidsWhichAreCheckedForFiring)
        );

        final Query.QueryResult<Float, MetaType> queryResult = Query.query(queryCommands, network);
        for( IEdge iterationEdge : queryResult.edgesSet ) {
            iterationEdge.setWeight(targetWeight);
            network.getNetworkAccessor().updateEdge(iterationEdge);
        }
    }


    protected final float T; // standard threshold
    protected final int r; // how many neuroids should be (roughtly?) allocated
    protected final int joinK; // how many synapses should go for the join operation for all input neurons to the result neuron?
    protected final int linkK;
    protected final INeuroidAllocator<Float, MetaType> neuroidAllocator;

    // TODO< method to set this >
    private Set<NeuronAdress> globalRelayNeuroids;

    private Network<Float, MetaType> network;
}
