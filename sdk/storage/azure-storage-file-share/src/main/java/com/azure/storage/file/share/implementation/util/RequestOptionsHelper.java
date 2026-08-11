// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.implementation.XmlSerializer;
import com.azure.storage.file.share.implementation.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.share.implementation.models.CopyFileSmbInfo;
import com.azure.storage.file.share.implementation.models.DestinationLeaseAccessConditions;
import com.azure.storage.file.share.implementation.models.ListFilesIncludeType;
import com.azure.storage.file.share.implementation.models.ListSharesIncludeType;
import com.azure.storage.file.share.implementation.models.ShareSignedIdentifierWrapper;
import com.azure.storage.file.share.implementation.models.SourceLeaseAccessConditions;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.FilePosixProperties;
import com.azure.storage.file.share.models.FilePropertySemantics;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType;
import com.azure.storage.file.share.options.ShareCreateOptions;
import com.azure.storage.file.share.options.ShareSetPropertiesOptions;

import java.util.List;
import java.util.Map;

/**
 * Builds the {@link RequestOptions} passed to the generated {@code implementation/*Impl} protocol methods.
 * <p>
 * The hand-written {@code Share*} clients call the emitter's protocol {@code xxxWithResponse(RequestOptions)} methods
 * (which target the account-scoped service URL and take all inputs through {@link RequestOptions}). These helpers
 * translate the typed client inputs into the storage wire contract -- {@code x-ms-*} headers, query parameters, and the
 * per-resource URL -- so the migration glue lives here rather than in {@code ModelHelper} (which maps responses).
 */
public final class RequestOptionsHelper {

    private static final HttpHeaderName X_MS_LEASE_ID = HttpHeaderName.fromString("x-ms-lease-id");
    private static final HttpHeaderName X_MS_DELETE_SNAPSHOTS = HttpHeaderName.fromString("x-ms-delete-snapshots");
    private static final HttpHeaderName X_MS_DELETED_SHARE_NAME = HttpHeaderName.fromString("x-ms-deleted-share-name");
    private static final HttpHeaderName X_MS_DELETED_SHARE_VERSION
        = HttpHeaderName.fromString("x-ms-deleted-share-version");
    private static final HttpHeaderName X_MS_FILE_PERMISSION_FORMAT
        = HttpHeaderName.fromString("x-ms-file-permission-format");
    private static final HttpHeaderName X_MS_FILE_PERMISSION = HttpHeaderName.fromString("x-ms-file-permission");
    private static final HttpHeaderName X_MS_FILE_PERMISSION_KEY
        = HttpHeaderName.fromString("x-ms-file-permission-key");
    private static final HttpHeaderName X_MS_FILE_ATTRIBUTES = HttpHeaderName.fromString("x-ms-file-attributes");
    private static final HttpHeaderName X_MS_FILE_CREATION_TIME = HttpHeaderName.fromString("x-ms-file-creation-time");
    private static final HttpHeaderName X_MS_FILE_LAST_WRITE_TIME
        = HttpHeaderName.fromString("x-ms-file-last-write-time");
    private static final HttpHeaderName X_MS_FILE_CHANGE_TIME = HttpHeaderName.fromString("x-ms-file-change-time");
    private static final HttpHeaderName X_MS_OWNER = HttpHeaderName.fromString("x-ms-owner");
    private static final HttpHeaderName X_MS_GROUP = HttpHeaderName.fromString("x-ms-group");
    private static final HttpHeaderName X_MS_MODE = HttpHeaderName.fromString("x-ms-mode");
    private static final HttpHeaderName X_MS_FILE_PROPERTY_SEMANTICS
        = HttpHeaderName.fromString("x-ms-file-property-semantics");
    private static final HttpHeaderName X_MS_RECURSIVE = HttpHeaderName.fromString("x-ms-recursive");
    private static final HttpHeaderName X_MS_FILE_EXTENDED_INFO = HttpHeaderName.fromString("x-ms-file-extended-info");
    private static final HttpHeaderName X_MS_FILE_RENAME_REPLACE_IF_EXISTS
        = HttpHeaderName.fromString("x-ms-file-rename-replace-if-exists");
    private static final HttpHeaderName X_MS_FILE_RENAME_IGNORE_READONLY
        = HttpHeaderName.fromString("x-ms-file-rename-ignore-readonly");
    private static final HttpHeaderName X_MS_SOURCE_LEASE_ID = HttpHeaderName.fromString("x-ms-source-lease-id");
    private static final HttpHeaderName X_MS_DESTINATION_LEASE_ID
        = HttpHeaderName.fromString("x-ms-destination-lease-id");
    private static final HttpHeaderName X_MS_SHARE_QUOTA = HttpHeaderName.fromString("x-ms-share-quota");
    private static final HttpHeaderName X_MS_ACCESS_TIER = HttpHeaderName.fromString("x-ms-access-tier");
    private static final HttpHeaderName X_MS_ENABLED_PROTOCOLS = HttpHeaderName.fromString("x-ms-enabled-protocols");
    private static final HttpHeaderName X_MS_ROOT_SQUASH = HttpHeaderName.fromString("x-ms-root-squash");
    private static final HttpHeaderName X_MS_ENABLE_SNAPSHOT_VIRTUAL_DIRECTORY_ACCESS
        = HttpHeaderName.fromString("x-ms-enable-snapshot-virtual-directory-access");
    private static final HttpHeaderName X_MS_SHARE_PAID_BURSTING_ENABLED
        = HttpHeaderName.fromString("x-ms-share-paid-bursting-enabled");
    private static final HttpHeaderName X_MS_SHARE_PAID_BURSTING_MAX_IOPS
        = HttpHeaderName.fromString("x-ms-share-paid-bursting-max-iops");
    private static final HttpHeaderName X_MS_SHARE_PAID_BURSTING_MAX_BANDWIDTH_MIBPS
        = HttpHeaderName.fromString("x-ms-share-paid-bursting-max-bandwidth-mibps");
    private static final HttpHeaderName X_MS_SHARE_PROVISIONED_IOPS
        = HttpHeaderName.fromString("x-ms-share-provisioned-iops");
    private static final HttpHeaderName X_MS_SHARE_PROVISIONED_BANDWIDTH_MIBPS
        = HttpHeaderName.fromString("x-ms-share-provisioned-bandwidth-mibps");

    private static final ObjectSerializer XML_SERIALIZER = new XmlSerializer();

    /**
     * Serializes an XML request body model (e.g. {@link com.azure.storage.file.share.models.ShareServiceProperties} or
     * {@code KeyInfo}) to {@link BinaryData} for protocol methods that accept the body as an explicit parameter.
     *
     * @param xmlSerializable the XML-serializable model to serialize.
     * @return the serialized request body.
     */
    public static BinaryData serializeToXml(Object xmlSerializable) {
        return BinaryData.fromObject(xmlSerializable, XML_SERIALIZER);
    }

    /**
     * Builds the {@link RequestOptions} for the List Shares Segment operation, wiring the {@code prefix},
     * {@code marker}, {@code maxresults} and {@code include} query parameters onto the account-scoped request.
     *
     * @param prefix filters results to share names beginning with this prefix; may be {@code null}.
     * @param marker continuation token identifying the page to return; may be {@code null}.
     * @param maxResults maximum number of shares to return per page; may be {@code null}.
     * @param include datasets to include in the response; may be {@code null} or empty.
     * @param context the request context.
     * @return the configured request options.
     */
    public static RequestOptions listSharesRequestOptions(String prefix, String marker, Integer maxResults,
        List<ListSharesIncludeType> include, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        if (prefix != null) {
            requestOptions.addQueryParam("prefix", prefix, false);
        }
        if (marker != null) {
            requestOptions.addQueryParam("marker", marker, false);
        }
        if (maxResults != null) {
            requestOptions.addQueryParam("maxresults", String.valueOf(maxResults), false);
        }
        if (include != null && !include.isEmpty()) {
            requestOptions.addQueryParam("include",
                include.stream().map(ListSharesIncludeType::toString).collect(java.util.stream.Collectors.joining(",")),
                false);
        }
        return requestOptions;
    }

    /**
     * The generated protocol methods target the account-scoped service URL; this appends the resource path (e.g.
     * {@code "{shareName}"} or {@code "{shareName}/{filePath}"}) to the request URL while preserving the route's query
     * parameters. Must be added after any {@link #addSnapshot} call so the snapshot query parameter is retained.
     */
    public static void scopeRequestToResourcePath(RequestOptions requestOptions, String resourcePath) {
        requestOptions.addRequestCallback(request -> {
            UrlBuilder urlBuilder = UrlBuilder.parse(request.getUrl());
            urlBuilder.setPath(resourcePath);
            try {
                request.setUrl(urlBuilder.toUrl());
            } catch (java.net.MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /** Adds the {@code x-ms-lease-id} header when a lease id is present. */
    public static void addLeaseId(RequestOptions requestOptions, String leaseId) {
        if (leaseId != null) {
            requestOptions.setHeader(X_MS_LEASE_ID, leaseId);
        }
    }

    /** Adds the {@code x-ms-file-permission-format} header when a format is present. */
    public static void addFilePermissionFormat(RequestOptions requestOptions, FilePermissionFormat format) {
        if (format != null) {
            requestOptions.setHeader(X_MS_FILE_PERMISSION_FORMAT, format.toString());
        }
    }

    /** Adds the {@code sharesnapshot} query parameter when a snapshot is present. */
    public static void addSnapshot(RequestOptions requestOptions, String snapshot) {
        if (snapshot != null) {
            requestOptions.addQueryParam("sharesnapshot", snapshot);
        }
    }

    /** Adds the {@code x-ms-meta-*} headers for each metadata entry. */
    public static void addMetadata(RequestOptions requestOptions, Map<String, String> metadata) {
        if (metadata != null) {
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                requestOptions.setHeader(
                    HttpHeaderName.fromString(Constants.HeaderConstants.X_MS_META + "-" + entry.getKey()),
                    entry.getValue());
            }
        }
    }

    /** Adds the {@code x-ms-delete-snapshots} header when a delete-snapshots option is present. */
    public static void addDeleteSnapshotsHeader(RequestOptions requestOptions, ShareSnapshotsDeleteOptionType option) {
        DeleteSnapshotsOptionType deleteSnapshots = ModelHelper.toDeleteSnapshotsOptionType(option);
        if (deleteSnapshots != null) {
            requestOptions.setHeader(X_MS_DELETE_SNAPSHOTS, deleteSnapshots.toString());
        }
    }

    /**
     * Sets the {@code x-ms-delete-snapshots} header from the wire {@link DeleteSnapshotsOptionType} used by the
     * service-level {@code deleteShare} convenience.
     *
     * @param requestOptions the request options to mutate.
     * @param option the delete-snapshots option; a no-op when {@code null}.
     */
    public static void addDeleteSnapshotsHeader(RequestOptions requestOptions, DeleteSnapshotsOptionType option) {
        if (option != null) {
            requestOptions.setHeader(X_MS_DELETE_SNAPSHOTS, option.toString());
        }
    }

    /**
     * Sets the {@code x-ms-deleted-share-name} and {@code x-ms-deleted-share-version} headers for the
     * {@code undeleteShare} (restore) operation.
     *
     * @param requestOptions the request options to mutate.
     * @param deletedShareName the name of the previously deleted share to restore.
     * @param deletedShareVersion the version of the previously deleted share to restore.
     */
    public static void addUndeleteShareHeaders(RequestOptions requestOptions, String deletedShareName,
        String deletedShareVersion) {
        requestOptions.setHeader(X_MS_DELETED_SHARE_NAME, deletedShareName);
        requestOptions.setHeader(X_MS_DELETED_SHARE_VERSION, deletedShareVersion);
    }

    /**
     * Builds the {@link RequestOptions} for {@code Share.create}: the share-provisioning headers plus optional
     * metadata, scoped to the share resource. {@code enabledProtocols} is pre-computed by the caller.
     */
    public static RequestOptions createShareRequestOptions(String shareName, ShareCreateOptions options,
        String enabledProtocols, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        addMetadata(requestOptions, options.getMetadata());
        addHeader(requestOptions, X_MS_SHARE_QUOTA, options.getQuotaInGb());
        addHeader(requestOptions, X_MS_ACCESS_TIER, options.getAccessTier());
        addHeader(requestOptions, X_MS_ENABLED_PROTOCOLS, enabledProtocols);
        addHeader(requestOptions, X_MS_ROOT_SQUASH, options.getRootSquash());
        addHeader(requestOptions, X_MS_ENABLE_SNAPSHOT_VIRTUAL_DIRECTORY_ACCESS,
            options.isSnapshotVirtualDirectoryAccessEnabled());
        addHeader(requestOptions, X_MS_SHARE_PAID_BURSTING_ENABLED, options.isPaidBurstingEnabled());
        addHeader(requestOptions, X_MS_SHARE_PAID_BURSTING_MAX_BANDWIDTH_MIBPS,
            options.getPaidBurstingMaxBandwidthMibps());
        addHeader(requestOptions, X_MS_SHARE_PAID_BURSTING_MAX_IOPS, options.getPaidBurstingMaxIops());
        addHeader(requestOptions, X_MS_SHARE_PROVISIONED_IOPS, options.getProvisionedMaxIops());
        addHeader(requestOptions, X_MS_SHARE_PROVISIONED_BANDWIDTH_MIBPS, options.getProvisionedMaxBandwidthMibps());
        scopeRequestToResourcePath(requestOptions, shareName);
        return requestOptions;
    }

    /**
     * Builds the {@link RequestOptions} for {@code Share.setProperties}: the share-provisioning headers plus the lease
     * id, scoped to the share resource.
     */
    public static RequestOptions setSharePropertiesRequestOptions(String shareName, ShareSetPropertiesOptions options,
        String leaseId, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        addHeader(requestOptions, X_MS_SHARE_QUOTA, options.getQuotaInGb());
        addHeader(requestOptions, X_MS_ACCESS_TIER, options.getAccessTier());
        addLeaseId(requestOptions, leaseId);
        addHeader(requestOptions, X_MS_ROOT_SQUASH, options.getRootSquash());
        addHeader(requestOptions, X_MS_ENABLE_SNAPSHOT_VIRTUAL_DIRECTORY_ACCESS,
            options.isSnapshotVirtualDirectoryAccessEnabled());
        addHeader(requestOptions, X_MS_SHARE_PAID_BURSTING_ENABLED, options.isPaidBurstingEnabled());
        addHeader(requestOptions, X_MS_SHARE_PAID_BURSTING_MAX_BANDWIDTH_MIBPS,
            options.getPaidBurstingMaxBandwidthMibps());
        addHeader(requestOptions, X_MS_SHARE_PAID_BURSTING_MAX_IOPS, options.getPaidBurstingMaxIops());
        addHeader(requestOptions, X_MS_SHARE_PROVISIONED_IOPS, options.getProvisionedMaxIops());
        addHeader(requestOptions, X_MS_SHARE_PROVISIONED_BANDWIDTH_MIBPS, options.getProvisionedMaxBandwidthMibps());
        scopeRequestToResourcePath(requestOptions, shareName);
        return requestOptions;
    }

    /**
     * Builds the {@link RequestOptions} for {@code Share.setAccessPolicy}: the lease id plus the signed identifiers
     * serialized as the XML request body, scoped to the share resource.
     */
    public static RequestOptions setAccessPolicyRequestOptions(String shareName,
        List<ShareSignedIdentifier> permissions, String leaseId, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        addLeaseId(requestOptions, leaseId);
        requestOptions.setBody(BinaryData.fromObject(new ShareSignedIdentifierWrapper(permissions), XML_SERIALIZER));
        scopeRequestToResourcePath(requestOptions, shareName);
        return requestOptions;
    }

    /** Adds the SMB property headers (permission key, attributes, and the creation/last-write/change times). */
    public static void addSmbProperties(RequestOptions requestOptions, String filePermissionKey,
        String ntfsFileAttributes, String fileCreationTime, String fileLastWriteTime, String fileChangeTime) {
        addHeader(requestOptions, X_MS_FILE_PERMISSION_KEY, filePermissionKey);
        addHeader(requestOptions, X_MS_FILE_ATTRIBUTES, ntfsFileAttributes);
        addHeader(requestOptions, X_MS_FILE_CREATION_TIME, fileCreationTime);
        addHeader(requestOptions, X_MS_FILE_LAST_WRITE_TIME, fileLastWriteTime);
        addHeader(requestOptions, X_MS_FILE_CHANGE_TIME, fileChangeTime);
    }

    /** Adds the NFS POSIX property headers (owner, group, and file mode). */
    public static void addPosixProperties(RequestOptions requestOptions, FilePosixProperties posixProperties) {
        if (posixProperties != null) {
            addHeader(requestOptions, X_MS_OWNER, posixProperties.getOwner());
            addHeader(requestOptions, X_MS_GROUP, posixProperties.getGroup());
            addHeader(requestOptions, X_MS_MODE, posixProperties.getFileMode());
        }
    }

    /**
     * Builds the {@link RequestOptions} for {@code Directory.create}: metadata, file permission, SMB and POSIX
     * properties, and the file-property semantics, scoped to the directory resource.
     */
    public static RequestOptions createDirectoryRequestOptions(String resourcePath, Map<String, String> metadata,
        String filePermission, FilePermissionFormat filePermissionFormat, String filePermissionKey,
        String ntfsFileAttributes, String fileCreationTime, String fileLastWriteTime, String fileChangeTime,
        FilePosixProperties posixProperties, FilePropertySemantics filePropertySemantics, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        addMetadata(requestOptions, metadata);
        addHeader(requestOptions, X_MS_FILE_PERMISSION, filePermission);
        addFilePermissionFormat(requestOptions, filePermissionFormat);
        addSmbProperties(requestOptions, filePermissionKey, ntfsFileAttributes, fileCreationTime, fileLastWriteTime,
            fileChangeTime);
        addPosixProperties(requestOptions, posixProperties);
        addHeader(requestOptions, X_MS_FILE_PROPERTY_SEMANTICS, filePropertySemantics);
        scopeRequestToResourcePath(requestOptions, resourcePath);
        return requestOptions;
    }

    /**
     * Builds the {@link RequestOptions} for {@code Directory.setProperties}: file permission plus SMB and POSIX
     * properties, scoped to the directory resource.
     */
    public static RequestOptions setDirectoryPropertiesRequestOptions(String resourcePath, String filePermission,
        FilePermissionFormat filePermissionFormat, String filePermissionKey, String ntfsFileAttributes,
        String fileCreationTime, String fileLastWriteTime, String fileChangeTime, FilePosixProperties posixProperties,
        Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        addHeader(requestOptions, X_MS_FILE_PERMISSION, filePermission);
        addFilePermissionFormat(requestOptions, filePermissionFormat);
        addSmbProperties(requestOptions, filePermissionKey, ntfsFileAttributes, fileCreationTime, fileLastWriteTime,
            fileChangeTime);
        addPosixProperties(requestOptions, posixProperties);
        scopeRequestToResourcePath(requestOptions, resourcePath);
        return requestOptions;
    }

    /**
     * Builds the {@link RequestOptions} for {@code Directory.listFilesAndDirectoriesSegment}: the prefix, snapshot,
     * marker, maxresults and include query parameters plus the extended-info header, scoped to the directory resource.
     */
    public static RequestOptions listFilesAndDirectoriesRequestOptions(String resourcePath, String prefix,
        String snapshot, String marker, Integer maxResults, List<ListFilesIncludeType> include,
        boolean includeExtendedInfo, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        if (prefix != null) {
            requestOptions.addQueryParam("prefix", prefix, false);
        }
        addSnapshot(requestOptions, snapshot);
        if (marker != null) {
            requestOptions.addQueryParam("marker", marker, false);
        }
        if (maxResults != null) {
            requestOptions.addQueryParam("maxresults", String.valueOf(maxResults), false);
        }
        if (include != null && !include.isEmpty()) {
            requestOptions.addQueryParam("include",
                include.stream().map(ListFilesIncludeType::toString).collect(java.util.stream.Collectors.joining(",")),
                false);
        }
        addHeader(requestOptions, X_MS_FILE_EXTENDED_INFO, includeExtendedInfo);
        scopeRequestToResourcePath(requestOptions, resourcePath);
        return requestOptions;
    }

    /**
     * Builds the {@link RequestOptions} for {@code Directory.listHandles}: the marker, maxresults and snapshot query
     * parameters plus the recursive header, scoped to the directory resource.
     */
    public static RequestOptions listHandlesRequestOptions(String resourcePath, String marker, Integer maxResults,
        String snapshot, boolean recursive, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        if (marker != null) {
            requestOptions.addQueryParam("marker", marker, false);
        }
        if (maxResults != null) {
            requestOptions.addQueryParam("maxresults", String.valueOf(maxResults), false);
        }
        addSnapshot(requestOptions, snapshot);
        addHeader(requestOptions, X_MS_RECURSIVE, recursive);
        scopeRequestToResourcePath(requestOptions, resourcePath);
        return requestOptions;
    }

    /**
     * Builds the {@link RequestOptions} for {@code Directory.forceCloseHandles}: the marker and snapshot query
     * parameters plus the recursive header, scoped to the directory resource. The handle id is passed to the protocol
     * method as an explicit parameter.
     */
    public static RequestOptions forceCloseHandlesRequestOptions(String resourcePath, String marker, String snapshot,
        boolean recursive, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        if (marker != null) {
            requestOptions.addQueryParam("marker", marker, false);
        }
        addSnapshot(requestOptions, snapshot);
        addHeader(requestOptions, X_MS_RECURSIVE, recursive);
        scopeRequestToResourcePath(requestOptions, resourcePath);
        return requestOptions;
    }

    /**
     * Builds the {@link RequestOptions} for {@code Directory.rename}: the rename flags, permission, metadata, source
     * and destination lease ids, and the copy SMB info headers, scoped to the destination directory resource. The
     * rename source is passed to the protocol method as an explicit parameter.
     */
    public static RequestOptions renameDirectoryRequestOptions(String resourcePath, Boolean replaceIfExists,
        Boolean ignoreReadOnly, String filePermission, FilePermissionFormat filePermissionFormat,
        String filePermissionKey, Map<String, String> metadata, SourceLeaseAccessConditions sourceConditions,
        DestinationLeaseAccessConditions destinationConditions, CopyFileSmbInfo smbInfo, Context context) {
        RequestOptions requestOptions = new RequestOptions().setContext(context);
        addHeader(requestOptions, X_MS_FILE_RENAME_REPLACE_IF_EXISTS, replaceIfExists);
        addHeader(requestOptions, X_MS_FILE_RENAME_IGNORE_READONLY, ignoreReadOnly);
        addHeader(requestOptions, X_MS_FILE_PERMISSION, filePermission);
        addFilePermissionFormat(requestOptions, filePermissionFormat);
        addHeader(requestOptions, X_MS_FILE_PERMISSION_KEY, filePermissionKey);
        addMetadata(requestOptions, metadata);
        if (sourceConditions != null) {
            addHeader(requestOptions, X_MS_SOURCE_LEASE_ID, sourceConditions.getSourceLeaseId());
        }
        if (destinationConditions != null) {
            addHeader(requestOptions, X_MS_DESTINATION_LEASE_ID, destinationConditions.getDestinationLeaseId());
        }
        if (smbInfo != null) {
            addHeader(requestOptions, X_MS_FILE_ATTRIBUTES, smbInfo.getFileAttributes());
            addHeader(requestOptions, X_MS_FILE_CREATION_TIME, smbInfo.getFileCreationTime());
            addHeader(requestOptions, X_MS_FILE_LAST_WRITE_TIME, smbInfo.getFileLastWriteTime());
            addHeader(requestOptions, X_MS_FILE_CHANGE_TIME, smbInfo.getFileChangeTime());
        }
        scopeRequestToResourcePath(requestOptions, resourcePath);
        return requestOptions;
    }

    /** Sets {@code name} to {@code String.valueOf(value)} when {@code value} is non-null. */
    private static void addHeader(RequestOptions requestOptions, HttpHeaderName name, Object value) {
        if (value != null) {
            requestOptions.setHeader(name, String.valueOf(value));
        }
    }

    private RequestOptionsHelper() {
    }
}
