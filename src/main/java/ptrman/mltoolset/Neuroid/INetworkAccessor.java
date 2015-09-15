package ptrman.mltoolset.Neuroid;

import java.util.List;

/**
 * Abstract the implementation of the network away for more generality.
 *
 * Allows a implementation to use a graph based representation or a implicit graph based representation where the edges are not stored explicitly.
 */
public interface INetworkAccessor<Weighttype, MetaType> {
    void allocateNeurons(final int neuronCount, final int inputCount, final int outputCount);

    INeuronAccessor<Weighttype, MetaType> getNeuroidAccessorByAdress(final NeuronAdress adress);

    void initialize(IWeighttypeHelper<Weighttype> weighttypeHelper, IUpdate<Weighttype, MetaType> update);

    void setActivationOfInputNeuron(final int index, final boolean activation);

    void addEdges(final List<EdgeWeightTuple<Weighttype>> edgeWeightTuples);

    boolean getActivationOfOutputNeuron(final int index);

    void resizeFiringHistory(final int firingHistoryLength);

    void updateFiringHistory();

    void updateNeuronStates(IUpdate<Weighttype, MetaType> update, IWeighttypeHelper<Weighttype> weighttypeHelper);

    void updateIncommingWeigthsForAllNeuroids(IWeighttypeHelper<Weighttype> weighttypeHelper);

    void updateFiringForAllNeuroids();

    void decreaseLatency();

    void updateEdge(IEdge<Weighttype> edge);

    int getNumberOfNeurons(NeuronAdress.EnumType type);
}
