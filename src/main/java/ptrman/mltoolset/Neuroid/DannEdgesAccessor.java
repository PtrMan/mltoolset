package ptrman.mltoolset.Neuroid;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class DannEdgesAccessor<Weighttype, Metatype> implements IEdgesAccessor<Weighttype, Metatype> {
    private final List<DannNetworkAccessor.Edge<Weighttype, Metatype>> dannEdges;

    public DannEdgesAccessor(List<DannNetworkAccessor.Edge<Weighttype, Metatype>> dannEdges) {
        this.dannEdges = dannEdges;
    }

    @Override
    public int getNumberOfEdges() {
        return dannEdges.size();
    }

    @Override
    public IEdge<Weighttype> getEdge(int index) {
        return dannEdges.get(index);
    }

    @Override
    public Set<IEdge> asSet() {
        return new HashSet<>(dannEdges);
    }
}
