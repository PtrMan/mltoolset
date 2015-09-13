package ptrman.mltoolset.Neuroid.algorithms;

import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.Neuroid.helper.EnumStandardNeuroidState;

import java.util.List;

/**
 * book "circuits of the mind" chapter 8.2 supervised learning algorithm
 */
public class NeuroidSupervisedLearningAlgorithms {
    public static class Update implements Neuroid.IUpdate<Integer, Integer> {
        @Override
        public void calculateUpdateFunction(Neuroid.NeuroidGraph<Integer, Integer> graph, Neuroid.NeuroidGraph.NeuronNode<Integer, Integer> neuroid, Neuroid.IWeighttypeHelper<Integer> weighttypeHelper) {
            if( neuroid.graphElement.state == EnumStandardNeuroidState.UnsuperviseMemoryFired.ordinal() ) {
                neuroid.graphElement.state = EnumStandardNeuroidState.UnsupervisedMemory1.ordinal();

                for( Neuroid.NeuroidGraph.Edge<Integer, Integer> iterationEdge : graph.graph.getInEdges(neuroid) ) {
                    if( iterationEdge.getSourceNode().graphElement.firing ) {
                        iterationEdge.weight = 1;
                    }
                }
            }
            else if( neuroid.graphElement.state == EnumStandardNeuroidState.UnsupervisedMemory1.ordinal() ) {
                neuroid.graphElement.state = EnumStandardNeuroidState.SupervisedMemory.ordinal();

                // official
                neuroid.graphElement.threshold = neuroid.graphElement.sumOfIncommingWeights;
                // my version which could help in teaching multiple examples?
                //neuroid.graphElement.threshold = Math.min(neuroid.graphElement.threshold, neuroid.graphElement.sumOfIncommingWeights);
            }
            else if( neuroid.graphElement.state == EnumStandardNeuroidState.SupervisedMemory.ordinal() ) {
                neuroid.graphElement.nextFiring = neuroid.graphElement.isStimulated(weighttypeHelper);
            }
            else if( neuroid.graphElement.state == EnumStandardNeuroidState.Relay.ordinal() ) {
                neuroid.graphElement.nextFiring = neuroid.graphElement.isStimulated(weighttypeHelper);
            }
            // else do nothing
        }

        @Override
        public void initialize(Neuroid.NeuroidGraph.NeuronNode<Integer, Integer> neuroid, List<Integer> updatedWeights) {
            //neuroid.graphElement.state = EnumStandardNeuroidState.AvailableMemory.ordinal();
            // TODO
        }
    }
}
