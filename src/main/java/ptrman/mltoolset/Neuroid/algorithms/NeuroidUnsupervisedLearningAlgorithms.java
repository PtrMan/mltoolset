package ptrman.mltoolset.Neuroid.algorithms;

import ptrman.mltoolset.Neuroid.Neuroid;
import ptrman.mltoolset.Neuroid.helper.EnumStandardNeuroidState;

import java.util.List;

/**
 * book "circuits of the mind" chapter 7.2 unsupervised learning algorithm
 */
public class NeuroidUnsupervisedLearningAlgorithms {
    public static class Update implements Neuroid.IUpdate<Integer,Integer> {
        @Override
        public void calculateUpdateFunction(Neuroid.NeuroidGraph<Integer, Integer> graph, Neuroid.NeuroidGraph.NeuronNode<Integer, Integer> neuroid, Neuroid.IWeighttypeHelper<Integer> weighttypeHelper) {
            if( neuroid.graphElement.state == EnumStandardNeuroidState.AvailableMemory.ordinal() ) {
                if( neuroid.graphElement.sumOfIncommingWeights >= 1 ) {
                    neuroid.graphElement.state = EnumStandardNeuroidState.AvailableMemory1.ordinal();
                    neuroid.graphElement.threshold = neuroid.graphElement.sumOfIncommingWeights;

                    for( Neuroid.NeuroidGraph.Edge<Integer, Integer> iterationEdge : graph.graph.getInEdges(neuroid) ) {
                        if( iterationEdge.getSourceNode().graphElement.firing ) {
                            iterationEdge.weight = 2;
                        }
                    }
                }
            }
            else if( neuroid.graphElement.state == EnumStandardNeuroidState.AvailableMemory1.ordinal() ) {
                if (neuroid.graphElement.sumOfIncommingWeights >= 1) {
                    neuroid.graphElement.state = EnumStandardNeuroidState.UnsupervisedMemory.ordinal();
                    neuroid.graphElement.threshold += neuroid.graphElement.sumOfIncommingWeights;

                    for( Neuroid.NeuroidGraph.Edge<Integer, Integer> iterationEdge : graph.graph.getInEdges(neuroid) ) {
                        if( !iterationEdge.getSourceNode().graphElement.firing ) {
                            if( iterationEdge.weight == 1 ) {
                                iterationEdge.weight = 0;
                            }
                            else if( iterationEdge.weight == 2 ) {
                                iterationEdge.weight = 1;
                            }
                        }
                    }
                } else {
                    neuroid.graphElement.state = EnumStandardNeuroidState.AvailableMemory.ordinal();
                    neuroid.graphElement.threshold = 0xffff; // inf

                    for( Neuroid.NeuroidGraph.Edge<Integer, Integer> iterationEdge : graph.graph.getInEdges(neuroid) ) {
                        iterationEdge.weight = 1;
                    }
                }
            }
            else { // EnumNeuroidState.UnsupervisedMemory
                neuroid.graphElement.nextFiring = neuroid.graphElement.isStimulated(weighttypeHelper);

                if (neuroid.graphElement.nextFiring) {
                    neuroid.graphElement.remainingLatency = 1;
                }
            }
        }

        @Override
        public void initialize(Neuroid.NeuroidGraph.NeuronNode<Integer, Integer> neuroid, List<Integer> updatedMode, List<Integer> updatedWeights) {
            neuroid.graphElement.state = EnumStandardNeuroidState.AvailableMemory.ordinal();
        }
    }
}
