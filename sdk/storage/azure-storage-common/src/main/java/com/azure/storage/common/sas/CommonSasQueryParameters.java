// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.sas;

import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;

import java.time.OffsetDateTime;
import java.util.Map;

public class CommonSasQueryParameters extends BaseSasQueryParameters {

    private final String services;

    private final String resourceTypes;

    private final String identifier;

    private final String keyObjectId;

    private final String keyTenantId;

    private final OffsetDateTime keyStart;

    private final OffsetDateTime keyExpiry;

    private final String keyService;

    private final String keyVersion;

    private final String resource;

    private final String cacheControl;

    private final String contentDisposition;

    private final String contentEncoding;

    private final String contentLanguage;

    private final String contentType;

    /**
     * Creates a new {@link AccountSasQueryParameters} object.
     *
     * @param queryParamsMap All query parameters for the request as key-value pairs
     * @param removeSasParametersFromMap When {@code true}, the SAS query parameters will be removed from
     * queryParamsMap
     */
    public CommonSasQueryParameters(Map<String, String[]> queryParamsMap, boolean removeSasParametersFromMap) {
        super(queryParamsMap, removeSasParametersFromMap);
        this.resourceTypes = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_RESOURCES_TYPES,
            removeSasParametersFromMap);
        this.services = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_SERVICES,
            removeSasParametersFromMap);
        this.identifier = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_SIGNED_IDENTIFIER,
            removeSasParametersFromMap);
        this.keyObjectId = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_SIGNED_OBJECT_ID,
            removeSasParametersFromMap);
        this.keyTenantId = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_SIGNED_TENANT_ID,
            removeSasParametersFromMap);
        this.keyStart = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_SIGNED_KEY_START,
            removeSasParametersFromMap, Utility::parseDate);
        this.keyExpiry = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_SIGNED_KEY_EXPIRY,
            removeSasParametersFromMap, Utility::parseDate);
        this.keyService = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_SIGNED_KEY_SERVICE,
            removeSasParametersFromMap);
        this.keyVersion = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_SIGNED_KEY_VERSION,
            removeSasParametersFromMap);
        this.resource = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_SIGNED_RESOURCE,
            removeSasParametersFromMap);
        this.cacheControl = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_CACHE_CONTROL,
            removeSasParametersFromMap);
        this.contentDisposition = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_CONTENT_DISPOSITION,
            removeSasParametersFromMap);
        this.contentEncoding = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_CONTENT_ENCODING,
            removeSasParametersFromMap);
        this.contentLanguage = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_CONTENT_LANGUAGE,
            removeSasParametersFromMap);
        this.contentType = getQueryParameter(queryParamsMap, Constants.UrlConstants.SAS_CONTENT_TYPE,
            removeSasParametersFromMap);
    }

    @Override
    public String encode() {
                /*
         We should be url-encoding each key and each value, but because we know all the keys and values will encode to
         themselves, we cheat except for the signature value.
         */
        StringBuilder sb = new StringBuilder();

        // Common
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SERVICE_VERSION, this.version);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_PROTOCOL, this.protocol);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_START_TIME, formatQueryParameterDate(this.startTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_EXPIRY_TIME, formatQueryParameterDate(this.expiryTime));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_IP_RANGE, this.sasIpRange);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_PERMISSIONS, this.permissions);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNATURE, this.signature);

        // Account
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SERVICES, this.services);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_RESOURCES_TYPES, this.resourceTypes);

        // Services
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_IDENTIFIER, this.identifier);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_OBJECT_ID, this.keyObjectId);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_TENANT_ID, this.keyTenantId);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_START,
            formatQueryParameterDate(this.keyStart));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_EXPIRY,
            formatQueryParameterDate(this.keyExpiry));
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_SERVICE, this.keyService);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_KEY_VERSION, this.keyVersion);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_SIGNED_RESOURCE, this.resource);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CACHE_CONTROL, this.cacheControl);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_DISPOSITION, this.contentDisposition);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_ENCODING, this.contentEncoding);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_LANGUAGE, this.contentLanguage);
        tryAppendQueryParameter(sb, Constants.UrlConstants.SAS_CONTENT_TYPE, this.contentType);

        return sb.toString();
    }
}
