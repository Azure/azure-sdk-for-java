package com.azure.redisson.sample;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;


public class HelloWorld {

    public static void main(String[] args) {
        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch an Azure AD token to be used for authentication.
        // Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
        String token = defaultAzureCredential
            .getToken(new TokenRequestContext()
                .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();

        // Create Client Configuration
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://<HOST_NAME>:6380") // TODO: Replace Host Name with Azure Cache for Redis Host Name.
            .setKeepAlive(true) // Keep the connection alive.
            .setUsername("<USERNAME>") // Username is Required
            .setPassword(token) // Azure AD Access Token as password is required.
            .setClientName("Reddison-Client");

        RedissonClient redisson = Redisson.create(config);

        // perform operations
        RBuckets rBuckets =  redisson.getBuckets();
        RBucket<String> bucket = redisson.getBucket("Az:key");
        bucket.set("This is object value");

        String objectValue = bucket.get();
        System.out.println("stored object value: " + objectValue);

        redisson.shutdown();
    }
}
