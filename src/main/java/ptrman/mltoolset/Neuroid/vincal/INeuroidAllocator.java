package ptrman.mltoolset.Neuroid.vincal;

import ptrman.mltoolset.Neuroid.Neuroid;

import java.util.Set;

/**
 * Allocates (unused) neurons with a set of contraints which should be satisfied
 */
public interface INeuroidAllocator<WeightType, ModeType> {
    /**
     *
     * @param a set of neuroids for the input a
     * @param b set of neuroids for the input b
     * @param numberOfResultNeuroids
     * @param k how many synapses should at least go from both input sets to the result
     * @return set of neuroids where the constraints are satisfied
     */
    Set<Neuroid.NeuroidGraph.NeuronNode<WeightType, ModeType>> allocateNeuroidsWithConstraintsConnectedToBothInputsAndWithAtLeastNSynapsesToBoth(final Set<Neuroid.NeuroidGraph.NeuronNode<WeightType, ModeType>> a, final Set<Neuroid.NeuroidGraph.NeuronNode<WeightType, ModeType>> b, int numberOfResultNeuroids, int k);
}
