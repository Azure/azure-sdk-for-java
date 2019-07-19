// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.CosmosClientException;

/**
 * Models session token.
 *
 * We make assumption that instances of this interface are immutable (read only after they are constructed), so if you want to change
 * this behaviour please review all of its uses and make sure that mutability doesn't break anything.
 */
public interface ISessionToken {

    String PARTITION_KEY_RANGE_SESSION_SEPARATOR = ":";

    /**
     * Returns true if this instance of session token is valid with respect to <code>other</code> session token.
     * This is used to decide if the client can accept server's response (based on comparison between client's
     * and server's session token)
     *
     * @param other SESSION token to validate
     * @return true if this instance of session  token is valid with respect to <code>other</code> session token;
     * false otherwise
     */
    boolean isValid(ISessionToken other) throws CosmosClientException;

    /**
     * Returns a new instance of session token obtained by merging this session token with
     * the given session token <code>other</code>.
     *
     * Merge is commutative operation, so a.Merge(b).Equals(b.Merge(a))
     *
     * @param other Other session token to merge
     * @return Instance of merged session token
     */
    ISessionToken merge(ISessionToken other) throws CosmosClientException;

    long getLSN();

    String convertToString();
}
