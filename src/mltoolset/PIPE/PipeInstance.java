package mltoolset.PIPE;

import java.util.ArrayList;
import java.util.Random;
import mltoolset.PIPE.program.Program;

/**
 * PIPE algorithm
 * after the paper
 * "Probabilistic Incremental Program Evolution"
 */
public class PipeInstance
{
    public void work(Parameters parameters, ProblemspecificDescriptor problemspecificDescriptor)
    {
        this.parameters = parameters;
        this.problemspecificDescriptor = problemspecificDescriptor;
        
        rootnode = problemspecificDescriptor.createPptNode();
        
        for(;;)
        {
            generationBasedLearningIteration();
            
            int x = 0;
        }
    }
    
    
    private void createProgramNodeFromPptNode(mltoolset.PIPE.PropabilisticPrototypeTree.Node pptNode, mltoolset.PIPE.program.Node programNode)
    {
        int selectedInstruction;

        selectedInstruction = selectInstructionIndexFromPptNode(pptNode);

        if (selectedInstruction != -1)
        {
            programNode.setInstruction(problemspecificDescriptor.getInstructionByIndex(selectedInstruction));
        }

        // special handling for grc
        if (selectedInstruction == -1)
        {
            if (pptNode.randomConstant > parameters.randomThreshold)
            {
                programNode.setInstruction(problemspecificDescriptor.createTerminalNode(pptNode.randomConstant));
            }
            else
            {
                programNode.setInstruction(problemspecificDescriptor.createTerminalNode(problemspecificDescriptor.createTerminalNodeFromProblemdependSet()));
            }
        }
        else
        {
            int i;

            for( i = 0; i < problemspecificDescriptor.getNumberOfArgumentsOfInstruction(problemspecificDescriptor.getInstructionByIndex(selectedInstruction)); i++ )
            {
                mltoolset.PIPE.program.Node createdProgramNode;

                // create node if it is not present
                if (pptNode.childrens.size() < i + 1)
                {
                    pptNode.childrens.add(createPptNode());
                }
                
                if (pptNode.childrens.get(i) == null)
                {
                    pptNode.childrens.set(i, createPptNode());
                }
                
                createdProgramNode = new mltoolset.PIPE.program.Node();
                
                mltoolset.misc.Assert.Assert(programNode.getChildrens().size() >= i, "");
                if( programNode.getChildrens().size() == i )
                {
                    programNode.getChildrens().add(null);
                }
                
                programNode.getChildrens().set(i, createdProgramNode);
                
                // call recursivly for the childnodes
                createProgramNodeFromPptNode(pptNode.childrens.get(i), programNode.getChildrens().get(i));
            }
        }
    }

    private int selectInstructionIndexFromPptNode(mltoolset.PIPE.PropabilisticPrototypeTree.Node pptNode)
    {
        float randomValue;
        float accumulated;
        int i;
        
        mltoolset.misc.Assert.Assert(pptNode.propabilityVector.length > 0, "");
        
        randomValue = random.nextFloat();
        accumulated = 0.0f;
        
        for (i = 0; i < pptNode.propabilityVector.length; i++)
        {
            accumulated += pptNode.propabilityVector[i];
            
            mltoolset.misc.Assert.Assert(accumulated > 0.0f, "");
            
            if (accumulated > randomValue)
            {
                return i;
            }
        }

        return -1; // in case of grc
    }

    private mltoolset.PIPE.PropabilisticPrototypeTree.Node createPptNode()
    {
        return problemspecificDescriptor.createPptNode();
    }

    private static float calcProgramPropability(mltoolset.PIPE.PropabilisticPrototypeTree.Node pptNode, mltoolset.PIPE.program.Node programNode)
    {
        float propability;
        int i;

        propability = pptNode.propabilityVector[programNode.getInstruction().getIndex()];

        for( i = 0; i < programNode.getChildrens().size(); i++ )
        {
            propability *= calcProgramPropability(pptNode.childrens.get(i), programNode.getChildrens().get(i));
        }

        return propability;
    }

    private void increaseProgramPropability(mltoolset.PIPE.PropabilisticPrototypeTree.Node pptNode, mltoolset.PIPE.program.Node programNode)
    {
        int i;
        float propabilityDelta;

        propabilityDelta = (parameters.learningRate * parameters.learningRateConstant * (1.0f - pptNode.propabilityVector[programNode.getInstruction().getIndex()]));
        pptNode.propabilityVector[programNode.getInstruction().getIndex()] += propabilityDelta;

        for (i = 0; i < problemspecificDescriptor.getInstructionByIndex(programNode.getInstruction().getIndex()).getNumberOfParameters(); i++)
        {
            increaseProgramPropability(pptNode.childrens.get(i), programNode.getChildrens().get(i));
        }
    }

    private void increaseNodePropabilitiesUntilTargetPropabilityIsReached(mltoolset.PIPE.PropabilisticPrototypeTree.Node pptNode, mltoolset.PIPE.program.Node programNode, float targetPropability)
    {
        float currentPropability;
        
        currentPropability = calcProgramPropability(pptNode, programNode);

        while (currentPropability < targetPropability)
        {
            increaseProgramPropability(pptNode, programNode);
            currentPropability = calcProgramPropability(pptNode, programNode);
        }
    }

    private void normalizePropabilities(mltoolset.PIPE.PropabilisticPrototypeTree.Node pptNode, mltoolset.PIPE.program.Node programNode)
    {
        float propabilitySum;
        float oldPropability;
        float gamma;
        int i;

        propabilitySum = pptNode.getSumOfPropabilities();
        oldPropability = pptNode.propabilityVector[programNode.getInstruction().getIndex()] - (propabilitySum - 1.0f);
        gamma = calcGamma(pptNode, programNode, oldPropability);

        for (i = 0; i < pptNode.propabilityVector.length; i++)
        {
            if (i != programNode.getInstruction().getIndex())
            {
                pptNode.propabilityVector[i] *= (1.0f - gamma);
                mltoolset.misc.Assert.Assert(pptNode.propabilityVector[i] <= 1.0f && pptNode.propabilityVector[i] >= 0.0f, "");
            }
        }

        for (i = 0; i < problemspecificDescriptor.getInstructionByIndex(programNode.getInstruction().getIndex()).getNumberOfParameters(); i++)
        {
            normalizePropabilities(pptNode.childrens.get(i), programNode.getChildrens().get(i));
        }
    }

    private static float calcGamma(mltoolset.PIPE.PropabilisticPrototypeTree.Node pptNode, mltoolset.PIPE.program.Node programNode, float oldPropability)
    {
        if (oldPropability == 1.0f)
        {
            return 0.0f;
        }
        else
        {
            return (pptNode.propabilityVector[programNode.getInstruction().getIndex()] - oldPropability) / (1.0f - oldPropability);
        }
    }

    private void mutationOfPrototypeTree(mltoolset.PIPE.program.Program bestProgram)
    {
        float mutationPropability;
        float bestRating;
        
        // ASK< redudant ? >
        bestRating = problemspecificDescriptor.getFitnessOfProgram(bestProgram);

        mutationPropability = parameters.mutationPropability / (problemspecificDescriptor.getNumberOfInstructions() * (float) Math.sqrt(bestRating));

        mutateProgram(bestProgram, mutationPropability);
    }

    private void mutateProgram(mltoolset.PIPE.program.Program bestProgram, float mutationPropability)
    {
        mutateNode(bestProgram.entry, mutationPropability);
    }
    
    private void mutateNode(mltoolset.PIPE.program.Node bestProgram, float mutationPropability)
    {
        boolean changed;
        int i;

        changed = false;

        for (i = 0; i < problemspecificDescriptor.getNumberOfInstructions(); i++)
        {
            boolean mutate;

            mutate = mutationPropability < random.nextFloat();
            
            if( mutate )
            {
                rootnode.propabilityVector[i] += (parameters.mutationRate * (1.0f - rootnode.propabilityVector[i]));
            }
            
            changed |= mutate;
        }
        
        // normalize
        if( changed )
        {
            rootnode.normalizePropabilities();
        }
        
        for( mltoolset.PIPE.program.Node iterationNode : bestProgram.getChildrens() )
        {
            if( iterationNode != null )
            {
                mutateNode(iterationNode, mutationPropability);
            }
        }
    }
    
    // 4.3.3 Generation-Based Learning
    private void generationBasedLearningIteration()
    {
        int i;
        ArrayList<mltoolset.PIPE.program.Program> population;
        Program bestProgram;
        
        population = new ArrayList<>();
        
        // (1) creation of program population
        
        mltoolset.misc.Assert.Assert(parameters.populationSize > 0, "populationsize must be >= 1");
        for( i = 0; i < parameters.populationSize; i++ )
        {
            mltoolset.PIPE.program.Program createdProgram;
            
            createdProgram = new Program();
            createdProgram.entry = new mltoolset.PIPE.program.Node();
            createProgramNodeFromPptNode(rootnode, createdProgram.entry);
            population.add(createdProgram);
        }
        
        // (2) population evaluation
        
        for( Program iterationProgram : population )
        {
            iterationProgram.fitness = problemspecificDescriptor.getFitnessOfProgram(iterationProgram);
        }
        
        bestProgram = population.get(0);
        for( Program iterationProgram : population )
        {
            if( iterationProgram.fitness < bestProgram.fitness )
            {
                bestProgram = iterationProgram;
            }
        }
        
        if( elitist == null )
        {
            elitist = bestProgram;
        }
        else
        {
            if( bestProgram.fitness < elitist.fitness )
            {
                elitist = bestProgram;
            }
        }
        
        System.out.println(elitist.fitness);
        
        // (3) learning from population

        // TODO< belongs into own method >
        float targetPropability;
        
        float currentPropability;

        currentPropability = calcProgramPropability(rootnode, bestProgram.entry);
        
        targetPropability = currentPropability + (1.0f - currentPropability) * parameters.learningRate *  ((parameters.epsilon+elitist.fitness)/(parameters.epsilon+bestProgram.fitness));
        
        increaseNodePropabilitiesUntilTargetPropabilityIsReached(rootnode, bestProgram.entry, targetPropability);
        normalizePropabilities(rootnode, bestProgram.entry);
        // TODO< learn constants >
        
        // (4) mutation of prototype tree
        mutationOfPrototypeTree(bestProgram);
        
        // (5) prototype tree pruning
        // TODO
        
    }
    
    // is at the beginning null
    private mltoolset.PIPE.program.Program elitist;

    private mltoolset.PIPE.PropabilisticPrototypeTree.Node rootnode;

    private ProblemspecificDescriptor problemspecificDescriptor;
    private Parameters parameters;

    private Random random = new Random();

}
