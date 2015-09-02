package ptrman.mltoolset.misc;

import java.util.Random;
import java.util.Set;

/**
 * Created by r0b3 on 02.09.15.
 */
public class SetHelper {
    static public<Type> Type chooseRandomElement(Set<Type> set, Random random) {
        final int index = random.nextInt(set.size());

        int i = 0;

        // slow for large sets
        for(Type obj : set) {
            if (i == index) {
                return obj;
            }
            i++;
        }

        // should be unreachable!
        throw new InternalError();
    }
}
