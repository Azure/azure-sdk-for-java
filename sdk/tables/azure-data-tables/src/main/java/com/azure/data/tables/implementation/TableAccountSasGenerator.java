// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.sas.TableAccountSasPermission;
import com.azure.data.tables.sas.TableAccountSasSignatureValues;
import com.azure.data.tables.sas.TableSasIpRange;
import com.azure.data.tables.sas.TableSasProtocol;

import java.time.OffsetDateTime;
import java.util.Objects;

import static com.azure.data.tables.implementation.TableSasUtils.computeHmac256;
import static com.azure.data.tables.implementation.TableSasUtils.formatQueryParameterDate;
import static com.azure.data.tables.implementation.TableSasUtils.tryAppendQueryParameter;

/**
 * A class containing utility methods for generating SAS tokens for the Azure Storage accounts.
 */
public class TableAccountSasGenerator {
    private final ClientLogger logger = new ClientLogger(TableAccountSasGenerator.class);
    private final OffsetDateTime expiryTime;
    private final OffsetDateTime startTime;
    private final String permissions;
    private final String resourceTypes;
    private final String services;
    private final String sas;
    private final TableSasProtocol protocol;
    private final TableSasIpRange sasIpRange;
    private String version;

    /**
     * Creates a new {@link TableAccountSasGenerator} which will generate an account-level SAS signed with an
     * {@link AzureNamedKeyCredential}.
     *
     * @param sasValues The {@link TableAccountSasSignatureValues account signature values}.
     * @param azureNamedKeyCredential An {@link AzureNamedKeyCredential} whose key will be used to sign the SAS.
     */
    public TableAccountSasGenerator(TableAccountSasSignatureValues sasValues,
                                    AzureNamedKeyCredential azureNamedKeyCredential) {
        Objects.requireNonNull(sasValues, "'sasValues' cannot be null.");
        Objects.requireNonNull(azureNamedKeyCredential, "'azureNamedKeyCredential' cannot be null.");
        Objects.requireNonNull(sasValues.getServices(), "'services' in 'sasValues' cannot be null.");
        Objects.requireNonNull(sasValues.getResourceTypes(), "'resourceTypes' in 'sasValues' cannot be null.");
        Objects.requireNonNull(sasValues.getExpiryTime(), "'expiryTime' in 'sasValues' cannot be null.");
        Objects.requireNonNull(sasValues.getPermissions(), "'permissions' in 'sasValues' cannot be null.");

        this.version = sasValues.getVersion();
        this.protocol = sasValues.getProtocol();
        this.startTime = sasValues.getStartTime();
        this.expiryTime = sasValues.getExpiryTime();
        this.permissions = sasValues.getPermissions();
        this.sasIpRange = sasValues.getSasIpRange();
        this.services = sasValues.getServices();
        this.resourceTypes = sasValues.getResourceTypes();

        if (CoreUtils.isNullOrEmpty(version)) {
            version = StorageConstants.HeaderConstants.TARGET_STORAGE_VERSION;
        }

        String stringToSign = stringToSign(azureNamedKeyCredential);

        // Signature is generated on the un-url-encoded values.
        String signature = computeHmac256(azureNamedKeyCredential.getAzureNamedKey().getKey(), stringToSign);

        this.sas = encode(signature);
    }

    /**
     * Get the SAS produced by this {@link TableAccountSasGenerator}.
     *
     * @return The SAS produced by this {@link TableAccountSasGenerator}.
     */
    public String getSas() {
        return sas;
    }

    private String stringToSign(final AzureNamedKeyCredential azureNamedKeyCredential) {
        return String.join("\n",
            azureNamedKeyCredential.getAzureNamedKey().getName(),
            TableAccountSasPermission.parse(this.permissions).toString(), // guarantees ordering
            this.services,
            resourceTypes,
            this.startTime == null ? "" : StorageConstants.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            StorageConstants.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            this.sasIpRange == null ? "" : this.sasIpRange.toString(),
            this.protocol == null ? "" : this.protocol.toString(),
            this.version,
            "" // Account SAS requires an additional newline character
        );
    }

    private String encode(String signature) {
        /*
         We should be url-encoding each key and each value, but because we know all the keys and values will encode to
         themselves, we cheat except for the signature value.
         */
        StringBuilder sb = new StringBuilder();

        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SERVICE_VERSION, this.version);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SERVICES, this.services);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_RESOURCES_TYPES, this.resourceTypes);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_START_TIME,
            formatQueryParameterDate(this.startTime));
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_EXPIRY_TIME,
            formatQueryParameterDate(this.expiryTime));
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SIGNED_PERMISSIONS, this.permissions);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_IP_RANGE, this.sasIpRange);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_PROTOCOL, this.protocol);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SIGNATURE, signature);

        return sb.toString();
    }
}
