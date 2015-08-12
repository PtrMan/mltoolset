package ptrman.mltoolset.Neuroid;

/**
 *
 */
public class IntegerWeightHelper implements Neuroid.IWeighttypeHelper {
    @Override
    public Object getValueForZero() {
        return 0;
    }

    @Override
    public Object getValueForObject(Object value) {
        if( value instanceof Integer ) {
            return value;
        }
        else if( value instanceof Float ) {
            return ((Float)value).intValue();
        }

        throw new InternalError();
    }

    @Override
    public boolean greater(Object left, Object right) {
        final int leftAsFloat = (Integer)left;
        final int rightAsFloat = (Integer)right;

        return leftAsFloat > rightAsFloat;
    }

    @Override
    public boolean greaterEqual(Object left, Object right) {
        final int leftAsFloat = (Integer)left;
        final int rightAsFloat = (Integer)right;

        return leftAsFloat >= rightAsFloat;
    }

    @Override
    public Object add(Object left, Object right) {
        final int leftAsFloat = (Integer)left;
        final int rightAsFloat = (Integer)right;

        return leftAsFloat + rightAsFloat;
    }
}
