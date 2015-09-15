package ptrman.mltoolset.Neuroid;

/**
 *
 */
public class NeuronAdress {
    public final int index;
    public final EnumType type;

    public enum EnumType {
        INPUT,
        HIDDEN,
        OUTPUT
    }

    public NeuronAdress(int index, EnumType type) {
        this.index = index;
        this.type = type;
    }
}