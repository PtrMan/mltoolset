package ptrman.mltoolset.Neuroid.helper;

import ptrman.mltoolset.Neuroid.EdgeWeightTuple;
import ptrman.mltoolset.Neuroid.NeuronAdress;

import java.util.ArrayList;
import java.util.List;

public class NetworkTopology {
    public static List<EdgeWeightTuple<Float>> getConnectionsForChainBetweenNeurons(final List<NeuronAdress> neuronAdresses, final float weight) {
        List<EdgeWeightTuple<Float>> resultList = new ArrayList<>();

        for( int neuronIndicesIndex = 0; neuronIndicesIndex < neuronAdresses.size()-1; neuronIndicesIndex++ ) {
            final NeuronAdress sourceAdress = neuronAdresses.get(neuronIndicesIndex);
            final NeuronAdress destinationAdress = neuronAdresses.get(neuronIndicesIndex+1);

            resultList.add(new EdgeWeightTuple<>(sourceAdress, destinationAdress, weight));
        }

        return resultList;
    }
}
