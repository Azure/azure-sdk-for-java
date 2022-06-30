package com.azure.sample;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;

public class HellloWorld {

    public static void main(String[] args) {

        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch an Azure AD token to be used for authentication. This token will be used as the password.
        String token = defaultAzureCredential
            .getToken(new TokenRequestContext()
                .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();

        // SSL connection is required for non 6379 ports. It is recommeded to use SSL connections.
        boolean useSsl = true;
        String cacheHostname = "YOUR_HOST_NAME.redis.cache.windows.net";

        // Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
        Jedis jedis = new Jedis(cacheHostname, 6380, DefaultJedisClientConfig.builder()
            .password(token)
            .user("<username>")
            .ssl(useSsl)
            .build());

        // Set a value against your key in the Redis cache.
        jedis.set("Az:key", "testValue");
        System.out.println(jedis.get("Az:key"));

        // Close the Jedis Client
        jedis.close();
    }
}
