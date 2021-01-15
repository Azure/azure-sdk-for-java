// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import java.time.OffsetDateTime;
import java.util.List;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

/**
 * Synchronous client interface for Communication Service identity operations
 */
@ServiceClient(builder = CommunicationIdentityClientBuilder.class, isAsync = false)
public final class CommunicationIdentityClient {

    private final CommunicationIdentityAsyncClient asyncClient;
    private final ClientLogger logger = new ClientLogger(CommunicationIdentityClient.class);

    CommunicationIdentityClient(CommunicationIdentityAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Creates a new CommunicationUser.
     *
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationUserIdentifier createUser() {
        return this.asyncClient.createUser().block();
    }

    /**
     * Creates a new CommunicationUser.
     *
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public Response<CommunicationUserIdentifier> createUserWithResponse(Context context) {
        return this.asyncClient.createUser(context).block();
    }

    /**
     * Deletes a CommunicationUser, revokes its tokens and deletes its data.
     *
     * @param communicationUser The user to be deleted.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public void deleteUser(CommunicationUserIdentifier communicationUser) {
        this.asyncClient.deleteUser(communicationUser).block();
    }

    /**
     * Deletes a CommunicationUser, revokes its tokens and deletes its data.
     *
     * @param communicationUser The user to be deleted.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public Response<Void> deleteUserWithResponse(CommunicationUserIdentifier communicationUser, Context context) {
        return this.asyncClient.deleteUser(communicationUser, context).block();
    }

    /**
     * Revokes all the tokens created for a user before a specific date.
     *
     * @param communicationUser The CommunicationUser whose tokens will be revoked.
     * @param issuedBefore All tokens that are issued prior to this time should get revoked.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public void revokeTokens(CommunicationUserIdentifier communicationUser, OffsetDateTime issuedBefore) {
        this.asyncClient.revokeTokens(communicationUser, issuedBefore).block();
    }

    /**
     * Revokes all the tokens created for a user before a specific date.
     *
     * @param communicationUser The CommunicationUser whose tokens will be revoked.
     * @param issuedBefore All tokens that are issued prior to this time should get revoked.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public Response<Void> revokeTokensWithResponse(
        CommunicationUserIdentifier communicationUser, OffsetDateTime issuedBefore, Context context) {
        return this.asyncClient.revokeTokens(communicationUser, issuedBefore, context).block();
    }


    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The CommunicationUser from whom to issue a token.
     * @param scopes The scopes that the token should have.
     * @return the created CommunicationUserToken.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public CommunicationUserToken issueToken(CommunicationUserIdentifier communicationUser, List<String> scopes) {
        return this.asyncClient.issueToken(communicationUser, scopes).block();
    }

    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The CommunicationUser from whom to issue a token.
     * @param scopes The scopes that the token should have.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the created CommunicationUserToken.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public Response<CommunicationUserToken> issueTokenWithResponse(
        CommunicationUserIdentifier communicationUser, List<String> scopes, Context context) {
        return this.asyncClient.issueToken(communicationUser, scopes, context).block();
    }
}
