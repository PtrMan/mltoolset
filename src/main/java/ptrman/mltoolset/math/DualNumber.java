package ptrman.mltoolset.math;

// see https://en.wikipedia.org/wiki/Automatic_differentiation#Automatic_differentiation_using_dual_numbers
public class DualNumber {
    public double real;
    public double[] diff;
    
    // generalization of addition and subtraction because it is in mathematics a additive group
    public static DualNumber additiveRing(DualNumber left, DualNumber right, int mul) {
        assert left.diff.length == right.diff.length;
        assert Math.abs(mul) == 1; // others values are not valid because the multipication is just used to abstract away the difference between addition and subtraction
        
        DualNumber res = new DualNumber();
        res.real = left.real + right.real * mul;
        res.diff = new double[left.diff.length];
        for(int i=0;i<left.diff.length;i++)   res.diff[i] = left.diff[i] + right.diff[i] * mul;
        return res;
    }
    
    public static DualNumber mul(DualNumber left, DualNumber right) {
        assert left.diff.length == right.diff.length;
        
        DualNumber res = new DualNumber();
        res.real = left.real * right.real;
        res.diff = new double[left.diff.length];
        for(int i=0;i<left.diff.length;i++)   res.diff[i] = left.real*right.diff[i] + left.diff[i]*right.real;
        return res;
    }
    
    public static DualNumber exp(DualNumber val) {
        DualNumber res = new DualNumber();
        res.real = Math.exp(val.real);
        res.diff = new double[val.diff.length];
        for(int i=0;i<val.diff.length;i++)   res.diff[i] = val.diff[i] * Math.exp(val.real);
        return res;
    }
}
