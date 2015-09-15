package ptrman.mltoolset.Neuroid.algorithms;

import ptrman.mltoolset.Neuroid.*;
import ptrman.mltoolset.Neuroid.helper.EnumStandardNeuroidState;

import java.util.List;

/**
 * book "circuits of the mind" chapter 8.2 supervised learning algorithm
 */
public class NeuroidSupervisedLearningAlgorithms {
    public static class Update implements IUpdate<Integer, Integer> {
        @Override
        public void calculateUpdateFunction(INetworkAccessor<Integer, Integer> neuroidNetworkAccessor, NeuronAdress neuronAdress, IWeighttypeHelper<Integer> weighttypeHelper) {
            INeuronAccessor<Integer, Integer> neuroidAccessor = neuroidNetworkAccessor.getNeuroidAccessorByAdress(neuronAdress);
            final int neuroidState = neuroidAccessor.getState();

            if( neuroidState == EnumStandardNeuroidState.UnsuperviseMemoryFired.ordinal() ) {
                neuroidAccessor.setState(EnumStandardNeuroidState.UnsupervisedMemory1.ordinal());

                IEdgesAccessor<Integer, Integer> inEdgesAccessor = neuroidAccessor.getInEdgesAccessor();

                for( int edgeIndex = 0; edgeIndex < inEdgesAccessor.getNumberOfEdges(); edgeIndex++ ) {
                    IEdge<Integer> iterationEdge = inEdgesAccessor.getEdge(edgeIndex);
                    if( neuroidNetworkAccessor.getNeuroidAccessorByAdress(iterationEdge.getSourceAdress()).getFiring() ) {
                        iterationEdge.setWeight(1);
                        neuroidNetworkAccessor.updateEdge(iterationEdge);
                    }
                }
            }
            else if( neuroidState == EnumStandardNeuroidState.UnsupervisedMemory1.ordinal() ) {
                neuroidAccessor.setState(EnumStandardNeuroidState.SupervisedMemory.ordinal());

                // official
                neuroidAccessor.setThreshold(neuroidAccessor.getSumOfIncommingWeights());
                // my version which could help in teaching multiple examples?
                //neuroid.graphElement.threshold = Math.min(neuroid.graphElement.threshold, neuroid.graphElement.sumOfIncommingWeights);
            }
            else if( neuroidState == EnumStandardNeuroidState.SupervisedMemory.ordinal() ) {
                neuroidAccessor.setNextFiring(neuroidAccessor.isStimulated(weighttypeHelper));
            }
            else if( neuroidState == EnumStandardNeuroidState.Relay.ordinal() ) {
                neuroidAccessor.setNextFiring(neuroidAccessor.isStimulated(weighttypeHelper));
            }
            // else do nothing
        }

        @Override
        public void initialize(INetworkAccessor<Integer, Integer> neuroidAccessor, NeuronAdress neuronAdress, List<Integer> updatedWeights) {
            //neuroid.graphElement.state = EnumStandardNeuroidState.AvailableMemory.ordinal();
            // TODO
        }
    }
}
