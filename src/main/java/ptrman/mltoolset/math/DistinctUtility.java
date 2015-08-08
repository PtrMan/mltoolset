package ptrman.mltoolset.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DistinctUtility {
    public static List<Integer> getTwoDisjunctNumbers(Random random, int max) {
        return getNDisjunctIntegers(random, 2, max);
    }

    public static List<Integer> getNDisjunctIntegers(Random random, int count, int max) {
        int randomNumber = random.nextInt(max);
        List<Integer> temporaryList = new ArrayList<>();
        temporaryList.add(randomNumber);

        for( int counter = 0; counter < count-1; counter++ ) {
            List<Integer> additionalNumbers = getDisjuctNumbersTo(random, temporaryList, 1, max);
            temporaryList.addAll(additionalNumbers);
        }

        return temporaryList;
    }

    public static List<Integer> getDisjuctNumbersTo(Random random, List<Integer> numbers, int count, int max) {
        List<Integer> disjunctNumbers = new ArrayList<>();

        for( int counter = 0; counter < count; counter++ ) {
            for(;;) {
                int randomNumber = random.nextInt(max);
                if( !numbers.contains(randomNumber) && !disjunctNumbers.contains(randomNumber) ) {
                    disjunctNumbers.add(randomNumber);
                    break;
                }
            }
        }

        return disjunctNumbers;
    }
}