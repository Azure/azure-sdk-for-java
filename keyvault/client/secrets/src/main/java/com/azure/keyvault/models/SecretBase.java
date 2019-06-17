// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.models;

import com.azure.keyvault.SecretAsyncClient;
import com.azure.keyvault.SecretClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * SecretBase is the resource containing all the properties of the secret except its value.
 * It is managed by the Secret Service.
 *
 *  @see SecretClient
 *  @see SecretAsyncClient
 */
public class SecretBase {

    /**
     * The secret id.
     */
    private String id;

    /**
     * The secret version.
     */
    private String version;

    /**
     * Determines whether the object is enabled.
     */
    private Boolean enabled;

    /**
     * Not before date in UTC.
     */
    private OffsetDateTime notBefore;

    /**
     * Expiry date in UTC.
     */
    private OffsetDateTime expires;

    /**
     * Creation time in UTC.
     */
    private OffsetDateTime created;

    /**
     * Last updated time in UTC.
     */
    private OffsetDateTime updated;

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
    private String recoveryLevel;

    /**
     * The content type of the secret.
     */
    @JsonProperty(value = "contentType")
    private String contentType;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    /**
     * If this is a secret backing a KV certificate, then this field specifies
     * the corresponding key backing the KV certificate.
     */
    @JsonProperty(value = "kid", access = JsonProperty.Access.WRITE_ONLY)
    private String keyId;

    /**
     * True if the secret's lifetime is managed by key vault. If this is a
     * secret backing a certificate, then managed will be true.
     */
    @JsonProperty(value = "managed", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean managed;

    /**
     * Get the secret name.
     *
     * @return the name of the secret.
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the recovery level of the secret.

     * @return the recoveryLevel of the secret.
     */
    public String recoveryLevel() {
        return recoveryLevel;
    }

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @return the SecretBase object itself.
     */
    public SecretBase enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the notBefore UTC time.
     *
     * @return the notBefore UTC time.
     */
    public OffsetDateTime notBefore() {
        return notBefore;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the SecretBase object itself.
     */
    public SecretBase notBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    /**
     * Get the Secret Expiry time in UTC.
     *
     * @return the expires UTC time.
     */
    public OffsetDateTime expires() {
        if (this.expires == null) {
            return null;
        }
        return this.expires;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time to set for the secret.
     * @return the SecretBase object itself.
     */
    public SecretBase expires(OffsetDateTime expires) {
        this.expires = expires;
        return this;
    }

    /**
     * Get the the UTC time at which secret was created.
     *
     * @return the created UTC time.
     */
    public OffsetDateTime created() {
        return created;
    }

    /**
     * Get the UTC time at which secret was last updated.
     *
     * @return the last updated UTC time.
     */
    public OffsetDateTime updated() {
        return updated;
    }

    /**
     * Get the secret identifier.
     *
     * @return the secret identifier.
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the secret identifier.
     *
     * @param id The secret identifier to set
     * @return the SecretBase object itself.
     */
    public SecretBase id(String id) {
        unpackId(id);
        return this;
    }

    /**
     * Get the content type.
     *
     * @return the content type.
     */
    public String contentType() {
        return this.contentType;
    }

    /**
     * Set the contentType.
     *
     * @param contentType The contentType to set
     * @return the SecretBase object itself.
     */
    public SecretBase contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the tags associated with the secret.
     *
     * @return the value of the tags.
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags to be associated with the secret.
     *
     * @param tags The tags to set
     * @return the SecretBase object itself.
     */
    public SecretBase tags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the keyId identifier.
     *
     * @return the keyId identifier.
     */
    public String keyId() {
        return this.keyId;
    }

    /**
     * Get the managed value.
     *
     * @return the managed value
     */
    public Boolean managed() {
        return this.managed;
    }

    /**
     * Get the version of the secret.
     *
     * @return the version of the secret.
     */
    public String version() {
        return this.version;
    }

    @JsonProperty(value = "id")
    private void unpackId(String id) {
        if (id != null && id.length() > 0) {
            this.id = id;
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                this.name = (tokens.length >= 3 ? tokens[2] : null);
                this.version = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Unpacks the attributes json response and updates the variables in the Secret Attributes object.
     * Uses Lazy Update to set values for variables id, tags, contentType, managed and keyId as these variables are
     * part of main json body and not attributes json body when the secret response comes from list Secrets operations.
     * @param attributes The key value mapping of the Secret attributes
     */
    @JsonProperty("attributes")
    private void unpackAttributes(Map<String, Object> attributes) {
        this.enabled = (Boolean) attributes.get("enabled");
        this.notBefore =  epochToOffsetDateTime(attributes.get("nbf"));
        this.expires =  epochToOffsetDateTime(attributes.get("exp"));
        this.created = epochToOffsetDateTime(attributes.get("created"));
        this.updated = epochToOffsetDateTime(attributes.get("updated"));
        this.recoveryLevel = (String) attributes.get("recoveryLevel");
        this.contentType = (String) lazyValueSelection(attributes.get("contentType"), this.contentType);
        this.keyId = (String) lazyValueSelection(attributes.get("keyId"), this.keyId);
        this.tags = (Map<String, String>) lazyValueSelection(attributes.get("tags"), this.tags);
        this.managed = (Boolean) lazyValueSelection(attributes.get("managed"), this.managed);
        unpackId((String) attributes.get("id"));
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
