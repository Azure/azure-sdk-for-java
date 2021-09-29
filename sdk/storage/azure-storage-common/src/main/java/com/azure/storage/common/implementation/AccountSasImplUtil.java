// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;

import java.time.OffsetDateTime;

import static com.azure.storage.common.implementation.SasImplUtils.formatQueryParameterDate;
import static com.azure.storage.common.implementation.SasImplUtils.tryAppendQueryParameter;

/**
 * This class provides helper methods for common account sas patterns.
 *
 * RESERVED FOR INTERNAL USE.
 */
public class AccountSasImplUtil {

    private static final ClientLogger LOGGER = new ClientLogger(AccountSasImplUtil.class);

    private static final String VERSION = Constants.SAS_SERVICE_VERSION;

    private SasProtocol protocol;

    private OffsetDateTime startTime;

    private OffsetDateTime expiryTime;

    private String permissions;

    private SasIpRange sasIpRange;

    private String services;

    private String resourceTypes;

    private String encryptionScope;

    /**
     * Creates a new {@link AccountSasImplUtil} with the specified parameters
     *
     * @param sasValues {@link AccountSasSignatureValues}
     * @param encryptionScope An encryption scope that will be applied to any write operations performed with the sas
     */
    public AccountSasImplUtil(AccountSasSignatureValues sasValues, String encryptionScope) {
        this.protocol = sasValues.getProtocol();
        this.startTime = sasValues.getStartTime();
        this.expiryTime = sasValues.getExpiryTime();
        this.permissions = sasValues.getPermissions();
        this.sasIpRange = sasValues.getSasIpRange();
        this.services = sasValues.getServices();
        this.resourceTypes = sasValues.getResourceTypes();
        /*
        Prefer the encryption scope explicitly set on the sas values. If none present, fallback to the value on the
        client.
         */
        this.encryptionScope = sasValues.getEncryptionScope() == null
            ? encryptionScope : sasValues.getEncryptionScope();
    }

    /**
     * Generates a Sas signed with a {@link StorageSharedKeyCredential}
     *
     * @param storageSharedKeyCredentials {@link StorageSharedKeyCredential}
     * @param context Additional context that is passed through the code when generating a SAS.
     * @return A String representing the Sas
     */
    public String generateSas(StorageSharedKeyCredential storageSharedKeyCredentials, Context context) {
        StorageImplUtils.assertNotNull("storageSharedKeyCredentials", storageSharedKeyCredentials);
        StorageImplUtils.assertNotNull("services", this.services);
        StorageImplUtils.assertNotNull("resourceTypes", this.resourceTypes);
        StorageImplUtils.assertNotNull("expiryTime", this.expiryTime);
        StorageImplUtils.assertNotNull("permissions", this.permissions);

        String stringToSign = stringToSign(storageSharedKeyCredentials);
        StorageImplUtils.logStringToSign(LOGGER, stringToSign, context);

        // Signature is generated on the un-url-encoded values.
        String signature = storageSharedKeyCredentials.computeHmac256(stringToSign);

        return encode(signature);
    }

    private String stringToSign(final StorageSharedKeyCredential storageSharedKeyCredentials) {
        return String.join("\n",
            storageSharedKeyCredentials.getAccountName(),
            AccountSasPermission.parse(this.permissions).toString(), // guarantees ordering
            this.services,
            resourceTypes,
            this.startTime == null ? "" : Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.startTime),
            Constants.ISO_8601_UTC_DATE_FORMATTER.format(this.expiryTime),
            this.sasIpRange == null ? "" : this.sasIpRange.toString(),
            this.protocol == null ? "" : this.protocol.toString(),
            VERSION,
            "" // Account SAS requires an additional newline character
        );
    }

    private String encode(String signature) {
        /*
         We should be url-encoding each key and each value, but because we know all the keys and values will encode to
         themselves, we cheat except for the signature value.
         */
        StringBuilder sb = new StringBuilder();

        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SERVICE_VERSION, VERSION);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SERVICES, this.services);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_RESOURCES_TYPES, this.resourceTypes);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_PROTOCOL, this.protocol);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_START_TIME, formatQueryParameterDate(this.startTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_EXPIRY_TIME, formatQueryParameterDate(this.expiryTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_IP_RANGE, this.sasIpRange);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_PERMISSIONS, this.permissions);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNATURE, signature);

        return sb.toString();
    }
}
