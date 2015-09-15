// translated from C#
// implementation derived extremly from the C# implementation

package ptrman.mltoolset.Neuroid;

import java.util.List;

/**
 * 
 * Concept of the neural network is from the book "Circuits of the mind"
 * 
 */
public class Network<Weighttype, MetaType> {
    public Network(IWeighttypeHelper<Weighttype> weighttypeHelper, INetworkAccessor<Weighttype, MetaType> neuroidAccessor) {
        this.weighttypeHelper = weighttypeHelper;
        this.neuroidAccessor = neuroidAccessor;
    }
    
    public void initialize() {
        neuroidAccessor.initialize(weighttypeHelper, update);
    }

    /*
    commented because just for debugging
    public void debugAllNeurons() {
        int neuronI;
        System.out.format("===");
        for (neuronI = 0;neuronI < neuroidsGraph.neuronNodes.length;neuronI++) {
            NeuroidGraphElement neuroidGraphElement = neuroidsGraph.neuronNodes[neuronI].graphElement;
            System.out.println("neuronI " + Integer.toString(neuronI) + " getFiring " + Boolean.toString(neuroidGraphElement.firing));
        }
    }
    */

    public void timestep() {
        updateFiringHistory();

        // order is important

        updateFiringForAllNeuroids();
        updateIncommingWeigthsForAllNeuroids();
        updateNeuronStates();
        decreaseLatency();
    }

    /*
     not any more supported
    public void addConnections(List<NeuroidGraph.Edge<Weighttype, MetaType>> edges) {
        for( final NeuroidGraph.Edge<Weighttype, MetaType> edge : edges) {
            addConnection(edge);
        }
    }
      not any more supported
    public void addConnection(NeuroidGraph.Edge<Weighttype, MetaType> edge) {
        neuroidsGraph.graph.add(edge);
    }
    */

    public void addEdgeWeightTuples(List<EdgeWeightTuple<Weighttype>> edgeWeightTuples) {
        final boolean debugEdgeWeights = false; // enable just for finding bugs

        if( debugEdgeWeights ) {
            for( final EdgeWeightTuple<Weighttype> iterationEdgeWeightTuple : edgeWeightTuples ) {
                System.out.format("edge %d->%d, float-w %f", iterationEdgeWeightTuple.sourceAdress.index, iterationEdgeWeightTuple.destinationAdress.index, ((Float)iterationEdgeWeightTuple.weight).floatValue());
                System.out.println();
            }
        }

        neuroidAccessor.addEdges(edgeWeightTuples);
    }

    /*
    uncommented because it was only required for debugging?
    public boolean[] getActiviationOfNeurons() {
        boolean[] activationResult = new boolean[neuroidsGraph.neuronNodes.length];
        
        for( int neuronI = 0; neuronI < neuroidsGraph.neuronNodes.length; neuronI++ ) {
            activationResult[neuronI] = neuroidsGraph.neuronNodes[neuronI].graphElement.firing;
        }
        return activationResult;
    }
    */

    public void setActivationOfInputNeuron(final int index, final boolean activation) {
        neuroidAccessor.setActivationOfInputNeuron(index, activation);
    }

    public boolean getActivationOfOutputNeuron(int index) {
        return neuroidAccessor.getActivationOfOutputNeuron(index);
    }

    /** \brief reallocates the neurons
     *
     * the neuronCount includes the count of the input neurons
     *
     */
    public void allocateNeurons(final int neuronCount, final int inputCount, final int outputCount) {
        neuroidAccessor.allocateNeurons(neuronCount, inputCount, outputCount);
    }

    public void resizeFiringHistory(final int firingHistoryLength) {
        neuroidAccessor.resizeFiringHistory(firingHistoryLength);
    }

    /*
    shouldn't be accessable
    public NeuroidGraph<Weighttype, MetaType> getGraph() {
        return neuroidsGraph;
    }
    */

    public INetworkAccessor<Weighttype, MetaType> getNetworkAccessor() {
        return neuroidAccessor;
    }

    public boolean[] getActiviationOfHiddenNeurons() {
        boolean[] activation = new boolean[neuroidAccessor.getNumberOfNeurons(NeuronAdress.EnumType.HIDDEN)];

        for( int hiddenNeuronIndex = 0; hiddenNeuronIndex < 0; hiddenNeuronIndex++ ) {
            activation[hiddenNeuronIndex] = neuroidAccessor.getNeuroidAccessorByAdress(new NeuronAdress(hiddenNeuronIndex, NeuronAdress.EnumType.HIDDEN)).getFiring();
        }

        return activation;
    }

    protected void updateFiringHistory() {
        neuroidAccessor.updateFiringHistory();
    }

    protected void updateNeuronStates() {
        neuroidAccessor.updateNeuronStates(update, weighttypeHelper);
    }


    protected void updateIncommingWeigthsForAllNeuroids() {
        neuroidAccessor.updateIncommingWeigthsForAllNeuroids(weighttypeHelper);
    }

    protected void updateFiringForAllNeuroids() {
        neuroidAccessor.updateFiringForAllNeuroids();
    }

    protected void decreaseLatency() {
        neuroidAccessor.decreaseLatency();
    }


    public IUpdate<Weighttype, MetaType> update;

    protected INetworkAccessor<Weighttype, MetaType> neuroidAccessor; // used to retrieve the neuroids and the connections
    protected IWeighttypeHelper<Weighttype> weighttypeHelper;

}
