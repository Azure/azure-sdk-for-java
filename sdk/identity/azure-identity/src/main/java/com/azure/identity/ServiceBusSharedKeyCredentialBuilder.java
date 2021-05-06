package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ServiceBusSharedKeyCredentialBuilder extends AadCredentialBuilderBase<ServiceBusSharedKeyCredentialBuilder> {

    private static final String HASH_ALGORITHM = "HMACSHA256";

    private String sharedAccessPolicy;
    private SecretKeySpec secretKeySpec;
    private String sharedAccessSignature;

    /**
     * Sets the shardAccessAccount of the user.
     *
     * @param sharedAccessPolicy the sharedAccessPolicy of the user
     * @return the ServiceBusSharedKeyCredentialBuilder itself
     */
    public ServiceBusSharedKeyCredentialBuilder sharedAccessPolicy(String sharedAccessPolicy) {
        this.sharedAccessPolicy = sharedAccessPolicy;
        return this;
    }

    /**
     * Sets the shardAccessKey of the user.
     *
     * @param sharedAccessKey the sharedAccessKey of the user
     * @return the ServiceBusSharedKeyCredentialBuilder itself
     */
    public ServiceBusSharedKeyCredentialBuilder sharedAccessKey(String sharedAccessKey) {
        this.secretKeySpec = new SecretKeySpec(sharedAccessKey.getBytes(UTF_8),HASH_ALGORITHM);
        return this;
    }

    /**
     * Sets the sharedAccessSignature of the user.
     *
     * @param sharedAccessSignature the sharedAccessSignature of the user
     * @return the ServiceBusSharedKeyCredentialBuilder itself
     */
    public ServiceBusSharedKeyCredentialBuilder sharedAccessSignature(String sharedAccessSignature) {
        this.sharedAccessSignature = sharedAccessSignature;
        return this;
    }

    /**
     * Creates a new {@link ServiceBusSharedKeyCredential} with the current configurations.
     *
     * @return a {@link ServiceBusSharedKeyCredential} with the current configurations.
     */
    public ServiceBusSharedKeyCredential build() {
        if (sharedAccessSignature == null) {
            ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("sharedAccessPolicy", sharedAccessPolicy);
                put("sharedAccessKey", secretKeySpec);
            }});
        } else {
            ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("sharedAccessSignature", sharedAccessSignature);
            }});
        }
        return new ServiceBusSharedKeyCredential(sharedAccessPolicy, secretKeySpec, sharedAccessSignature);
    }
}
