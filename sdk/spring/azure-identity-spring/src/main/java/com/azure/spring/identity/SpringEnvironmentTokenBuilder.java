// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import java.util.HashMap;

import org.springframework.core.env.Environment;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * A helper class to deal with credentials in a Spring environment.
 *
 * <p>
 * This helper class makes it possible to configure credentials to be used within a Spring context.
 * </p>
 *
 * <table summary="">
 * <tr>
 * <th>Property Tuples</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>azure.credential.(name.)tenantId <br>
 * azure.credential.(name.)clientId <br>
 * azure.credential.(name.)clientSecret</td>
 * <td>the Azure Tenant ID <br>
 * the Client ID <br>
 * the Client Certificate <br>
 * </td>
 * </tr>
 * <tr>
 * <td>azure.credential.(name.)tenantId <br>
 * azure.credential.(name.)clientId <br>
 * azure.credential.(name.)clientCertificate</td>
 * <td>the Azure Tenant ID <br>
 * the Client ID <br>
 * the path to the PEM client certificate</td>
 * </tr>
 * </table>
 * <p>
 * where name is the <code>name</code> of the credential. Note if
 * <code>name</code> is entirely omitted it is taken to be the default
 * credential. Note if the default credential is omitted it is configure to use
 * AzureDefaultCredential which allows for the use a Managed Identity (if it is
 * present).
 *
 * @author manfred.riem@microsoft.com
 */
public class SpringEnvironmentTokenBuilder {

    /**
     * Defines the AZURE_CREDENTIAL_PREFIX.
     */
    private static final String AZURE_CREDENTIAL_PREFIX = "azure.credential.";

    /**
     * Stores the named credentials.
     */
    private final HashMap<String, TokenCredential> credentials;

    /**
     * Stores the name of the credential to be returned. If omitted, the default credential will be returned.
     */
    private String name = "";

    /**
     * Constructor.
     */
    public SpringEnvironmentTokenBuilder() {
        credentials = new HashMap<>();
        credentials.put("", new DefaultAzureCredentialBuilder().build());
    }

    /**
     * Populate from Environment.
     *
     * @param environment the environment.
     * @return this builder.
     */
    public SpringEnvironmentTokenBuilder fromEnvironment(Environment environment) {
        populateNamedCredential(environment, "");
        String credentialNamesKey = AZURE_CREDENTIAL_PREFIX + "names";
        if (environment.containsProperty(credentialNamesKey)) {
            String[] credentialNames = environment.getProperty(credentialNamesKey).split(",");
            for (int i = 0; i < credentialNames.length; i++) {
                populateNamedCredential(environment, credentialNames[i]);
            }
        }
        return this;
    }

    /**
     * Sets a credential to override a named credential. If this credential fails to produce a token, the original token
     * credential will be used.
     *
     * @param name the name for the credential.
     * @param credential the token credential.
     * @return this builder.
     */
    public SpringEnvironmentTokenBuilder overrideNamedCredential(String name, TokenCredential credential) {
        TokenCredential currentCredential = credentials.get(name);
        if (currentCredential == null) {
            credentials.put(name, credential);
        } else {
            ChainedTokenCredentialBuilder builder = new ChainedTokenCredentialBuilder();
            builder.addFirst(credential);
            builder.addLast(currentCredential);
            credentials.put(name, builder.build());
        }

        return this;
    }

    /**
     * Populate a named credential.
     *
     * @param environment the environment
     * @param name the name.
     */
    private void populateNamedCredential(Environment environment, String name) {
        String standardizedName = name;

        if (!standardizedName.equals("") && !standardizedName.endsWith(".")) {
            standardizedName = standardizedName + ".";
        }

        String tenantIdKey = AZURE_CREDENTIAL_PREFIX + standardizedName + "tenantId";
        String clientIdKey = AZURE_CREDENTIAL_PREFIX + standardizedName + "clientId";
        String clientSecretKey = AZURE_CREDENTIAL_PREFIX + standardizedName + "clientSecret";

        String tenantId = environment.getProperty(tenantIdKey);
        String clientId = environment.getProperty(clientIdKey);
        String clientSecret = environment.getProperty(clientSecretKey);

        if (tenantId != null && clientId != null && clientSecret != null) {
            TokenCredential credential = new ClientSecretCredentialBuilder().tenantId(tenantId).clientId(clientId)
                .clientSecret(clientSecret).build();
            credentials.put(name, credential);
            return;
        }

        String clientCertificateKey = AZURE_CREDENTIAL_PREFIX + standardizedName + "clientCertificate";
        String clientCertificatePath = environment.getProperty(clientCertificateKey);

        if (tenantId != null && clientId != null && clientCertificatePath != null) {
            TokenCredential credential = new ClientCertificateCredentialBuilder().tenantId(tenantId).clientId(clientId)
                .pemCertificate(clientCertificatePath).build();
            credentials.put(name, credential);
            return;
        }

        if (!name.equals("")) {
            throw new IllegalStateException("Configuration for azure.credential." + name + " is incomplete");
        }
    }

    /**
     * Sets the builder to return a credential named <code>name</code>
     *
     * @param name the name of the credential.
     * @return this builder.
     */
    public SpringEnvironmentTokenBuilder namedCredential(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the builder to return the default credential.
     *
     * @return the credential builder with default name.
     */
    public SpringEnvironmentTokenBuilder defaultCredential() {
        return namedCredential("");
    }

    /**
     * Builds an Azure TokenCredential.
     *
     * @return the built token credential.
     * @throws IllegalArgumentException if attempting to retrieve a named credential not defined in the environment.
     */
    public TokenCredential build() {
        TokenCredential result = credentials.get(name);
        if (result == null) {
            throw new IllegalArgumentException(
                "Attempting to retrieve Azure credential not configured in the environment. (name=" + name + ")");
        } else {
            return result;
        }
    }
}
