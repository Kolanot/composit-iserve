package es.usc.citius.composit.iserve.match;


import es.usc.citius.composit.core.matcher.graph.AbstractMatchGraph;
import uk.ac.open.kmi.iserve.discovery.disco.LogicConceptMatchType;
import uk.ac.open.kmi.iserve.sal.manager.KnowledgeBaseManager;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class iServePluginKBMatchGraph extends AbstractMatchGraph<URI, LogicConceptMatchType> {
    private KnowledgeBaseManager kb;
    private URI ontologyGraph;

    public iServePluginKBMatchGraph(URI ontologyGraph, KnowledgeBaseManager kb) {
        this.ontologyGraph = ontologyGraph;
        this.kb = kb;
    }

    @Override
    public Set<URI> getElements() {
        return kb.listConcepts(ontologyGraph);
    }

    @Override
    public Map<URI, LogicConceptMatchType> getTargetElementsMatchedBy(URI source) {
        Map<URI, LogicConceptMatchType> matchMapping = new HashMap<URI, LogicConceptMatchType>();
        matchMapping.put(source, LogicConceptMatchType.Exact);
        for(URI superclass : kb.listSuperClasses(source, false)){
            matchMapping.put(superclass, LogicConceptMatchType.Plugin);
        }
        return matchMapping;
    }

    @Override
    public Map<URI, LogicConceptMatchType> getSourceElementsThatMatch(URI target) {
        Map<URI, LogicConceptMatchType> matchMapping = new HashMap<URI, LogicConceptMatchType>();
        matchMapping.put(target, LogicConceptMatchType.Exact);
        for(URI subclass : kb.listSubClasses(target, false)){
            matchMapping.put(subclass, LogicConceptMatchType.Plugin);
        }
        return matchMapping;
    }
}