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

    @Override
    public boolean equals(Object other) {
        if( !(other instanceof NeuronAdress) ) {
            return false;
        }

        NeuronAdress otherNeuronAdress = (NeuronAdress)other;

        return otherNeuronAdress.index == index && otherNeuronAdress.type == type;
    }

    @Override
    public int hashCode() {
        return index + type.ordinal();
    }
}