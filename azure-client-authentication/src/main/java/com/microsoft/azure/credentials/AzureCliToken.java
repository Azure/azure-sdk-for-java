package com.microsoft.azure.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.aad.adal4j.AuthenticationResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

public class AzureCliToken implements Cloneable {
    @JsonProperty("_authority")
    private String authority;
    @JsonProperty("_clientId")
    private String clientId;
    private String tokenType;
    private long expiresIn;
    private String expiresOn;
    private Date expiresOnDate;
    private String oid;
    private String userId;
    private String servicePrincipalId;
    private String servicePrincipalTenant;
    private boolean isMRRT;
    private String resource;
    private String accessToken;
    private String refreshToken;
    private String identityProvider;

    public boolean isServicePrincipal() {
        return servicePrincipalId != null;
    }

    public String tenant() {
        if (isServicePrincipal()) {
            return servicePrincipalTenant;
        } else {
            String[] parts = authority.split("/");
            return parts[parts.length - 1];
        }
    }

    public String clientId() {
        if (isServicePrincipal()) {
            return servicePrincipalId;
        } else {
            return clientId;
        }
    }

    public String authority() {
        return authority;
    }

    public boolean expired() {
        return expiresOn != null && expiresOn().before(new Date());
    }

    public String accessToken() {
        return accessToken;
    }

    public Date expiresOn() {
        if (expiresOnDate == null) {
            try {
                expiresOnDate = DateTime.parse(expiresOn, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")).toDate();
            } catch (IllegalArgumentException e) {
                expiresOnDate = DateTime.parse(expiresOn).toDate();
            }
        }
        return expiresOnDate;
    }

    AzureCliToken withAuthenticationResult(AuthenticationResult result) {
        this.accessToken = result.getAccessToken();
        this.refreshToken = result.getRefreshToken();
        this.expiresIn = result.getExpiresAfter();
        this.expiresOnDate = result.getExpiresOnDate();
        return this;
    }

    AzureCliToken withAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String refreshToken() {
        return refreshToken;
    }

    AzureCliToken withRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public String user() {
        if (isServicePrincipal()) {
            return servicePrincipalId;
        } else {
            return userId;
        }
    }

    public boolean isMRRT() {
        return isMRRT;
    }

    public String resource() {
        return resource;
    }

    public AzureCliToken withResource(String resource) {
        this.resource = resource;
        return this;
    }

    public AzureCliToken clone() throws CloneNotSupportedException {
        AzureCliToken token = (AzureCliToken) super.clone();
        token.expiresOnDate = (Date) expiresOn().clone();
        return token;
    }
}
