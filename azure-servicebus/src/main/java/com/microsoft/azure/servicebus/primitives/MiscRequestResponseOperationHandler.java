package com.microsoft.azure.servicebus.primitives;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.rules.RuleDescription;

public final class MiscRequestResponseOperationHandler extends ClientEntity
{
    private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
    
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
	            creationFuture.complete(requestResponseOperationHandler);
	        }
	        else
	        {
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
                            if (TRACE_LOGGER.isLoggable(Level.WARNING))
                            {
                                TRACE_LOGGER.log(Level.WARNING, operationTimedout.getMessage());
                            }

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
	    this.cancelSASTokenRenewTimer();
		return this.requestResponseLink == null ? CompletableFuture.completedFuture(null) : this.requestResponseLink.closeAsync();
	}
	
	CompletableFuture<Void> sendSASTokenAndSetRenewTimer()
    {
        if(this.getIsClosingOrClosed())
        {
            return CompletableFuture.completedFuture(null);
        }
        else
        {
            CompletableFuture<ScheduledFuture<?>> sendTokenFuture = this.underlyingFactory.sendSASTokenAndSetRenewTimer(this.sasTokenAudienceURI, () -> this.sendSASTokenAndSetRenewTimer());
            return sendTokenFuture.thenAccept((f) -> {this.sasTokenRenewTimerFuture = f;});
        }
    }
    
    private void cancelSASTokenRenewTimer()
    {
        if(this.sasTokenRenewTimerFuture != null && !this.sasTokenRenewTimerFuture.isDone())
        {
            this.sasTokenRenewTimerFuture.cancel(true);
        }
    }
	
	private CompletableFuture<Void> createRequestResponseLink()
	{
	    synchronized (this.requestResonseLinkCreationLock) {
            if(this.requestResponseLinkCreationFuture == null)
            {
                this.requestResponseLinkCreationFuture = new CompletableFuture<Void>();
                String requestResponseLinkPath = RequestResponseLink.getManagementNodeLinkPath(this.entityPath);                
                RequestResponseLink.createAsync(this.underlyingFactory, this.getClientId() + "-RequestResponse", requestResponseLinkPath).handleAsync((rrlink, ex) ->
                {
                    if(ex == null)
                    {
                        this.requestResponseLink = rrlink;
                        this.requestResponseLinkCreationFuture.complete(null);
                    }
                    else
                    {
                        this.requestResponseLinkCreationFuture.completeExceptionally(ExceptionUtil.extractAsyncCompletionCause(ex));
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
					returningFuture.complete(new Pair<>(sessionIds, responseSkip));				
				}
				else if(statusCode == ClientConstants.REQUEST_RESPONSE_NOCONTENT_STATUS_CODE ||
						(statusCode == ClientConstants.REQUEST_RESPONSE_NOTFOUND_STATUS_CODE && ClientConstants.SESSION_NOT_FOUND_ERROR.equals(RequestResponseUtils.getResponseErrorCondition(responseMessage))))
				{
					returningFuture.complete(new Pair<>(new String[0], 0));
				}
				else
				{
					// error response
					returningFuture.completeExceptionally(RequestResponseUtils.genereateExceptionFromResponse(responseMessage));
				}
				return returningFuture;
			});
		});
	}
	
	public CompletableFuture<Void> removeRuleAsync(String ruleName)
	{
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
					returningFuture.complete(null);
				}
				else
				{
					// error response
					returningFuture.completeExceptionally(RequestResponseUtils.genereateExceptionFromResponse(responseMessage));
				}
				return returningFuture;
			});
		});
	}
	
	public CompletableFuture<Void> addRuleAsync(RuleDescription ruleDescription)
	{
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
					returningFuture.complete(null);
				}
				else
				{
					// error response
					returningFuture.completeExceptionally(RequestResponseUtils.genereateExceptionFromResponse(responseMessage));
				}
				return returningFuture;
			});
		});		
	}
}
