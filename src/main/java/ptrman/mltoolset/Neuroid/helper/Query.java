package ptrman.mltoolset.Neuroid.helper;

import ptrman.mltoolset.Neuroid.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Functionality to search Neuroids after criteria based on Neuroid, subsets and edges/synapses
 *
 * Used to hide Neuroid implementation details (CPU, GPU) (index access, OOP references)
 */
public class Query {


    public static abstract class QueryCommand<Weighttype, Modetype> {
    }

    public static class GetInEdgesByNeuroidIndexQueryCommand<Weighttype, Modetype> extends QueryCommand<Weighttype, Modetype> {
        public final int neuroidIndex;

        public GetInEdgesByNeuroidIndexQueryCommand(int neuroidIndex) {
            this.neuroidIndex = neuroidIndex;
        }
    }

    // TODO< add a Index version for gpu support >
    public static class FilterEdgeSourceQueryCommand<Weighttype, Modetype> extends QueryCommand<Weighttype, Modetype> {
        public final Set<NeuronAdress> filteringNeuroidSet;

        public FilterEdgeSourceQueryCommand(final Set<NeuronAdress> filteringNeuroidSet) {
            this.filteringNeuroidSet = filteringNeuroidSet;
        }
    }

    public static abstract class FilterEdgeByCondition<Weighttype, Modetype> extends QueryCommand<Weighttype, Modetype> {
        public abstract boolean query(IEdge edge);
    }

    public static class QueryResult<Weighttype, Modetype> {
        public final Set<Network<Weighttype, Modetype>> neuroidSet;
        public final Set<IEdge<Weighttype>> edgesSet;

        public QueryResult(Set<Network<Weighttype, Modetype>> neuroidSet, Set<IEdge<Weighttype>> edgesSet) {
            this.neuroidSet = neuroidSet;
            this.edgesSet = edgesSet;
        }
    }

    public static<Weighttype, Modetype> QueryResult<Weighttype, Modetype> query(final List<QueryCommand<Weighttype, Modetype>> commands, Network<Weighttype, Modetype> network) {
        Set<Network<Weighttype, Modetype>> workingNeuroidSet = null;
        Set<IEdge> workingEdgesSet = null;

        for( final QueryCommand<Weighttype, Modetype> currentCommand : commands ) {
            if( currentCommand instanceof GetInEdgesByNeuroidIndexQueryCommand) {
                GetInEdgesByNeuroidIndexQueryCommand<Weighttype, Modetype> castedCurrentCommand = (GetInEdgesByNeuroidIndexQueryCommand<Weighttype, Modetype>)currentCommand;

                INetworkAccessor<Weighttype, Modetype> networkAccessor = network.getNetworkAccessor();
                INeuronAccessor<Weighttype, Modetype> neuroidAccessor = networkAccessor.getNeuroidAccessorByAdress(new NeuronAdress(castedCurrentCommand.neuroidIndex, NeuronAdress.EnumType.HIDDEN));
                workingEdgesSet = neuroidAccessor.getInEdgesAccessor().asSet();
            }
            else if( currentCommand instanceof FilterEdgeSourceQueryCommand ) {
                FilterEdgeSourceQueryCommand<Weighttype, Modetype> castedCurrentCommand = (FilterEdgeSourceQueryCommand<Weighttype, Modetype>)currentCommand;

                final Set<IEdge> queryEdges = workingEdgesSet;
                workingEdgesSet = new HashSet<>();

                for( final IEdge iterationEdge : queryEdges ) {
                    if( castedCurrentCommand.filteringNeuroidSet.contains(iterationEdge.getSourceAdress()) ) {
                        workingEdgesSet.add(iterationEdge);
                    }
                }
            }
            else if( currentCommand instanceof FilterEdgeByCondition) {
                FilterEdgeByCondition<Weighttype, Modetype> castedCurrentCommand = (FilterEdgeByCondition<Weighttype, Modetype>)currentCommand;

                final Set<IEdge> queryEdges = workingEdgesSet;
                workingEdgesSet = new HashSet<>();

                for( final IEdge iterationEdge : queryEdges ) {
                    if( castedCurrentCommand.query(iterationEdge) ) {
                        workingEdgesSet.add(iterationEdge);
                    }
                }
            }

            // TODO< command to set the weight of the edges to a specific value >


            else {
                throw new InternalError();
            }
        }

        return new QueryResult(workingNeuroidSet, workingEdgesSet);
    }
}
