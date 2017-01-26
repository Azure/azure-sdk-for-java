package com.microsoft.azure.credentials;

import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.AzureEnvironment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AzureCliSubscription {
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

    public String id() {
        return id;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String clientId() {
        return clientId;
    }

    public AzureCliSubscription withToken(AzureCliToken token) {
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

    public synchronized AzureTokenCredentials credentialInstance() {
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
                public synchronized String getToken(String resource) throws IOException {
                    AzureCliToken token = userTokens.get(resource);
                    // Management endpoint also works for resource manager
                    if (token == null && (resource.equalsIgnoreCase(environment().resourceManagerEndpoint()))) {
                        token = userTokens.get(environment().managementEndpoint());
                    }
                    // Exact match and token hasn't expired
                    if (token != null && !token.expired()) {
                        return token.accessToken();
                    }
                    // If found then refresh
                    boolean shouldRefresh = token != null;
                    // If not found for the resource, but is MRRT then also refresh
                    if (token == null) {
                        token = new ArrayList<>(userTokens.values()).get(0);
                        shouldRefresh = token.isMRRT();
                    }
                    if (shouldRefresh) {
                        AuthenticationResult authenticationResult = acquireAccessTokenFromRefreshToken(resource, token.refreshToken(), token.isMRRT());
                        try {
                            AzureCliToken newToken = token.clone().withResource(resource).withAuthenticationResult(authenticationResult);
                            userTokens.put(resource, newToken);
                            return newToken.accessToken();
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        return null;
                    }
                }
            };
        }
        return credentialInstance;
    }

    public static class UserInfo {
        private String type;
        private String name;
    }

    static class Wrapper {
        List<AzureCliSubscription> subscriptions;
    }
}
