/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

import com.microsoft.azure.cosmosdb.DocumentClientException;

/**
 * Models session token.
 */
public interface ISessionToken {

    String PARTITION_KEY_RANGE_SESSION_SEPARATOR = ":";

    /**
     * Returns true if this instance of session token is valid with respect to <code>other</code> session token.
     * This is used to decide if the the client can accept server's response (based on comparison between client's
     * and server's session token)
     *
     * @param other Session token to validate
     * @return true if this instance of session  token is valid with respect to <code>other</code> session token;
     * false otherwise
     */
    boolean isValid(ISessionToken other) throws DocumentClientException;

    /**
     * Returns a new instance of session token obtained by merging this session token with
     * the given session token <code>other</code>.
     *
     * @param other Other session token to merge
     * @return Instance of merged session token
     */
    ISessionToken merge(ISessionToken other) throws DocumentClientException;

    long getLSN();

    String convertToString();
}
