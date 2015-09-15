package ptrman.mltoolset.Neuroid;

/**
 *
 */
public class Neuroid<Weighttype, MetaType> {
    /** \brief indicates if this neuroid is an input neuroid, means that it gets its activation from outside and has no parent connections or theshold and so on */
    public Weighttype threshold;
    public MetaType meta;
    public int state = 0;
    // startstate
    public boolean firing = false;
    public boolean nextFiring = false;
    public boolean[] firingHistory;
    /** \brief indicates if the neuron should fore on the next timestep, is updated by the update function */
    public int remainingLatency = 0;
    /** \brief as long as this is > 0 its mode nor its weights can be changed */
    public Weighttype sumOfIncommingWeights;

    public boolean isStimulated(IWeighttypeHelper<Weighttype> weighttypeHelper) {
        return weighttypeHelper.greaterEqual(sumOfIncommingWeights, threshold);
    }

    public void updateFiring() {
        firing = nextFiring;
        nextFiring = false;
    }
}
