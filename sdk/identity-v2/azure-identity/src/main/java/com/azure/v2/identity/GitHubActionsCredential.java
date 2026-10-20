// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.utils.CoreUtils;

import java.io.IOException;

/**
 * Authenticates an application in GitHub Actions using an OIDC federated identity credential.
 *
 * <p>Configure a federated identity credential on the Microsoft Entra application that trusts the GitHub workflow's
 * issuer, subject, and audience. The workflow needs {@code permissions: id-token: write}. GitHub supplies the
 * OIDC request URL and bearer token. The client and tenant IDs default to {@code AZURE_CLIENT_ID} and
 * {@code AZURE_TENANT_ID}; they can also be set on {@link GitHubActionsCredentialBuilder}.</p>
 *
 * <p>The assertion audience is selected from the authority host. This credential supports Azure Public, China,
 * US Government, France Government, Germany Government, and Singapore Government. It is not included in
 * {@link DefaultAzureCredential}.</p>
 *
 * <!-- src_embed com.azure.v2.identity.credential.githubactionscredential.construct -->
 * <pre>
 * GitHubActionsCredential credential = new GitHubActionsCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.v2.identity.credential.githubactionscredential.construct -->
 *
 * @see GitHubActionsCredentialBuilder
 */
public class GitHubActionsCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(GitHubActionsCredential.class);
    private final ClientAssertionCredential clientAssertionCredential;

    GitHubActionsCredential(String requestUrl, String requestToken, ConfidentialClientOptions options) {
        options.setClientAssertionFunction(pipeline -> {
            HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET)
                .setUri(requestUrl)
                .setHeaders(new HttpHeaders().set(HttpHeaderName.AUTHORIZATION, "Bearer " + requestToken));
            try (Response<BinaryData> response = pipeline.send(request)) {
                if (response.getStatusCode() != 200) {
                    throw authenticationError(
                        "GitHub OIDC request returned status code " + response.getStatusCode() + ".");
                }
                return readAssertion(response);
            }
        });
        clientAssertionCredential = new ClientAssertionCredential(options);
    }

    /**
     * Acquires an access token, using the token cache when possible.
     *
     * @param request the scopes, tenant, and authentication options for the token request.
     * @return the access token.
     * @throws CredentialAuthenticationException if the GitHub OIDC request fails or returns an invalid assertion response.
     */
    @Override
    public AccessToken getToken(TokenRequestContext request) {
        try {
            return clientAssertionCredential.getToken(request);
        } catch (CoreException e) {
            // Preserve the OIDC failure category through client-assertion delegation.
            Throwable cause = e.getCause();
            if (cause instanceof CredentialAuthenticationException) {
                throw (CredentialAuthenticationException) cause;
            }
            throw e;
        }
    }

    private static String readAssertion(Response<BinaryData> response) {
        BinaryData body = response.getValue();
        if (body == null) {
            throw authenticationError("GitHub OIDC response must contain a nonempty string 'value'.");
        }
        try (JsonReader reader = JsonReader.fromString(body.toString())) {
            String assertion = reader.readObject(objectReader -> {
                String value = null;
                while (objectReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = objectReader.getFieldName();
                    objectReader.nextToken();
                    if ("value".equals(fieldName)) {
                        if (objectReader.currentToken() != JsonToken.STRING) {
                            throw authenticationError("GitHub OIDC response must contain a string 'value'.");
                        }
                        value = objectReader.getString();
                    } else {
                        objectReader.skipChildren();
                    }
                }
                return value;
            });
            if (CoreUtils.isNullOrEmpty(assertion) || reader.nextToken() != JsonToken.END_DOCUMENT) {
                throw authenticationError("GitHub OIDC response must contain a nonempty string 'value'.");
            }
            return assertion;
        } catch (IOException | IllegalStateException e) {
            throw LOGGER.throwableAtError()
                .log("GitHubActionsCredential authentication failed. Invalid GitHub OIDC response.", sanitizedCause(e),
                    CredentialAuthenticationException::new);
        }
    }

    // Parser and URI exception messages can contain secrets. Retain their type and stack trace, not their input.
    static RuntimeException sanitizedCause(Exception exception) {
        if (exception == null) {
            return null;
        }
        RuntimeException sanitized
            = new IllegalArgumentException(exception.getClass().getSimpleName() + " (input redacted)");
        sanitized.setStackTrace(exception.getStackTrace());
        return sanitized;
    }

    private static CredentialAuthenticationException authenticationError(String message) {
        throw LOGGER.throwableAtError()
            .addKeyValue("reason", message)
            .log("GitHubActionsCredential authentication failed.",
                prefix -> new CredentialAuthenticationException(prefix + " " + message));
    }
}
