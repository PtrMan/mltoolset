package ptrman.mltoolset.Neuroid;

/**
 *
 */
public interface IEdge<Weighttype> {
    //public final NeuronAdress source;
    //public final NeuronAdress destination;

    //public IEdge(final NeuronAdress source, final NeuronAdress destination) {
        //this.source = source;
        //this.destination = destination;
    //}

    NeuronAdress getSourceAdress();
    NeuronAdress getDestinationAdress();

    void setWeight(Weighttype weight);
    Weighttype getWeight();

    void setState(int state);
}
