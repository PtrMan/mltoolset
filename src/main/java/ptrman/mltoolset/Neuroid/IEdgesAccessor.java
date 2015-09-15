package ptrman.mltoolset.Neuroid;

import java.util.Set;

/**
 *
 */
public interface IEdgesAccessor<Weighttype, Metatype> {
    int getNumberOfEdges();

    IEdge<Weighttype> getEdge(final int index);

    Set<IEdge> asSet();
}
