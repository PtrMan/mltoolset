// translated from C#
// implementation derived very much from the C# implementation

package ptrman.mltoolset.Neuroid;


import com.syncleus.dann.graph.AbstractDirectedEdge;
import com.syncleus.dann.graph.MutableDirectedAdjacencyGraph;
import ptrman.mltoolset.misc.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.Math.max;

/**
 * 
 * Idea of the neural network is from the book "Circuits of the mind"
 * 
 */
public class Neuroid<Weighttype, MetaType> {

    public interface IWeighttypeHelper<Weighttype> {
        // deprecated
        public Weighttype getValueForZero();

        Weighttype getValueForObject(Object value);

        public boolean greater(Weighttype left, Weighttype right);
        public boolean greaterEqual(Weighttype left, Weighttype right);

        public Weighttype add(Weighttype left, Weighttype right);
    }

    public static class NeuroidGraph<Weighttype, MetaType> {
        public static class NeuronNode<Weighttype, MetaType>  {
            public int index = -1; // for a opencl compatible implementation and a "simple" access

            public NeuroidGraphElement<Weighttype, MetaType> graphElement = new NeuroidGraphElement<>();
        }

        public static class Edge<Weighttype, MetaType> extends AbstractDirectedEdge<NeuronNode<Weighttype, MetaType>> {
            public Edge(NeuronNode source, NeuronNode destination, Weighttype weight) {
                super(source, destination);
                this.weight = weight;
            }

            public Weighttype weight;
            public int state; // state of the neruons, isnt touched by this core neuroid network
        }

        public NeuronNode<Weighttype, MetaType>[] outputNeuronNodes;
        public NeuronNode<Weighttype, MetaType>[] inputNeuronNodes;
        public NeuronNode<Weighttype, MetaType>[] neuronNodes; // "hidden" Neuroid nodes

        public MutableDirectedAdjacencyGraph<NeuronNode<Weighttype, MetaType>, Edge<Weighttype, MetaType>> graph = new MutableDirectedAdjacencyGraph<>();
    }

    public static class NeuroidGraphElement<Weighttype, MetaType> {
        /** \brief indicates if this neuroid is an input neuroid, means that it gets its activation from outside and has no parent connections or theshold and so on */
        public Weighttype threshold;
        public MetaType meta;
        public int state = 0;
        // startstate
        public boolean firing = false;
        public boolean nextFiring = false;
        public boolean[] firingHistory;
        /** \brief indicates if the neuron should fore on the next timestep, is updated by the update function */
        public int remainingLatency = 0;
        /** \brief as long as this is > 0 its mode nor its weights can be changed */
        public Weighttype sumOfIncommingWeights;
        
        public boolean isStimulated(IWeighttypeHelper<Weighttype> weighttypeHelper) {
            return weighttypeHelper.greaterEqual(sumOfIncommingWeights, threshold);
        }

        public void updateFiring() {
            firing = nextFiring;
            nextFiring = false;
        }
    
    }

    public interface IUpdate<Weighttype, MetaType> {
        void calculateUpdateFunction(NeuroidGraph<Weighttype, MetaType> graph, NeuroidGraph.NeuronNode<Weighttype, MetaType> neuroid, IWeighttypeHelper<Weighttype> weighttypeHelper);

        void initialize(NeuroidGraph.NeuronNode<Weighttype, MetaType> neuroid, List<Weighttype> updatedWeights);
    }

    public static class Helper {
        public static class EdgeWeightTuple<Weighttype> {
            public static class NeuronAdress {
                public final int index;
                public final EnumType type;

                public enum EnumType {
                    INPUT,
                    HIDDEN,
                    OUTPUT
                }

                public NeuronAdress(int index, EnumType type) {
                    this.index = index;
                    this.type = type;
                }
            }

            public final NeuronAdress sourceAdress;
            public final NeuronAdress destinationAdress;
            public final Weighttype weight;

            public EdgeWeightTuple(NeuronAdress sourceIndex, NeuronAdress destinationIndex, Weighttype weight) {
                this.sourceAdress = sourceIndex;
                this.destinationAdress = destinationIndex;
                this.weight = weight;
            }
        }
    }

    public Neuroid(IWeighttypeHelper<Weighttype> weighttypeHelper) {
        this.weighttypeHelper = weighttypeHelper;
    }
    
    public void initialize() {
        for( NeuroidGraph.NeuronNode<Weighttype, MetaType> iterationNeuronNode : neuroidsGraph.neuronNodes ) {
            List<Weighttype> weights = new ArrayList<>();
            update.initialize(iterationNeuronNode, weights);
            //neuroidsGraph.elements[neuronI].content.weights = weights;
            boolean thresholdValid = weighttypeHelper.greater((Weighttype)iterationNeuronNode.graphElement.threshold, weighttypeHelper.getValueForZero());
            Assert.Assert(thresholdValid, "threshold must be greater than 0.0!");
        }
    }

    // just for debugging
    public void debugAllNeurons() {
        int neuronI;
        System.out.format("===");
        for (neuronI = 0;neuronI < neuroidsGraph.neuronNodes.length;neuronI++) {
            NeuroidGraphElement neuroidGraphElement = neuroidsGraph.neuronNodes[neuronI].graphElement;
            System.out.println("neuronI " + Integer.toString(neuronI) + " isFiring " + Boolean.toString(neuroidGraphElement.firing));
        }
    }

    public void timestep() {
        updateFiringHistory();

        // order is important

        updateFiringForAllNeuroids();
        updateIncommingWeigthsForAllNeuroids();
        updateNeuronStates();
        decreaseLatency();
    }

    public void addConnections(List<NeuroidGraph.Edge<Weighttype, MetaType>> edges) {
        for( final NeuroidGraph.Edge<Weighttype, MetaType> edge : edges) {
            addConnection(edge);
        }
    }

    public void addConnection(NeuroidGraph.Edge<Weighttype, MetaType> edge) {
        neuroidsGraph.graph.add(edge);
    }

    public void addEdgeWeightTuples(List<Helper.EdgeWeightTuple<Weighttype>> edgeWeightTuples) {
        final boolean debugEdgeWeights = false; // enable just for finding bugs

        if( debugEdgeWeights ) {
            for( final Helper.EdgeWeightTuple<Weighttype> iterationEdgeWeightTuple : edgeWeightTuples ) {
                System.out.format("edge %d->%d, float-w %f", iterationEdgeWeightTuple.sourceAdress.index, iterationEdgeWeightTuple.destinationAdress.index, ((Float)iterationEdgeWeightTuple.weight).floatValue());
                System.out.println();
            }
        }


        for( final Helper.EdgeWeightTuple<Weighttype> iterationEdgeWeightTuple : edgeWeightTuples ) {
            final NeuroidGraph.NeuronNode<Weighttype, MetaType> sourceNode, destinationNode;

            if( iterationEdgeWeightTuple.sourceAdress.type == Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN ) {
                sourceNode = neuroidsGraph.neuronNodes[iterationEdgeWeightTuple.sourceAdress.index];
            }
            else if( iterationEdgeWeightTuple.sourceAdress.type == Helper.EdgeWeightTuple.NeuronAdress.EnumType.INPUT ) {
                sourceNode = neuroidsGraph.inputNeuronNodes[iterationEdgeWeightTuple.sourceAdress.index];
            }
            else {
                throw new InternalError();
            }

            if( iterationEdgeWeightTuple.destinationAdress.type == Helper.EdgeWeightTuple.NeuronAdress.EnumType.HIDDEN ) {
                destinationNode = neuroidsGraph.neuronNodes[iterationEdgeWeightTuple.destinationAdress.index];
            }
            else if( iterationEdgeWeightTuple.destinationAdress.type == Helper.EdgeWeightTuple.NeuronAdress.EnumType.OUTPUT ) {
                destinationNode = neuroidsGraph.outputNeuronNodes[iterationEdgeWeightTuple.destinationAdress.index];
            }
            else {
                throw new InternalError();
            }

            addConnection(new NeuroidGraph.Edge<>(sourceNode, destinationNode, iterationEdgeWeightTuple.weight));
        }
    }

    public boolean[] getActiviationOfNeurons() {
        boolean[] activationResult = new boolean[neuroidsGraph.neuronNodes.length];
        
        for( int neuronI = 0; neuronI < neuroidsGraph.neuronNodes.length; neuronI++ ) {
            activationResult[neuronI] = neuroidsGraph.neuronNodes[neuronI].graphElement.firing;
        }
        return activationResult;
    }

    public void setActivationOfInputNeuron(int index, boolean activation) {
        neuroidsGraph.inputNeuronNodes[index].graphElement.firing = activation;
    }

    public boolean getActivationOfOutputNeuron(int index) {
        return neuroidsGraph.outputNeuronNodes[index].graphElement.firing;
    }

    /** \brief reallocates the neurons
     *
     * the neuronCount includes the count of the input neurons
     *
     */
    public void allocateNeurons(int neuronCount, int inputCount, int outputCount) {
        neuroidsGraph.inputNeuronNodes = new NeuroidGraph.NeuronNode[inputCount];
        neuroidsGraph.outputNeuronNodes = new NeuroidGraph.NeuronNode[outputCount];
        neuroidsGraph.neuronNodes = new NeuroidGraph.NeuronNode[neuronCount];

        for( int neuronI = 0;neuronI < neuroidsGraph.inputNeuronNodes.length; neuronI++ ) {
            neuroidsGraph.inputNeuronNodes[neuronI] = new NeuroidGraph.NeuronNode();
            neuroidsGraph.graph.add(neuroidsGraph.inputNeuronNodes[neuronI]);
        }

        for( int neuronI = 0;neuronI < neuroidsGraph.outputNeuronNodes.length; neuronI++ ) {
            neuroidsGraph.outputNeuronNodes[neuronI] = new NeuroidGraph.NeuronNode();
            neuroidsGraph.graph.add(neuroidsGraph.outputNeuronNodes[neuronI]);
        }


        for( int neuronI = 0;neuronI < neuroidsGraph.neuronNodes.length; neuronI++ ) {
            neuroidsGraph.neuronNodes[neuronI] = new NeuroidGraph.NeuronNode();
            neuroidsGraph.neuronNodes[neuronI].index = neuronI;
            neuroidsGraph.graph.add(neuroidsGraph.neuronNodes[neuronI]);
        }
    }

    public void resizeFiringHistory(int firingHistoryLength) {
        this.firingHistoryLength = firingHistoryLength;

        for( NeuroidGraph.NeuronNode<Weighttype, MetaType> iterationNeuroid : neuroidsGraph.neuronNodes ) {
            iterationNeuroid.graphElement.firingHistory = new boolean[firingHistoryLength];
        }
    }

    public NeuroidGraph<Weighttype, MetaType> getGraph() {
        return neuroidsGraph;
    }

    protected void updateFiringHistory() {
        if( firingHistoryLength == 0 ) {
            return;
        }

        for( NeuroidGraph.NeuronNode<Weighttype, MetaType> iterationNeuroid : neuroidsGraph.neuronNodes ) {
            System.arraycopy(iterationNeuroid.graphElement.firingHistory, 0, iterationNeuroid.graphElement.firingHistory, 1, firingHistoryLength-1);

            iterationNeuroid.graphElement.firingHistory[0] = iterationNeuroid.graphElement.firing;
        }
    }

    protected void updateNeuronStates() {
        for( NeuroidGraph.NeuronNode<Weighttype, MetaType> iterationNeuron : neuroidsGraph.neuronNodes ) {
            // neurons with latency doesn't have to be updated
            if (iterationNeuron.graphElement.remainingLatency > 0) {
                continue;
            }

            List<MetaType> updatedMode = null;
            List<Weighttype> updatedWeights = null;
            update.calculateUpdateFunction(getGraph(), iterationNeuron, weighttypeHelper);
        }
    }


    protected void updateIncommingWeigthsForAllNeuroids() {
        // add up the weights of the incomming edges
        for( int iterationNeuronI = 0; iterationNeuronI < neuroidsGraph.neuronNodes.length; iterationNeuronI++ ) {
            NeuroidGraph.NeuronNode<Weighttype, MetaType> iterationNeuroid = neuroidsGraph.neuronNodes[iterationNeuronI];
            updateIncommingWeighsForNeuroid(iterationNeuroid);
        }
    }

    protected void updateIncommingWeighsForNeuroid(NeuroidGraph.NeuronNode<Weighttype, MetaType> neuroid) {
        Weighttype sumOfWeightsOfThisNeuron = weighttypeHelper.getValueForZero();
        Set<NeuroidGraph.Edge<Weighttype, MetaType>> incommingEdges = neuroidsGraph.graph.getInEdges(neuroid);

        for( NeuroidGraph.Edge<Weighttype, MetaType> iterationIncommingEdge : incommingEdges ) {
            final boolean activation = iterationIncommingEdge.getSourceNode().graphElement.firing;
            if (activation) {
                final Weighttype edgeWeight = iterationIncommingEdge.weight;
                sumOfWeightsOfThisNeuron = weighttypeHelper.add(sumOfWeightsOfThisNeuron, edgeWeight);
            }
        }

        neuroid.graphElement.sumOfIncommingWeights = sumOfWeightsOfThisNeuron;
    }

    protected void updateFiringForAllNeuroids() {
        for( NeuroidGraph.NeuronNode<Weighttype, MetaType> iterationNeuron : neuroidsGraph.neuronNodes ) {
            iterationNeuron.graphElement.updateFiring();
        }
    }

    protected void decreaseLatency() {
        for( NeuroidGraph.NeuronNode<Weighttype, MetaType> iterationNeuronNode : neuroidsGraph.neuronNodes ) {
            iterationNeuronNode.graphElement.remainingLatency = max(iterationNeuronNode.graphElement.remainingLatency-1, 0);
        }
    }

    protected int firingHistoryLength;

    public IUpdate update;

    protected IWeighttypeHelper<Weighttype> weighttypeHelper;

    protected NeuroidGraph<Weighttype, MetaType> neuroidsGraph = new NeuroidGraph();
}
