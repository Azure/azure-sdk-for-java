// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;

/**
 * SecretProperties is the resource containing all the properties of the secret except its value.
 * It is managed by the Secret Service.
 *
 *  @see SecretClient
 *  @see SecretAsyncClient
 */
@Fluent
public class SecretProperties {
    private final ClientLogger logger = new ClientLogger(SecretProperties.class);

    /**
     * The secret id.
     */
    String id;

    /**
     * The secret version.
     */
    String version;

    /**
     * Determines whether the object is enabled.
     */
    Boolean enabled;

    /**
     * Not before date in UTC.
     */
    OffsetDateTime notBefore;

    /**
     * Expiry date in UTC.
     */
    OffsetDateTime expiresOn;

    /**
     * Creation time in UTC.
     */
    OffsetDateTime createdOn;

    /**
     * Last updated time in UTC.
     */
    OffsetDateTime updatedOn;

    /**
     * The secret name.
     */
    String name;

    /**
     * Reflects the deletion recovery level currently in effect for secrets in
     * the current vault. If it contains 'Purgeable', the secret can be
     * permanently deleted by a privileged user; otherwise, only the system can
     * purge the secret, at the end of the retention interval. Possible values
     * include: 'Purgeable', 'Recoverable+Purgeable', 'Recoverable',
     * 'Recoverable+ProtectedSubscription'.
     */
    String recoveryLevel;

    /**
     * The content type of the secret.
     */
    @JsonProperty(value = "contentType")
    String contentType;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    @JsonProperty(value = "tags")
    Map<String, String> tags;

    /**
     * If this is a secret backing a KV certificate, then this field specifies
     * the corresponding key backing the KV certificate.
     */
    @JsonProperty(value = "kid", access = JsonProperty.Access.WRITE_ONLY)
    String keyId;

    /**
     * True if the secret's lifetime is managed by key vault. If this is a
     * secret backing a certificate, then managed will be true.
     */
    @JsonProperty(value = "managed", access = JsonProperty.Access.WRITE_ONLY)
    Boolean managed;

    SecretProperties(String secretName) {
        this.name = secretName;
    }

    /**
     * Creates empty instance of SecretProperties.
     */
    public SecretProperties() { }

    /**
     * Get the secret name.
     *
     * @return the name of the secret.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the recovery level of the secret.

     * @return the recoveryLevel of the secret.
     */
    public String getRecoveryLevel() {
        return recoveryLevel;
    }

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @throws NullPointerException if {@code enabled} is null.
     * @return the SecretProperties object itself.
     */
    public SecretProperties setEnabled(Boolean enabled) {
        Objects.requireNonNull(enabled);
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the notBefore UTC time.
     *
     * @return the notBefore UTC time.
     */
    public OffsetDateTime getNotBefore() {
        return notBefore;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the SecretProperties object itself.
     */
    public SecretProperties setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    /**
     * Get the Secret Expiry time in UTC.
     *
     * @return the expires UTC time.
     */
    public OffsetDateTime getExpiresOn() {
        if (this.expiresOn == null) {
            return null;
        }
        return this.expiresOn;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expiresOn The expiry time to set for the secret.
     * @return the SecretProperties object itself.
     */
    public SecretProperties setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    /**
     * Get the the UTC time at which secret was created.
     *
     * @return the created UTC time.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Get the UTC time at which secret was last updated.
     *
     * @return the last updated UTC time.
     */
    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Get the secret identifier.
     *
     * @return the secret identifier.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the content type.
     *
     * @return the content type.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType.
     *
     * @param contentType The contentType to set
     * @return the updated SecretProperties object itself.
     */
    public SecretProperties setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the tags associated with the secret.
     *
     * @return the value of the tags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the tags to be associated with the secret.
     *
     * @param tags The tags to set
     * @return the updated SecretProperties object itself.
     */
    public SecretProperties setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the keyId identifier.
     *
     * @return the keyId identifier.
     */
    public String getKeyId() {
        return this.keyId;
    }

    /**
     * Get the managed value.
     *
     * @return the managed value
     */
    public Boolean isManaged() {
        return this.managed;
    }

    /**
     * Get the version of the secret.
     *
     * @return the version of the secret.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Unpacks the attributes json response and updates the variables in the Secret Attributes object.
     * Uses Lazy Update to set values for variables id, tags, contentType, managed and keyId as these variables are
     * part of main json body and not attributes json body when the secret response comes from list Secrets operations.
     * @param attributes The key value mapping of the Secret attributes
     */
    @JsonProperty("attributes")
    @SuppressWarnings("unchecked")
    void unpackAttributes(Map<String, Object> attributes) {
        this.enabled = (Boolean) attributes.get("enabled");
        this.notBefore = epochToOffsetDateTime(attributes.get("nbf"));
        this.expiresOn = epochToOffsetDateTime(attributes.get("exp"));
        this.createdOn = epochToOffsetDateTime(attributes.get("created"));
        this.updatedOn = epochToOffsetDateTime(attributes.get("updated"));
        this.recoveryLevel = (String) attributes.get("recoveryLevel");
        this.contentType = (String) lazyValueSelection(attributes.get("contentType"), this.contentType);
        this.keyId = (String) lazyValueSelection(attributes.get("keyId"), this.keyId);
        this.tags = (Map<String, String>) lazyValueSelection(attributes.get("tags"), this.tags);
        this.managed = (Boolean) lazyValueSelection(attributes.get("managed"), this.managed);
        unpackId((String) attributes.get("id"));
    }

    @JsonProperty(value = "id")
    void unpackId(String id) {
        if (id != null && id.length() > 0) {
            this.id = id;
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                this.name = (tokens.length >= 3 ? tokens[2] : null);
                this.version = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                // Should never come here.
                logger.error("Received Malformed Secret Id URL from KV Service");
            }
        }
    }

    private OffsetDateTime epochToOffsetDateTime(Object epochValue) {
        if (epochValue != null) {
            Instant instant = Instant.ofEpochMilli(((Number) epochValue).longValue() * 1000L);
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return null;
    }

    private Object lazyValueSelection(Object input1, Object input2) {
        if (input1 == null) {
            return input2;
        }
        return input1;
    }

}
