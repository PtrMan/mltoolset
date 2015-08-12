package ptrman.mltoolset.misc;

import java.util.List;
import java.util.Random;

/**
 *
 */
public class ListHelper {
    static public<Type> Type chooseRandomElement(List<Type> list, Random random) {
        final int index = random.nextInt(list.size());
        return list.get(index);
    }
}
