package com.azure.v2.data.appconfiguration.implementation;

import com.azure.v2.data.appconfiguration.implementation.models.KeyListResult;
import com.azure.v2.data.appconfiguration.implementation.models.KeyValueListResult;
import com.azure.v2.data.appconfiguration.implementation.models.LabelListResult;
import com.azure.v2.data.appconfiguration.implementation.models.SnapshotListResult;
import com.azure.v2.data.appconfiguration.models.Error;
import com.azure.v2.data.appconfiguration.models.KeyValue;
import com.azure.v2.data.appconfiguration.models.Snapshot;
import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;

import java.lang.reflect.InvocationTargetException;

@ServiceInterface(name = "AzureAppConfiguratio", host = "{endpoint}")
public interface AzureAppConfigurationClientService {
    static AzureAppConfigurationClientService getNewInstance(HttpPipeline pipeline, ObjectSerializer serializer,
                                                             @HostParam("endpoint") String endpoint, @HeaderParam("Sync-Token") String syncToken,
                                                             @HeaderParam("Accept") String accept, RequestOptions requestOptions) {
        try {
            Class<?> clazz = Class.forName(
                "com.azure.v2.data.appconfiguration.implementation.AzureAppConfigurationClientServiceImpl");
            return (AzureAppConfigurationClientService) clazz
                .getMethod("getNewInstance", HttpPipeline.class, ObjectSerializer.class, String.class, String.class,
                    String.class, RequestOptions.class)
                .invoke(null, pipeline, serializer, endpoint, syncToken, accept, requestOptions);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    @HttpRequestInformation(method = HttpMethod.GET, path = "/keys", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyListResult> getKeys(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.HEAD, path = "/keys", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Void> checkKeys(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.GET, path = "/kv", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyValueListResult> getKeyValues(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.HEAD, path = "/kv", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Void> checkKeyValues(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.GET, path = "/kv/{key}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyValue> getKeyValue(@QueryParam("api-version") String apiVersion, @PathParam("key") String key);

    @HttpRequestInformation(method = HttpMethod.PUT, path = "/kv/{key}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyValue> putKeyValue(@QueryParam("api-version") String apiVersion, @PathParam("key") String key);

    @HttpRequestInformation(method = HttpMethod.DELETE, path = "/kv/{key}", expectedStatusCodes = { 200, 204 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyValue> deleteKeyValue(@QueryParam("api-version") String apiVersion, @PathParam("key") String key);

    @HttpRequestInformation(method = HttpMethod.HEAD, path = "/kv/{key}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Void> checkKeyValue(@QueryParam("api-version") String apiVersion, @PathParam("key") String key);

    @HttpRequestInformation(method = HttpMethod.GET, path = "/snapshots", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<SnapshotListResult> getSnapshots(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.HEAD, path = "/snapshots", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Void> checkSnapshots(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.GET, path = "/snapshots/{name}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Snapshot> getSnapshot(@QueryParam("api-version") String apiVersion, @PathParam("name") String name);

    @HttpRequestInformation(method = HttpMethod.PATCH, path = "/snapshots/{name}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Snapshot> updateSnapshot(@QueryParam("api-version") String apiVersion,
                                      @HeaderParam("Content-Type") String contentType, @PathParam("name") String name,
                                      @BodyParam("application/json") BinaryData entity);

    @HttpRequestInformation(method = HttpMethod.HEAD, path = "/snapshots/{name}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Void> checkSnapshot(@QueryParam("api-version") String apiVersion, @PathParam("name") String name);

    @HttpRequestInformation(method = HttpMethod.GET, path = "/labels", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<LabelListResult> getLabels(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.HEAD, path = "/labels", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Void> checkLabels(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.PUT, path = "/locks/{key}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyValue> putLock(@QueryParam("api-version") String apiVersion, @PathParam("key") String key);

    @HttpRequestInformation(method = HttpMethod.DELETE, path = "/locks/{key}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyValue> deleteLock(@QueryParam("api-version") String apiVersion, @PathParam("key") String key);

    @HttpRequestInformation(method = HttpMethod.GET, path = "/revisions", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyValueListResult> getRevisions(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.HEAD, path = "/revisions", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<Void> checkRevisions(@QueryParam("api-version") String apiVersion);

    @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyListResult> getKeysNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

    @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyValueListResult> getKeyValuesNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

    @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<SnapshotListResult> getSnapshotsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

    @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<LabelListResult> getLabelsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);

    @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}", expectedStatusCodes = { 200 })
    @UnexpectedResponseExceptionDetail(exceptionBodyClass = Error.class)
    Response<KeyValueListResult> getRevisionsNext(@PathParam(value = "nextLink", encoded = true) String nextLink);
}
