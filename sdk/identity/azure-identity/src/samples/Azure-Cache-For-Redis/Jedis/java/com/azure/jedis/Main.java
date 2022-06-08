// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class Main {
    public static void main(String[] args) {

        //Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
            .clientId("YOUR-CLIENT-ID")
            .pfxCertificate("YOUR-CERTIFICATE-PATH", "CERTIFICATE-PASSWORD")
            .tenantId("YOUR-TENANT-ID")
            .build();

        // Fetch an Azure AD token to be used for authentication. This token will be used as the password.
        TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
        AccessToken accessToken = getAccessToken(clientCertificateCredential, trc);

        // SSL connection is required for non 6379 ports.
        boolean useSsl = true;
        String cacheHostname = "YOUR_HOST_NAME.redis.cache.windows.net";

        // Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
        Jedis jedis = createJedisClient(cacheHostname, 6380, "USERNAME", accessToken, useSsl);

        int maxTries = 3;
        int i = 0;

        while (i < maxTries) {
            try {
                // Set a value against your key in the Redis cache.
                jedis.set("Az:key", "testValue");
                System.out.println(jedis.get("Az:key"));
                break;
            } catch (JedisException e) {
                // Handle The Exception as required in your application.
                e.printStackTrace();

                // Check if the client is broken, if it is then close and recreate it to create a new healthy connection.
                if (jedis.isBroken() || accessToken.isExpired()) {
                    jedis.close();
                    jedis = createJedisClient(cacheHostname, 6380, "USERNAME", getAccessToken(clientCertificateCredential, trc), useSsl);
                }
            }
            i++;
        }

        // Close the Jedis Client
        jedis.close();
    }


    // Helper Code
    private static Jedis createJedisClient(String cacheHostname, int port, String username, AccessToken accessToken, boolean useSsl) {
        return new Jedis(cacheHostname, port, DefaultJedisClientConfig.builder()
            .password(accessToken.getToken())
            .user(username)
            .ssl(useSsl)
            .build());
    }

    private static AccessToken getAccessToken(TokenCredential tokenCredential, TokenRequestContext trc) {
        return tokenCredential.getToken(trc).block();
    }
}
