package mltoolset.PIPE.program;

/**
 *
 */
public interface Instruction
{
    public int getNumberOfParameters();
    
    public int getIndex();
    
    public Instruction getClone();
}
