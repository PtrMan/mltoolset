package ptrman.mltoolset.Neuroid;

/**
 *
 */
public class FloatWeightHelper implements IWeighttypeHelper {
    @Override
    public Object getValueForZero() {
        return 0.0f;
    }

    @Override
    public Object getValueForObject(Object value) {
        if( value instanceof Integer ) {
            return ((Integer)value).floatValue();
        }
        else if( value instanceof Float ) {
            return value;
        }

        throw new InternalError();
    }

    @Override
    public boolean greater(Object left, Object right) {
        final float leftAsFloat = (Float)left;
        final float rightAsFloat = (Float)right;

        return leftAsFloat > rightAsFloat;
    }

    @Override
    public boolean greaterEqual(Object left, Object right) {
        final float leftAsFloat = (Float)left;
        final float rightAsFloat = (Float)right;

        return leftAsFloat >= rightAsFloat;
    }

    @Override
    public Object add(Object left, Object right) {
        final float leftAsFloat = (Float)left;
        final float rightAsFloat = (Float)right;

        return leftAsFloat + rightAsFloat;
    }
}
