/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials;

import com.azure.common.annotations.Beta;
import com.azure.common.AzureEnvironment;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An instance of this class represents a subscription record in azureProfiles.json.
 */
@Beta
final class AzureCliSubscription {
    private String environmentName;
    private String id;
    private String name;
    private String tenantId;
    private String state;
    private UserInfo user;
    private String clientId;
    private boolean isDefault;

    private AzureTokenCredentials credentialInstance;

    private Map<String, AzureCliToken> userTokens = new ConcurrentHashMap<>();
    private AzureCliToken servicePrincipalToken;

    String id() {
        return id;
    }

    boolean isDefault() {
        return isDefault;
    }

    String clientId() {
        return clientId;
    }

    AzureCliSubscription withToken(AzureCliToken token) {
        if (isServicePrincipal()) {
            this.servicePrincipalToken = token;
        } else {
            if (token.resource() != null) {
                this.userTokens.put(token.resource(), token);
            }
            if (this.clientId == null) {
                this.clientId = token.clientId();
            }
        }
        return this;
    }

    AzureEnvironment environment() {
        if (environmentName == null) {
            return null;
        } else if (environmentName.equalsIgnoreCase("AzureCloud")) {
            return AzureEnvironment.AZURE;
        } else if (environmentName.equalsIgnoreCase("AzureChinaCloud")) {
            return AzureEnvironment.AZURE_CHINA;
        } else if (environmentName.equalsIgnoreCase("AzureGermanCloud")) {
            return AzureEnvironment.AZURE_GERMANY;
        } else if (environmentName.equalsIgnoreCase("AzureUSGovernment")) {
            return AzureEnvironment.AZURE_US_GOVERNMENT;
        } else {
            return null;
        }
    }

    String tenant() {
        return tenantId;
    }

    boolean isServicePrincipal() {
        return user.type.equalsIgnoreCase("ServicePrincipal");
    }

    String userName() {
        return user.name;
    }

    synchronized AzureTokenCredentials credentialInstance() {
        if (credentialInstance != null) {
            return credentialInstance;
        }
        if (isServicePrincipal()) {
            credentialInstance = new ApplicationTokenCredentials(
                clientId(),
                tenant(),
                servicePrincipalToken.accessToken(),
                environment()
                );
        } else {
            credentialInstance = new UserTokenCredentials(clientId(), tenant(), null, null, environment()) {
                @Override
                public synchronized Mono<String> getToken(String resource) {
                    AzureCliToken token = userTokens.get(resource);
                    // Management endpoint also works for resource manager
                    if (token == null && (resource.equalsIgnoreCase(environment().resourceManagerEndpoint()))) {
                        token = userTokens.get(environment().managementEndpoint());
                    }
                    // Exact match and token hasn't expired
                    if (token != null && !token.expired()) {
                        return Mono.just(token.accessToken());
                    }
                    // If found then refresh
                    boolean shouldRefresh = token != null;
                    // If not found for the resource, but is MRRT then also refresh
                    if (token == null && userTokens.values().size() > 0) {
                        token = new ArrayList<>(userTokens.values()).get(0);
                        shouldRefresh = token.isMRRT();
                    }
                    if (shouldRefresh) {
                        AzureCliToken finalToken = token;
                        return acquireAccessTokenFromRefreshToken(resource, token.refreshToken(), token.isMRRT())
                                .map(authenticationResult -> {
                                    try {
                                        AzureCliToken newToken = finalToken.clone().withResource(resource).withAuthenticationResult(authenticationResult);
                                        userTokens.put(resource, newToken);
                                        return newToken.accessToken();
                                    } catch (CloneNotSupportedException cnse) {
                                        throw Exceptions.propagate(cnse);
                                    }
                                });
                    } else {
                        return Mono.error(new RuntimeException("No refresh token available for user " + userName()));
                    }
                }
            };
        }
        return credentialInstance;
    }

    private static class UserInfo {
        private String type;
        private String name;
    }

    static class Wrapper {
        List<AzureCliSubscription> subscriptions;
    }
}
