package es.usc.citius.composit.iserve;



import com.google.common.collect.ImmutableSet;
import es.usc.citius.composit.core.model.Operation;
import es.usc.citius.composit.core.model.Signature;
import es.usc.citius.composit.core.model.impl.ResourceOperation;
import uk.ac.open.kmi.iserve.sal.exception.ServiceException;
import uk.ac.open.kmi.iserve.sal.manager.ServiceManager;
import uk.ac.open.kmi.msm4j.MessageContent;
import uk.ac.open.kmi.msm4j.MessagePart;
import uk.ac.open.kmi.msm4j.Resource;
import uk.ac.open.kmi.msm4j.Service;

import java.net.URI;
import java.util.*;

public class iServeIndexedOperationTranslator implements OperationTranslator {
    private ServiceManager serviceManager;
    private Map<URI, Operation<URI>> operations = new HashMap<URI, Operation<URI>>();

    public iServeIndexedOperationTranslator(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        for(URI serviceUri : serviceManager.listServices()) {
            System.out.println("Processing " + serviceUri);
            for(Operation<URI> op : getOperationsOfService(serviceUri)){
                operations.put(URI.create(op.getID()), op);
            }
        }
    }

    public Set<URI> loadInputs(uk.ac.open.kmi.msm4j.Operation op){
        Set<URI> models = new HashSet<URI>();
        for(MessageContent c : op.getInputs()){
            models.addAll(getModelReferences(c));
        }
        return models;
    }

    public Set<URI> loadOutputs(uk.ac.open.kmi.msm4j.Operation op){
        Set<URI> models = new HashSet<URI>();
        for(MessageContent c : op.getOutputs()){
            models.addAll(getModelReferences(c));
        }
        return models;
    }


    public Set<URI> getModelReferences(MessageContent msg){
        Set<URI> uris = new HashSet<URI>();
        for(MessagePart p : msg.getMandatoryParts()){
            for(Resource r : p.getModelReferences()){
                uris.add(r.getUri());
            }
        }
        return uris;
    }


    @Override
    public Set<Operation<URI>> getOperations() {
        return ImmutableSet.copyOf(operations.values());
    }

    @Override
    public Operation<URI> getOperation(URI operation) {
        return operations.get(operation);
    }

    @Override
    public Set<Operation<URI>> getOperationsOfService(URI serviceUri) {
        try {
            Service s = serviceManager.getService(serviceUri);
            Set<Operation<URI>> ops = new HashSet<Operation<URI>>();
            for(final uk.ac.open.kmi.msm4j.Operation op : s.getOperations()){
                ops.add(new ResourceOperation<URI>(op.getUri().toString(), new Signature<URI>() {
                    @Override
                    public Set<URI> getInputs() {
                        return loadInputs(op);
                    }

                    @Override
                    public Set<URI> getOutputs() {
                        return loadOutputs(op);
                    }
                }));
            }
            return ops;
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}