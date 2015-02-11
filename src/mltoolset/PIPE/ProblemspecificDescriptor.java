
package mltoolset.PIPE;

import mltoolset.PIPE.PropabilisticPrototypeTree.Node;

public interface ProblemspecificDescriptor
{
    int getNumberOfArgumentsOfInstruction(mltoolset.PIPE.program.Instruction selectedInstruction);
    
    public mltoolset.PIPE.program.Instruction createTerminalNode(float randomConstant);

    public mltoolset.PIPE.program.Instruction getInstructionByIndex(int selectedInstruction);

    public float createTerminalNodeFromProblemdependSet();
    
    // lower fitness is better
    public float getFitnessOfProgram(mltoolset.PIPE.program.Program program);
    

    public float getNumberOfInstructions();


    public Node createPptNode();
    
}
