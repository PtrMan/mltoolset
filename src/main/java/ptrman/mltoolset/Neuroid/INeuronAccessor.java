package ptrman.mltoolset.Neuroid;

/**
 * Abstracts away the changes on a neuroid neuron
 */
public interface INeuronAccessor<WeightType, MetaType> {
    void setThreshold(WeightType threshold);

    boolean isStimulated(IWeighttypeHelper<WeightType> weighttypeHelper);

    void setNextFiring(boolean nextFiring);
    boolean getNextFiring();

    void setRemainingLatency(int latencyAfterActivation);

    void setState(int state);
    int getState();

    boolean getFiring();

    IEdgesAccessor<WeightType, MetaType> getInEdgesAccessor();

    WeightType getSumOfIncommingWeights();

    WeightType getThreshold();

    MetaType getMeta();

    boolean getFiringHistory(int index);
}
