
package mltoolset.PIPE.tests;

import mltoolset.PIPE.program.Instruction;

/**
 *
 * 
 */
public class TerminalNumberConstantInstruction implements mltoolset.PIPE.program.Instruction
{
    public TerminalNumberConstantInstruction(float value, int index)
    {
        this.value = value;
        this.index = index;
    }
    
    @Override
    public int getNumberOfParameters()
    {
        return 0;
    }
    
    @Override
    public int getIndex()
    {
        return index;
    }
    
    public float getValue()
    {
        return value;
    }
    
    private float value;
    private int index;

    @Override
    public Instruction getClone()
    {
        return new TerminalNumberConstantInstruction(value, index);
    }
}
