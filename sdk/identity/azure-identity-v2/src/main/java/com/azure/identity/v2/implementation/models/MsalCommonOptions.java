// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

import com.azure.identity.v2.AzureAuthorityHosts;
import com.azure.identity.v2.TokenCachePersistenceOptions;
import com.azure.identity.v2.implementation.util.ValidationUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

/**
 * Represents Msal Options common across Confidential, Public and Managed Identity OAuth Flows .
 */
public class MsalCommonOptions implements Cloneable {

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public MsalCommonOptions(Configuration configuration) {

    }

}
