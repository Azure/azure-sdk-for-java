// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.models;

import com.azure.keyvault.SecretAsyncClient;
import com.azure.keyvault.SecretClient;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * SecretAttributes is the resource containing all the properties of the secret except its value.
 * It is managed by the Secret Service.
 *
 *  @see SecretClient
 *  @see SecretAsyncClient
 */
public class SecretAttributes {

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
    protected String name;

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
     * @return the name value.
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the recovery level of the secret.

     * @return the recoveryLevel value.
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
     * @param enabled the enabled value to set
     * @return the Attributes object itself.
     */
    public SecretAttributes enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the notBefore value.
     *
     * @return the notBefore value
     */
    public OffsetDateTime notBefore() {
        return notBefore;
    }

    /**
     * Set the {@link LocalDateTime notBefore} time value. The time gets converted to UTC time.
     *
     * @param notBefore the notBefore time value to set
     * @return the SecretAttributes object itself.
     */
    public SecretAttributes notBefore(LocalDateTime notBefore) {
        this.notBefore = OffsetDateTime.of(notBefore, ZoneOffset.UTC);
        return this;
    }

    /**
     * Get the Secret Expiry time in UTC.
     *
     * @return the expires value
     */
    public OffsetDateTime expires() {
        if (this.expires == null) {
            return null;
        }
        return this.expires;
    }

    /**
     * Set the {@link LocalDateTime expiry} time for the Secret. The time gets converted to UTC time.
     *
     * @param expires the expiry time to set for the secret.
     * @return the SecretAttributes object itself.
     */
    public SecretAttributes expires(LocalDateTime expires) {
        this.expires = OffsetDateTime.of(expires, ZoneOffset.UTC);
        return this;
    }

    /**
     * Get the the UTC time at which secret was created.
     *
     * @return the created time value.
     */
    public OffsetDateTime created() {
        return created;
    }

    /**
     * Get the UTC time at which secret was last updated.
     *
     * @return the updated time value.
     */
    public OffsetDateTime updated() {
        return updated;
    }

    /**
     * Get the secret identifier value.
     *
     * @return the secret identifier value.
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the secret identifier value.
     *
     * @param id the secret identifier value to set
     * @return the SecretAttributes object itself.
     */
    public SecretAttributes id(String id) {
        unpackId(id);
        return this;
    }

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String contentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     * @return the SecretAttributes object itself.
     */
    public SecretAttributes contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the SecretAttributes object itself.
     */
    public SecretAttributes tags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the keyId value.
     *
     * @return the keyId value
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
     * Get the version value.
     *
     * @return the version value
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
     * @param attributes the key value mapping of the Secret attributes
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
