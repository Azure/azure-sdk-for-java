package com.azure.cosmos.implementation;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;




public class ExpiringAuthTokenCache implements AuthTokenCache {
    
    static public class Request {

        HttpConstants.HttpMethod verb;
        String resourceIdOrFullName;
        ResourceType resourceType;
        Map<String, String> headers;

        public Request(HttpConstants.HttpMethod verb, String resourceIdOrFullName, ResourceType resourceType, Map<String, String> headers) {

            Preconditions.checkNotNull(verb);
            Preconditions.checkNotNull(resourceIdOrFullName);
            Preconditions.checkNotNull(headers);


            this.verb = verb;
            this.resourceIdOrFullName = resourceIdOrFullName;
            this.resourceType = resourceType;
            this.headers = headers;
        }


        public Map<String, String> getHeaders() {
            return headers;
        }
    }

    private final static int MaxCacheExpiryInMinutes = 15;
    private final ConcurrentHashMap<AuthTokenKey, AuthTokenValue> authTokenMap = new ConcurrentHashMap<AuthTokenKey, AuthTokenValue>();

    private final Duration expiryInterval;


    public ExpiringAuthTokenCache(Duration expiryInterval) {
        if (Duration.ofMinutes(ExpiringAuthTokenCache.MaxCacheExpiryInMinutes).minus(expiryInterval).isNegative()) {
            throw new IllegalArgumentException(
                "Cannot set cache interval greater than " + ExpiringAuthTokenCache.MaxCacheExpiryInMinutes +
                    " minutes");
        }

        this.expiryInterval = expiryInterval;
    }

    /// <inheritdoc />
    public void setOrUpdateAuthToken(Request request, Consumer<Request> setTokenAction) {
        AuthTokenKey key = AuthTokenKey.create(request);
        AuthTokenValue existingAuthValue = this.authTokenMap.get(key);

        if (existingAuthValue != null && !existingAuthValue.isExpired()) {
            existingAuthValue.populate(request);
        } else {
            setTokenAction.accept(request);
            this.authTokenMap.merge(
                key,
                AuthTokenValue.create(request, this.expiryInterval),
                (existingValue, newValue) ->
                {
                    return AuthTokenValue.getMostRecent(existingValue, newValue);
                });
        }
    }

    //
    private static class AuthTokenValue {
        private AuthTokenValue(Instant expiry, String authorization, String date) {
            this.Expiry = expiry;
            this.Authorization = authorization;
            this.Date = date;
        }

        private Instant Expiry;

        private String Authorization;

        private String Date;

        public static AuthTokenValue create(Request serviceRequest, Duration expiryTime) {

            final Map<String, String> headers = serviceRequest.getHeaders();

            return new AuthTokenValue(
                Instant.now().plus(expiryTime),
                headers.get(HttpConstants.HttpHeaders.AUTHORIZATION),
                StringUtils.defaultString(headers.get(HttpConstants.HttpHeaders.X_DATE), headers.get(HttpConstants.HttpHeaders.HTTP_DATE)));
        }

        public Boolean isExpired() {
            return Instant.now().isAfter(this.Expiry);
        }

        public void populate(Request serviceRequest) {
            serviceRequest.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, this.Authorization);
            serviceRequest.getHeaders().put(HttpConstants.HttpHeaders.X_DATE, this.Date);
        }

        public static AuthTokenValue getMostRecent(AuthTokenValue left, AuthTokenValue right) {
            return left.Expiry.isAfter(right.Expiry) ? left : right;
        }
    }

    private static class AuthTokenKey {
        private AuthTokenKey(HttpConstants.HttpMethod verb, ResourceType resourceType, String resourceId) {
            this.Verb = verb;
            this.ResourceType = resourceType;
            this.ResourceId = resourceId;
        }

        public static AuthTokenKey create(Request request) {
            return new AuthTokenKey(request.verb, request.resourceType, request.resourceIdOrFullName);
        }

        private HttpConstants.HttpMethod Verb;

        private ResourceType ResourceType;

        private String ResourceId;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AuthTokenKey that = (AuthTokenKey) o;
            return Verb == that.Verb &&
                ResourceType == that.ResourceType &&
                Objects.equals(ResourceId, that.ResourceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(Verb, ResourceType, ResourceId);
        }
    }

}
