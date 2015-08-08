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
