package ptrman.mltoolset.Neuroid.algorithms;

import ptrman.mltoolset.Neuroid.*;
import ptrman.mltoolset.Neuroid.helper.EnumStandardNeuroidState;

import java.util.List;

/**
 * book "circuits of the mind" chapter 7.2 unsupervised learning algorithm
 */
public class NeuroidUnsupervisedLearningAlgorithms {
    public static class Update implements IUpdate<Integer,Integer> {
        @Override
        public void calculateUpdateFunction(INetworkAccessor<Integer, Integer> neuroidNetworkAccessor, NeuronAdress neuronAdress, IWeighttypeHelper<Integer> weighttypeHelper) {
            INeuronAccessor<Integer, Integer> neuroidAccessor = neuroidNetworkAccessor.getNeuroidAccessorByAdress(neuronAdress);
            final int neuroidState = neuroidAccessor.getState();

            if( neuroidState == EnumStandardNeuroidState.AvailableMemory.ordinal() ) {
                if( neuroidAccessor.getSumOfIncommingWeights() >= 1 ) {
                    neuroidAccessor.setState(EnumStandardNeuroidState.AvailableMemory1.ordinal());
                    neuroidAccessor.setThreshold(neuroidAccessor.getSumOfIncommingWeights());

                    IEdgesAccessor<Integer, Integer> inEdgesAccessor = neuroidAccessor.getInEdgesAccessor();

                    for( int edgeIndex = 0; edgeIndex < inEdgesAccessor.getNumberOfEdges(); edgeIndex++ ) {
                        IEdge<Integer> iterationEdge = inEdgesAccessor.getEdge(edgeIndex);
                        final NeuronAdress iterationNeuroidSourceAdress = iterationEdge.getSourceAdress();
                        if( neuroidNetworkAccessor.getNeuroidAccessorByAdress(iterationNeuroidSourceAdress).getFiring() ) {
                            iterationEdge.setWeight(2);
                            neuroidNetworkAccessor.updateEdge(iterationEdge);
                        }
                    }
                }
            }
            else if( neuroidState == EnumStandardNeuroidState.AvailableMemory1.ordinal() ) {
                if (neuroidAccessor.getSumOfIncommingWeights() >= 1) {
                    neuroidAccessor.setState(EnumStandardNeuroidState.UnsupervisedMemory.ordinal());
                    neuroidAccessor.setThreshold(neuroidAccessor.getThreshold() + neuroidAccessor.getSumOfIncommingWeights());

                    IEdgesAccessor<Integer, Integer> inEdgesAccessor = neuroidAccessor.getInEdgesAccessor();

                    for( int edgeIndex = 0; edgeIndex < inEdgesAccessor.getNumberOfEdges(); edgeIndex++ ) {
                        IEdge<Integer> iterationEdge = inEdgesAccessor.getEdge(edgeIndex);

                        if( !neuroidNetworkAccessor.getNeuroidAccessorByAdress(iterationEdge.getSourceAdress()).getFiring() ) {
                            if( iterationEdge.getWeight() == 1 ) {
                                iterationEdge.setWeight(0);
                            }
                            else if( iterationEdge.getWeight() == 2 ) {
                                iterationEdge.setWeight(1);
                            }

                            neuroidNetworkAccessor.updateEdge(iterationEdge);
                        }
                    }
                } else {
                    neuroidAccessor.setState(EnumStandardNeuroidState.AvailableMemory.ordinal());
                    neuroidAccessor.setThreshold(0xffff); // inf

                    IEdgesAccessor<Integer, Integer> inEdgesAccessor = neuroidAccessor.getInEdgesAccessor();

                    for( int edgeIndex = 0; edgeIndex < inEdgesAccessor.getNumberOfEdges(); edgeIndex++ ) {
                        IEdge<Integer> iterationEdge = inEdgesAccessor.getEdge(edgeIndex);
                        iterationEdge.setWeight(1);
                        neuroidNetworkAccessor.updateEdge(iterationEdge);
                    }
                }
            }
            else { // EnumNeuroidState.UnsupervisedMemory
                neuroidAccessor.setNextFiring(neuroidAccessor.isStimulated(weighttypeHelper));

                if (neuroidAccessor.getNextFiring()) {
                    neuroidAccessor.setRemainingLatency(1);
                }
            }
        }

        @Override
        public void initialize(INetworkAccessor<Integer, Integer> neuroidAccessor, NeuronAdress neuronAdress, List<Integer> updatedWeights) {
            neuroidAccessor.getNeuroidAccessorByAdress(neuronAdress).setState(EnumStandardNeuroidState.AvailableMemory.ordinal());
        }
    }
}
