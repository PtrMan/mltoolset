package ptrman.mltoolset.Neuroid.helper.networkStructure;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TestMultilevelPermutation {
    @Test
    public void permutationForwardBackward() {
        MultilevelPermutation.Permutation testPermutation = MultilevelPermutation.createPermutation(new int[]{2,1,3,0});

        Assert.assertEquals(testPermutation.forward[0], 2);
        Assert.assertEquals(testPermutation.forward[1], 1);
        Assert.assertEquals(testPermutation.forward[2], 3);
        Assert.assertEquals(testPermutation.forward[3], 0);

        Assert.assertEquals(testPermutation.backward[0], 3);
        Assert.assertEquals(testPermutation.backward[1], 1);
        Assert.assertEquals(testPermutation.backward[2], 0);
        Assert.assertEquals(testPermutation.backward[3], 2);
    }
}
