package mltoolset.PIPE.tests;

import mltoolset.PIPE.Parameters;
import mltoolset.PIPE.PipeInstance;

public class SimpleMath
{
    public static void main(String[] args) 
    {
        test();
    }
    
    public static void test()
    {
        Parameters parameters;
        ProblemspecificDescriptorForTest problemspecificDescriptor;
        
        parameters = new Parameters();
        parameters.populationSize = 5;
        parameters.learningRate = 0.3f;
        parameters.learningRateConstant = 0.1f; // like in paper
        parameters.mutationPropability = 0.08f;
        parameters.mutationRate = 0.1f;
        parameters.epsilon = 0.01f;
        parameters.randomThreshold = 0.5f;
        
        problemspecificDescriptor = new ProblemspecificDescriptorForTest();
        
        PipeInstance pipeInstance = new PipeInstance();
        pipeInstance.work(parameters, problemspecificDescriptor);
    }
}
