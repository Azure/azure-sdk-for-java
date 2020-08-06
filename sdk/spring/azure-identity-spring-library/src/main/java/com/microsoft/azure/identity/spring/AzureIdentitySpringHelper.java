/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
package com.microsoft.azure.identity.spring;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.HashMap;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * A helper class to deal with credentials in a Spring environment.
 *
 * @author manfred.riem@microsoft.com
 */
@Component
public class AzureIdentitySpringHelper {

    /**
     * Defines the AZURE_CREDENTIAL_PREFIX.
     */
    private static final String AZURE_CREDENTIAL_PREFIX = "azure.credential.";

    /**
     * Stores the named credentials.
     */
    private final HashMap<String, TokenCredential> credentials;

    /**
     * Constructor.
     */
    public AzureIdentitySpringHelper() {
        credentials = new HashMap<>();
        credentials.put("", new DefaultAzureCredentialBuilder().build());
    }

    /**
     * Add a named credential.
     *
     * @param name the name.
     * @param credential the credential.
     */
    public void addNamedCredential(String name, TokenCredential credential) {
        credentials.put(name, credential);
    }

    /**
     * Get the default Azure credential.
     *
     * @return the default Azure credential
     */
    public TokenCredential getDefaultCredential() {
        return credentials.get("");
    }

    /**
     * Get the named credential.
     *
     * @param name the name.
     * @return the named credential, or null if not found.
     */
    public TokenCredential getNamedCredential(String name) {
        return credentials.get(name);
    }

    /**
     * Populate from Environment.
     *
     * @param environment the environment.
     */
    public void populate(Environment environment) {
        populateNamedCredential(environment, "");
        String credentialNamesKey = AZURE_CREDENTIAL_PREFIX + "names";
        if (environment.containsProperty(credentialNamesKey)) {
            String[] credentialNames = environment.getProperty(credentialNamesKey).split(",");
            for(int i=0; i<credentialNames.length; i++) {
                populateNamedCredential(environment, credentialNames[i]);
            }
        }
    }

    /**
     * Populate a named credential.
     *
     * @param environment the environment
     * @param name the name.
     */
    private void populateNamedCredential(Environment environment, String name) {
        
        if (!name.equals("") && !name.endsWith(".")) {
            name = name + ".";
        }
        
        String tenantIdKey = AZURE_CREDENTIAL_PREFIX + name + "tenantId";
        String clientIdKey = AZURE_CREDENTIAL_PREFIX + name + "clientId";
        String clientSecretKey = AZURE_CREDENTIAL_PREFIX + name + "clientSecret";

        String tenantId = environment.getProperty(tenantIdKey);
        String clientId = environment.getProperty(clientIdKey);
        String clientSecret = environment.getProperty(clientSecretKey);

        if (tenantId != null && clientId != null && clientSecret != null) {
            TokenCredential credential = new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();
            credentials.put(name, credential);
            return;
        }
        
        String clientCertificateKey = AZURE_CREDENTIAL_PREFIX + name + "clientSecret";
        String clientCertificatePath = environment.getProperty(clientCertificateKey);
        
        if (clientCertificatePath != null) {
            TokenCredential credential = new ClientCertificateCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .pemCertificate(clientCertificatePath)
                    .build();            
            credentials.put(name, credential);
            return;
        }
        
        throw new RuntimeException("Configuration for azure.credential" + name + " is incomplete");
    }

    /**
     * Remove the named credential.
     *
     * @param name the name.
     * @return the removed named credential, or null if not found.
     */
    public TokenCredential removeNamedCredential(String name) {
        return credentials.remove(name);
    }
}
