/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import retrofit2.Retrofit;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.DateTimeRfc1123;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.ServiceResponseEmptyCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import org.joda.time.DateTime;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in Pools.
 */
public final class PoolsInner {
    /** The Retrofit service to perform REST calls. */
    private PoolsService service;
    /** The service client containing this operation class. */
    private BatchServiceClientImpl client;

    /**
     * Initializes an instance of PoolsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public PoolsInner(Retrofit retrofit, BatchServiceClientImpl client) {
        this.service = retrofit.create(PoolsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Pools to be
     * used by Retrofit to perform actually REST calls.
     */
    interface PoolsService {
        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("poolusagemetrics")
        Call<ResponseBody> listPoolUsageMetrics(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("starttime") DateTime startTime, @Query("endtime") DateTime endTime, @Query("$filter") String filter, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("lifetimepoolstats")
        Call<ResponseBody> getAllPoolsLifetimeStatistics(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools")
        Call<ResponseBody> add(@Body PoolAddParameterInner pool, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools")
        Call<ResponseBody> list(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("$expand") String expand, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HTTP(path = "pools/{poolId}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("poolId") String poolId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HEAD("pools/{poolId}")
        Call<Void> exists(@Path("poolId") String poolId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("pools/{poolId}")
        Call<ResponseBody> get(@Path("poolId") String poolId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("$expand") String expand, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @PATCH("pools/{poolId}")
        Call<ResponseBody> patch(@Path("poolId") String poolId, @Body PoolPatchParameterInner poolPatchParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/disableautoscale")
        Call<ResponseBody> disableAutoScale(@Path("poolId") String poolId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/enableautoscale")
        Call<ResponseBody> enableAutoScale(@Path("poolId") String poolId, @Body PoolEnableAutoScaleParameterInner poolEnableAutoScaleParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/evaluateautoscale")
        Call<ResponseBody> evaluateAutoScale(@Path("poolId") String poolId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Body PoolEvaluateAutoScaleParameter poolEvaluateAutoScaleParameter);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/resize")
        Call<ResponseBody> resize(@Path("poolId") String poolId, @Body PoolResizeParameterInner poolResizeParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/stopresize")
        Call<ResponseBody> stopResize(@Path("poolId") String poolId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/updateproperties")
        Call<ResponseBody> updateProperties(@Path("poolId") String poolId, @Body PoolUpdatePropertiesParameterInner poolUpdatePropertiesParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/upgradeos")
        Call<ResponseBody> upgradeOS(@Path("poolId") String poolId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince, @Body PoolUpgradeOSParameter poolUpgradeOSParameter);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("pools/{poolId}/removenodes")
        Call<ResponseBody> removeNodes(@Path("poolId") String poolId, @Body NodeRemoveParameterInner nodeRemoveParameter, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate, @Header("If-Match") String ifMatch, @Header("If-None-Match") String ifNoneMatch, @Header("If-Modified-Since") DateTimeRfc1123 ifModifiedSince, @Header("If-Unmodified-Since") DateTimeRfc1123 ifUnmodifiedSince);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listPoolUsageMetricsNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

    }

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PoolUsageMetricsInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> listPoolUsageMetrics() throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final PoolListPoolUsageMetricsOptionsInner poolListPoolUsageMetricsOptions = null;
        DateTime startTime = null;
        DateTime endTime = null;
        String filter = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listPoolUsageMetrics(this.client.apiVersion(), this.client.acceptLanguage(), startTime, endTime, filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> response = listPoolUsageMetricsDelegate(call.execute());
        PagedList<PoolUsageMetricsInner> result = new PagedList<PoolUsageMetricsInner>(response.getBody()) {
            @Override
            public Page<PoolUsageMetricsInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listPoolUsageMetricsNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listPoolUsageMetricsAsync(final ListOperationCallback<PoolUsageMetricsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final PoolListPoolUsageMetricsOptionsInner poolListPoolUsageMetricsOptions = null;
        DateTime startTime = null;
        DateTime endTime = null;
        String filter = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listPoolUsageMetrics(this.client.apiVersion(), this.client.acceptLanguage(), startTime, endTime, filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PoolUsageMetricsInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> result = listPoolUsageMetricsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listPoolUsageMetricsNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param poolListPoolUsageMetricsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PoolUsageMetricsInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> listPoolUsageMetrics(final PoolListPoolUsageMetricsOptionsInner poolListPoolUsageMetricsOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolListPoolUsageMetricsOptions);
        DateTime startTime = null;
        if (poolListPoolUsageMetricsOptions != null) {
            startTime = poolListPoolUsageMetricsOptions.startTime();
        }
        DateTime endTime = null;
        if (poolListPoolUsageMetricsOptions != null) {
            endTime = poolListPoolUsageMetricsOptions.endTime();
        }
        String filter = null;
        if (poolListPoolUsageMetricsOptions != null) {
            filter = poolListPoolUsageMetricsOptions.filter();
        }
        Integer maxResults = null;
        if (poolListPoolUsageMetricsOptions != null) {
            maxResults = poolListPoolUsageMetricsOptions.maxResults();
        }
        Integer timeout = null;
        if (poolListPoolUsageMetricsOptions != null) {
            timeout = poolListPoolUsageMetricsOptions.timeout();
        }
        String clientRequestId = null;
        if (poolListPoolUsageMetricsOptions != null) {
            clientRequestId = poolListPoolUsageMetricsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolListPoolUsageMetricsOptions != null) {
            returnClientRequestId = poolListPoolUsageMetricsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolListPoolUsageMetricsOptions != null) {
            ocpDate = poolListPoolUsageMetricsOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listPoolUsageMetrics(this.client.apiVersion(), this.client.acceptLanguage(), startTime, endTime, filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> response = listPoolUsageMetricsDelegate(call.execute());
        PagedList<PoolUsageMetricsInner> result = new PagedList<PoolUsageMetricsInner>(response.getBody()) {
            @Override
            public Page<PoolUsageMetricsInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                PoolListPoolUsageMetricsNextOptionsInner poolListPoolUsageMetricsNextOptions = null;
                if (poolListPoolUsageMetricsOptions != null) {
                    poolListPoolUsageMetricsNextOptions = new PoolListPoolUsageMetricsNextOptionsInner();
                    poolListPoolUsageMetricsNextOptions.setClientRequestId(poolListPoolUsageMetricsOptions.clientRequestId());
                    poolListPoolUsageMetricsNextOptions.setReturnClientRequestId(poolListPoolUsageMetricsOptions.returnClientRequestId());
                    poolListPoolUsageMetricsNextOptions.setOcpDate(poolListPoolUsageMetricsOptions.ocpDate());
                }
                return listPoolUsageMetricsNext(nextPageLink, poolListPoolUsageMetricsNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param poolListPoolUsageMetricsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listPoolUsageMetricsAsync(final PoolListPoolUsageMetricsOptionsInner poolListPoolUsageMetricsOptions, final ListOperationCallback<PoolUsageMetricsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolListPoolUsageMetricsOptions, serviceCallback);
        DateTime startTime = null;
        if (poolListPoolUsageMetricsOptions != null) {
            startTime = poolListPoolUsageMetricsOptions.startTime();
        }
        DateTime endTime = null;
        if (poolListPoolUsageMetricsOptions != null) {
            endTime = poolListPoolUsageMetricsOptions.endTime();
        }
        String filter = null;
        if (poolListPoolUsageMetricsOptions != null) {
            filter = poolListPoolUsageMetricsOptions.filter();
        }
        Integer maxResults = null;
        if (poolListPoolUsageMetricsOptions != null) {
            maxResults = poolListPoolUsageMetricsOptions.maxResults();
        }
        Integer timeout = null;
        if (poolListPoolUsageMetricsOptions != null) {
            timeout = poolListPoolUsageMetricsOptions.timeout();
        }
        String clientRequestId = null;
        if (poolListPoolUsageMetricsOptions != null) {
            clientRequestId = poolListPoolUsageMetricsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolListPoolUsageMetricsOptions != null) {
            returnClientRequestId = poolListPoolUsageMetricsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolListPoolUsageMetricsOptions != null) {
            ocpDate = poolListPoolUsageMetricsOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listPoolUsageMetrics(this.client.apiVersion(), this.client.acceptLanguage(), startTime, endTime, filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<PoolUsageMetricsInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> result = listPoolUsageMetricsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        PoolListPoolUsageMetricsNextOptionsInner poolListPoolUsageMetricsNextOptions = null;
                        if (poolListPoolUsageMetricsOptions != null) {
                            poolListPoolUsageMetricsNextOptions = new PoolListPoolUsageMetricsNextOptionsInner();
                            poolListPoolUsageMetricsNextOptions.setClientRequestId(poolListPoolUsageMetricsOptions.clientRequestId());
                            poolListPoolUsageMetricsNextOptions.setReturnClientRequestId(poolListPoolUsageMetricsOptions.returnClientRequestId());
                            poolListPoolUsageMetricsNextOptions.setOcpDate(poolListPoolUsageMetricsOptions.ocpDate());
                        }
                        listPoolUsageMetricsNextAsync(result.getBody().getNextPageLink(), poolListPoolUsageMetricsNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> listPoolUsageMetricsDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PoolUsageMetricsInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PoolUsageMetricsInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolListPoolUsageMetricsHeadersInner.class);
    }

    /**
     * Gets lifetime summary statistics for all of the pools in the specified account. Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PoolStatisticsInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PoolStatisticsInner, PoolGetAllPoolsLifetimeStatisticsHeadersInner> getAllPoolsLifetimeStatistics() throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final PoolGetAllPoolsLifetimeStatisticsOptionsInner poolGetAllPoolsLifetimeStatisticsOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.getAllPoolsLifetimeStatistics(this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getAllPoolsLifetimeStatisticsDelegate(call.execute());
    }

    /**
     * Gets lifetime summary statistics for all of the pools in the specified account. Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAllPoolsLifetimeStatisticsAsync(final ServiceCallback<PoolStatisticsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final PoolGetAllPoolsLifetimeStatisticsOptionsInner poolGetAllPoolsLifetimeStatisticsOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.getAllPoolsLifetimeStatistics(this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PoolStatisticsInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getAllPoolsLifetimeStatisticsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets lifetime summary statistics for all of the pools in the specified account. Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param poolGetAllPoolsLifetimeStatisticsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the PoolStatisticsInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PoolStatisticsInner, PoolGetAllPoolsLifetimeStatisticsHeadersInner> getAllPoolsLifetimeStatistics(PoolGetAllPoolsLifetimeStatisticsOptionsInner poolGetAllPoolsLifetimeStatisticsOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolGetAllPoolsLifetimeStatisticsOptions);
        Integer timeout = null;
        if (poolGetAllPoolsLifetimeStatisticsOptions != null) {
            timeout = poolGetAllPoolsLifetimeStatisticsOptions.timeout();
        }
        String clientRequestId = null;
        if (poolGetAllPoolsLifetimeStatisticsOptions != null) {
            clientRequestId = poolGetAllPoolsLifetimeStatisticsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolGetAllPoolsLifetimeStatisticsOptions != null) {
            returnClientRequestId = poolGetAllPoolsLifetimeStatisticsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolGetAllPoolsLifetimeStatisticsOptions != null) {
            ocpDate = poolGetAllPoolsLifetimeStatisticsOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.getAllPoolsLifetimeStatistics(this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getAllPoolsLifetimeStatisticsDelegate(call.execute());
    }

    /**
     * Gets lifetime summary statistics for all of the pools in the specified account. Statistics are aggregated across all pools that have ever existed in the account, from account creation to the last update time of the statistics.
     *
     * @param poolGetAllPoolsLifetimeStatisticsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAllPoolsLifetimeStatisticsAsync(PoolGetAllPoolsLifetimeStatisticsOptionsInner poolGetAllPoolsLifetimeStatisticsOptions, final ServiceCallback<PoolStatisticsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolGetAllPoolsLifetimeStatisticsOptions, serviceCallback);
        Integer timeout = null;
        if (poolGetAllPoolsLifetimeStatisticsOptions != null) {
            timeout = poolGetAllPoolsLifetimeStatisticsOptions.timeout();
        }
        String clientRequestId = null;
        if (poolGetAllPoolsLifetimeStatisticsOptions != null) {
            clientRequestId = poolGetAllPoolsLifetimeStatisticsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolGetAllPoolsLifetimeStatisticsOptions != null) {
            returnClientRequestId = poolGetAllPoolsLifetimeStatisticsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolGetAllPoolsLifetimeStatisticsOptions != null) {
            ocpDate = poolGetAllPoolsLifetimeStatisticsOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.getAllPoolsLifetimeStatistics(this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<PoolStatisticsInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getAllPoolsLifetimeStatisticsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PoolStatisticsInner, PoolGetAllPoolsLifetimeStatisticsHeadersInner> getAllPoolsLifetimeStatisticsDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PoolStatisticsInner, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PoolStatisticsInner>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolGetAllPoolsLifetimeStatisticsHeadersInner.class);
    }

    /**
     * Adds a pool to the specified account.
     *
     * @param pool Specifies the pool to be added.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolAddHeadersInner> add(PoolAddParameterInner pool) throws BatchErrorException, IOException, IllegalArgumentException {
        if (pool == null) {
            throw new IllegalArgumentException("Parameter pool is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(pool);
        final PoolAddOptionsInner poolAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(pool, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addDelegate(call.execute());
    }

    /**
     * Adds a pool to the specified account.
     *
     * @param pool Specifies the pool to be added.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(PoolAddParameterInner pool, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (pool == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter pool is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(pool, serviceCallback);
        final PoolAddOptionsInner poolAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(pool, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(addDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Adds a pool to the specified account.
     *
     * @param pool Specifies the pool to be added.
     * @param poolAddOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolAddHeadersInner> add(PoolAddParameterInner pool, PoolAddOptionsInner poolAddOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (pool == null) {
            throw new IllegalArgumentException("Parameter pool is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(pool);
        Validator.validate(poolAddOptions);
        Integer timeout = null;
        if (poolAddOptions != null) {
            timeout = poolAddOptions.timeout();
        }
        String clientRequestId = null;
        if (poolAddOptions != null) {
            clientRequestId = poolAddOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolAddOptions != null) {
            returnClientRequestId = poolAddOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolAddOptions != null) {
            ocpDate = poolAddOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(pool, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addDelegate(call.execute());
    }

    /**
     * Adds a pool to the specified account.
     *
     * @param pool Specifies the pool to be added.
     * @param poolAddOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(PoolAddParameterInner pool, PoolAddOptionsInner poolAddOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (pool == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter pool is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(pool, serviceCallback);
        Validator.validate(poolAddOptions, serviceCallback);
        Integer timeout = null;
        if (poolAddOptions != null) {
            timeout = poolAddOptions.timeout();
        }
        String clientRequestId = null;
        if (poolAddOptions != null) {
            clientRequestId = poolAddOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolAddOptions != null) {
            returnClientRequestId = poolAddOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolAddOptions != null) {
            ocpDate = poolAddOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.add(pool, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(addDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolAddHeadersInner> addDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolAddHeadersInner.class);
    }

    /**
     * Lists all of the pools in the specified account.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudPoolInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudPoolInner>, PoolListHeadersInner> list() throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final PoolListOptionsInner poolListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> response = listDelegate(call.execute());
        PagedList<CloudPoolInner> result = new PagedList<CloudPoolInner>(response.getBody()) {
            @Override
            public Page<CloudPoolInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the pools in the specified account.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final ListOperationCallback<CloudPoolInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final PoolListOptionsInner poolListOptions = null;
        String filter = null;
        String select = null;
        String expand = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudPoolInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists all of the pools in the specified account.
     *
     * @param poolListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudPoolInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<CloudPoolInner>, PoolListHeadersInner> list(final PoolListOptionsInner poolListOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolListOptions);
        String filter = null;
        if (poolListOptions != null) {
            filter = poolListOptions.filter();
        }
        String select = null;
        if (poolListOptions != null) {
            select = poolListOptions.select();
        }
        String expand = null;
        if (poolListOptions != null) {
            expand = poolListOptions.expand();
        }
        Integer maxResults = null;
        if (poolListOptions != null) {
            maxResults = poolListOptions.maxResults();
        }
        Integer timeout = null;
        if (poolListOptions != null) {
            timeout = poolListOptions.timeout();
        }
        String clientRequestId = null;
        if (poolListOptions != null) {
            clientRequestId = poolListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolListOptions != null) {
            returnClientRequestId = poolListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolListOptions != null) {
            ocpDate = poolListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> response = listDelegate(call.execute());
        PagedList<CloudPoolInner> result = new PagedList<CloudPoolInner>(response.getBody()) {
            @Override
            public Page<CloudPoolInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                PoolListNextOptionsInner poolListNextOptions = null;
                if (poolListOptions != null) {
                    poolListNextOptions = new PoolListNextOptionsInner();
                    poolListNextOptions.setClientRequestId(poolListOptions.clientRequestId());
                    poolListNextOptions.setReturnClientRequestId(poolListOptions.returnClientRequestId());
                    poolListNextOptions.setOcpDate(poolListOptions.ocpDate());
                }
                return listNext(nextPageLink, poolListNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the pools in the specified account.
     *
     * @param poolListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final PoolListOptionsInner poolListOptions, final ListOperationCallback<CloudPoolInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolListOptions, serviceCallback);
        String filter = null;
        if (poolListOptions != null) {
            filter = poolListOptions.filter();
        }
        String select = null;
        if (poolListOptions != null) {
            select = poolListOptions.select();
        }
        String expand = null;
        if (poolListOptions != null) {
            expand = poolListOptions.expand();
        }
        Integer maxResults = null;
        if (poolListOptions != null) {
            maxResults = poolListOptions.maxResults();
        }
        Integer timeout = null;
        if (poolListOptions != null) {
            timeout = poolListOptions.timeout();
        }
        String clientRequestId = null;
        if (poolListOptions != null) {
            clientRequestId = poolListOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolListOptions != null) {
            returnClientRequestId = poolListOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolListOptions != null) {
            ocpDate = poolListOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.list(this.client.apiVersion(), this.client.acceptLanguage(), filter, select, expand, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudPoolInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        PoolListNextOptionsInner poolListNextOptions = null;
                        if (poolListOptions != null) {
                            poolListNextOptions = new PoolListNextOptionsInner();
                            poolListNextOptions.setClientRequestId(poolListOptions.clientRequestId());
                            poolListNextOptions.setReturnClientRequestId(poolListOptions.returnClientRequestId());
                            poolListNextOptions.setOcpDate(poolListOptions.ocpDate());
                        }
                        listNextAsync(result.getBody().getNextPageLink(), poolListNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> listDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudPoolInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudPoolInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolListHeadersInner.class);
    }

    /**
     * Deletes a pool from the specified account.
     *
     * @param poolId The id of the pool to delete.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolDeleteHeadersInner> delete(String poolId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final PoolDeleteOptionsInner poolDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.delete(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a pool from the specified account.
     *
     * @param poolId The id of the pool to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String poolId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final PoolDeleteOptionsInner poolDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.delete(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Deletes a pool from the specified account.
     *
     * @param poolId The id of the pool to delete.
     * @param poolDeleteOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolDeleteHeadersInner> delete(String poolId, PoolDeleteOptionsInner poolDeleteOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolDeleteOptions);
        Integer timeout = null;
        if (poolDeleteOptions != null) {
            timeout = poolDeleteOptions.timeout();
        }
        String clientRequestId = null;
        if (poolDeleteOptions != null) {
            clientRequestId = poolDeleteOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolDeleteOptions != null) {
            returnClientRequestId = poolDeleteOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolDeleteOptions != null) {
            ocpDate = poolDeleteOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolDeleteOptions != null) {
            ifMatch = poolDeleteOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolDeleteOptions != null) {
            ifNoneMatch = poolDeleteOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolDeleteOptions != null) {
            ifModifiedSince = poolDeleteOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolDeleteOptions != null) {
            ifUnmodifiedSince = poolDeleteOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.delete(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a pool from the specified account.
     *
     * @param poolId The id of the pool to delete.
     * @param poolDeleteOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String poolId, PoolDeleteOptionsInner poolDeleteOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolDeleteOptions, serviceCallback);
        Integer timeout = null;
        if (poolDeleteOptions != null) {
            timeout = poolDeleteOptions.timeout();
        }
        String clientRequestId = null;
        if (poolDeleteOptions != null) {
            clientRequestId = poolDeleteOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolDeleteOptions != null) {
            returnClientRequestId = poolDeleteOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolDeleteOptions != null) {
            ocpDate = poolDeleteOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolDeleteOptions != null) {
            ifMatch = poolDeleteOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolDeleteOptions != null) {
            ifNoneMatch = poolDeleteOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolDeleteOptions != null) {
            ifModifiedSince = poolDeleteOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolDeleteOptions != null) {
            ifUnmodifiedSince = poolDeleteOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.delete(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolDeleteHeadersInner> deleteDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolDeleteHeadersInner.class);
    }

    /**
     * Gets basic properties of a pool.
     *
     * @param poolId The id of the pool to get.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the boolean object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<Boolean, PoolExistsHeadersInner> exists(String poolId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final PoolExistsOptionsInner poolExistsOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<Void> call = service.exists(poolId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return existsDelegate(call.execute());
    }

    /**
     * Gets basic properties of a pool.
     *
     * @param poolId The id of the pool to get.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall existsAsync(String poolId, final ServiceCallback<Boolean> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final PoolExistsOptionsInner poolExistsOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<Void> call = service.exists(poolId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseEmptyCallback<Boolean>(serviceCallback) {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    serviceCallback.success(existsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets basic properties of a pool.
     *
     * @param poolId The id of the pool to get.
     * @param poolExistsOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the boolean object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<Boolean, PoolExistsHeadersInner> exists(String poolId, PoolExistsOptionsInner poolExistsOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolExistsOptions);
        String select = null;
        if (poolExistsOptions != null) {
            select = poolExistsOptions.select();
        }
        Integer timeout = null;
        if (poolExistsOptions != null) {
            timeout = poolExistsOptions.timeout();
        }
        String clientRequestId = null;
        if (poolExistsOptions != null) {
            clientRequestId = poolExistsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolExistsOptions != null) {
            returnClientRequestId = poolExistsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolExistsOptions != null) {
            ocpDate = poolExistsOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolExistsOptions != null) {
            ifMatch = poolExistsOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolExistsOptions != null) {
            ifNoneMatch = poolExistsOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolExistsOptions != null) {
            ifModifiedSince = poolExistsOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolExistsOptions != null) {
            ifUnmodifiedSince = poolExistsOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<Void> call = service.exists(poolId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return existsDelegate(call.execute());
    }

    /**
     * Gets basic properties of a pool.
     *
     * @param poolId The id of the pool to get.
     * @param poolExistsOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall existsAsync(String poolId, PoolExistsOptionsInner poolExistsOptions, final ServiceCallback<Boolean> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolExistsOptions, serviceCallback);
        String select = null;
        if (poolExistsOptions != null) {
            select = poolExistsOptions.select();
        }
        Integer timeout = null;
        if (poolExistsOptions != null) {
            timeout = poolExistsOptions.timeout();
        }
        String clientRequestId = null;
        if (poolExistsOptions != null) {
            clientRequestId = poolExistsOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolExistsOptions != null) {
            returnClientRequestId = poolExistsOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolExistsOptions != null) {
            ocpDate = poolExistsOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolExistsOptions != null) {
            ifMatch = poolExistsOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolExistsOptions != null) {
            ifNoneMatch = poolExistsOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolExistsOptions != null) {
            ifModifiedSince = poolExistsOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolExistsOptions != null) {
            ifUnmodifiedSince = poolExistsOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<Void> call = service.exists(poolId, this.client.apiVersion(), this.client.acceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseEmptyCallback<Boolean>(serviceCallback) {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                try {
                    serviceCallback.success(existsDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Boolean, PoolExistsHeadersInner> existsDelegate(Response<Void> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Boolean, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildEmptyWithHeaders(response, PoolExistsHeadersInner.class);
    }

    /**
     * Gets information about the specified pool.
     *
     * @param poolId The id of the pool to get.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudPoolInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudPoolInner, PoolGetHeadersInner> get(String poolId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final PoolGetOptionsInner poolGetOptions = null;
        String select = null;
        String expand = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.get(poolId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified pool.
     *
     * @param poolId The id of the pool to get.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String poolId, final ServiceCallback<CloudPoolInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final PoolGetOptionsInner poolGetOptions = null;
        String select = null;
        String expand = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.get(poolId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudPoolInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets information about the specified pool.
     *
     * @param poolId The id of the pool to get.
     * @param poolGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the CloudPoolInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<CloudPoolInner, PoolGetHeadersInner> get(String poolId, PoolGetOptionsInner poolGetOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolGetOptions);
        String select = null;
        if (poolGetOptions != null) {
            select = poolGetOptions.select();
        }
        String expand = null;
        if (poolGetOptions != null) {
            expand = poolGetOptions.expand();
        }
        Integer timeout = null;
        if (poolGetOptions != null) {
            timeout = poolGetOptions.timeout();
        }
        String clientRequestId = null;
        if (poolGetOptions != null) {
            clientRequestId = poolGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolGetOptions != null) {
            returnClientRequestId = poolGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolGetOptions != null) {
            ocpDate = poolGetOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolGetOptions != null) {
            ifMatch = poolGetOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolGetOptions != null) {
            ifNoneMatch = poolGetOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolGetOptions != null) {
            ifModifiedSince = poolGetOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolGetOptions != null) {
            ifUnmodifiedSince = poolGetOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.get(poolId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified pool.
     *
     * @param poolId The id of the pool to get.
     * @param poolGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String poolId, PoolGetOptionsInner poolGetOptions, final ServiceCallback<CloudPoolInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolGetOptions, serviceCallback);
        String select = null;
        if (poolGetOptions != null) {
            select = poolGetOptions.select();
        }
        String expand = null;
        if (poolGetOptions != null) {
            expand = poolGetOptions.expand();
        }
        Integer timeout = null;
        if (poolGetOptions != null) {
            timeout = poolGetOptions.timeout();
        }
        String clientRequestId = null;
        if (poolGetOptions != null) {
            clientRequestId = poolGetOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolGetOptions != null) {
            returnClientRequestId = poolGetOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolGetOptions != null) {
            ocpDate = poolGetOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolGetOptions != null) {
            ifMatch = poolGetOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolGetOptions != null) {
            ifNoneMatch = poolGetOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolGetOptions != null) {
            ifModifiedSince = poolGetOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolGetOptions != null) {
            ifUnmodifiedSince = poolGetOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.get(poolId, this.client.apiVersion(), this.client.acceptLanguage(), select, expand, timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<CloudPoolInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<CloudPoolInner, PoolGetHeadersInner> getDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<CloudPoolInner, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<CloudPoolInner>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolGetHeadersInner.class);
    }

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolPatchParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolPatchHeadersInner> patch(String poolId, PoolPatchParameterInner poolPatchParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (poolPatchParameter == null) {
            throw new IllegalArgumentException("Parameter poolPatchParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolPatchParameter);
        final PoolPatchOptionsInner poolPatchOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.patch(poolId, poolPatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return patchDelegate(call.execute());
    }

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolPatchParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall patchAsync(String poolId, PoolPatchParameterInner poolPatchParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (poolPatchParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolPatchParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolPatchParameter, serviceCallback);
        final PoolPatchOptionsInner poolPatchOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.patch(poolId, poolPatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(patchDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolPatchParameter The parameters for the request.
     * @param poolPatchOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolPatchHeadersInner> patch(String poolId, PoolPatchParameterInner poolPatchParameter, PoolPatchOptionsInner poolPatchOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (poolPatchParameter == null) {
            throw new IllegalArgumentException("Parameter poolPatchParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolPatchParameter);
        Validator.validate(poolPatchOptions);
        Integer timeout = null;
        if (poolPatchOptions != null) {
            timeout = poolPatchOptions.timeout();
        }
        String clientRequestId = null;
        if (poolPatchOptions != null) {
            clientRequestId = poolPatchOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolPatchOptions != null) {
            returnClientRequestId = poolPatchOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolPatchOptions != null) {
            ocpDate = poolPatchOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolPatchOptions != null) {
            ifMatch = poolPatchOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolPatchOptions != null) {
            ifNoneMatch = poolPatchOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolPatchOptions != null) {
            ifModifiedSince = poolPatchOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolPatchOptions != null) {
            ifUnmodifiedSince = poolPatchOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.patch(poolId, poolPatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return patchDelegate(call.execute());
    }

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolPatchParameter The parameters for the request.
     * @param poolPatchOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall patchAsync(String poolId, PoolPatchParameterInner poolPatchParameter, PoolPatchOptionsInner poolPatchOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (poolPatchParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolPatchParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolPatchParameter, serviceCallback);
        Validator.validate(poolPatchOptions, serviceCallback);
        Integer timeout = null;
        if (poolPatchOptions != null) {
            timeout = poolPatchOptions.timeout();
        }
        String clientRequestId = null;
        if (poolPatchOptions != null) {
            clientRequestId = poolPatchOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolPatchOptions != null) {
            returnClientRequestId = poolPatchOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolPatchOptions != null) {
            ocpDate = poolPatchOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolPatchOptions != null) {
            ifMatch = poolPatchOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolPatchOptions != null) {
            ifNoneMatch = poolPatchOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolPatchOptions != null) {
            ifModifiedSince = poolPatchOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolPatchOptions != null) {
            ifUnmodifiedSince = poolPatchOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.patch(poolId, poolPatchParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(patchDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolPatchHeadersInner> patchDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolPatchHeadersInner.class);
    }

    /**
     * Disables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to disable automatic scaling.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolDisableAutoScaleHeadersInner> disableAutoScale(String poolId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final PoolDisableAutoScaleOptionsInner poolDisableAutoScaleOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.disableAutoScale(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return disableAutoScaleDelegate(call.execute());
    }

    /**
     * Disables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to disable automatic scaling.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall disableAutoScaleAsync(String poolId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final PoolDisableAutoScaleOptionsInner poolDisableAutoScaleOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.disableAutoScale(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(disableAutoScaleDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Disables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to disable automatic scaling.
     * @param poolDisableAutoScaleOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolDisableAutoScaleHeadersInner> disableAutoScale(String poolId, PoolDisableAutoScaleOptionsInner poolDisableAutoScaleOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolDisableAutoScaleOptions);
        Integer timeout = null;
        if (poolDisableAutoScaleOptions != null) {
            timeout = poolDisableAutoScaleOptions.timeout();
        }
        String clientRequestId = null;
        if (poolDisableAutoScaleOptions != null) {
            clientRequestId = poolDisableAutoScaleOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolDisableAutoScaleOptions != null) {
            returnClientRequestId = poolDisableAutoScaleOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolDisableAutoScaleOptions != null) {
            ocpDate = poolDisableAutoScaleOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.disableAutoScale(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return disableAutoScaleDelegate(call.execute());
    }

    /**
     * Disables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to disable automatic scaling.
     * @param poolDisableAutoScaleOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall disableAutoScaleAsync(String poolId, PoolDisableAutoScaleOptionsInner poolDisableAutoScaleOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolDisableAutoScaleOptions, serviceCallback);
        Integer timeout = null;
        if (poolDisableAutoScaleOptions != null) {
            timeout = poolDisableAutoScaleOptions.timeout();
        }
        String clientRequestId = null;
        if (poolDisableAutoScaleOptions != null) {
            clientRequestId = poolDisableAutoScaleOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolDisableAutoScaleOptions != null) {
            returnClientRequestId = poolDisableAutoScaleOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolDisableAutoScaleOptions != null) {
            ocpDate = poolDisableAutoScaleOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.disableAutoScale(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(disableAutoScaleDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolDisableAutoScaleHeadersInner> disableAutoScaleDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolDisableAutoScaleHeadersInner.class);
    }

    /**
     * Enables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to enable automatic scaling.
     * @param poolEnableAutoScaleParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolEnableAutoScaleHeadersInner> enableAutoScale(String poolId, PoolEnableAutoScaleParameterInner poolEnableAutoScaleParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (poolEnableAutoScaleParameter == null) {
            throw new IllegalArgumentException("Parameter poolEnableAutoScaleParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolEnableAutoScaleParameter);
        final PoolEnableAutoScaleOptionsInner poolEnableAutoScaleOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.enableAutoScale(poolId, poolEnableAutoScaleParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return enableAutoScaleDelegate(call.execute());
    }

    /**
     * Enables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to enable automatic scaling.
     * @param poolEnableAutoScaleParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall enableAutoScaleAsync(String poolId, PoolEnableAutoScaleParameterInner poolEnableAutoScaleParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (poolEnableAutoScaleParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolEnableAutoScaleParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolEnableAutoScaleParameter, serviceCallback);
        final PoolEnableAutoScaleOptionsInner poolEnableAutoScaleOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.enableAutoScale(poolId, poolEnableAutoScaleParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(enableAutoScaleDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Enables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to enable automatic scaling.
     * @param poolEnableAutoScaleParameter The parameters for the request.
     * @param poolEnableAutoScaleOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolEnableAutoScaleHeadersInner> enableAutoScale(String poolId, PoolEnableAutoScaleParameterInner poolEnableAutoScaleParameter, PoolEnableAutoScaleOptionsInner poolEnableAutoScaleOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (poolEnableAutoScaleParameter == null) {
            throw new IllegalArgumentException("Parameter poolEnableAutoScaleParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolEnableAutoScaleParameter);
        Validator.validate(poolEnableAutoScaleOptions);
        Integer timeout = null;
        if (poolEnableAutoScaleOptions != null) {
            timeout = poolEnableAutoScaleOptions.timeout();
        }
        String clientRequestId = null;
        if (poolEnableAutoScaleOptions != null) {
            clientRequestId = poolEnableAutoScaleOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolEnableAutoScaleOptions != null) {
            returnClientRequestId = poolEnableAutoScaleOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolEnableAutoScaleOptions != null) {
            ocpDate = poolEnableAutoScaleOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolEnableAutoScaleOptions != null) {
            ifMatch = poolEnableAutoScaleOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolEnableAutoScaleOptions != null) {
            ifNoneMatch = poolEnableAutoScaleOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolEnableAutoScaleOptions != null) {
            ifModifiedSince = poolEnableAutoScaleOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolEnableAutoScaleOptions != null) {
            ifUnmodifiedSince = poolEnableAutoScaleOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.enableAutoScale(poolId, poolEnableAutoScaleParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return enableAutoScaleDelegate(call.execute());
    }

    /**
     * Enables automatic scaling for a pool.
     *
     * @param poolId The id of the pool on which to enable automatic scaling.
     * @param poolEnableAutoScaleParameter The parameters for the request.
     * @param poolEnableAutoScaleOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall enableAutoScaleAsync(String poolId, PoolEnableAutoScaleParameterInner poolEnableAutoScaleParameter, PoolEnableAutoScaleOptionsInner poolEnableAutoScaleOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (poolEnableAutoScaleParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolEnableAutoScaleParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolEnableAutoScaleParameter, serviceCallback);
        Validator.validate(poolEnableAutoScaleOptions, serviceCallback);
        Integer timeout = null;
        if (poolEnableAutoScaleOptions != null) {
            timeout = poolEnableAutoScaleOptions.timeout();
        }
        String clientRequestId = null;
        if (poolEnableAutoScaleOptions != null) {
            clientRequestId = poolEnableAutoScaleOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolEnableAutoScaleOptions != null) {
            returnClientRequestId = poolEnableAutoScaleOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolEnableAutoScaleOptions != null) {
            ocpDate = poolEnableAutoScaleOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolEnableAutoScaleOptions != null) {
            ifMatch = poolEnableAutoScaleOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolEnableAutoScaleOptions != null) {
            ifNoneMatch = poolEnableAutoScaleOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolEnableAutoScaleOptions != null) {
            ifModifiedSince = poolEnableAutoScaleOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolEnableAutoScaleOptions != null) {
            ifUnmodifiedSince = poolEnableAutoScaleOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.enableAutoScale(poolId, poolEnableAutoScaleParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(enableAutoScaleDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolEnableAutoScaleHeadersInner> enableAutoScaleDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolEnableAutoScaleHeadersInner.class);
    }

    /**
     * Gets the result of evaluating an automatic scaling formula on the pool.
     *
     * @param poolId The id of the pool on which to evaluate the automatic scaling formula.
     * @param autoScaleFormula Sets a formula for the desired number of compute nodes in the pool.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AutoScaleRunInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<AutoScaleRunInner, PoolEvaluateAutoScaleHeadersInner> evaluateAutoScale(String poolId, String autoScaleFormula) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (autoScaleFormula == null) {
            throw new IllegalArgumentException("Parameter autoScaleFormula is required and cannot be null.");
        }
        final PoolEvaluateAutoScaleOptionsInner poolEvaluateAutoScaleOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        PoolEvaluateAutoScaleParameter poolEvaluateAutoScaleParameter = new PoolEvaluateAutoScaleParameter();
        poolEvaluateAutoScaleParameter.setAutoScaleFormula(autoScaleFormula);
        Call<ResponseBody> call = service.evaluateAutoScale(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, poolEvaluateAutoScaleParameter);
        return evaluateAutoScaleDelegate(call.execute());
    }

    /**
     * Gets the result of evaluating an automatic scaling formula on the pool.
     *
     * @param poolId The id of the pool on which to evaluate the automatic scaling formula.
     * @param autoScaleFormula Sets a formula for the desired number of compute nodes in the pool.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall evaluateAutoScaleAsync(String poolId, String autoScaleFormula, final ServiceCallback<AutoScaleRunInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        if (autoScaleFormula == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter autoScaleFormula is required and cannot be null."));
            return null;
        }
        final PoolEvaluateAutoScaleOptionsInner poolEvaluateAutoScaleOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        PoolEvaluateAutoScaleParameter poolEvaluateAutoScaleParameter = new PoolEvaluateAutoScaleParameter();
        poolEvaluateAutoScaleParameter.setAutoScaleFormula(autoScaleFormula);
        Call<ResponseBody> call = service.evaluateAutoScale(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, poolEvaluateAutoScaleParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<AutoScaleRunInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(evaluateAutoScaleDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Gets the result of evaluating an automatic scaling formula on the pool.
     *
     * @param poolId The id of the pool on which to evaluate the automatic scaling formula.
     * @param autoScaleFormula Sets a formula for the desired number of compute nodes in the pool.
     * @param poolEvaluateAutoScaleOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AutoScaleRunInner object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<AutoScaleRunInner, PoolEvaluateAutoScaleHeadersInner> evaluateAutoScale(String poolId, String autoScaleFormula, PoolEvaluateAutoScaleOptionsInner poolEvaluateAutoScaleOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (autoScaleFormula == null) {
            throw new IllegalArgumentException("Parameter autoScaleFormula is required and cannot be null.");
        }
        Validator.validate(poolEvaluateAutoScaleOptions);
        Integer timeout = null;
        if (poolEvaluateAutoScaleOptions != null) {
            timeout = poolEvaluateAutoScaleOptions.timeout();
        }
        String clientRequestId = null;
        if (poolEvaluateAutoScaleOptions != null) {
            clientRequestId = poolEvaluateAutoScaleOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolEvaluateAutoScaleOptions != null) {
            returnClientRequestId = poolEvaluateAutoScaleOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolEvaluateAutoScaleOptions != null) {
            ocpDate = poolEvaluateAutoScaleOptions.ocpDate();
        }
        PoolEvaluateAutoScaleParameter poolEvaluateAutoScaleParameter = new PoolEvaluateAutoScaleParameter();
        poolEvaluateAutoScaleParameter.setAutoScaleFormula(autoScaleFormula);
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.evaluateAutoScale(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, poolEvaluateAutoScaleParameter);
        return evaluateAutoScaleDelegate(call.execute());
    }

    /**
     * Gets the result of evaluating an automatic scaling formula on the pool.
     *
     * @param poolId The id of the pool on which to evaluate the automatic scaling formula.
     * @param autoScaleFormula Sets a formula for the desired number of compute nodes in the pool.
     * @param poolEvaluateAutoScaleOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall evaluateAutoScaleAsync(String poolId, String autoScaleFormula, PoolEvaluateAutoScaleOptionsInner poolEvaluateAutoScaleOptions, final ServiceCallback<AutoScaleRunInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        if (autoScaleFormula == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter autoScaleFormula is required and cannot be null."));
            return null;
        }
        Validator.validate(poolEvaluateAutoScaleOptions, serviceCallback);
        Integer timeout = null;
        if (poolEvaluateAutoScaleOptions != null) {
            timeout = poolEvaluateAutoScaleOptions.timeout();
        }
        String clientRequestId = null;
        if (poolEvaluateAutoScaleOptions != null) {
            clientRequestId = poolEvaluateAutoScaleOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolEvaluateAutoScaleOptions != null) {
            returnClientRequestId = poolEvaluateAutoScaleOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolEvaluateAutoScaleOptions != null) {
            ocpDate = poolEvaluateAutoScaleOptions.ocpDate();
        }
        PoolEvaluateAutoScaleParameter poolEvaluateAutoScaleParameter = new PoolEvaluateAutoScaleParameter();
        poolEvaluateAutoScaleParameter.setAutoScaleFormula(autoScaleFormula);
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.evaluateAutoScale(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, poolEvaluateAutoScaleParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<AutoScaleRunInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(evaluateAutoScaleDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<AutoScaleRunInner, PoolEvaluateAutoScaleHeadersInner> evaluateAutoScaleDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<AutoScaleRunInner, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<AutoScaleRunInner>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolEvaluateAutoScaleHeadersInner.class);
    }

    /**
     * Changes the number of compute nodes that are assigned to a pool.
     *
     * @param poolId The id of the pool to resize.
     * @param poolResizeParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolResizeHeadersInner> resize(String poolId, PoolResizeParameterInner poolResizeParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (poolResizeParameter == null) {
            throw new IllegalArgumentException("Parameter poolResizeParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolResizeParameter);
        final PoolResizeOptionsInner poolResizeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.resize(poolId, poolResizeParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return resizeDelegate(call.execute());
    }

    /**
     * Changes the number of compute nodes that are assigned to a pool.
     *
     * @param poolId The id of the pool to resize.
     * @param poolResizeParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall resizeAsync(String poolId, PoolResizeParameterInner poolResizeParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (poolResizeParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolResizeParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolResizeParameter, serviceCallback);
        final PoolResizeOptionsInner poolResizeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.resize(poolId, poolResizeParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(resizeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Changes the number of compute nodes that are assigned to a pool.
     *
     * @param poolId The id of the pool to resize.
     * @param poolResizeParameter The parameters for the request.
     * @param poolResizeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolResizeHeadersInner> resize(String poolId, PoolResizeParameterInner poolResizeParameter, PoolResizeOptionsInner poolResizeOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (poolResizeParameter == null) {
            throw new IllegalArgumentException("Parameter poolResizeParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolResizeParameter);
        Validator.validate(poolResizeOptions);
        Integer timeout = null;
        if (poolResizeOptions != null) {
            timeout = poolResizeOptions.timeout();
        }
        String clientRequestId = null;
        if (poolResizeOptions != null) {
            clientRequestId = poolResizeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolResizeOptions != null) {
            returnClientRequestId = poolResizeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolResizeOptions != null) {
            ocpDate = poolResizeOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolResizeOptions != null) {
            ifMatch = poolResizeOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolResizeOptions != null) {
            ifNoneMatch = poolResizeOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolResizeOptions != null) {
            ifModifiedSince = poolResizeOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolResizeOptions != null) {
            ifUnmodifiedSince = poolResizeOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.resize(poolId, poolResizeParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return resizeDelegate(call.execute());
    }

    /**
     * Changes the number of compute nodes that are assigned to a pool.
     *
     * @param poolId The id of the pool to resize.
     * @param poolResizeParameter The parameters for the request.
     * @param poolResizeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall resizeAsync(String poolId, PoolResizeParameterInner poolResizeParameter, PoolResizeOptionsInner poolResizeOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (poolResizeParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolResizeParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolResizeParameter, serviceCallback);
        Validator.validate(poolResizeOptions, serviceCallback);
        Integer timeout = null;
        if (poolResizeOptions != null) {
            timeout = poolResizeOptions.timeout();
        }
        String clientRequestId = null;
        if (poolResizeOptions != null) {
            clientRequestId = poolResizeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolResizeOptions != null) {
            returnClientRequestId = poolResizeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolResizeOptions != null) {
            ocpDate = poolResizeOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolResizeOptions != null) {
            ifMatch = poolResizeOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolResizeOptions != null) {
            ifNoneMatch = poolResizeOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolResizeOptions != null) {
            ifModifiedSince = poolResizeOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolResizeOptions != null) {
            ifUnmodifiedSince = poolResizeOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.resize(poolId, poolResizeParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(resizeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolResizeHeadersInner> resizeDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolResizeHeadersInner.class);
    }

    /**
     * Stops an ongoing resize operation on the pool. This does not restore the pool to its previous state before the resize operation: it only stops any further changes being made, and the pool maintains its current state.
     *
     * @param poolId The id of the pool whose resizing you want to stop.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolStopResizeHeadersInner> stopResize(String poolId) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final PoolStopResizeOptionsInner poolStopResizeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.stopResize(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return stopResizeDelegate(call.execute());
    }

    /**
     * Stops an ongoing resize operation on the pool. This does not restore the pool to its previous state before the resize operation: it only stops any further changes being made, and the pool maintains its current state.
     *
     * @param poolId The id of the pool whose resizing you want to stop.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall stopResizeAsync(String poolId, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final PoolStopResizeOptionsInner poolStopResizeOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.stopResize(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(stopResizeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Stops an ongoing resize operation on the pool. This does not restore the pool to its previous state before the resize operation: it only stops any further changes being made, and the pool maintains its current state.
     *
     * @param poolId The id of the pool whose resizing you want to stop.
     * @param poolStopResizeOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolStopResizeHeadersInner> stopResize(String poolId, PoolStopResizeOptionsInner poolStopResizeOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolStopResizeOptions);
        Integer timeout = null;
        if (poolStopResizeOptions != null) {
            timeout = poolStopResizeOptions.timeout();
        }
        String clientRequestId = null;
        if (poolStopResizeOptions != null) {
            clientRequestId = poolStopResizeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolStopResizeOptions != null) {
            returnClientRequestId = poolStopResizeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolStopResizeOptions != null) {
            ocpDate = poolStopResizeOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolStopResizeOptions != null) {
            ifMatch = poolStopResizeOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolStopResizeOptions != null) {
            ifNoneMatch = poolStopResizeOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolStopResizeOptions != null) {
            ifModifiedSince = poolStopResizeOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolStopResizeOptions != null) {
            ifUnmodifiedSince = poolStopResizeOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.stopResize(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return stopResizeDelegate(call.execute());
    }

    /**
     * Stops an ongoing resize operation on the pool. This does not restore the pool to its previous state before the resize operation: it only stops any further changes being made, and the pool maintains its current state.
     *
     * @param poolId The id of the pool whose resizing you want to stop.
     * @param poolStopResizeOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall stopResizeAsync(String poolId, PoolStopResizeOptionsInner poolStopResizeOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolStopResizeOptions, serviceCallback);
        Integer timeout = null;
        if (poolStopResizeOptions != null) {
            timeout = poolStopResizeOptions.timeout();
        }
        String clientRequestId = null;
        if (poolStopResizeOptions != null) {
            clientRequestId = poolStopResizeOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolStopResizeOptions != null) {
            returnClientRequestId = poolStopResizeOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolStopResizeOptions != null) {
            ocpDate = poolStopResizeOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolStopResizeOptions != null) {
            ifMatch = poolStopResizeOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolStopResizeOptions != null) {
            ifNoneMatch = poolStopResizeOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolStopResizeOptions != null) {
            ifModifiedSince = poolStopResizeOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolStopResizeOptions != null) {
            ifUnmodifiedSince = poolStopResizeOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.stopResize(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(stopResizeDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolStopResizeHeadersInner> stopResizeDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolStopResizeHeadersInner.class);
    }

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolUpdatePropertiesParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolUpdatePropertiesHeadersInner> updateProperties(String poolId, PoolUpdatePropertiesParameterInner poolUpdatePropertiesParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (poolUpdatePropertiesParameter == null) {
            throw new IllegalArgumentException("Parameter poolUpdatePropertiesParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolUpdatePropertiesParameter);
        final PoolUpdatePropertiesOptionsInner poolUpdatePropertiesOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.updateProperties(poolId, poolUpdatePropertiesParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return updatePropertiesDelegate(call.execute());
    }

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolUpdatePropertiesParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updatePropertiesAsync(String poolId, PoolUpdatePropertiesParameterInner poolUpdatePropertiesParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (poolUpdatePropertiesParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolUpdatePropertiesParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolUpdatePropertiesParameter, serviceCallback);
        final PoolUpdatePropertiesOptionsInner poolUpdatePropertiesOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.updateProperties(poolId, poolUpdatePropertiesParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(updatePropertiesDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolUpdatePropertiesParameter The parameters for the request.
     * @param poolUpdatePropertiesOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolUpdatePropertiesHeadersInner> updateProperties(String poolId, PoolUpdatePropertiesParameterInner poolUpdatePropertiesParameter, PoolUpdatePropertiesOptionsInner poolUpdatePropertiesOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (poolUpdatePropertiesParameter == null) {
            throw new IllegalArgumentException("Parameter poolUpdatePropertiesParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(poolUpdatePropertiesParameter);
        Validator.validate(poolUpdatePropertiesOptions);
        Integer timeout = null;
        if (poolUpdatePropertiesOptions != null) {
            timeout = poolUpdatePropertiesOptions.timeout();
        }
        String clientRequestId = null;
        if (poolUpdatePropertiesOptions != null) {
            clientRequestId = poolUpdatePropertiesOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolUpdatePropertiesOptions != null) {
            returnClientRequestId = poolUpdatePropertiesOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolUpdatePropertiesOptions != null) {
            ocpDate = poolUpdatePropertiesOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.updateProperties(poolId, poolUpdatePropertiesParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return updatePropertiesDelegate(call.execute());
    }

    /**
     * Updates the properties of a pool.
     *
     * @param poolId The id of the pool to update.
     * @param poolUpdatePropertiesParameter The parameters for the request.
     * @param poolUpdatePropertiesOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updatePropertiesAsync(String poolId, PoolUpdatePropertiesParameterInner poolUpdatePropertiesParameter, PoolUpdatePropertiesOptionsInner poolUpdatePropertiesOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (poolUpdatePropertiesParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolUpdatePropertiesParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(poolUpdatePropertiesParameter, serviceCallback);
        Validator.validate(poolUpdatePropertiesOptions, serviceCallback);
        Integer timeout = null;
        if (poolUpdatePropertiesOptions != null) {
            timeout = poolUpdatePropertiesOptions.timeout();
        }
        String clientRequestId = null;
        if (poolUpdatePropertiesOptions != null) {
            clientRequestId = poolUpdatePropertiesOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolUpdatePropertiesOptions != null) {
            returnClientRequestId = poolUpdatePropertiesOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolUpdatePropertiesOptions != null) {
            ocpDate = poolUpdatePropertiesOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.updateProperties(poolId, poolUpdatePropertiesParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(updatePropertiesDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolUpdatePropertiesHeadersInner> updatePropertiesDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolUpdatePropertiesHeadersInner.class);
    }

    /**
     * Upgrades the operating system of the specified pool.
     *
     * @param poolId The id of the pool to upgrade.
     * @param targetOSVersion Sets the Azure Guest OS version to be installed on the virtual machines in the pool.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolUpgradeOSHeadersInner> upgradeOS(String poolId, String targetOSVersion) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (targetOSVersion == null) {
            throw new IllegalArgumentException("Parameter targetOSVersion is required and cannot be null.");
        }
        final PoolUpgradeOSOptionsInner poolUpgradeOSOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        PoolUpgradeOSParameter poolUpgradeOSParameter = new PoolUpgradeOSParameter();
        poolUpgradeOSParameter.setTargetOSVersion(targetOSVersion);
        Call<ResponseBody> call = service.upgradeOS(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, poolUpgradeOSParameter);
        return upgradeOSDelegate(call.execute());
    }

    /**
     * Upgrades the operating system of the specified pool.
     *
     * @param poolId The id of the pool to upgrade.
     * @param targetOSVersion Sets the Azure Guest OS version to be installed on the virtual machines in the pool.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall upgradeOSAsync(String poolId, String targetOSVersion, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        if (targetOSVersion == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter targetOSVersion is required and cannot be null."));
            return null;
        }
        final PoolUpgradeOSOptionsInner poolUpgradeOSOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        PoolUpgradeOSParameter poolUpgradeOSParameter = new PoolUpgradeOSParameter();
        poolUpgradeOSParameter.setTargetOSVersion(targetOSVersion);
        Call<ResponseBody> call = service.upgradeOS(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, poolUpgradeOSParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(upgradeOSDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Upgrades the operating system of the specified pool.
     *
     * @param poolId The id of the pool to upgrade.
     * @param targetOSVersion Sets the Azure Guest OS version to be installed on the virtual machines in the pool.
     * @param poolUpgradeOSOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolUpgradeOSHeadersInner> upgradeOS(String poolId, String targetOSVersion, PoolUpgradeOSOptionsInner poolUpgradeOSOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        if (targetOSVersion == null) {
            throw new IllegalArgumentException("Parameter targetOSVersion is required and cannot be null.");
        }
        Validator.validate(poolUpgradeOSOptions);
        Integer timeout = null;
        if (poolUpgradeOSOptions != null) {
            timeout = poolUpgradeOSOptions.timeout();
        }
        String clientRequestId = null;
        if (poolUpgradeOSOptions != null) {
            clientRequestId = poolUpgradeOSOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolUpgradeOSOptions != null) {
            returnClientRequestId = poolUpgradeOSOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolUpgradeOSOptions != null) {
            ocpDate = poolUpgradeOSOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolUpgradeOSOptions != null) {
            ifMatch = poolUpgradeOSOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolUpgradeOSOptions != null) {
            ifNoneMatch = poolUpgradeOSOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolUpgradeOSOptions != null) {
            ifModifiedSince = poolUpgradeOSOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolUpgradeOSOptions != null) {
            ifUnmodifiedSince = poolUpgradeOSOptions.ifUnmodifiedSince();
        }
        PoolUpgradeOSParameter poolUpgradeOSParameter = new PoolUpgradeOSParameter();
        poolUpgradeOSParameter.setTargetOSVersion(targetOSVersion);
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.upgradeOS(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, poolUpgradeOSParameter);
        return upgradeOSDelegate(call.execute());
    }

    /**
     * Upgrades the operating system of the specified pool.
     *
     * @param poolId The id of the pool to upgrade.
     * @param targetOSVersion Sets the Azure Guest OS version to be installed on the virtual machines in the pool.
     * @param poolUpgradeOSOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall upgradeOSAsync(String poolId, String targetOSVersion, PoolUpgradeOSOptionsInner poolUpgradeOSOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        if (targetOSVersion == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter targetOSVersion is required and cannot be null."));
            return null;
        }
        Validator.validate(poolUpgradeOSOptions, serviceCallback);
        Integer timeout = null;
        if (poolUpgradeOSOptions != null) {
            timeout = poolUpgradeOSOptions.timeout();
        }
        String clientRequestId = null;
        if (poolUpgradeOSOptions != null) {
            clientRequestId = poolUpgradeOSOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolUpgradeOSOptions != null) {
            returnClientRequestId = poolUpgradeOSOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolUpgradeOSOptions != null) {
            ocpDate = poolUpgradeOSOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolUpgradeOSOptions != null) {
            ifMatch = poolUpgradeOSOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolUpgradeOSOptions != null) {
            ifNoneMatch = poolUpgradeOSOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolUpgradeOSOptions != null) {
            ifModifiedSince = poolUpgradeOSOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolUpgradeOSOptions != null) {
            ifUnmodifiedSince = poolUpgradeOSOptions.ifUnmodifiedSince();
        }
        PoolUpgradeOSParameter poolUpgradeOSParameter = new PoolUpgradeOSParameter();
        poolUpgradeOSParameter.setTargetOSVersion(targetOSVersion);
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.upgradeOS(poolId, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted, poolUpgradeOSParameter);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(upgradeOSDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolUpgradeOSHeadersInner> upgradeOSDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolUpgradeOSHeadersInner.class);
    }

    /**
     * Removes compute nodes from the specified pool.
     *
     * @param poolId The id of the pool from which you want to remove nodes.
     * @param nodeRemoveParameter The parameters for the request.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolRemoveNodesHeadersInner> removeNodes(String poolId, NodeRemoveParameterInner nodeRemoveParameter) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeRemoveParameter == null) {
            throw new IllegalArgumentException("Parameter nodeRemoveParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(nodeRemoveParameter);
        final PoolRemoveNodesOptionsInner poolRemoveNodesOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.removeNodes(poolId, nodeRemoveParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return removeNodesDelegate(call.execute());
    }

    /**
     * Removes compute nodes from the specified pool.
     *
     * @param poolId The id of the pool from which you want to remove nodes.
     * @param nodeRemoveParameter The parameters for the request.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall removeNodesAsync(String poolId, NodeRemoveParameterInner nodeRemoveParameter, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeRemoveParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeRemoveParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(nodeRemoveParameter, serviceCallback);
        final PoolRemoveNodesOptionsInner poolRemoveNodesOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        String ifMatch = null;
        String ifNoneMatch = null;
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        Call<ResponseBody> call = service.removeNodes(poolId, nodeRemoveParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(removeNodesDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Removes compute nodes from the specified pool.
     *
     * @param poolId The id of the pool from which you want to remove nodes.
     * @param nodeRemoveParameter The parameters for the request.
     * @param poolRemoveNodesOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, PoolRemoveNodesHeadersInner> removeNodes(String poolId, NodeRemoveParameterInner nodeRemoveParameter, PoolRemoveNodesOptionsInner poolRemoveNodesOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (poolId == null) {
            throw new IllegalArgumentException("Parameter poolId is required and cannot be null.");
        }
        if (nodeRemoveParameter == null) {
            throw new IllegalArgumentException("Parameter nodeRemoveParameter is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(nodeRemoveParameter);
        Validator.validate(poolRemoveNodesOptions);
        Integer timeout = null;
        if (poolRemoveNodesOptions != null) {
            timeout = poolRemoveNodesOptions.timeout();
        }
        String clientRequestId = null;
        if (poolRemoveNodesOptions != null) {
            clientRequestId = poolRemoveNodesOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolRemoveNodesOptions != null) {
            returnClientRequestId = poolRemoveNodesOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolRemoveNodesOptions != null) {
            ocpDate = poolRemoveNodesOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolRemoveNodesOptions != null) {
            ifMatch = poolRemoveNodesOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolRemoveNodesOptions != null) {
            ifNoneMatch = poolRemoveNodesOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolRemoveNodesOptions != null) {
            ifModifiedSince = poolRemoveNodesOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolRemoveNodesOptions != null) {
            ifUnmodifiedSince = poolRemoveNodesOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.removeNodes(poolId, nodeRemoveParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        return removeNodesDelegate(call.execute());
    }

    /**
     * Removes compute nodes from the specified pool.
     *
     * @param poolId The id of the pool from which you want to remove nodes.
     * @param nodeRemoveParameter The parameters for the request.
     * @param poolRemoveNodesOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall removeNodesAsync(String poolId, NodeRemoveParameterInner nodeRemoveParameter, PoolRemoveNodesOptionsInner poolRemoveNodesOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (poolId == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter poolId is required and cannot be null."));
            return null;
        }
        if (nodeRemoveParameter == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nodeRemoveParameter is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(nodeRemoveParameter, serviceCallback);
        Validator.validate(poolRemoveNodesOptions, serviceCallback);
        Integer timeout = null;
        if (poolRemoveNodesOptions != null) {
            timeout = poolRemoveNodesOptions.timeout();
        }
        String clientRequestId = null;
        if (poolRemoveNodesOptions != null) {
            clientRequestId = poolRemoveNodesOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolRemoveNodesOptions != null) {
            returnClientRequestId = poolRemoveNodesOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolRemoveNodesOptions != null) {
            ocpDate = poolRemoveNodesOptions.ocpDate();
        }
        String ifMatch = null;
        if (poolRemoveNodesOptions != null) {
            ifMatch = poolRemoveNodesOptions.ifMatch();
        }
        String ifNoneMatch = null;
        if (poolRemoveNodesOptions != null) {
            ifNoneMatch = poolRemoveNodesOptions.ifNoneMatch();
        }
        DateTime ifModifiedSince = null;
        if (poolRemoveNodesOptions != null) {
            ifModifiedSince = poolRemoveNodesOptions.ifModifiedSince();
        }
        DateTime ifUnmodifiedSince = null;
        if (poolRemoveNodesOptions != null) {
            ifUnmodifiedSince = poolRemoveNodesOptions.ifUnmodifiedSince();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        DateTimeRfc1123 ifModifiedSinceConverted = null;
        if (ifModifiedSince != null) {
            ifModifiedSinceConverted = new DateTimeRfc1123(ifModifiedSince);
        }
        DateTimeRfc1123 ifUnmodifiedSinceConverted = null;
        if (ifUnmodifiedSince != null) {
            ifUnmodifiedSinceConverted = new DateTimeRfc1123(ifUnmodifiedSince);
        }
        Call<ResponseBody> call = service.removeNodes(poolId, nodeRemoveParameter, this.client.apiVersion(), this.client.acceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted, ifMatch, ifNoneMatch, ifModifiedSinceConverted, ifUnmodifiedSinceConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(removeNodesDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, PoolRemoveNodesHeadersInner> removeNodesDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolRemoveNodesHeadersInner.class);
    }

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PoolUsageMetricsInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> listPoolUsageMetricsNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final PoolListPoolUsageMetricsNextOptionsInner poolListPoolUsageMetricsNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listPoolUsageMetricsNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listPoolUsageMetricsNextDelegate(call.execute());
    }

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listPoolUsageMetricsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<PoolUsageMetricsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final PoolListPoolUsageMetricsNextOptionsInner poolListPoolUsageMetricsNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listPoolUsageMetricsNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<PoolUsageMetricsInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> result = listPoolUsageMetricsNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listPoolUsageMetricsNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param poolListPoolUsageMetricsNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;PoolUsageMetricsInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> listPoolUsageMetricsNext(final String nextPageLink, final PoolListPoolUsageMetricsNextOptionsInner poolListPoolUsageMetricsNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(poolListPoolUsageMetricsNextOptions);
        String clientRequestId = null;
        if (poolListPoolUsageMetricsNextOptions != null) {
            clientRequestId = poolListPoolUsageMetricsNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolListPoolUsageMetricsNextOptions != null) {
            returnClientRequestId = poolListPoolUsageMetricsNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolListPoolUsageMetricsNextOptions != null) {
            ocpDate = poolListPoolUsageMetricsNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listPoolUsageMetricsNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listPoolUsageMetricsNextDelegate(call.execute());
    }

    /**
     * Lists the usage metrics, aggregated by pool across individual time intervals, for the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param poolListPoolUsageMetricsNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listPoolUsageMetricsNextAsync(final String nextPageLink, final PoolListPoolUsageMetricsNextOptionsInner poolListPoolUsageMetricsNextOptions, final ServiceCall serviceCall, final ListOperationCallback<PoolUsageMetricsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(poolListPoolUsageMetricsNextOptions, serviceCallback);
        String clientRequestId = null;
        if (poolListPoolUsageMetricsNextOptions != null) {
            clientRequestId = poolListPoolUsageMetricsNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolListPoolUsageMetricsNextOptions != null) {
            returnClientRequestId = poolListPoolUsageMetricsNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolListPoolUsageMetricsNextOptions != null) {
            ocpDate = poolListPoolUsageMetricsNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listPoolUsageMetricsNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<PoolUsageMetricsInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> result = listPoolUsageMetricsNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listPoolUsageMetricsNextAsync(result.getBody().getNextPageLink(), poolListPoolUsageMetricsNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<PoolUsageMetricsInner>, PoolListPoolUsageMetricsHeadersInner> listPoolUsageMetricsNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<PoolUsageMetricsInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<PoolUsageMetricsInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolListPoolUsageMetricsHeadersInner.class);
    }

    /**
     * Lists all of the pools in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudPoolInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final PoolListNextOptionsInner poolListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the pools in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<CloudPoolInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final PoolListNextOptionsInner poolListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudPoolInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Lists all of the pools in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param poolListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;CloudPoolInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> listNext(final String nextPageLink, final PoolListNextOptionsInner poolListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(poolListNextOptions);
        String clientRequestId = null;
        if (poolListNextOptions != null) {
            clientRequestId = poolListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolListNextOptions != null) {
            returnClientRequestId = poolListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolListNextOptions != null) {
            ocpDate = poolListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the pools in the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param poolListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final PoolListNextOptionsInner poolListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<CloudPoolInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(poolListNextOptions, serviceCallback);
        String clientRequestId = null;
        if (poolListNextOptions != null) {
            clientRequestId = poolListNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (poolListNextOptions != null) {
            returnClientRequestId = poolListNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (poolListNextOptions != null) {
            ocpDate = poolListNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<CloudPoolInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), poolListNextOptions, serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponseWithHeaders<>(serviceCallback.get(), result.getHeaders(), result.getResponse()));
                    }
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<PageImpl<CloudPoolInner>, PoolListHeadersInner> listNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<CloudPoolInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<CloudPoolInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, PoolListHeadersInner.class);
    }

}
