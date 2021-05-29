// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.sas.TableAccountSasPermission;
import com.azure.data.tables.sas.TableAccountSasSignatureValues;
import com.azure.data.tables.sas.TableSasIpRange;
import com.azure.data.tables.sas.TableSasProtocol;

import java.time.OffsetDateTime;
import java.util.Objects;

import static com.azure.data.tables.implementation.TableSasUtils.computeHMac256;
import static com.azure.data.tables.implementation.TableSasUtils.formatQueryParameterDate;
import static com.azure.data.tables.implementation.TableSasUtils.logStringToSign;
import static com.azure.data.tables.implementation.TableSasUtils.tryAppendQueryParameter;

/**
 * A class containing utility methods for generating SAS tokens for the Azure Storage accounts.
 */
public class TableAccountSasGenerator {
    private final ClientLogger logger = new ClientLogger(TableAccountSasGenerator.class);
    private String version;
    private TableSasProtocol protocol;
    private OffsetDateTime startTime;
    private OffsetDateTime expiryTime;
    private String permissions;
    private TableSasIpRange sasIpRange;
    private String services;
    private String resourceTypes;

    /**
     * Creates a new {@link TableAccountSasGenerator} with the specified parameters.
     *
     * @param sasValues The {@link TableAccountSasSignatureValues account signature values}.
     */
    public TableAccountSasGenerator(TableAccountSasSignatureValues sasValues) {
        this.version = sasValues.getVersion();
        this.protocol = sasValues.getProtocol();
        this.startTime = sasValues.getStartTime();
        this.expiryTime = sasValues.getExpiryTime();
        this.permissions = sasValues.getPermissions();
        this.sasIpRange = sasValues.getSasIpRange();
        this.services = sasValues.getServices();
        this.resourceTypes = sasValues.getResourceTypes();
    }

    /**
     * Generates a Sas signed with a {@link AzureNamedKeyCredential}.
     *
     * @param azureNamedKeyCredential {@link AzureNamedKeyCredential}
     * @param context Additional context that is passed through the code when generating a SAS.
     * @return A String representing the Sas
     */
    public String generateSas(AzureNamedKeyCredential azureNamedKeyCredential, Context context) {
        Objects.requireNonNull(azureNamedKeyCredential, "'azureNamedKeyCredential' cannot be null.");
        Objects.requireNonNull(services, "'services' cannot be null.");
        Objects.requireNonNull(resourceTypes, "'resourceTypes' cannot be null.");
        Objects.requireNonNull(expiryTime, "'expiryTime' cannot be null.");
        Objects.requireNonNull(permissions, "'permissions' cannot be null.");

        if (CoreUtils.isNullOrEmpty(version)) {
            version = StorageConstants.HeaderConstants.TARGET_STORAGE_VERSION;
        }
        String stringToSign = stringToSign(azureNamedKeyCredential);
        logStringToSign(logger, stringToSign, context);

        // Signature is generated on the un-url-encoded values.
        String signature = computeHMac256(azureNamedKeyCredential.getAzureNamedKey().getKey(), stringToSign);

        return encode(signature);
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
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_PROTOCOL, this.protocol);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_START_TIME,
            formatQueryParameterDate(this.startTime));
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_EXPIRY_TIME,
            formatQueryParameterDate(this.expiryTime));
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_IP_RANGE, this.sasIpRange);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SIGNED_PERMISSIONS, this.permissions);
        tryAppendQueryParameter(sb, StorageConstants.UrlConstants.SAS_SIGNATURE, signature);

        return sb.toString();
    }
}
