package mltoolset.PIPE.program;

import java.util.ArrayList;

/**
 *
 * 
 */
public class Node
{
    public void setInstruction(Instruction instruction)
    {
        this.instruction = instruction;
    }
    
    public Instruction getInstruction()
    {
        return instruction;
    }
    
    public ArrayList<Node> getChildrens()
    {
        return childrens;
    }
    
    
    public int getNumberOfNodesRecursive()
    {
        int numberOfNodes;
        
        numberOfNodes = 1;
        
        for( mltoolset.PIPE.program.Node iterationNode : getChildrens() )
        {
            numberOfNodes += iterationNode.getNumberOfNodesRecursive();
        }
        
        return numberOfNodes;
    }
    
    private ArrayList<Node> childrens = new ArrayList<>();
    private Instruction instruction = null;
}
