package ptrman.mltoolset.Neuroid;

import com.syncleus.dann.graph.AbstractDirectedEdge;
import com.syncleus.dann.graph.MutableDirectedAdjacencyGraph;
import ptrman.mltoolset.misc.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class DannNetworkAccessor<Weighttype, MetaType> implements INetworkAccessor<Weighttype, MetaType> {


    public static class Edge<Weighttype, MetaType> extends AbstractDirectedEdge<Neuroid<Weighttype, MetaType>> implements IEdge<Weighttype> {
        public Edge(DannNetworkAccessor<Weighttype, MetaType> networkAccessor, final NeuronAdress sourceAdress, final NeuronAdress destinationAdress, Weighttype weight) {
            super(networkAccessor.getNeuronByAdress(sourceAdress), networkAccessor.getNeuronByAdress(destinationAdress));
            this.weight = weight;

            this.sourceAdress = sourceAdress;
            this.destinationAdress = destinationAdress;
        }

        @Override
        public NeuronAdress getSourceAdress() {
            return sourceAdress;
        }

        @Override
        public NeuronAdress getDestinationAdress() {
            return destinationAdress;
        }

        @Override
        public void setWeight(Weighttype weight) {
            this.weight = weight;
        }

        @Override
        public Weighttype getWeight() {
            return weight;
        }

        @Override
        public void setState(int state) {
            this.state = state;
        }

        public Weighttype weight;
        public int state = 0; // state of the neurons, isnt touched by this core neuroid network

        private final NeuronAdress sourceAdress;
        private final NeuronAdress destinationAdress;

    }

    private Neuroid<Weighttype, MetaType>[] outputNeuronNodes;
    private Neuroid<Weighttype, MetaType>[] inputNeuronNodes;
    private Neuroid<Weighttype, MetaType>[] neuronNodes; // "hidden" Neuroid nodes

    private MutableDirectedAdjacencyGraph<Neuroid<Weighttype, MetaType>, Edge<Weighttype, MetaType>> graph = new MutableDirectedAdjacencyGraph<>();

    @Override
    public void allocateNeurons(int neuronCount, int inputCount, int outputCount) {
        inputNeuronNodes = new Neuroid[inputCount];
        outputNeuronNodes = new Neuroid[outputCount];
        neuronNodes = new Neuroid[neuronCount];

        for( int neuronI = 0;neuronI < inputNeuronNodes.length; neuronI++ ) {
            inputNeuronNodes[neuronI] = new Neuroid<>();
            graph.add(inputNeuronNodes[neuronI]);
        }

        for( int neuronI = 0;neuronI < outputNeuronNodes.length; neuronI++ ) {
            outputNeuronNodes[neuronI] = new Neuroid<>();
            graph.add(outputNeuronNodes[neuronI]);
        }

        for( int neuronI = 0;neuronI < neuronNodes.length; neuronI++ ) {
            neuronNodes[neuronI] = new Neuroid<>();
            graph.add(neuronNodes[neuronI]);
        }
    }

    @Override
    public INeuronAccessor<Weighttype, MetaType> getNeuroidAccessorByAdress(NeuronAdress adress) {
        return new DannNeuronAccessor<>(getNeuronByAdress(adress), this);
    }

    protected Neuroid getNeuronByAdress(NeuronAdress adress) {
        if( adress.type == NeuronAdress.EnumType.INPUT ) {
            return inputNeuronNodes[adress.index];
        }
        else if( adress.type == NeuronAdress.EnumType.HIDDEN ) {
            return neuronNodes[adress.index];
        }
        else {
            return outputNeuronNodes[adress.index];
        }
    }

    @Override
    public void initialize(IWeighttypeHelper<Weighttype> weighttypeHelper, IUpdate<Weighttype, MetaType> update) {
        int hiddenNeuronIndex = 0;

        for( Neuroid<Weighttype, MetaType> iterationNeuronNode : neuronNodes ) {
            List<Weighttype> weights = new ArrayList<>();
            update.initialize(this, new NeuronAdress(hiddenNeuronIndex, NeuronAdress.EnumType.HIDDEN), weights);
            //neuroidsGraph.elements[neuronI].content.weights = weights;
            boolean thresholdValid = weighttypeHelper.greater((Weighttype)iterationNeuronNode.threshold, weighttypeHelper.getValueForZero());
            Assert.Assert(thresholdValid, "threshold must be greater than 0.0!");

            hiddenNeuronIndex++;
        }
    }

    @Override
    public void setActivationOfInputNeuron(int index, boolean activation) {
        inputNeuronNodes[index].firing = activation;
    }

    @Override
    public void addEdges(List<EdgeWeightTuple<Weighttype>> edgeWeightTuples) {
        for( final EdgeWeightTuple<Weighttype> iterationEdgeWeightTuple : edgeWeightTuples ) {

            if( iterationEdgeWeightTuple.sourceAdress.type == NeuronAdress.EnumType.OUTPUT ) {
                throw new InternalError();
            }

            if( iterationEdgeWeightTuple.destinationAdress.type == NeuronAdress.EnumType.INPUT ) {
                throw new InternalError();
            }

            addConnection(new Edge(this, iterationEdgeWeightTuple.sourceAdress, iterationEdgeWeightTuple.destinationAdress, iterationEdgeWeightTuple.weight));
        }
    }

    @Override
    public boolean getActivationOfOutputNeuron(int index) {
        return outputNeuronNodes[index].firing;
    }

    @Override
    public void resizeFiringHistory(int firingHistoryLength) {
        this.firingHistoryLength = firingHistoryLength;

        for( Neuroid<Weighttype, MetaType> iterationNeuroid : neuronNodes ) {
            iterationNeuroid.firingHistory = new boolean[firingHistoryLength];
        }
    }

    @Override
    public void updateFiringHistory() {
        if( firingHistoryLength == 0 ) {
            return;
        }

        for( Neuroid<Weighttype, MetaType> iterationNeuroid : neuronNodes ) {
            System.arraycopy(iterationNeuroid.firingHistory, 0, iterationNeuroid.firingHistory, 1, firingHistoryLength - 1);

            iterationNeuroid.firingHistory[0] = iterationNeuroid.firing;
        }
    }

    @Override
    public void updateNeuronStates(IUpdate<Weighttype, MetaType> update, IWeighttypeHelper<Weighttype> weighttypeHelper) {
        int hiddenNeuronIndex = 0;

        for( Neuroid<Weighttype, MetaType> iterationNeuron : neuronNodes ) {
            // neurons with latency doesn't have to be updated
            if (iterationNeuron.remainingLatency > 0) {
                continue;
            }

            update.calculateUpdateFunction(this, new NeuronAdress(hiddenNeuronIndex, NeuronAdress.EnumType.HIDDEN), weighttypeHelper);

            hiddenNeuronIndex++;
        }
    }

    @Override
    public void updateIncommingWeigthsForAllNeuroids(IWeighttypeHelper<Weighttype> weighttypeHelper) {
        // add up the weights of the incomming edges
        for( int iterationNeuronI = 0; iterationNeuronI < neuronNodes.length; iterationNeuronI++ ) {
            Neuroid<Weighttype, MetaType> iterationNeuroid = neuronNodes[iterationNeuronI];
            updateIncommingWeighsForNeuroid(weighttypeHelper, iterationNeuroid);
        }
    }

    protected void updateIncommingWeighsForNeuroid(IWeighttypeHelper<Weighttype> weighttypeHelper, Neuroid<Weighttype, MetaType> neuroid) {
        Weighttype sumOfWeightsOfThisNeuron = weighttypeHelper.getValueForZero();
        Set<DannNetworkAccessor.Edge<Weighttype, MetaType>> incommingEdges = graph.getInEdges(neuroid);

        for( DannNetworkAccessor.Edge<Weighttype, MetaType> iterationIncommingEdge : incommingEdges ) {
            final boolean activation = getNeuronByAdress(iterationIncommingEdge.getSourceAdress()).firing;
            if (activation) {
                final Weighttype edgeWeight = iterationIncommingEdge.getWeight();
                sumOfWeightsOfThisNeuron = weighttypeHelper.add(sumOfWeightsOfThisNeuron, edgeWeight);
            }
        }

        neuroid.sumOfIncommingWeights = sumOfWeightsOfThisNeuron;
    }

    @Override
    public void updateFiringForAllNeuroids() {
        for( Neuroid<Weighttype, MetaType> iterationNeuron : neuronNodes ) {
            iterationNeuron.updateFiring();
        }
    }

    @Override
    public void decreaseLatency() {
        for( Neuroid<Weighttype, MetaType> iterationNeuronNode : neuronNodes ) {
            iterationNeuronNode.remainingLatency = Math.max(iterationNeuronNode.remainingLatency-1, 0);
        }
    }

    @Override
    public void updateEdge(IEdge<Weighttype> edge) {
        // this is done implcitly because the DannNetworkAccessor.Edge implements IEdge
    }

    @Override
    public int getNumberOfNeurons(NeuronAdress.EnumType type) {
        if( type == NeuronAdress.EnumType.INPUT ) {
            return inputNeuronNodes.length;
        }
        else if( type == NeuronAdress.EnumType.OUTPUT ) {
            return outputNeuronNodes.length;
        }
        else {
            return  neuronNodes.length;
        }
    }

    public Set<Edge<Weighttype, MetaType>> getInEdgesOfNeuron(Neuroid<Weighttype, MetaType> neuroid) {
        return graph.getInEdges(neuroid);
    }

    private void addConnection(Edge edge) {
        graph.add(edge);
    }


    protected int firingHistoryLength;
}
