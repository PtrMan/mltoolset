package ptrman.mltoolset.Neuroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class DannNeuronAccessor<WeightType, MetaType> implements INeuronAccessor<WeightType, MetaType> {
    private final DannNetworkAccessor<WeightType, MetaType> networkAccessor;

    public DannNeuronAccessor(Neuroid neuroid, DannNetworkAccessor<WeightType, MetaType> networkAccessor) {
        this.neuroid = neuroid;
        this.networkAccessor = networkAccessor;
    }

    @Override
    public void setThreshold(WeightType threshold) {
        neuroid.threshold = threshold;
    }

    @Override
    public boolean isStimulated(IWeighttypeHelper<WeightType> weighttypeHelper) {
        return neuroid.isStimulated(weighttypeHelper);
    }

    @Override
    public void setNextFiring(boolean nextFiring) {
        neuroid.nextFiring = nextFiring;
    }

    @Override
    public boolean getNextFiring() {
        return neuroid.nextFiring;
    }

    @Override
    public void setRemainingLatency(int latencyAfterActivation) {
        neuroid.remainingLatency = latencyAfterActivation;
    }

    @Override
    public void setState(int state) {
        neuroid.state = state;
    }

    @Override
    public int getState() {
        return neuroid.state;
    }

    @Override
    public boolean getFiring() {
        return neuroid.firing;
    }

    @Override
    public IEdgesAccessor<WeightType, MetaType> getInEdgesAccessor() {
        Set<DannNetworkAccessor.Edge<WeightType, MetaType>> inEdgesSet = networkAccessor.getInEdgesOfNeuron(neuroid);
        List<DannNetworkAccessor.Edge<WeightType, MetaType>> inEdgesList = new ArrayList<>();
        inEdgesList.addAll(inEdgesSet);

        return new DannEdgesAccessor<>(inEdgesList);
    }

    @Override
    public WeightType getSumOfIncommingWeights() {
        return neuroid.sumOfIncommingWeights;
    }

    @Override
    public WeightType getThreshold() {
        return neuroid.threshold;
    }

    @Override
    public MetaType getMeta() {
        return neuroid.meta;
    }

    @Override
    public boolean getFiringHistory(int index) {
        return neuroid.firingHistory[index];
    }

    private Neuroid<WeightType, MetaType> neuroid;
}
