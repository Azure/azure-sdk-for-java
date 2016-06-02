/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import retrofit2.Retrofit;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in Catalogs.
 */
public final class CatalogsInner {
    /** The Retrofit service to perform REST calls. */
    private CatalogsService service;
    /** The service client containing this operation class. */
    private DataLakeAnalyticsCatalogManagementClientImpl client;

    /**
     * Initializes an instance of CatalogsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public CatalogsInner(Retrofit retrofit, DataLakeAnalyticsCatalogManagementClientImpl client) {
        this.service = retrofit.create(CatalogsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Catalogs to be
     * used by Retrofit to perform actually REST calls.
     */
    interface CatalogsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("catalog/usql/databases/{databaseName}/secrets/{secretName}")
        Call<ResponseBody> createSecret(@Path("databaseName") String databaseName, @Path("secretName") String secretName, @Body DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PATCH("catalog/usql/databases/{databaseName}/secrets/{secretName}")
        Call<ResponseBody> updateSecret(@Path("databaseName") String databaseName, @Path("secretName") String secretName, @Body DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/secrets/{secretName}")
        Call<ResponseBody> getSecret(@Path("databaseName") String databaseName, @Path("secretName") String secretName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "catalog/usql/databases/{databaseName}/secrets/{secretName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteSecret(@Path("databaseName") String databaseName, @Path("secretName") String secretName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "catalog/usql/databases/{databaseName}/secrets", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteAllSecrets(@Path("databaseName") String databaseName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/externaldatasources/{externalDataSourceName}")
        Call<ResponseBody> getExternalDataSource(@Path("databaseName") String databaseName, @Path("externalDataSourceName") String externalDataSourceName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/externaldatasources")
        Call<ResponseBody> listExternalDataSources(@Path("databaseName") String databaseName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/credentials/{credentialName}")
        Call<ResponseBody> getCredential(@Path("databaseName") String databaseName, @Path("credentialName") String credentialName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/credentials")
        Call<ResponseBody> listCredentials(@Path("databaseName") String databaseName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/procedures/{procedureName}")
        Call<ResponseBody> getProcedure(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("procedureName") String procedureName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/procedures")
        Call<ResponseBody> listProcedures(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}")
        Call<ResponseBody> getTable(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables")
        Call<ResponseBody> listTables(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/views/{viewName}")
        Call<ResponseBody> getView(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("viewName") String viewName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/views")
        Call<ResponseBody> listViews(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}/statistics/{statisticsName}")
        Call<ResponseBody> getTableStatistic(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Path("statisticsName") String statisticsName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}/statistics")
        Call<ResponseBody> listTableStatistics(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}/partitions/{partitionName}")
        Call<ResponseBody> getTablePartition(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Path("partitionName") String partitionName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}/partitions")
        Call<ResponseBody> listTablePartitions(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/types")
        Call<ResponseBody> listTypes(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tablevaluedfunctions/{tableValuedFunctionName}")
        Call<ResponseBody> getTableValuedFunction(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableValuedFunctionName") String tableValuedFunctionName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tablevaluedfunctions")
        Call<ResponseBody> listTableValuedFunctions(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/assemblies/{assemblyName}")
        Call<ResponseBody> getAssembly(@Path("databaseName") String databaseName, @Path("assemblyName") String assemblyName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/assemblies")
        Call<ResponseBody> listAssemblies(@Path("databaseName") String databaseName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}")
        Call<ResponseBody> getSchema(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas")
        Call<ResponseBody> listSchemas(@Path("databaseName") String databaseName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}")
        Call<ResponseBody> getDatabase(@Path("databaseName") String databaseName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases")
        Call<ResponseBody> listDatabases(@Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listExternalDataSourcesNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listCredentialsNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listProceduresNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listTablesNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listViewsNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listTableStatisticsNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listTablePartitionsNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listTypesNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listTableValuedFunctionsNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listAssembliesNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listSchemasNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listDatabasesNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Creates the specified secret for use with external data sources in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database in which to create the secret.
     * @param secretName The name of the secret.
     * @param parameters The parameters required to create the secret (name and password)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlSecretInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlSecretInner> createSecret(String accountName, String databaseName, String secretName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (secretName == null) {
            throw new IllegalArgumentException("Parameter secretName is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.createSecret(databaseName, secretName, parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return createSecretDelegate(call.execute());
    }

    /**
     * Creates the specified secret for use with external data sources in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database in which to create the secret.
     * @param secretName The name of the secret.
     * @param parameters The parameters required to create the secret (name and password)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createSecretAsync(String accountName, String databaseName, String secretName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner parameters, final ServiceCallback<USqlSecretInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (secretName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter secretName is required and cannot be null."));
            return null;
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(parameters, serviceCallback);
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.createSecret(databaseName, secretName, parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlSecretInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createSecretDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlSecretInner> createSecretDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlSecretInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlSecretInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Modifies the specified secret for use with external data sources in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret.
     * @param parameters The parameters required to modify the secret (name and password)
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlSecretInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlSecretInner> updateSecret(String accountName, String databaseName, String secretName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (secretName == null) {
            throw new IllegalArgumentException("Parameter secretName is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.updateSecret(databaseName, secretName, parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return updateSecretDelegate(call.execute());
    }

    /**
     * Modifies the specified secret for use with external data sources in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret.
     * @param parameters The parameters required to modify the secret (name and password)
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall updateSecretAsync(String accountName, String databaseName, String secretName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParametersInner parameters, final ServiceCallback<USqlSecretInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (secretName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter secretName is required and cannot be null."));
            return null;
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(parameters, serviceCallback);
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.updateSecret(databaseName, secretName, parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlSecretInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(updateSecretDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlSecretInner> updateSecretDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlSecretInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlSecretInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets the specified secret in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret to get
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlSecretInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlSecretInner> getSecret(String accountName, String databaseName, String secretName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (secretName == null) {
            throw new IllegalArgumentException("Parameter secretName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getSecret(databaseName, secretName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getSecretDelegate(call.execute());
    }

    /**
     * Gets the specified secret in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret to get
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getSecretAsync(String accountName, String databaseName, String secretName, final ServiceCallback<USqlSecretInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (secretName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter secretName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getSecret(databaseName, secretName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlSecretInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getSecretDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlSecretInner> getSecretDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlSecretInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlSecretInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes the specified secret in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret to delete
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> deleteSecret(String accountName, String databaseName, String secretName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (secretName == null) {
            throw new IllegalArgumentException("Parameter secretName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.deleteSecret(databaseName, secretName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return deleteSecretDelegate(call.execute());
    }

    /**
     * Deletes the specified secret in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the secret.
     * @param secretName The name of the secret to delete
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteSecretAsync(String accountName, String databaseName, String secretName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (secretName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter secretName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.deleteSecret(databaseName, secretName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteSecretDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> deleteSecretDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Deletes all secrets in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the secret.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> deleteAllSecrets(String accountName, String databaseName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.deleteAllSecrets(databaseName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return deleteAllSecretsDelegate(call.execute());
    }

    /**
     * Deletes all secrets in the specified database.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the secret.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAllSecretsAsync(String accountName, String databaseName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.deleteAllSecrets(databaseName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteAllSecretsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> deleteAllSecretsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Retrieves the specified external data source from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the external data source.
     * @param externalDataSourceName The name of the external data source.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlExternalDataSourceInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlExternalDataSourceInner> getExternalDataSource(String accountName, String databaseName, String externalDataSourceName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (externalDataSourceName == null) {
            throw new IllegalArgumentException("Parameter externalDataSourceName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getExternalDataSource(databaseName, externalDataSourceName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getExternalDataSourceDelegate(call.execute());
    }

    /**
     * Retrieves the specified external data source from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the external data source.
     * @param externalDataSourceName The name of the external data source.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getExternalDataSourceAsync(String accountName, String databaseName, String externalDataSourceName, final ServiceCallback<USqlExternalDataSourceInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (externalDataSourceName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter externalDataSourceName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getExternalDataSource(databaseName, externalDataSourceName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlExternalDataSourceInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getExternalDataSourceDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlExternalDataSourceInner> getExternalDataSourceDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlExternalDataSourceInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlExternalDataSourceInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the external data sources.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlExternalDataSourceInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlExternalDataSourceInner>> listExternalDataSources(final String accountName, final String databaseName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listExternalDataSources(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlExternalDataSourceInner>> response = listExternalDataSourcesDelegate(call.execute());
        PagedList<USqlExternalDataSourceInner> result = new PagedList<USqlExternalDataSourceInner>(response.getBody()) {
            @Override
            public Page<USqlExternalDataSourceInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listExternalDataSourcesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the external data sources.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listExternalDataSourcesAsync(final String accountName, final String databaseName, final ListOperationCallback<USqlExternalDataSourceInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listExternalDataSources(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlExternalDataSourceInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlExternalDataSourceInner>> result = listExternalDataSourcesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listExternalDataSourcesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the external data sources.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlExternalDataSourceInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlExternalDataSourceInner>> listExternalDataSources(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listExternalDataSources(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlExternalDataSourceInner>> response = listExternalDataSourcesDelegate(call.execute());
        PagedList<USqlExternalDataSourceInner> result = new PagedList<USqlExternalDataSourceInner>(response.getBody()) {
            @Override
            public Page<USqlExternalDataSourceInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listExternalDataSourcesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the external data sources.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listExternalDataSourcesAsync(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlExternalDataSourceInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listExternalDataSources(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlExternalDataSourceInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlExternalDataSourceInner>> result = listExternalDataSourcesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listExternalDataSourcesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlExternalDataSourceInner>> listExternalDataSourcesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlExternalDataSourceInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlExternalDataSourceInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified credential from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param credentialName The name of the credential.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlCredentialInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlCredentialInner> getCredential(String accountName, String databaseName, String credentialName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (credentialName == null) {
            throw new IllegalArgumentException("Parameter credentialName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getCredential(databaseName, credentialName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getCredentialDelegate(call.execute());
    }

    /**
     * Retrieves the specified credential from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param credentialName The name of the credential.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getCredentialAsync(String accountName, String databaseName, String credentialName, final ServiceCallback<USqlCredentialInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (credentialName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter credentialName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getCredential(databaseName, credentialName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlCredentialInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getCredentialDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlCredentialInner> getCredentialDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlCredentialInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlCredentialInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlCredentialInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlCredentialInner>> listCredentials(final String accountName, final String databaseName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listCredentials(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlCredentialInner>> response = listCredentialsDelegate(call.execute());
        PagedList<USqlCredentialInner> result = new PagedList<USqlCredentialInner>(response.getBody()) {
            @Override
            public Page<USqlCredentialInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listCredentialsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listCredentialsAsync(final String accountName, final String databaseName, final ListOperationCallback<USqlCredentialInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listCredentials(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlCredentialInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlCredentialInner>> result = listCredentialsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listCredentialsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlCredentialInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlCredentialInner>> listCredentials(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listCredentials(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlCredentialInner>> response = listCredentialsDelegate(call.execute());
        PagedList<USqlCredentialInner> result = new PagedList<USqlCredentialInner>(response.getBody()) {
            @Override
            public Page<USqlCredentialInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listCredentialsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listCredentialsAsync(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlCredentialInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listCredentials(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlCredentialInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlCredentialInner>> result = listCredentialsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listCredentialsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlCredentialInner>> listCredentialsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlCredentialInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlCredentialInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified procedure from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the procedure.
     * @param schemaName The name of the schema containing the procedure.
     * @param procedureName The name of the procedure.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlProcedureInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlProcedureInner> getProcedure(String accountName, String databaseName, String schemaName, String procedureName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (procedureName == null) {
            throw new IllegalArgumentException("Parameter procedureName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getProcedure(databaseName, schemaName, procedureName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getProcedureDelegate(call.execute());
    }

    /**
     * Retrieves the specified procedure from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the procedure.
     * @param schemaName The name of the schema containing the procedure.
     * @param procedureName The name of the procedure.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getProcedureAsync(String accountName, String databaseName, String schemaName, String procedureName, final ServiceCallback<USqlProcedureInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (procedureName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter procedureName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getProcedure(databaseName, schemaName, procedureName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlProcedureInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getProcedureDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlProcedureInner> getProcedureDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlProcedureInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlProcedureInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the procedures.
     * @param schemaName The name of the schema containing the procedures.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlProcedureInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlProcedureInner>> listProcedures(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listProcedures(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlProcedureInner>> response = listProceduresDelegate(call.execute());
        PagedList<USqlProcedureInner> result = new PagedList<USqlProcedureInner>(response.getBody()) {
            @Override
            public Page<USqlProcedureInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listProceduresNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the procedures.
     * @param schemaName The name of the schema containing the procedures.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listProceduresAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlProcedureInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listProcedures(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlProcedureInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlProcedureInner>> result = listProceduresDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listProceduresNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the procedures.
     * @param schemaName The name of the schema containing the procedures.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlProcedureInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlProcedureInner>> listProcedures(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listProcedures(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlProcedureInner>> response = listProceduresDelegate(call.execute());
        PagedList<USqlProcedureInner> result = new PagedList<USqlProcedureInner>(response.getBody()) {
            @Override
            public Page<USqlProcedureInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listProceduresNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the procedures.
     * @param schemaName The name of the schema containing the procedures.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listProceduresAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlProcedureInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listProcedures(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlProcedureInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlProcedureInner>> result = listProceduresDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listProceduresNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlProcedureInner>> listProceduresDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlProcedureInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlProcedureInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified table from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table.
     * @param schemaName The name of the schema containing the table.
     * @param tableName The name of the table.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlTableInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlTableInner> getTable(String accountName, String databaseName, String schemaName, String tableName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("Parameter tableName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTable(databaseName, schemaName, tableName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getTableDelegate(call.execute());
    }

    /**
     * Retrieves the specified table from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table.
     * @param schemaName The name of the schema containing the table.
     * @param tableName The name of the table.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getTableAsync(String accountName, String databaseName, String schemaName, String tableName, final ServiceCallback<USqlTableInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (tableName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter tableName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTable(databaseName, schemaName, tableName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlTableInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getTableDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlTableInner> getTableDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlTableInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlTableInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the tables.
     * @param schemaName The name of the schema containing the tables.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableInner>> listTables(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTables(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableInner>> response = listTablesDelegate(call.execute());
        PagedList<USqlTableInner> result = new PagedList<USqlTableInner>(response.getBody()) {
            @Override
            public Page<USqlTableInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTablesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the tables.
     * @param schemaName The name of the schema containing the tables.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTablesAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlTableInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTables(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableInner>> result = listTablesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTablesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the tables.
     * @param schemaName The name of the schema containing the tables.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableInner>> listTables(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTables(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableInner>> response = listTablesDelegate(call.execute());
        PagedList<USqlTableInner> result = new PagedList<USqlTableInner>(response.getBody()) {
            @Override
            public Page<USqlTableInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTablesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the tables.
     * @param schemaName The name of the schema containing the tables.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTablesAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTableInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTables(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableInner>> result = listTablesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTablesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTableInner>> listTablesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified view from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the view.
     * @param schemaName The name of the schema containing the view.
     * @param viewName The name of the view.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlViewInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlViewInner> getView(String accountName, String databaseName, String schemaName, String viewName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (viewName == null) {
            throw new IllegalArgumentException("Parameter viewName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getView(databaseName, schemaName, viewName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getViewDelegate(call.execute());
    }

    /**
     * Retrieves the specified view from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the view.
     * @param schemaName The name of the schema containing the view.
     * @param viewName The name of the view.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getViewAsync(String accountName, String databaseName, String schemaName, String viewName, final ServiceCallback<USqlViewInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (viewName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter viewName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getView(databaseName, schemaName, viewName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlViewInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getViewDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlViewInner> getViewDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlViewInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlViewInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the views.
     * @param schemaName The name of the schema containing the views.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlViewInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlViewInner>> listViews(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listViews(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlViewInner>> response = listViewsDelegate(call.execute());
        PagedList<USqlViewInner> result = new PagedList<USqlViewInner>(response.getBody()) {
            @Override
            public Page<USqlViewInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listViewsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the views.
     * @param schemaName The name of the schema containing the views.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listViewsAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlViewInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listViews(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlViewInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlViewInner>> result = listViewsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listViewsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the views.
     * @param schemaName The name of the schema containing the views.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlViewInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlViewInner>> listViews(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listViews(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlViewInner>> response = listViewsDelegate(call.execute());
        PagedList<USqlViewInner> result = new PagedList<USqlViewInner>(response.getBody()) {
            @Override
            public Page<USqlViewInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listViewsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the views.
     * @param schemaName The name of the schema containing the views.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listViewsAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlViewInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listViews(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlViewInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlViewInner>> result = listViewsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listViewsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlViewInner>> listViewsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlViewInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlViewInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified table statistics from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param statisticsName The name of the table statistics.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlTableStatisticsInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlTableStatisticsInner> getTableStatistic(String accountName, String databaseName, String schemaName, String tableName, String statisticsName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("Parameter tableName is required and cannot be null.");
        }
        if (statisticsName == null) {
            throw new IllegalArgumentException("Parameter statisticsName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableStatistic(databaseName, schemaName, tableName, statisticsName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getTableStatisticDelegate(call.execute());
    }

    /**
     * Retrieves the specified table statistics from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param statisticsName The name of the table statistics.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getTableStatisticAsync(String accountName, String databaseName, String schemaName, String tableName, String statisticsName, final ServiceCallback<USqlTableStatisticsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (tableName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter tableName is required and cannot be null."));
            return null;
        }
        if (statisticsName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter statisticsName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableStatistic(databaseName, schemaName, tableName, statisticsName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlTableStatisticsInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getTableStatisticDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlTableStatisticsInner> getTableStatisticDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlTableStatisticsInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlTableStatisticsInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of table statistics from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableStatisticsInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableStatisticsInner>> listTableStatistics(final String accountName, final String databaseName, final String schemaName, final String tableName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("Parameter tableName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableStatistics(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableStatisticsInner>> response = listTableStatisticsDelegate(call.execute());
        PagedList<USqlTableStatisticsInner> result = new PagedList<USqlTableStatisticsInner>(response.getBody()) {
            @Override
            public Page<USqlTableStatisticsInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTableStatisticsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of table statistics from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTableStatisticsAsync(final String accountName, final String databaseName, final String schemaName, final String tableName, final ListOperationCallback<USqlTableStatisticsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (tableName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter tableName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableStatistics(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableStatisticsInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableStatisticsInner>> result = listTableStatisticsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTableStatisticsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of table statistics from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableStatisticsInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableStatisticsInner>> listTableStatistics(final String accountName, final String databaseName, final String schemaName, final String tableName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("Parameter tableName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableStatistics(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableStatisticsInner>> response = listTableStatisticsDelegate(call.execute());
        PagedList<USqlTableStatisticsInner> result = new PagedList<USqlTableStatisticsInner>(response.getBody()) {
            @Override
            public Page<USqlTableStatisticsInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTableStatisticsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of table statistics from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the statistics.
     * @param schemaName The name of the schema containing the statistics.
     * @param tableName The name of the table containing the statistics.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTableStatisticsAsync(final String accountName, final String databaseName, final String schemaName, final String tableName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTableStatisticsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (tableName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter tableName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableStatistics(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableStatisticsInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableStatisticsInner>> result = listTableStatisticsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTableStatisticsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTableStatisticsInner>> listTableStatisticsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableStatisticsInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableStatisticsInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified table partition from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the partition.
     * @param schemaName The name of the schema containing the partition.
     * @param tableName The name of the table containing the partition.
     * @param partitionName The name of the table partition.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlTablePartitionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlTablePartitionInner> getTablePartition(String accountName, String databaseName, String schemaName, String tableName, String partitionName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("Parameter tableName is required and cannot be null.");
        }
        if (partitionName == null) {
            throw new IllegalArgumentException("Parameter partitionName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTablePartition(databaseName, schemaName, tableName, partitionName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getTablePartitionDelegate(call.execute());
    }

    /**
     * Retrieves the specified table partition from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the partition.
     * @param schemaName The name of the schema containing the partition.
     * @param tableName The name of the table containing the partition.
     * @param partitionName The name of the table partition.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getTablePartitionAsync(String accountName, String databaseName, String schemaName, String tableName, String partitionName, final ServiceCallback<USqlTablePartitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (tableName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter tableName is required and cannot be null."));
            return null;
        }
        if (partitionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter partitionName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTablePartition(databaseName, schemaName, tableName, partitionName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlTablePartitionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getTablePartitionDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlTablePartitionInner> getTablePartitionDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlTablePartitionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlTablePartitionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of table partitions from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the partitions.
     * @param schemaName The name of the schema containing the partitions.
     * @param tableName The name of the table containing the partitions.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTablePartitionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTablePartitionInner>> listTablePartitions(final String accountName, final String databaseName, final String schemaName, final String tableName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("Parameter tableName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTablePartitions(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTablePartitionInner>> response = listTablePartitionsDelegate(call.execute());
        PagedList<USqlTablePartitionInner> result = new PagedList<USqlTablePartitionInner>(response.getBody()) {
            @Override
            public Page<USqlTablePartitionInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTablePartitionsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of table partitions from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the partitions.
     * @param schemaName The name of the schema containing the partitions.
     * @param tableName The name of the table containing the partitions.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTablePartitionsAsync(final String accountName, final String databaseName, final String schemaName, final String tableName, final ListOperationCallback<USqlTablePartitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (tableName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter tableName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTablePartitions(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTablePartitionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTablePartitionInner>> result = listTablePartitionsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTablePartitionsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of table partitions from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the partitions.
     * @param schemaName The name of the schema containing the partitions.
     * @param tableName The name of the table containing the partitions.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTablePartitionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTablePartitionInner>> listTablePartitions(final String accountName, final String databaseName, final String schemaName, final String tableName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("Parameter tableName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTablePartitions(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTablePartitionInner>> response = listTablePartitionsDelegate(call.execute());
        PagedList<USqlTablePartitionInner> result = new PagedList<USqlTablePartitionInner>(response.getBody()) {
            @Override
            public Page<USqlTablePartitionInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTablePartitionsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of table partitions from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the partitions.
     * @param schemaName The name of the schema containing the partitions.
     * @param tableName The name of the table containing the partitions.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTablePartitionsAsync(final String accountName, final String databaseName, final String schemaName, final String tableName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTablePartitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (tableName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter tableName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTablePartitions(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTablePartitionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTablePartitionInner>> result = listTablePartitionsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTablePartitionsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTablePartitionInner>> listTablePartitionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTablePartitionInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTablePartitionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the types.
     * @param schemaName The name of the schema containing the types.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTypeInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTypeInner>> listTypes(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTypeInner>> response = listTypesDelegate(call.execute());
        PagedList<USqlTypeInner> result = new PagedList<USqlTypeInner>(response.getBody()) {
            @Override
            public Page<USqlTypeInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTypesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the types.
     * @param schemaName The name of the schema containing the types.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTypesAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlTypeInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTypeInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTypeInner>> result = listTypesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTypesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the types.
     * @param schemaName The name of the schema containing the types.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTypeInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTypeInner>> listTypes(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTypeInner>> response = listTypesDelegate(call.execute());
        PagedList<USqlTypeInner> result = new PagedList<USqlTypeInner>(response.getBody()) {
            @Override
            public Page<USqlTypeInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTypesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the types.
     * @param schemaName The name of the schema containing the types.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTypesAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTypeInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTypeInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTypeInner>> result = listTypesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTypesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTypeInner>> listTypesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTypeInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTypeInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified table valued function from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table valued function.
     * @param schemaName The name of the schema containing the table valued function.
     * @param tableValuedFunctionName The name of the tableValuedFunction.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlTableValuedFunctionInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlTableValuedFunctionInner> getTableValuedFunction(String accountName, String databaseName, String schemaName, String tableValuedFunctionName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (tableValuedFunctionName == null) {
            throw new IllegalArgumentException("Parameter tableValuedFunctionName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableValuedFunction(databaseName, schemaName, tableValuedFunctionName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getTableValuedFunctionDelegate(call.execute());
    }

    /**
     * Retrieves the specified table valued function from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table valued function.
     * @param schemaName The name of the schema containing the table valued function.
     * @param tableValuedFunctionName The name of the tableValuedFunction.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getTableValuedFunctionAsync(String accountName, String databaseName, String schemaName, String tableValuedFunctionName, final ServiceCallback<USqlTableValuedFunctionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (tableValuedFunctionName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter tableValuedFunctionName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableValuedFunction(databaseName, schemaName, tableValuedFunctionName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlTableValuedFunctionInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getTableValuedFunctionDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlTableValuedFunctionInner> getTableValuedFunctionDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlTableValuedFunctionInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlTableValuedFunctionInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table valued functions.
     * @param schemaName The name of the schema containing the table valued functions.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableValuedFunctionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableValuedFunctionInner>> listTableValuedFunctions(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableValuedFunctions(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableValuedFunctionInner>> response = listTableValuedFunctionsDelegate(call.execute());
        PagedList<USqlTableValuedFunctionInner> result = new PagedList<USqlTableValuedFunctionInner>(response.getBody()) {
            @Override
            public Page<USqlTableValuedFunctionInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTableValuedFunctionsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table valued functions.
     * @param schemaName The name of the schema containing the table valued functions.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTableValuedFunctionsAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlTableValuedFunctionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableValuedFunctions(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableValuedFunctionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableValuedFunctionInner>> result = listTableValuedFunctionsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTableValuedFunctionsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table valued functions.
     * @param schemaName The name of the schema containing the table valued functions.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableValuedFunctionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableValuedFunctionInner>> listTableValuedFunctions(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableValuedFunctions(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableValuedFunctionInner>> response = listTableValuedFunctionsDelegate(call.execute());
        PagedList<USqlTableValuedFunctionInner> result = new PagedList<USqlTableValuedFunctionInner>(response.getBody()) {
            @Override
            public Page<USqlTableValuedFunctionInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTableValuedFunctionsNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table valued functions.
     * @param schemaName The name of the schema containing the table valued functions.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTableValuedFunctionsAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTableValuedFunctionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableValuedFunctions(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableValuedFunctionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableValuedFunctionInner>> result = listTableValuedFunctionsDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTableValuedFunctionsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTableValuedFunctionInner>> listTableValuedFunctionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableValuedFunctionInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableValuedFunctionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified assembly from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the assembly.
     * @param assemblyName The name of the assembly.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlAssemblyInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlAssemblyInner> getAssembly(String accountName, String databaseName, String assemblyName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (assemblyName == null) {
            throw new IllegalArgumentException("Parameter assemblyName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getAssembly(databaseName, assemblyName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getAssemblyDelegate(call.execute());
    }

    /**
     * Retrieves the specified assembly from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the assembly.
     * @param assemblyName The name of the assembly.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAssemblyAsync(String accountName, String databaseName, String assemblyName, final ServiceCallback<USqlAssemblyInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (assemblyName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter assemblyName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getAssembly(databaseName, assemblyName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlAssemblyInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getAssemblyDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlAssemblyInner> getAssemblyDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlAssemblyInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlAssemblyInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the assembly.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlAssemblyClrInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlAssemblyClrInner>> listAssemblies(final String accountName, final String databaseName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listAssemblies(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlAssemblyClrInner>> response = listAssembliesDelegate(call.execute());
        PagedList<USqlAssemblyClrInner> result = new PagedList<USqlAssemblyClrInner>(response.getBody()) {
            @Override
            public Page<USqlAssemblyClrInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listAssembliesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the assembly.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAssembliesAsync(final String accountName, final String databaseName, final ListOperationCallback<USqlAssemblyClrInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listAssemblies(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlAssemblyClrInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlAssemblyClrInner>> result = listAssembliesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listAssembliesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the assembly.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlAssemblyClrInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlAssemblyClrInner>> listAssemblies(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listAssemblies(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlAssemblyClrInner>> response = listAssembliesDelegate(call.execute());
        PagedList<USqlAssemblyClrInner> result = new PagedList<USqlAssemblyClrInner>(response.getBody()) {
            @Override
            public Page<USqlAssemblyClrInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listAssembliesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the assembly.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAssembliesAsync(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlAssemblyClrInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listAssemblies(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlAssemblyClrInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlAssemblyClrInner>> result = listAssembliesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listAssembliesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlAssemblyClrInner>> listAssembliesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlAssemblyClrInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlAssemblyClrInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified schema from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param schemaName The name of the schema.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlSchemaInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlSchemaInner> getSchema(String accountName, String databaseName, String schemaName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException("Parameter schemaName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getSchema(databaseName, schemaName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getSchemaDelegate(call.execute());
    }

    /**
     * Retrieves the specified schema from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param schemaName The name of the schema.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getSchemaAsync(String accountName, String databaseName, String schemaName, final ServiceCallback<USqlSchemaInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (schemaName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter schemaName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getSchema(databaseName, schemaName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlSchemaInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getSchemaDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlSchemaInner> getSchemaDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlSchemaInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlSchemaInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlSchemaInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlSchemaInner>> listSchemas(final String accountName, final String databaseName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listSchemas(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlSchemaInner>> response = listSchemasDelegate(call.execute());
        PagedList<USqlSchemaInner> result = new PagedList<USqlSchemaInner>(response.getBody()) {
            @Override
            public Page<USqlSchemaInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listSchemasNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listSchemasAsync(final String accountName, final String databaseName, final ListOperationCallback<USqlSchemaInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listSchemas(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlSchemaInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlSchemaInner>> result = listSchemasDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listSchemasNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlSchemaInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlSchemaInner>> listSchemas(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listSchemas(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlSchemaInner>> response = listSchemasDelegate(call.execute());
        PagedList<USqlSchemaInner> result = new PagedList<USqlSchemaInner>(response.getBody()) {
            @Override
            public Page<USqlSchemaInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listSchemasNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the schema.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listSchemasAsync(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlSchemaInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listSchemas(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlSchemaInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlSchemaInner>> result = listSchemasDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listSchemasNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlSchemaInner>> listSchemasDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlSchemaInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlSchemaInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified database from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlDatabaseInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlDatabaseInner> getDatabase(String accountName, String databaseName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (databaseName == null) {
            throw new IllegalArgumentException("Parameter databaseName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getDatabase(databaseName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getDatabaseDelegate(call.execute());
    }

    /**
     * Retrieves the specified database from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getDatabaseAsync(String accountName, String databaseName, final ServiceCallback<USqlDatabaseInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (databaseName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter databaseName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getDatabase(databaseName, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlDatabaseInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDatabaseDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlDatabaseInner> getDatabaseDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlDatabaseInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<USqlDatabaseInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlDatabaseInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlDatabaseInner>> listDatabases(final String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listDatabases(filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlDatabaseInner>> response = listDatabasesDelegate(call.execute());
        PagedList<USqlDatabaseInner> result = new PagedList<USqlDatabaseInner>(response.getBody()) {
            @Override
            public Page<USqlDatabaseInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listDatabasesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listDatabasesAsync(final String accountName, final ListOperationCallback<USqlDatabaseInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String filter = null;
        final Integer top = null;
        final Integer skip = null;
        final String expand = null;
        final String select = null;
        final String orderby = null;
        final Boolean count = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listDatabases(filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlDatabaseInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlDatabaseInner>> result = listDatabasesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listDatabasesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlDatabaseInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlDatabaseInner>> listDatabases(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listDatabases(filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<USqlDatabaseInner>> response = listDatabasesDelegate(call.execute());
        PagedList<USqlDatabaseInner> result = new PagedList<USqlDatabaseInner>(response.getBody()) {
            @Override
            public Page<USqlDatabaseInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listDatabasesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories?$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listDatabasesAsync(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlDatabaseInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listDatabases(filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlDatabaseInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlDatabaseInner>> result = listDatabasesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listDatabasesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlDatabaseInner>> listDatabasesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlDatabaseInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlDatabaseInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlExternalDataSourceInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlExternalDataSourceInner>> listExternalDataSourcesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listExternalDataSourcesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listExternalDataSourcesNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of external data sources from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listExternalDataSourcesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlExternalDataSourceInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listExternalDataSourcesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlExternalDataSourceInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlExternalDataSourceInner>> result = listExternalDataSourcesNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listExternalDataSourcesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlExternalDataSourceInner>> listExternalDataSourcesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlExternalDataSourceInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlExternalDataSourceInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlCredentialInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlCredentialInner>> listCredentialsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listCredentialsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listCredentialsNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of credentials from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listCredentialsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlCredentialInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listCredentialsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlCredentialInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlCredentialInner>> result = listCredentialsNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listCredentialsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlCredentialInner>> listCredentialsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlCredentialInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlCredentialInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlProcedureInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlProcedureInner>> listProceduresNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listProceduresNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listProceduresNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of procedures from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listProceduresNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlProcedureInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listProceduresNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlProcedureInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlProcedureInner>> result = listProceduresNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listProceduresNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlProcedureInner>> listProceduresNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlProcedureInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlProcedureInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTableInner>> listTablesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTablesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listTablesNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of tables from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTablesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTableInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTablesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableInner>> result = listTablesNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTablesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTableInner>> listTablesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlViewInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlViewInner>> listViewsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listViewsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listViewsNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of views from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listViewsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlViewInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listViewsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlViewInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlViewInner>> result = listViewsNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listViewsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlViewInner>> listViewsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlViewInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlViewInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of table statistics from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableStatisticsInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTableStatisticsInner>> listTableStatisticsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTableStatisticsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listTableStatisticsNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of table statistics from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTableStatisticsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTableStatisticsInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTableStatisticsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableStatisticsInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableStatisticsInner>> result = listTableStatisticsNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTableStatisticsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTableStatisticsInner>> listTableStatisticsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableStatisticsInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableStatisticsInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of table partitions from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTablePartitionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTablePartitionInner>> listTablePartitionsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTablePartitionsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listTablePartitionsNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of table partitions from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTablePartitionsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTablePartitionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTablePartitionsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTablePartitionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTablePartitionInner>> result = listTablePartitionsNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTablePartitionsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTablePartitionInner>> listTablePartitionsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTablePartitionInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTablePartitionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTypeInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTypeInner>> listTypesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTypesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listTypesNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of types within the specified database and schema from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTypesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTypeInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTypesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTypeInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTypeInner>> result = listTypesNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTypesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTypeInner>> listTypesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTypeInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTypeInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableValuedFunctionInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTableValuedFunctionInner>> listTableValuedFunctionsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTableValuedFunctionsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listTableValuedFunctionsNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of table valued functions from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTableValuedFunctionsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTableValuedFunctionInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listTableValuedFunctionsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableValuedFunctionInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableValuedFunctionInner>> result = listTableValuedFunctionsNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTableValuedFunctionsNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlTableValuedFunctionInner>> listTableValuedFunctionsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableValuedFunctionInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableValuedFunctionInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlAssemblyClrInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlAssemblyClrInner>> listAssembliesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listAssembliesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listAssembliesNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of assemblies from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAssembliesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlAssemblyClrInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listAssembliesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlAssemblyClrInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlAssemblyClrInner>> result = listAssembliesNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listAssembliesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlAssemblyClrInner>> listAssembliesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlAssemblyClrInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlAssemblyClrInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlSchemaInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlSchemaInner>> listSchemasNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listSchemasNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listSchemasNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of schemas from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listSchemasNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlSchemaInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listSchemasNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlSchemaInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlSchemaInner>> result = listSchemasNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listSchemasNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlSchemaInner>> listSchemasNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlSchemaInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlSchemaInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlDatabaseInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlDatabaseInner>> listDatabasesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listDatabasesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listDatabasesNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of databases from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listDatabasesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlDatabaseInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        this.client.restClient().setBaseUrl();
        Call<ResponseBody> call = service.listDatabasesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlDatabaseInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlDatabaseInner>> result = listDatabasesNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listDatabasesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
                    } else {
                        serviceCallback.success(new ServiceResponse<>(serviceCallback.get(), result.getResponse()));
                    }
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<PageImpl<USqlDatabaseInner>> listDatabasesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlDatabaseInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlDatabaseInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
