package ptrman.mltoolset.Neuroid.vincal;

/**
 * Allocator for memories ()
 * Is similar to what the hippocampus does
 *
 * Recieves the activiation of neuroid concepts (groups of neuroids) and assigns a other
 * (free) group to it if nothing was assigned.
 *
 * paper except
 * "The Hippocampus as a Stable Memory Allocator for Cortex" Leslie G. Valiant
 * Chapter 6
 *
 The point is that an SMA can be used to fill this gap in the following way. Suppose
 that the set of all the neurons in cortex is regarded as the input layer to the SMA, and
 also as the output layer.  Then the firing of neuron sets A and B in cortex, and hence
 also in the input layer of the SMA, will cause a (stable) set of neurons, which we shall
 call D, to fire in the output layer of the SMA, and hence also in cortex.  This means
 that the SMA has (i) identified a set D, and also that (ii) it gives a way of causing
 D to be fired at will via the SMA during any training process.  This is equivalent to saying
 that memory allocation for A & B  can be realized by first having the SMA identify the
 neuron set D , and then training a circuit located entirely in cortex, with inputs
 A and  B and output D to realize the supervised memorization of
 A  &  B at the node set D .  The  effect will be memory allocation for the chunk
 A  & B  at the node set  D  via a circuit  entirely in cortex.
 *
 */
public class StableMemoryAllocator {
}
