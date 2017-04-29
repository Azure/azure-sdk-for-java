package com.microsoft.azure.servicebus.primitives;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.rules.RuleDescription;

public final class MiscRequestResponseOperationHandler extends ClientEntity
{
	private final Object requestResonseLinkCreationLock = new Object();
	private final String entityPath;
	private final MessagingFactory underlyingFactory;
	private RequestResponseLink requestResponseLink;
	
	private MiscRequestResponseOperationHandler(MessagingFactory factory, String linkName, String entityPath)
	{
		super(linkName, factory);
		
		this.underlyingFactory = factory;
		this.entityPath = entityPath;
	}	
	
	public static CompletableFuture<MiscRequestResponseOperationHandler> create(
			MessagingFactory factory,			
			String entityPath)
	{
		MiscRequestResponseOperationHandler sessionBrowser = new MiscRequestResponseOperationHandler(factory, StringUtil.getShortRandomString(), entityPath);
		return CompletableFuture.completedFuture(sessionBrowser);		
	}
	
	@Override
	protected CompletableFuture<Void> onClose() {
		return this.requestResponseLink == null ? CompletableFuture.completedFuture(null) : this.requestResponseLink.closeAsync();
	}
	
	private CompletableFuture<Void> createRequestResponseLink()
	{
		synchronized (this.requestResonseLinkCreationLock) {
			if(this.requestResponseLink == null)
			{
				String requestResponseLinkPath = RequestResponseLink.getManagementNodeLinkPath(this.entityPath);
				CompletableFuture<Void> crateAndAssignRequestResponseLink =
								RequestResponseLink.createAsync(this.underlyingFactory, this.getClientId() + "-RequestResponse", requestResponseLinkPath).thenAccept((rrlink) -> {this.requestResponseLink = rrlink;});
				return crateAndAssignRequestResponseLink;
			}
			else
			{
				return CompletableFuture.completedFuture(null);
			}
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
