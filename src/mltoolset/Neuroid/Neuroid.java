//
// Translated by CS2J (http://www.cs2j.com): 25.02.2015 04:30:45
//

package NeuralNetworks.Neuroids;

/**
 * 
 * Idea of the neural network is from the book "Circuits of the mind"
 * 
 */
public class Neuroid <Weighttype, ModeType>  
{
    public static class NeuroidGraphElement   
    {
        public boolean isInputNeuroid = false;
        /** \brief indicates if this neuroid is an input neuroid, means that it gets its activation from outside and has no parent connections or theshold and so on */
        public Weighttype threshold;
        public List<ModeType> mode = new List<ModeType>();
        public int state = 0;
        // startstate
        public boolean firing = false;
        public boolean nextFiring = false;
        /** \brief indicates if the neuron should fore on the next timestep, is updated by the update function */
        //public List<Weighttype> weights = new List<Weighttype>(); /* \brief weight for each children in the graph */
        public int remainingLatency = 0;
        /** \brief as long as this is > 0 its mode nor its weights can be changed */
        public Weighttype sumOfIncommingWeights;
        public boolean isStimulated() throws Exception {
            return (dynamic)sumOfIncommingWeights >= threshold;
        }

        public void updateFiring() throws Exception {
            firing = nextFiring;
            nextFiring = false;
        }
    
    }

    public static class State   
    {
        public State() {
        }

        public int latency = new int();
    }

    public interface IUpdate   
    {
        void calculateUpdateFunction(NeuroidGraphElement neuroid, List<ModeType> updatedMode, List<Weighttype> updatedWeights) throws Exception ;

        void initialize(NeuroidGraphElement neuroid, List<int> parentIndices, List<ModeType> updatedMode, List<Weighttype> updatedWeights) throws Exception ;
    
    }

    public void initialize() throws Exception {
        int neuronI = new int();
        for (neuronI = 0;neuronI < neuroidsGraph.elements.Count;neuronI++)
        {
            List<ModeType> modes = new List<ModeType>();
            List<Weighttype> weights = new List<Weighttype>();
            boolean thresholdValid = new boolean();
            // a input neuroid doesn't have to be initialized
            if (neuroidsGraph.elements[neuronI].content.isInputNeuroid)
            {
                continue;
            }
             
            modes = new List<ModeType>();
            weights = new List<Weighttype>();
            update.initialize(neuroidsGraph.elements[neuronI].content, neuroidsGraph.elements[neuronI].parentIndices, modes, weights);
            neuroidsGraph.elements[neuronI].content.mode = modes;
            //neuroidsGraph.elements[neuronI].content.weights = weights;
            thresholdValid = (dynamic)neuroidsGraph.elements[neuronI].content.threshold > (Weighttype)0.0;
            System.Diagnostics.Debug.Assert(thresholdValid, "threshold must be greater than 0.0!");
        }
    }

    // just for debugging
    public void debugAllNeurons() throws Exception {
        int neuronI = new int();
        System.Console.WriteLine("===");
        for (neuronI = 0;neuronI < neuroidsGraph.elements.Count;neuronI++)
        {
            NeuroidGraphElement neuroidGraphElement;
            neuroidGraphElement = neuroidsGraph.elements[neuronI].content;
            System.Console.WriteLine("neuron {0} isFiring {1}", neuronI, neuroidGraphElement.firing);
        }
    }

    public void timestep() throws Exception {
        /*
                    updateFiringForInputNeuroids();
                    updateIncommingWeigthsForAllNeuroids();
                     */
        // order is important, we first update input and then all neuroids
        updateFiringForInputNeuroids();
        updateFiringForAllNeuroids();
        updateIncommingWeigthsForAllNeuroids();
        updateNeuronStates();
        decreaseLatency();
    }

    public void addConnection(int a, int b, Weighttype weight) throws Exception {
        System.Diagnostics.Debug.Assert(a >= 0);
        System.Diagnostics.Debug.Assert(b >= 0);
        neuroidsGraph.elements[a].childIndices.Add(b);
        neuroidsGraph.elements[a].childWeights.Add(weight);
        neuroidsGraph.elements[b].parentIndices.Add(a);
    }

    //neuroidsGraph.elements[a].content.weights.Add(weight);
    public void addTwoWayConnection(int a, int b, Weighttype weight) throws Exception {
        addConnection(a,b,weight);
        addConnection(b,a,weight);
    }

    public boolean[] getActiviationOfNeurons() throws Exception {
        boolean[] activationResult = new boolean[]();
        int neuronI = new int();
        activationResult = new boolean[neuroidsGraph.elements.Count];
        for (neuronI = 0;neuronI < neuroidsGraph.elements.Count;neuronI++)
        {
            activationResult[neuronI] = neuroidsGraph.elements[neuronI].content.firing;
        }
        return activationResult;
    }

    /** \brief reallocates the neurons
             * 
             * the neuronCount includes the count of the input neurons
             * 
             */
    public void allocateNeurons(int neuronCount, int inputCount) throws Exception {
        int neuronI = new int();
        allocatedInputNeuroids = inputCount;
        neuroidsGraph.elements.Clear();
        for (neuronI = 0;neuronI < neuronCount;neuronI++)
        {
            neuroidsGraph.elements.Add(new Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>.Element(new NeuroidGraphElement()));
        }
        for (neuronI = 0;neuronI < inputCount;neuronI++)
        {
            neuroidsGraph.elements[neuronI].content.isInputNeuroid = true;
        }
    }

    private void updateNeuronStates() throws Exception {
        for (Object __dummyForeachVar0 : neuroidsGraph.elements)
        {
            Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>.Element iterationNeuron = (Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>.Element)__dummyForeachVar0;
            List<ModeType> updatedMode = new List<ModeType>();
            List<Weighttype> updatedWeights = new List<Weighttype>();
            // input neuron doesn't have to be updated
            if (iterationNeuron.content.isInputNeuroid)
            {
                continue;
            }
             
            // neurons with latency doesn't have to be updated
            if (iterationNeuron.content.remainingLatency > 0)
            {
                continue;
            }
             
            updatedMode = null;
            updatedWeights = null;
            update.calculateUpdateFunction(iterationNeuron.content, updatedMode, updatedWeights);
            iterationNeuron.content.mode = updatedMode;
        }
    }

    //iterationNeuron.content.weights = updatedWeights;
    private void updateIncommingWeigthsForAllNeuroids() throws Exception {
        int iterationNeuronI = new int();
        for (iterationNeuronI = 0;iterationNeuronI < neuroidsGraph.elements.Count;iterationNeuronI++)
        {
            // add up the weights of the incomming edges
            Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>.Element iterationNeuron = new Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>.Element();
            Weighttype sumOfWeightsOfThisNeuron;
            iterationNeuron = neuroidsGraph.elements[iterationNeuronI];
            // activation of a input neuron doesn't have to be calculated
            if (iterationNeuron.content.isInputNeuroid)
            {
                continue;
            }
            
            sumOfWeightsOfThisNeuron = (Weighttype)0.0;
            for (Object __dummyForeachVar1 : iterationNeuron.parentIndices)
            {
                int iterationParentIndex = (Integer)__dummyForeachVar1;
                boolean activation = new boolean();
                activation = neuroidsGraph.elements[iterationParentIndex].content.firing;
                if (activation)
                {
                    Weighttype edgeWeight = neuroidsGraph.getEdgeWeight(iterationParentIndex, iterationNeuronI);
                    sumOfWeightsOfThisNeuron += edgeWeight;
                }
                 
            }
            iterationNeuron.content.sumOfIncommingWeights = sumOfWeightsOfThisNeuron;
        }
    }

    private void updateFiringForAllNeuroids() throws Exception {
        for (Object __dummyForeachVar2 : neuroidsGraph.elements)
        {
            Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>.Element iterationNeuron = (Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>.Element)__dummyForeachVar2;
            iterationNeuron.content.updateFiring();
        }
    }

    private void updateFiringForInputNeuroids() throws Exception {
        int inputNeuroidI = new int();
        System.Diagnostics.Debug.Assert(allocatedInputNeuroids == input.Length);
        for (inputNeuroidI = 0;inputNeuroidI < input.Length;inputNeuroidI++)
        {
            neuroidsGraph.elements[inputNeuroidI].content.nextFiring = input[inputNeuroidI];
        }
    }

    private void decreaseLatency() throws Exception {
        for (Object __dummyForeachVar3 : neuroidsGraph.elements)
        {
            Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>.Element iterationNeuron = (Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>.Element)__dummyForeachVar3;
            if (iterationNeuron.content.remainingLatency > 0)
            {
                iterationNeuron.content.remainingLatency--;
            }
             
        }
    }

    // input from outside
    // must be set and resized from outside
    public boolean[] input = new boolean[]();
    public IUpdate update;
    public State[] stateInformations = new State[]();
    private Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype> neuroidsGraph = new Datastructures.DirectedGraph<NeuroidGraphElement, Weighttype>();
    private int allocatedInputNeuroids = new int();
}


