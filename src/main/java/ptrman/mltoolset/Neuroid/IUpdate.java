package ptrman.mltoolset.Neuroid;

import java.util.List;

/**
 *
 */
public interface IUpdate<Weighttype, MetaType> {
    void calculateUpdateFunction(INetworkAccessor<Weighttype, MetaType> neuroidAccessor, final NeuronAdress neuronAdress, IWeighttypeHelper<Weighttype> weighttypeHelper);

    void initialize(INetworkAccessor<Weighttype, MetaType> neuroidAccessor, final NeuronAdress neuronAdress, List<Weighttype> updatedWeights);
}
