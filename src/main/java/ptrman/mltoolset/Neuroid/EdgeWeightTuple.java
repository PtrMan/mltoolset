package ptrman.mltoolset.Neuroid;

/**
 * Describes an edge
 */
public class EdgeWeightTuple<Weighttype> {
    public final NeuronAdress sourceAdress;
    public final NeuronAdress destinationAdress;
    public final Weighttype weight;

    public EdgeWeightTuple(final NeuronAdress sourceIndex, final NeuronAdress destinationIndex, final Weighttype weight) {
        this.sourceAdress = sourceIndex;
        this.destinationAdress = destinationIndex;
        this.weight = weight;
    }
}