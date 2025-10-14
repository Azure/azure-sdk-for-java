// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.Base64Url;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.certificates.implementation.CertificatePropertiesHelper;
import com.azure.security.keyvault.certificates.implementation.IdMetadata;
import com.azure.security.keyvault.certificates.implementation.models.CertificateAttributes;
import com.azure.security.keyvault.certificates.implementation.models.CertificateBundle;
import com.azure.security.keyvault.certificates.implementation.models.CertificateItem;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateBundle;
import com.azure.security.keyvault.certificates.implementation.models.DeletedCertificateItem;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.CoreUtils.bytesToHexString;
import static com.azure.security.keyvault.certificates.implementation.CertificatesUtils.getIdMetadata;

/**
 * Represents base properties of a certificate.
 */
public class CertificateProperties implements JsonSerializable<CertificateProperties> {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateProperties.class);

    static {
        CertificatePropertiesHelper.setAccessor(new CertificatePropertiesHelper.CertificatePropertiesAccessor() {
            @Override
            public CertificateProperties createCertificateProperties(CertificateItem item) {
                return new CertificateProperties(item);
            }

            @Override
            public CertificateProperties createCertificateProperties(DeletedCertificateItem item) {
                return new CertificateProperties(item);
            }
        });
    }

    /**
     * URL for the Azure KeyVault service.
     */
    private final String vaultUrl;

    /**
     * The certificate version.
     */
    private final String version;

    /**
     * The Certificate name.
     */
    private String name;

    /**
     * Determines whether the object is enabled.
     */
    private Boolean enabled;

    /**
     * Not before date in UTC.
     */
    private final OffsetDateTime notBefore;

    /**
     * Expiry date in UTC.
     */
    private final OffsetDateTime expiresOn;

    /**
     * Creation time in UTC.
     */
    private final OffsetDateTime createdOn;

    /**
     * Last updated time in UTC.
     */
    private final OffsetDateTime updatedOn;

    /**
     * Reflects the deletion recovery level currently in effect for certificates in the current vault. If it contains
     * 'Purgeable', the certificate can be permanently deleted by a privileged user; otherwise, only the system can
     * purge the certificate, at the end of the retention interval. Possible values include: 'Purgeable',
     * 'Recoverable+Purgeable', 'Recoverable', 'Recoverable+ProtectedSubscription'.
     */
    private final String recoveryLevel;

    /**
     * The certificate id.
     */
    private final String id;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * Thumbprint of the certificate. Read-only.
     */
    private final Base64Url x509Thumbprint;

    /**
     * The number of days a certificate is retained before being deleted for a soft delete-enabled Key Vault.
     */
    private final Integer recoverableDays;

    /**
     * The flag indicating whether the order of the certificate chain is to be preserved in the vault. The default value
     * is {@code false}, which sets the leaf certificate at index 0.
     */
    private final Boolean certificateOrderPreserved;

    CertificateProperties() {
        this(null, new CertificateAttributes(), null, null, null, false);
    }

    CertificateProperties(CertificateItem item) {
        this(item.getId(), item.getAttributes(), item.getTags(), item.getX509Thumbprint(),
            item.getAttributes().getRecoverableDays(), false);
    }

    CertificateProperties(CertificateBundle bundle) {
        this(bundle.getId(), bundle.getAttributes(), bundle.getTags(), bundle.getX509Thumbprint(),
            bundle.getAttributes().getRecoverableDays(), bundle.isPreserveCertOrder());
    }

    CertificateProperties(DeletedCertificateItem item) {
        this(item.getId(), item.getAttributes(), item.getTags(), item.getX509Thumbprint(),
            item.getAttributes().getRecoverableDays(), false);
    }

    CertificateProperties(DeletedCertificateBundle bundle) {
        this(bundle.getId(), bundle.getAttributes(), bundle.getTags(), bundle.getX509Thumbprint(),
            bundle.getAttributes().getRecoverableDays(), bundle.isPreserveCertOrder());
    }

    CertificateProperties(String id, CertificateAttributes attributes, Map<String, String> tags, byte[] wireThumbprint,
        Integer recoverableDays, Boolean certificateOrderPreserved) {

        IdMetadata idMetadata = getIdMetadata(id, 1, 2, 3, LOGGER);
        this.id = idMetadata.getId();
        this.vaultUrl = idMetadata.getVaultUrl();
        this.name = idMetadata.getName();
        this.version = idMetadata.getVersion();

        if (attributes != null) {
            this.enabled = attributes.isEnabled();
            this.notBefore = attributes.getNotBefore();
            this.expiresOn = attributes.getExpires();
            this.createdOn = attributes.getCreated();
            this.updatedOn = attributes.getUpdated();
            this.recoveryLevel = Objects.toString(attributes.getAdminContacts(), null);
        } else {
            this.enabled = null;
            this.notBefore = null;
            this.expiresOn = null;
            this.createdOn = null;
            this.updatedOn = null;
            this.recoveryLevel = null;
        }

        this.tags = tags;
        this.x509Thumbprint
            = (wireThumbprint == null || wireThumbprint.length == 0) ? null : Base64Url.encode(wireThumbprint);
        this.recoverableDays = recoverableDays;
        this.certificateOrderPreserved = certificateOrderPreserved;
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     * Get the certificate identifier.
     *
     * @return The certificate identifier
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the notBefore UTC time.
     *
     * @return The notBefore UTC time.
     */
    public OffsetDateTime getNotBefore() {
        return notBefore;
    }

    /**
     * Gets the number of days a key is retained before being deleted for a soft delete-enabled Key Vault.
     *
     * @return The recoverable days.
     */
    public Integer getRecoverableDays() {
        return recoverableDays;
    }

    /**
     * Get the Certificate Expiry time in UTC.
     *
     * @return The expires UTC time.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Get the UTC time at which certificate was created.
     *
     * @return The created UTC time.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Get the UTC time at which certificate was last updated.
     *
     * @return The last updated UTC time.
     */
    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }

    /**
     * Get the tags associated with the certificate.
     *
     * @return The value of the tags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Get the URL for the Azure KeyVault service.
     *
     * @return The value of the URL for the Azure KeyVault service.
     */
    public String getVaultUrl() {
        return this.vaultUrl;
    }

    /**
     * Set the tags to be associated with the certificate.
     *
     * @param tags The tags to set.
     *
     * @return The updated {@link CertificateProperties} object.
     */
    public CertificateProperties setTags(Map<String, String> tags) {
        this.tags = tags;

        return this;
    }

    /**
     * Get the version of the certificate.
     *
     * @return The version of the certificate.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Get the certificate name.
     *
     * @return The name of the certificate.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the recovery level of the certificate.
    
     * @return The recovery level of the certificate.
     */
    public String getRecoveryLevel() {
        return recoveryLevel;
    }

    /**
     * Get the enabled status.
     *
     * @return The enabled status.
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled status.
     *
     * @param enabled The enabled status to set.
     *
     * @return The updated {@link CertificateProperties} object.
     */
    public CertificateProperties setEnabled(Boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    /**
     * Get the X509 Thumbprint of the certificate.
     *
     * @return The x509Thumbprint.
     */
    public byte[] getX509Thumbprint() {
        return x509Thumbprint != null ? x509Thumbprint.decodedBytes() : null;
    }

    /**
     * Gets the thumbprint of the certificate as a hex string which can be used to uniquely identify it.
     *
     * @return The thumbprint of the certificate as a hex string.
     */
    public String getX509ThumbprintAsString() {
        return bytesToHexString(getX509Thumbprint());
    }

    /**
     * Get a value indicating whether the order of certificate chain is to be preserved in the vault. The default value
     * is {@code false}, which sets the leaf certificate at index 0.
     *
     * @return The preserve certificate order status.
     */
    public Boolean isCertificateOrderPreserved() {
        return this.certificateOrderPreserved;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeMapField("tags", tags, JsonWriter::writeString).writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link CertificateProperties}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     *
     * @return The {@link CertificateProperties} that the JSON stream represented, may return {@code null}.
     *
     * @throws IOException If a {@link CertificateProperties} fails to be read from the {@code jsonReader}.
     */
    public static CertificateProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            CertificateAttributes attributes = null;
            Map<String, String> tags = null;
            byte[] wireThumbprint = null;
            Integer recoverableDays = null;
            boolean certificateOrderPreserved = false;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();

                reader.nextToken();

                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("attributes".equals(fieldName)) {
                    attributes = CertificateAttributes.fromJson(reader);
                } else if ("tags".equals(fieldName)) {
                    tags = reader.readMap(JsonReader::getString);
                } else if ("x5t".equals(fieldName)) {
                    wireThumbprint = reader.getBinary();
                } else if ("recoverableDays".equals(fieldName)) {
                    recoverableDays = reader.getInt();
                } else if ("preserveCertOrder".equals(fieldName)) {
                    certificateOrderPreserved = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }

            return new CertificateProperties(id, attributes == null ? new CertificateAttributes() : attributes, tags,
                wireThumbprint, recoverableDays, certificateOrderPreserved);
        });
    }
}
