package com.microsoft.azure.servicebus.primitives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RequestResponseLinkcache 
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(RequestResponseLinkcache.class);
    
    private Object lock = new Object();
    private final MessagingFactory underlyingFactory;    
    private HashMap<String, RequestResponseLinkWrapper> pathToRRLinkMap;
    
    public RequestResponseLinkcache(MessagingFactory underlyingFactory)
    {
        this.underlyingFactory = underlyingFactory;
        this.pathToRRLinkMap = new HashMap<>();
    }
    
    public CompletableFuture<RequestResponseLink> obtainRequestResponseLinkAsync(String entityPath)
    {
        RequestResponseLinkWrapper wrapper;
        synchronized (lock)
        {
            wrapper = this.pathToRRLinkMap.get(entityPath);
            if(wrapper == null)
            {
                wrapper = new RequestResponseLinkWrapper(this.underlyingFactory, entityPath);
                this.pathToRRLinkMap.put(entityPath, wrapper);            
            }
        }
        return wrapper.acquireReferenceAsync();
    }
    
    public void releaseRequestResponseLink(String entityPath)
    {
        RequestResponseLinkWrapper wrapper;
        synchronized (lock)
        {
            wrapper = this.pathToRRLinkMap.get(entityPath);
        }
        if(wrapper != null)
        {
            wrapper.releaseReference();
        }
    }
    
    public CompletableFuture<Void> freeAsync()
    {
        TRACE_LOGGER.info("Closing all cached request-response links");
        ArrayList<CompletableFuture<Void>> closeFutures = new ArrayList<>();
        for(RequestResponseLinkWrapper wrapper : this.pathToRRLinkMap.values())
        {
            closeFutures.add(wrapper.forceCloseAsync());
        }
        
        this.pathToRRLinkMap.clear();
        return CompletableFuture.allOf(closeFutures.toArray(new CompletableFuture[0]));
    }
    
    private void removeWrapperFromCache(String entityPath)
    {
        synchronized (lock)
        {
            this.pathToRRLinkMap.remove(entityPath);
        }
    }
    
    private class RequestResponseLinkWrapper
    {
        private Object lock = new Object();
        private final MessagingFactory underlyingFactory;
        private final String entityPath;
        private RequestResponseLink requestResponseLink;
        private int referenceCount;
        private ArrayList<CompletableFuture<RequestResponseLink>> waiters;
        
        public RequestResponseLinkWrapper(MessagingFactory underlyingFactory, String entityPath)
        {
            this.underlyingFactory = underlyingFactory;
            this.entityPath = entityPath;
            this.requestResponseLink = null;
            this.referenceCount = 0;
            this.waiters = new ArrayList<>();
            this.createRequestResponseLinkAsync();
        }
        
        private void createRequestResponseLinkAsync()
        {
            String requestResponseLinkPath = RequestResponseLink.getManagementNodeLinkPath(this.entityPath);
            TRACE_LOGGER.debug("Creating requestresponselink to '{}'", requestResponseLinkPath);
            RequestResponseLink.createAsync(this.underlyingFactory, StringUtil.getShortRandomString() + "-RequestResponse", requestResponseLinkPath).handleAsync((rrlink, ex) ->
            {
                synchronized (this.lock)
                {
                    if(ex == null)
                    {
                        TRACE_LOGGER.info("Created requestresponselink to '{}'", requestResponseLinkPath);
                        this.requestResponseLink = rrlink;
                        for(CompletableFuture<RequestResponseLink> waiter : this.waiters)
                        {
                            this.referenceCount++;
                            waiter.complete(this.requestResponseLink);
                        }
                    }
                    else
                    {
                        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(ex);
                        TRACE_LOGGER.error("Creating requestresponselink to '{}' failed.", requestResponseLinkPath, cause);
                        RequestResponseLinkcache.this.removeWrapperFromCache(this.entityPath);
                        for(CompletableFuture<RequestResponseLink> waiter : this.waiters)
                        {
                            waiter.completeExceptionally(cause);
                        }
                    }
                }
                
                return null;
            });
        }
        
        public CompletableFuture<RequestResponseLink> acquireReferenceAsync()
        {
            synchronized (this.lock)
            {
                if(this.requestResponseLink == null)
                {
                    CompletableFuture<RequestResponseLink> waiter = new CompletableFuture<>();
                    this.waiters.add(waiter);
                    return waiter;
                }
                else
                {
                    this.referenceCount++;
                    return CompletableFuture.completedFuture(this.requestResponseLink);
                }
            }
        }
        
        public void releaseReference()
        {            
            synchronized (this.lock)
            {
                if(--this.referenceCount == 0)
                {
                    RequestResponseLinkcache.this.removeWrapperFromCache(this.entityPath);
                    TRACE_LOGGER.info("Closing requestresponselink to '{}'", this.requestResponseLink.getLinkPath());
                    this.requestResponseLink.closeAsync();
                }
            }
        }
        
        public CompletableFuture<Void> forceCloseAsync()
        {
            TRACE_LOGGER.info("Force closing requestresponselink to '{}'", this.requestResponseLink.getLinkPath());
            return this.requestResponseLink.closeAsync();
        }
    }
}
