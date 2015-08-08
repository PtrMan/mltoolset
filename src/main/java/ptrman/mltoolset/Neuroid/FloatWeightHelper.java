package ptrman.mltoolset.Neuroid;

/**
 *
 */
public class FloatWeightHelper implements Neuroid.IWeighttypeHelper {
    @Override
    public Object getValueForZero() {
        return 0.0f;
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
