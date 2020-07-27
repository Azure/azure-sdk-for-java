/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.api;

/**
 * An interface meant to be implemented by configuration properties POJOs that store information about
 * Azure credentials.
 *
 * @author Warren Zhu
 */
public interface CredentialSupplier {

    /**
     * Supplies credential file path
     *
     * @return credential file path
     */
    String getCredentialFilePath();

    boolean isMsiEnabled();

    String getSubscriptionId();
}
