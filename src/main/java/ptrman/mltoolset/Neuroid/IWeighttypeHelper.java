package ptrman.mltoolset.Neuroid;

/**
 *
 */
public interface IWeighttypeHelper<Weighttype> {
    // deprecated
    Weighttype getValueForZero();

    Weighttype getValueForObject(Object value);

    boolean greater(Weighttype left, Weighttype right);
    boolean greaterEqual(Weighttype left, Weighttype right);

    Weighttype add(Weighttype left, Weighttype right);
}