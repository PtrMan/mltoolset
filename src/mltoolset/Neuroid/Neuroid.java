// translated from C#

package mltoolset.Neuroid;

import java.util.ArrayList;
import java.util.List;
import mltoolset.Datastructures.DirectedGraph;

/**
 * 
 * Idea of the neural network is from the book "Circuits of the mind"
 * 
 */
public class Neuroid<Weighttype, ModeType>
{
    public interface IWeighttypeHelper<Weighttype>
    {
        public Weighttype getValueForZero();
        public boolean greater(Weighttype left, Weighttype right);
        public boolean greaterEqual(Weighttype left, Weighttype right);
        
        public Weighttype add(Weighttype left, Weighttype right);
    }
    
    public static class NeuroidGraphElement<Weighttype, ModeType>
    {
        public boolean isInputNeuroid = false;
        /** \brief indicates if this neuroid is an input neuroid, means that it gets its activation from outside and has no parent connections or theshold and so on */
        public Weighttype threshold;
        public List<ModeType> mode = new ArrayList<ModeType>();
        public int state = 0;
        // startstate
        public boolean firing = false;
        public boolean nextFiring = false;
        /** \brief indicates if the neuron should fore on the next timestep, is updated by the update function */
        //public List<Weighttype> weights = new List<Weighttype>(); /* \brief weight for each children in the graph */
        public int remainingLatency = 0;
        /** \brief as long as this is > 0 its mode nor its weights can be changed */
        public Weighttype sumOfIncommingWeights;
        
        public boolean isStimulated(IWeighttypeHelper<Weighttype> weighttypeHelper)
        {
            return weighttypeHelper.greaterEqual(sumOfIncommingWeights, threshold);
        }

        public void updateFiring()
        {
            firing = nextFiring;
            nextFiring = false;
        }
    
    }

    public static class State   
    {
        public State() {
        }

        public int latency;
    }

    public interface IUpdate<Weighttype, ModeType>
    {
        void calculateUpdateFunction(NeuroidGraphElement neuroid, List<ModeType> updatedMode, List<Weighttype> updatedWeights);

        void initialize(NeuroidGraphElement neuroid, List<Integer> parentIndices, List<ModeType> updatedMode, List<Weighttype> updatedWeights);
    
    }
    
    public Neuroid(IWeighttypeHelper<Weighttype> weighttypeHelper)
    {
        this.weighttypeHelper = weighttypeHelper;
    }
    
    public void initialize()
    {
        int neuronI;
        for (neuronI = 0;neuronI < neuroidsGraph.elements.size();neuronI++)
        {
            List<ModeType> modes = new ArrayList<ModeType>();
            List<Weighttype> weights = new ArrayList<Weighttype>();
            boolean thresholdValid;
            // a input neuroid doesn't have to be initialized
            if( neuroidsGraph.elements.get(neuronI).content.isInputNeuroid )
            {
                continue;
            }
             
            modes = new ArrayList<ModeType>();
            weights = new ArrayList<Weighttype>();
            update.initialize(neuroidsGraph.elements.get(neuronI).content, neuroidsGraph.elements.get(neuronI).parentIndices, modes, weights);
            neuroidsGraph.elements.get(neuronI).content.mode = modes;
            //neuroidsGraph.elements[neuronI].content.weights = weights;
            thresholdValid = weighttypeHelper.greater((Weighttype) neuroidsGraph.elements.get(neuronI).content.threshold, weighttypeHelper.getValueForZero());
            mltoolset.misc.Assert.Assert(thresholdValid, "threshold must be greater than 0.0!");
        }
    }

    // just for debugging
    public void debugAllNeurons()
    {
        int neuronI;
        System.out.format("===");
        for (neuronI = 0;neuronI < neuroidsGraph.elements.size();neuronI++)
        {
            NeuroidGraphElement neuroidGraphElement;
            neuroidGraphElement = neuroidsGraph.elements.get(neuronI).content;
            System.out.format("neuron {0} isFiring {1}", neuronI, neuroidGraphElement.firing);
        }
    }

    public void timestep()
    {
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

    public void addConnection(int a, int b, Weighttype weight)
    {
        mltoolset.misc.Assert.Assert(a >= 0, "");
        mltoolset.misc.Assert.Assert(b >= 0, "");
        neuroidsGraph.elements.get(a).childIndices.add(b);
        neuroidsGraph.elements.get(a).childWeights.add(weight);
        neuroidsGraph.elements.get(b).parentIndices.add(a);
    }

    //neuroidsGraph.elements[a].content.weights.Add(weight);
    public void addTwoWayConnection(int a, int b, Weighttype weight)
    {
        addConnection(a,b,weight);
        addConnection(b,a,weight);
    }

    public boolean[] getActiviationOfNeurons()
    {
        boolean[] activationResult;
        int neuronI;
        activationResult = new boolean[neuroidsGraph.elements.size()];
        
        for (neuronI = 0;neuronI < neuroidsGraph.elements.size();neuronI++)
        {
            activationResult[neuronI] = neuroidsGraph.elements.get(neuronI).content.firing;
        }
        return activationResult;
    }

    /** \brief reallocates the neurons
     *
     * the neuronCount includes the count of the input neurons
     *
     */
    public void allocateNeurons(int neuronCount, int inputCount)
    {
        int neuronI;
        allocatedInputNeuroids = inputCount;
        neuroidsGraph.elements.clear();
        for (neuronI = 0;neuronI < neuronCount;neuronI++)
        {
            neuroidsGraph.elements.add(new DirectedGraph.Element<NeuroidGraphElement, Weighttype>(new NeuroidGraphElement()));
        }
        for (neuronI = 0;neuronI < inputCount;neuronI++)
        {
            neuroidsGraph.elements.get(neuronI).content.isInputNeuroid = true;
        }
    }

    private void updateNeuronStates()
    {
        for( DirectedGraph.Element<NeuroidGraphElement, Weighttype> iterationNeuron : neuroidsGraph.elements )
        {
            List<ModeType> updatedMode = new ArrayList<ModeType>();
            List<Weighttype> updatedWeights = new ArrayList<Weighttype>();
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
    private void updateIncommingWeigthsForAllNeuroids()
    {
        int iterationNeuronI;
        
        // add up the weights of the incomming edges
        for (iterationNeuronI = 0;iterationNeuronI < neuroidsGraph.elements.size();iterationNeuronI++)
        {
            DirectedGraph.Element<NeuroidGraphElement, Weighttype> iterationNeuron;
            Weighttype sumOfWeightsOfThisNeuron;
            
            iterationNeuron = neuroidsGraph.elements.get(iterationNeuronI);
            
            // activation of a input neuron doesn't have to be calculated
            if (iterationNeuron.content.isInputNeuroid)
            {
                continue;
            }
            
            sumOfWeightsOfThisNeuron = weighttypeHelper.getValueForZero();
            for( int iterationParentIndex : iterationNeuron.parentIndices )
            {
                boolean activation;
                activation = neuroidsGraph.elements.get(iterationParentIndex).content.firing;
                if (activation)
                {
                    Weighttype edgeWeight = neuroidsGraph.getEdgeWeight(iterationParentIndex, iterationNeuronI);
                    sumOfWeightsOfThisNeuron = weighttypeHelper.add(sumOfWeightsOfThisNeuron, edgeWeight);
                }
                 
            }
            iterationNeuron.content.sumOfIncommingWeights = sumOfWeightsOfThisNeuron;
        }
    }

    private void updateFiringForAllNeuroids()
    {
        for( DirectedGraph.Element<NeuroidGraphElement, Weighttype> iterationNeuron : neuroidsGraph.elements )
        {
            iterationNeuron.content.updateFiring();
        }
    }

    private void updateFiringForInputNeuroids()
    {
        int inputNeuroidI;
        mltoolset.misc.Assert.Assert(allocatedInputNeuroids == input.length, "");
        
        for (inputNeuroidI = 0;inputNeuroidI < input.length;inputNeuroidI++)
        {
            neuroidsGraph.elements.get(inputNeuroidI).content.nextFiring = input[inputNeuroidI];
        }
    }

    private void decreaseLatency()
    {
        for( DirectedGraph.Element<NeuroidGraphElement, Weighttype> iterationNeuron : neuroidsGraph.elements )
        {
            if( iterationNeuron.content.remainingLatency > 0 )
            {
                iterationNeuron.content.remainingLatency--;
            }
        }
    }

    // input from outside
    // must be set and resized from outside
    public boolean[] input;
    public IUpdate update;
    public State[] stateInformations;
    private DirectedGraph<NeuroidGraphElement, Weighttype> neuroidsGraph = new DirectedGraph<NeuroidGraphElement, Weighttype>();
    private int allocatedInputNeuroids;
    
    private IWeighttypeHelper<Weighttype> weighttypeHelper;
}
