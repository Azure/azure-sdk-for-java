package com.microsoft.azure.servicebus.primitives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.amqp.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RequestResponseLinkCache
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(RequestResponseLinkCache.class);

    private Object lock = new Object();
    private final MessagingFactory underlyingFactory;
    private HashMap<String, RequestResponseLinkWrapper> pathToRRLinkMap;

    public RequestResponseLinkCache(MessagingFactory underlyingFactory)
    {
        this.underlyingFactory = underlyingFactory;
        this.pathToRRLinkMap = new HashMap<>();
    }

    public CompletableFuture<RequestResponseLink> obtainRequestResponseLinkAsync(String entityPath, String transferEntityPath, MessagingEntityType entityType)
    {
        RequestResponseLinkWrapper wrapper;
        String mapKey;
        if (transferEntityPath != null)
        {
            mapKey = entityPath + ":" + transferEntityPath;
        }
        else
        {
            mapKey = entityPath;
        }

        synchronized (lock)
        {
            wrapper = this.pathToRRLinkMap.get(mapKey);
            if(wrapper == null)
            {
                wrapper = new RequestResponseLinkWrapper(this.underlyingFactory, entityPath, transferEntityPath, entityType);
                this.pathToRRLinkMap.put(mapKey, wrapper);
            }
        }
        return wrapper.acquireReferenceAsync();
    }

    public void releaseRequestResponseLink(String entityPath, String transferEntityPath)
    {
        String mapKey;
        if (transferEntityPath != null)
        {
            mapKey = entityPath + ":" + transferEntityPath;
        }
        else
        {
            mapKey = entityPath;
        }

        RequestResponseLinkWrapper wrapper;
        synchronized (lock)
        {
            wrapper = this.pathToRRLinkMap.get(mapKey);
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
        private final String transferEntityPath;
        private final MessagingEntityType entityType;
        private RequestResponseLink requestResponseLink;
        private int referenceCount;
        private ArrayList<CompletableFuture<RequestResponseLink>> waiters;
        private boolean isClosed;

        public RequestResponseLinkWrapper(MessagingFactory underlyingFactory, String entityPath, String transferEntityPath, MessagingEntityType entityType)
        {
            this.underlyingFactory = underlyingFactory;
            this.entityPath = entityPath;
            this.transferEntityPath = transferEntityPath;
            this.entityType = entityType;
            this.requestResponseLink = null;
            this.referenceCount = 0;
            this.waiters = new ArrayList<>();
            this.isClosed = false;
            this.createRequestResponseLinkAsync();
        }

        private void createRequestResponseLinkAsync()
        {
            String requestResponseLinkPath = RequestResponseLink.getManagementNodeLinkPath(this.entityPath);
            String sasTokenAudienceURI = String.format(ClientConstants.SAS_TOKEN_AUDIENCE_FORMAT, this.underlyingFactory.getHostName(), this.entityPath);

            String transferDestinationSasTokenAudienceURI = null;
            Map<Symbol, Object> additionalProperties = null;
            if (this.transferEntityPath != null) {
                transferDestinationSasTokenAudienceURI = String.format(ClientConstants.SAS_TOKEN_AUDIENCE_FORMAT, this.underlyingFactory.getHostName(), this.transferEntityPath);
                additionalProperties = new HashMap<>();
                additionalProperties.put(ClientConstants.LINK_TRANSFER_DESTINATION_PROPERTY, this.transferEntityPath);
            }

            TRACE_LOGGER.debug("Creating requestresponselink to '{}'", requestResponseLinkPath);
            RequestResponseLink.createAsync(
                    this.underlyingFactory,
                    StringUtil.getShortRandomString() + "-RequestResponse",
                    requestResponseLinkPath,
                    sasTokenAudienceURI,
                    transferDestinationSasTokenAudienceURI,
                    additionalProperties,
                    this.entityType).handleAsync((rrlink, ex) ->
            {
                synchronized (this.lock)
                {
                    if(ex == null)
                    {
                        TRACE_LOGGER.info("Created requestresponselink to '{}'", requestResponseLinkPath);
                        if(this.isClosed)
                        {
                        	// Factory is likely closed. Close the link too
                        	rrlink.closeAsync();
                        }
                        else
                        {
                        	this.requestResponseLink = rrlink;
                            this.completeWaiters(null);
                        }
                    }
                    else
                    {
                        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(ex);
                        TRACE_LOGGER.error("Creating requestresponselink to '{}' failed.", requestResponseLinkPath, cause);
                        RequestResponseLinkCache.this.removeWrapperFromCache(this.entityPath);
                        this.completeWaiters(cause);
                    }
                }
                
                return null;
            }, MessagingFactory.INTERNAL_THREAD_POOL);
        }
        
        private void completeWaiters(Throwable exception)
        {
        	for(CompletableFuture<RequestResponseLink> waiter : this.waiters)
            {
        		if(exception == null)
        		{
        			this.referenceCount++;
        			AsyncUtil.completeFuture(waiter, this.requestResponseLink);
        		}
        		else
        		{
        			AsyncUtil.completeFutureExceptionally(waiter, exception);
        		}
            }
        	
        	this.waiters.clear();
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
                    RequestResponseLinkCache.this.removeWrapperFromCache(this.entityPath);
                    TRACE_LOGGER.info("Closing requestresponselink to '{}'", this.requestResponseLink.getLinkPath());
                    this.requestResponseLink.closeAsync();
                }
            }
        }

        public CompletableFuture<Void> forceCloseAsync()
        {
            TRACE_LOGGER.info("Force closing requestresponselink to '{}'", this.requestResponseLink.getLinkPath());
            this.isClosed = true;
            if(this.waiters.size() > 0)
            {
            	this.completeWaiters(new ServiceBusException(false, "MessagingFactory closed."));
            }
            
            if(this.requestResponseLink != null)
            {
            	return this.requestResponseLink.closeAsync();
            }
            else
            {
            	return CompletableFuture.completedFuture(null);
            }
        }
    }
}