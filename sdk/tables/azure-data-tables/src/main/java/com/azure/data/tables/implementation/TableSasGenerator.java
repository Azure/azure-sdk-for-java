// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.TableServiceVersion;
import com.azure.data.tables.sas.TableSasIpRange;
import com.azure.data.tables.sas.TableSasPermission;
import com.azure.data.tables.sas.TableSasProtocol;
import com.azure.data.tables.sas.TableSasSignatureValues;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

import static com.azure.data.tables.implementation.TableSasUtils.computeHmac256;
import static com.azure.data.tables.implementation.TableSasUtils.formatQueryParameterDate;
import static com.azure.data.tables.implementation.TableSasUtils.tryAppendQueryParameter;

/**
 * A class containing utility methods for generating SAS tokens for the Azure Tables service.
 */
public class TableSasGenerator {
    private final ClientLogger logger = new ClientLogger(TableSasGenerator.class);
    private final OffsetDateTime expiryTime;
    private final OffsetDateTime startTime;
    private final String endPartitionKey;
    private final String endRowKey;
    private final String identifier;
    private final String sas;
    private final String startPartitionKey;
    private final String startRowKey;
    private final String tableName;
    private final TableSasProtocol protocol;
    private final TableSasIpRange sasIpRange;
    private String permissions;
    private String version;

    /**
     * Creates a new {@link TableSasGenerator} which will generate an table-level SAS signed with an
     * {@link AzureNamedKeyCredential}.
     *
     * @param sasValues The {@link TableSasSignatureValues} to generate the SAS token with.
     * @param tableName The table name.
     * @param azureNamedKeyCredential An {@link AzureNamedKeyCredential} whose key will be used to sign the SAS.
     */
    public TableSasGenerator(TableSasSignatureValues sasValues, String tableName,
                             AzureNamedKeyCredential azureNamedKeyCredential) {
        Objects.requireNonNull(sasValues, "'sasValues' cannot be null.");
        Objects.requireNonNull(azureNamedKeyCredential, "'azureNamedKeyCredential' cannot be null.");

        this.version = sasValues.getVersion();
        this.protocol = sasValues.getProtocol();
        this.startTime = sasValues.getStartTime();
        this.expiryTime = sasValues.getExpiryTime();
        this.permissions = sasValues.getPermissions();
        this.sasIpRange = sasValues.getSasIpRange();
        this.tableName = tableName;
        this.identifier = sasValues.getIdentifier();
        this.startPartitionKey = sasValues.getStartPartitionKey();
        this.startRowKey = sasValues.getStartRowKey();
        this.endPartitionKey = sasValues.getEndPartitionKey();
        this.endRowKey = sasValues.getEndRowKey();

        validateState();

        // Signature is generated on the un-url-encoded values.
        String canonicalName = getCanonicalName(azureNamedKeyCredential.getAzureNamedKey().getName());
        String stringToSign = stringToSign(canonicalName);
        String signature = computeHmac256(azureNamedKeyCredential.getAzureNamedKey().getKey(), stringToSign);

        this.sas = encode(signature);
    }

    /**
     * Get the SAS produced by this {@link TableSasGenerator}.
     *
     * @return The SAS produced by this {@link TableSasGenerator}.
     */
    public String getSas() {
        return sas;
    }

    private String encode(String signature) {
        /*
         * We should be url-encoding each key and each value, but because we know all the keys and values will encode to
         * themselves, we cheat except for the signature value.
         */
        StringBuilder sb = new StringBuilder();

        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SERVICE_VERSION, this.version);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_START_TIME,
            formatQueryParameterDate(this.startTime));
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_EXPIRY_TIME,
            formatQueryParameterDate(this.expiryTime));
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_TABLE_NAME, tableName);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SIGNED_PERMISSIONS, this.permissions);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_TABLE_START_PARTITION_KEY, startPartitionKey);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_TABLE_START_ROW_KEY, startRowKey);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_TABLE_END_PARTITION_KEY, endPartitionKey);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_TABLE_END_ROW_KEY, endRowKey);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_IP_RANGE, this.sasIpRange);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_PROTOCOL, this.protocol);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SIGNED_IDENTIFIER, this.identifier);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SIGNATURE, signature);

        return sb.toString();
    }

    /**
     * Ensures that the builder's properties are in a consistent state.
     *
     * 1. If there is no version, use latest.
     * 2. If there is no identifier set, ensure expiryTime and permissions are set.
     * 4. Re-parse permissions depending on what the resource is. If it is an unrecognised resource, do nothing.
     */
    private void validateState() {
        if (version == null) {
            version = TableServiceVersion.getLatest().getVersion();
        }

        if (identifier == null) {
            if (expiryTime == null || permissions == null) {
                throw logger.logExceptionAsError(new IllegalStateException("If identifier is not set, expiry time "
                    + "and permissions must be set"));
            }
        }

        if (permissions != null) {
            if (tableName != null) {
                permissions = TableSasPermission.parse(permissions).toString();
            } else {
                // We won't re-parse the permissions if we don't know the type.
                logger.info("Not re-parsing permissions. Resource type is not table.");
            }
        }

        if ((startPartitionKey != null && startRowKey == null) || (startPartitionKey == null && startRowKey != null)) {
            throw logger.logExceptionAsError(new IllegalStateException("'startPartitionKey' and 'startRowKey' must "
                + "either be both provided or both null. One cannot be provided without the other."));
        }

        if ((endPartitionKey != null && endRowKey == null) || (endPartitionKey == null && endRowKey != null)) {
            throw logger.logExceptionAsError(new IllegalStateException("'endPartitionKey' and 'endRowKey' must either "
                + "be both provided or both null. One cannot be provided without the other."));
        }
    }

    /**
     * Computes the canonical name for a table resource for SAS signing.
     *
     * @param account Account of the storage account.
     *
     * @return Canonical name as a string.
     */
    private String getCanonicalName(String account) {
        // Table: "/table/account/tablename"
        return String.join("/", new String[]{"/table", account, tableName});
    }

    private String stringToSign(String canonicalName) {
        return String.join("\n",
            this.permissions == null ? "" : this.permissions,
            this.startTime == null ? "" : StorageConstants.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            this.expiryTime == null ? "" : StorageConstants.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            canonicalName.toLowerCase(Locale.ROOT),
            this.identifier == null ? "" : this.identifier,
            this.sasIpRange == null ? "" : this.sasIpRange.toString(),
            this.protocol == null ? "" : protocol.toString(),
            this.version == null ? "" : this.version,
            this.startPartitionKey == null ? "" : this.startPartitionKey,
            this.startRowKey == null ? "" : this.startRowKey,
            this.endPartitionKey == null ? "" : this.endPartitionKey,
            this.endRowKey == null ? "" : this.endRowKey
        );
    }
}
