package com.microsoft.azure.servicebus.primitives;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;

import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.rules.RuleDescription;

public final class MiscRequestResponseOperationHandler extends ClientEntity
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(MiscRequestResponseOperationHandler.class);
    
	private final Object requestResonseLinkCreationLock = new Object();
	private final String entityPath;
	private final String sasTokenAudienceURI;
	private final MessagingFactory underlyingFactory;
	private RequestResponseLink requestResponseLink;
	private CompletableFuture<Void> requestResponseLinkCreationFuture;
	private ScheduledFuture<?> sasTokenRenewTimerFuture;
	
	private MiscRequestResponseOperationHandler(MessagingFactory factory, String linkName, String entityPath)
	{
		super(linkName, factory);
		
		this.underlyingFactory = factory;
		this.entityPath = entityPath;
		this.sasTokenAudienceURI = String.format(ClientConstants.SAS_TOKEN_AUDIENCE_FORMAT, factory.getHostName(), entityPath);
	}	
	
	public static CompletableFuture<MiscRequestResponseOperationHandler> create(MessagingFactory factory, String entityPath)
	{
	    CompletableFuture<MiscRequestResponseOperationHandler> creationFuture = new CompletableFuture<MiscRequestResponseOperationHandler>();
	    MiscRequestResponseOperationHandler requestResponseOperationHandler = new MiscRequestResponseOperationHandler(factory, StringUtil.getShortRandomString(), entityPath);
	    requestResponseOperationHandler.sendSASTokenAndSetRenewTimer().handleAsync((v, ex) -> {
	        if(ex == null)
	        {
	            TRACE_LOGGER.info("Opened MiscRequestResponseOperationHandler");
	            creationFuture.complete(requestResponseOperationHandler);
	        }
	        else
	        {
	            TRACE_LOGGER.error("Opening of MiscRequestResponseOperationHandler failed", ex);
	            creationFuture.completeExceptionally(ExceptionUtil.extractAsyncCompletionCause(ex));
	        }
	        return null;
	    });
	    
	    Timer.schedule(
                new Runnable()
                {
                    public void run()
                    {
                        if (!creationFuture.isDone())
                        {
                            requestResponseOperationHandler.cancelSASTokenRenewTimer();
                            Exception operationTimedout = new TimeoutException(
                                    String.format(Locale.US, "Open operation on CBSLink(%s) on Entity(%s) timed out at %s.", requestResponseOperationHandler.getClientId(), requestResponseOperationHandler.entityPath, ZonedDateTime.now().toString()));                            
                            TRACE_LOGGER.warn(operationTimedout.getMessage());

                            creationFuture.completeExceptionally(operationTimedout);
                        }
                    }
                }
                , factory.getOperationTimeout()
                , TimerType.OneTimeRun);       
	    return creationFuture;		
	}
	
	@Override
	protected CompletableFuture<Void> onClose() {
	    TRACE_LOGGER.trace("Closing MiscRequestResponseOperationHandler");
	    this.cancelSASTokenRenewTimer();
	    this.underlyingFactory.releaseRequestResponseLink(this.entityPath);
	    return CompletableFuture.completedFuture(null);
	}
	
	private CompletableFuture<Void> sendSASTokenAndSetRenewTimer()
    {
        if(this.getIsClosingOrClosed())
        {
            return CompletableFuture.completedFuture(null);
        }
        else
        {
            CompletableFuture<ScheduledFuture<?>> sendTokenFuture = this.underlyingFactory.sendSASTokenAndSetRenewTimer(this.sasTokenAudienceURI, () -> this.sendSASTokenAndSetRenewTimer());
            return sendTokenFuture.thenAccept((f) -> {this.sasTokenRenewTimerFuture = f; TRACE_LOGGER.debug("Set SAS Token renew timer");});
        }
    }
    
    private void cancelSASTokenRenewTimer()
    {
        if(this.sasTokenRenewTimerFuture != null && !this.sasTokenRenewTimerFuture.isDone())
        {
            TRACE_LOGGER.debug("Cancelling SAS Token renew timer");
            this.sasTokenRenewTimerFuture.cancel(true);
        }
    }
	
	private CompletableFuture<Void> createRequestResponseLink()
	{
	    synchronized (this.requestResonseLinkCreationLock) {
            if(this.requestResponseLinkCreationFuture == null)
            {                
                this.requestResponseLinkCreationFuture = new CompletableFuture<Void>();                
                this.underlyingFactory.obtainRequestResponseLinkAsync(this.entityPath).handleAsync((rrlink, ex) ->
                {
                    if(ex == null)
                    {                        
                        this.requestResponseLink = rrlink;
                        this.requestResponseLinkCreationFuture.complete(null);
                    }
                    else
                    {
                        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(ex);                        
                        this.requestResponseLinkCreationFuture.completeExceptionally(cause);
                        // Set it to null so next call will retry rr link creation
                        synchronized (this.requestResonseLinkCreationLock)
                        {
                            this.requestResponseLinkCreationFuture = null;
                        }                        
                    }
                    return null;
                });
            }
            
            return this.requestResponseLinkCreationFuture;
        }
	}
	
	public CompletableFuture<Pair<String[], Integer>> getMessageSessionsAsync(Date lastUpdatedTime, int skip, int top, String lastSessionId)
	{
	    TRACE_LOGGER.debug("Getting message sessions from entity '{}' with lastupdatedtime '{}', skip '{}', top '{}', lastsessionid '{}'", this.entityPath, lastUpdatedTime, skip, top, lastSessionId);
		return this.createRequestResponseLink().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_LAST_UPDATED_TIME, lastUpdatedTime);
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_SKIP, skip);
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_TOP, top);
			if(lastSessionId != null)
			{
				requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_LAST_SESSION_ID, lastSessionId);
			}
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_GET_MESSAGE_SESSIONS_OPERATION, requestBodyMap, Util.adjustServerTimeout(this.underlyingFactory.getOperationTimeout()));
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, this.underlyingFactory.getOperationTimeout());
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Pair<String[], Integer>> returningFuture = new CompletableFuture<Pair<String[], Integer>>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{				    
					Map responseBodyMap = RequestResponseUtils.getResponseBody(responseMessage);
					int responseSkip = (int)responseBodyMap.get(ClientConstants.REQUEST_RESPONSE_SKIP);
					String[] sessionIds = (String[])responseBodyMap.get(ClientConstants.REQUEST_RESPONSE_SESSIONIDS);
					TRACE_LOGGER.debug("Received '{}' sessions from entity '{}'. Response skip '{}'", sessionIds.length, this.entityPath, responseSkip);
					returningFuture.complete(new Pair<>(sessionIds, responseSkip));				
				}
				else if(statusCode == ClientConstants.REQUEST_RESPONSE_NOCONTENT_STATUS_CODE ||
						(statusCode == ClientConstants.REQUEST_RESPONSE_NOTFOUND_STATUS_CODE && ClientConstants.SESSION_NOT_FOUND_ERROR.equals(RequestResponseUtils.getResponseErrorCondition(responseMessage))))
				{
				    TRACE_LOGGER.debug("Received no sessions from entity '{}'.", this.entityPath);
					returningFuture.complete(new Pair<>(new String[0], 0));
				}
				else
				{
					// error response
				    TRACE_LOGGER.debug("Receiving sessions from entity '{}' failed with status code '{}'", this.entityPath, statusCode);
					returningFuture.completeExceptionally(RequestResponseUtils.genereateExceptionFromResponse(responseMessage));
				}
				return returningFuture;
			});
		});
	}
	
	public CompletableFuture<Void> removeRuleAsync(String ruleName)
	{
	    TRACE_LOGGER.debug("Removing rule '{}' from entity '{}'", ruleName, this.entityPath);
		return this.createRequestResponseLink().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_RULENAME, ruleName);
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_REMOVE_RULE_OPERATION, requestBodyMap, Util.adjustServerTimeout(this.underlyingFactory.getOperationTimeout()));
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, this.underlyingFactory.getOperationTimeout());
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Void> returningFuture = new CompletableFuture<Void>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
				    TRACE_LOGGER.debug("Removed rule '{}' from entity '{}'", ruleName, this.entityPath);
					returningFuture.complete(null);
				}
				else
				{
					// error response
				    TRACE_LOGGER.error("Removing rule '{}' from entity '{}' failed with status code '{}'", ruleName, this.entityPath, statusCode);
					returningFuture.completeExceptionally(RequestResponseUtils.genereateExceptionFromResponse(responseMessage));
				}
				return returningFuture;
			});
		});
	}
	
	public CompletableFuture<Void> addRuleAsync(RuleDescription ruleDescription)
	{
	    TRACE_LOGGER.debug("Adding rule '{}' to entity '{}'", ruleDescription.getName(), this.entityPath);
		return this.createRequestResponseLink().thenComposeAsync((v) -> {
			HashMap requestBodyMap = new HashMap();
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_RULENAME, ruleDescription.getName());
			requestBodyMap.put(ClientConstants.REQUEST_RESPONSE_RULEDESCRIPTION, RequestResponseUtils.encodeRuleDescriptionToMap(ruleDescription));
			
			Message requestMessage = RequestResponseUtils.createRequestMessageFromPropertyBag(ClientConstants.REQUEST_RESPONSE_ADD_RULE_OPERATION, requestBodyMap, Util.adjustServerTimeout(this.underlyingFactory.getOperationTimeout()));
			CompletableFuture<Message> responseFuture = this.requestResponseLink.requestAysnc(requestMessage, this.underlyingFactory.getOperationTimeout());
			return responseFuture.thenComposeAsync((responseMessage) -> {
				CompletableFuture<Void> returningFuture = new CompletableFuture<Void>();
				int statusCode = RequestResponseUtils.getResponseStatusCode(responseMessage);
				if(statusCode == ClientConstants.REQUEST_RESPONSE_OK_STATUS_CODE)
				{
				    TRACE_LOGGER.debug("Added rule '{}' to entity '{}'", ruleDescription.getName(), this.entityPath);
					returningFuture.complete(null);
				}
				else
				{
					// error response
				    TRACE_LOGGER.error("Adding rule '{}' to entity '{}' failed with status code '{}'", ruleDescription.getName(), this.entityPath, statusCode);
					returningFuture.completeExceptionally(RequestResponseUtils.genereateExceptionFromResponse(responseMessage));
				}
				return returningFuture;
			});
		});		
	}
}
