/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation;

import retrofit2.Retrofit;
import com.microsoft.azure.management.datalake.analytics.Catalogs;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.datalake.analytics.models.DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters;
import com.microsoft.azure.management.datalake.analytics.models.PageImpl;
import com.microsoft.azure.management.datalake.analytics.models.USqlAssembly;
import com.microsoft.azure.management.datalake.analytics.models.USqlAssemblyClr;
import com.microsoft.azure.management.datalake.analytics.models.USqlCredential;
import com.microsoft.azure.management.datalake.analytics.models.USqlDatabase;
import com.microsoft.azure.management.datalake.analytics.models.USqlExternalDataSource;
import com.microsoft.azure.management.datalake.analytics.models.USqlProcedure;
import com.microsoft.azure.management.datalake.analytics.models.USqlSchema;
import com.microsoft.azure.management.datalake.analytics.models.USqlSecret;
import com.microsoft.azure.management.datalake.analytics.models.USqlTable;
import com.microsoft.azure.management.datalake.analytics.models.USqlTablePartition;
import com.microsoft.azure.management.datalake.analytics.models.USqlTableStatistics;
import com.microsoft.azure.management.datalake.analytics.models.USqlTableType;
import com.microsoft.azure.management.datalake.analytics.models.USqlTableValuedFunction;
import com.microsoft.azure.management.datalake.analytics.models.USqlType;
import com.microsoft.azure.management.datalake.analytics.models.USqlView;
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
public final class CatalogsImpl implements Catalogs {
    /** The Retrofit service to perform REST calls. */
    private CatalogsService service;
    /** The service client containing this operation class. */
    private DataLakeAnalyticsCatalogManagementClientImpl client;

    /**
     * Initializes an instance of CatalogsImpl.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public CatalogsImpl(Retrofit retrofit, DataLakeAnalyticsCatalogManagementClientImpl client) {
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
        Call<ResponseBody> createSecret(@Path("databaseName") String databaseName, @Path("secretName") String secretName, @Body DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PATCH("catalog/usql/databases/{databaseName}/secrets/{secretName}")
        Call<ResponseBody> updateSecret(@Path("databaseName") String databaseName, @Path("secretName") String secretName, @Body DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/secrets/{secretName}")
        Call<ResponseBody> getSecret(@Path("databaseName") String databaseName, @Path("secretName") String secretName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "catalog/usql/databases/{databaseName}/secrets/{secretName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteSecret(@Path("databaseName") String databaseName, @Path("secretName") String secretName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "catalog/usql/databases/{databaseName}/secrets", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteAllSecrets(@Path("databaseName") String databaseName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/externaldatasources/{externalDataSourceName}")
        Call<ResponseBody> getExternalDataSource(@Path("databaseName") String databaseName, @Path("externalDataSourceName") String externalDataSourceName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/externaldatasources")
        Call<ResponseBody> listExternalDataSources(@Path("databaseName") String databaseName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/credentials/{credentialName}")
        Call<ResponseBody> getCredential(@Path("databaseName") String databaseName, @Path("credentialName") String credentialName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/credentials")
        Call<ResponseBody> listCredentials(@Path("databaseName") String databaseName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/procedures/{procedureName}")
        Call<ResponseBody> getProcedure(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("procedureName") String procedureName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/procedures")
        Call<ResponseBody> listProcedures(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}")
        Call<ResponseBody> getTable(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables")
        Call<ResponseBody> listTables(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tabletypes/{tableTypeName}")
        Call<ResponseBody> getTableType(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableTypeName") String tableTypeName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tabletypes")
        Call<ResponseBody> listTableTypes(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/views/{viewName}")
        Call<ResponseBody> getView(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("viewName") String viewName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/views")
        Call<ResponseBody> listViews(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}/statistics/{statisticsName}")
        Call<ResponseBody> getTableStatistic(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Path("statisticsName") String statisticsName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}/statistics")
        Call<ResponseBody> listTableStatistics(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}/partitions/{partitionName}")
        Call<ResponseBody> getTablePartition(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Path("partitionName") String partitionName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tables/{tableName}/partitions")
        Call<ResponseBody> listTablePartitions(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableName") String tableName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/types")
        Call<ResponseBody> listTypes(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tablevaluedfunctions/{tableValuedFunctionName}")
        Call<ResponseBody> getTableValuedFunction(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Path("tableValuedFunctionName") String tableValuedFunctionName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}/tablevaluedfunctions")
        Call<ResponseBody> listTableValuedFunctions(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/assemblies/{assemblyName}")
        Call<ResponseBody> getAssembly(@Path("databaseName") String databaseName, @Path("assemblyName") String assemblyName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/assemblies")
        Call<ResponseBody> listAssemblies(@Path("databaseName") String databaseName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas/{schemaName}")
        Call<ResponseBody> getSchema(@Path("databaseName") String databaseName, @Path("schemaName") String schemaName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}/schemas")
        Call<ResponseBody> listSchemas(@Path("databaseName") String databaseName, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases/{databaseName}")
        Call<ResponseBody> getDatabase(@Path("databaseName") String databaseName, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("catalog/usql/databases")
        Call<ResponseBody> listDatabases(@Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("x-ms-parameterized-host") String parameterizedHost, @Header("User-Agent") String userAgent);

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
        Call<ResponseBody> listTableTypesNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

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
     * @return the USqlSecret object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlSecret> createSecret(String accountName, String databaseName, String secretName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.createSecret(databaseName, secretName, parameters, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall createSecretAsync(String accountName, String databaseName, String secretName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters, final ServiceCallback<USqlSecret> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.createSecret(databaseName, secretName, parameters, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlSecret>(serviceCallback) {
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

    private ServiceResponse<USqlSecret> createSecretDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlSecret, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlSecret>() { }.getType())
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
     * @return the USqlSecret object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlSecret> updateSecret(String accountName, String databaseName, String secretName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.updateSecret(databaseName, secretName, parameters, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall updateSecretAsync(String accountName, String databaseName, String secretName, DataLakeAnalyticsCatalogSecretCreateOrUpdateParameters parameters, final ServiceCallback<USqlSecret> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.updateSecret(databaseName, secretName, parameters, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlSecret>(serviceCallback) {
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

    private ServiceResponse<USqlSecret> updateSecretDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlSecret, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlSecret>() { }.getType())
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
     * @return the USqlSecret object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlSecret> getSecret(String accountName, String databaseName, String secretName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getSecret(databaseName, secretName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getSecretAsync(String accountName, String databaseName, String secretName, final ServiceCallback<USqlSecret> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getSecret(databaseName, secretName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlSecret>(serviceCallback) {
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

    private ServiceResponse<USqlSecret> getSecretDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlSecret, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlSecret>() { }.getType())
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.deleteSecret(databaseName, secretName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.deleteSecret(databaseName, secretName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.mapperAdapter())
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.deleteAllSecrets(databaseName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.deleteAllSecrets(databaseName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.mapperAdapter())
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
     * @return the USqlExternalDataSource object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlExternalDataSource> getExternalDataSource(String accountName, String databaseName, String externalDataSourceName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getExternalDataSource(databaseName, externalDataSourceName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getExternalDataSourceAsync(String accountName, String databaseName, String externalDataSourceName, final ServiceCallback<USqlExternalDataSource> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getExternalDataSource(databaseName, externalDataSourceName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlExternalDataSource>(serviceCallback) {
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

    private ServiceResponse<USqlExternalDataSource> getExternalDataSourceDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlExternalDataSource, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlExternalDataSource>() { }.getType())
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
     * @return the List&lt;USqlExternalDataSource&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlExternalDataSource>> listExternalDataSources(final String accountName, final String databaseName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listExternalDataSources(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlExternalDataSource>> response = listExternalDataSourcesDelegate(call.execute());
        PagedList<USqlExternalDataSource> result = new PagedList<USqlExternalDataSource>(response.getBody()) {
            @Override
            public Page<USqlExternalDataSource> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listExternalDataSourcesAsync(final String accountName, final String databaseName, final ListOperationCallback<USqlExternalDataSource> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listExternalDataSources(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlExternalDataSource>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlExternalDataSource>> result = listExternalDataSourcesDelegate(response);
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
     * @return the List&lt;USqlExternalDataSource&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlExternalDataSource>> listExternalDataSources(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listExternalDataSources(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlExternalDataSource>> response = listExternalDataSourcesDelegate(call.execute());
        PagedList<USqlExternalDataSource> result = new PagedList<USqlExternalDataSource>(response.getBody()) {
            @Override
            public Page<USqlExternalDataSource> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listExternalDataSourcesAsync(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlExternalDataSource> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listExternalDataSources(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlExternalDataSource>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlExternalDataSource>> result = listExternalDataSourcesDelegate(response);
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

    private ServiceResponse<PageImpl<USqlExternalDataSource>> listExternalDataSourcesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlExternalDataSource>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlExternalDataSource>>() { }.getType())
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
     * @return the USqlCredential object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlCredential> getCredential(String accountName, String databaseName, String credentialName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getCredential(databaseName, credentialName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getCredentialAsync(String accountName, String databaseName, String credentialName, final ServiceCallback<USqlCredential> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getCredential(databaseName, credentialName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlCredential>(serviceCallback) {
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

    private ServiceResponse<USqlCredential> getCredentialDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlCredential, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlCredential>() { }.getType())
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
     * @return the List&lt;USqlCredential&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlCredential>> listCredentials(final String accountName, final String databaseName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listCredentials(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlCredential>> response = listCredentialsDelegate(call.execute());
        PagedList<USqlCredential> result = new PagedList<USqlCredential>(response.getBody()) {
            @Override
            public Page<USqlCredential> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listCredentialsAsync(final String accountName, final String databaseName, final ListOperationCallback<USqlCredential> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listCredentials(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlCredential>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlCredential>> result = listCredentialsDelegate(response);
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
     * @return the List&lt;USqlCredential&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlCredential>> listCredentials(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listCredentials(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlCredential>> response = listCredentialsDelegate(call.execute());
        PagedList<USqlCredential> result = new PagedList<USqlCredential>(response.getBody()) {
            @Override
            public Page<USqlCredential> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listCredentialsAsync(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlCredential> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listCredentials(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlCredential>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlCredential>> result = listCredentialsDelegate(response);
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

    private ServiceResponse<PageImpl<USqlCredential>> listCredentialsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlCredential>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlCredential>>() { }.getType())
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
     * @return the USqlProcedure object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlProcedure> getProcedure(String accountName, String databaseName, String schemaName, String procedureName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getProcedure(databaseName, schemaName, procedureName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getProcedureAsync(String accountName, String databaseName, String schemaName, String procedureName, final ServiceCallback<USqlProcedure> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getProcedure(databaseName, schemaName, procedureName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlProcedure>(serviceCallback) {
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

    private ServiceResponse<USqlProcedure> getProcedureDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlProcedure, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlProcedure>() { }.getType())
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
     * @return the List&lt;USqlProcedure&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlProcedure>> listProcedures(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listProcedures(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlProcedure>> response = listProceduresDelegate(call.execute());
        PagedList<USqlProcedure> result = new PagedList<USqlProcedure>(response.getBody()) {
            @Override
            public Page<USqlProcedure> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listProceduresAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlProcedure> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listProcedures(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlProcedure>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlProcedure>> result = listProceduresDelegate(response);
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
     * @return the List&lt;USqlProcedure&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlProcedure>> listProcedures(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listProcedures(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlProcedure>> response = listProceduresDelegate(call.execute());
        PagedList<USqlProcedure> result = new PagedList<USqlProcedure>(response.getBody()) {
            @Override
            public Page<USqlProcedure> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listProceduresAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlProcedure> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listProcedures(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlProcedure>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlProcedure>> result = listProceduresDelegate(response);
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

    private ServiceResponse<PageImpl<USqlProcedure>> listProceduresDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlProcedure>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlProcedure>>() { }.getType())
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
     * @return the USqlTable object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlTable> getTable(String accountName, String databaseName, String schemaName, String tableName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTable(databaseName, schemaName, tableName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getTableAsync(String accountName, String databaseName, String schemaName, String tableName, final ServiceCallback<USqlTable> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTable(databaseName, schemaName, tableName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlTable>(serviceCallback) {
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

    private ServiceResponse<USqlTable> getTableDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlTable, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlTable>() { }.getType())
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
     * @return the List&lt;USqlTable&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTable>> listTables(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTables(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTable>> response = listTablesDelegate(call.execute());
        PagedList<USqlTable> result = new PagedList<USqlTable>(response.getBody()) {
            @Override
            public Page<USqlTable> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTablesAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlTable> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTables(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTable>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTable>> result = listTablesDelegate(response);
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
     * @return the List&lt;USqlTable&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTable>> listTables(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTables(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTable>> response = listTablesDelegate(call.execute());
        PagedList<USqlTable> result = new PagedList<USqlTable>(response.getBody()) {
            @Override
            public Page<USqlTable> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTablesAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTable> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTables(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTable>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTable>> result = listTablesDelegate(response);
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

    private ServiceResponse<PageImpl<USqlTable>> listTablesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTable>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTable>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the specified table type from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table type.
     * @param schemaName The name of the schema containing the table type.
     * @param tableTypeName The name of the table type to retrieve.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the USqlTableType object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlTableType> getTableType(String accountName, String databaseName, String schemaName, String tableTypeName) throws CloudException, IOException, IllegalArgumentException {
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
        if (tableTypeName == null) {
            throw new IllegalArgumentException("Parameter tableTypeName is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableType(databaseName, schemaName, tableTypeName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        return getTableTypeDelegate(call.execute());
    }

    /**
     * Retrieves the specified table type from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table type.
     * @param schemaName The name of the schema containing the table type.
     * @param tableTypeName The name of the table type to retrieve.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getTableTypeAsync(String accountName, String databaseName, String schemaName, String tableTypeName, final ServiceCallback<USqlTableType> serviceCallback) throws IllegalArgumentException {
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
        if (tableTypeName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter tableTypeName is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableType(databaseName, schemaName, tableTypeName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlTableType>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getTableTypeDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<USqlTableType> getTableTypeDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlTableType, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlTableType>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of table types from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table types.
     * @param schemaName The name of the schema containing the table types.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableType&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableType>> listTableTypes(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableType>> response = listTableTypesDelegate(call.execute());
        PagedList<USqlTableType> result = new PagedList<USqlTableType>(response.getBody()) {
            @Override
            public Page<USqlTableType> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTableTypesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of table types from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table types.
     * @param schemaName The name of the schema containing the table types.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTableTypesAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlTableType> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableType>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableType>> result = listTableTypesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTableTypesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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
     * Retrieves the list of table types from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table types.
     * @param schemaName The name of the schema containing the table types.
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
     * @return the List&lt;USqlTableType&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableType>> listTableTypes(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableType>> response = listTableTypesDelegate(call.execute());
        PagedList<USqlTableType> result = new PagedList<USqlTableType>(response.getBody()) {
            @Override
            public Page<USqlTableType> nextPage(String nextPageLink) throws CloudException, IOException {
                return listTableTypesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Retrieves the list of table types from the Data Lake Analytics catalog.
     *
     * @param accountName The Azure Data Lake Analytics account to execute catalog operations on.
     * @param databaseName The name of the database containing the table types.
     * @param schemaName The name of the schema containing the table types.
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
    public ServiceCall listTableTypesAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTableType> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableType>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableType>> result = listTableTypesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTableTypesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<USqlTableType>> listTableTypesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableType>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableType>>() { }.getType())
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
     * @return the USqlView object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlView> getView(String accountName, String databaseName, String schemaName, String viewName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getView(databaseName, schemaName, viewName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getViewAsync(String accountName, String databaseName, String schemaName, String viewName, final ServiceCallback<USqlView> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getView(databaseName, schemaName, viewName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlView>(serviceCallback) {
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

    private ServiceResponse<USqlView> getViewDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlView, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlView>() { }.getType())
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
     * @return the List&lt;USqlView&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlView>> listViews(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listViews(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlView>> response = listViewsDelegate(call.execute());
        PagedList<USqlView> result = new PagedList<USqlView>(response.getBody()) {
            @Override
            public Page<USqlView> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listViewsAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlView> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listViews(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlView>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlView>> result = listViewsDelegate(response);
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
     * @return the List&lt;USqlView&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlView>> listViews(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listViews(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlView>> response = listViewsDelegate(call.execute());
        PagedList<USqlView> result = new PagedList<USqlView>(response.getBody()) {
            @Override
            public Page<USqlView> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listViewsAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlView> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listViews(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlView>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlView>> result = listViewsDelegate(response);
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

    private ServiceResponse<PageImpl<USqlView>> listViewsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlView>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlView>>() { }.getType())
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
     * @return the USqlTableStatistics object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlTableStatistics> getTableStatistic(String accountName, String databaseName, String schemaName, String tableName, String statisticsName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableStatistic(databaseName, schemaName, tableName, statisticsName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getTableStatisticAsync(String accountName, String databaseName, String schemaName, String tableName, String statisticsName, final ServiceCallback<USqlTableStatistics> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableStatistic(databaseName, schemaName, tableName, statisticsName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlTableStatistics>(serviceCallback) {
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

    private ServiceResponse<USqlTableStatistics> getTableStatisticDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlTableStatistics, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlTableStatistics>() { }.getType())
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
     * @return the List&lt;USqlTableStatistics&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableStatistics>> listTableStatistics(final String accountName, final String databaseName, final String schemaName, final String tableName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableStatistics(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableStatistics>> response = listTableStatisticsDelegate(call.execute());
        PagedList<USqlTableStatistics> result = new PagedList<USqlTableStatistics>(response.getBody()) {
            @Override
            public Page<USqlTableStatistics> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTableStatisticsAsync(final String accountName, final String databaseName, final String schemaName, final String tableName, final ListOperationCallback<USqlTableStatistics> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableStatistics(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableStatistics>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableStatistics>> result = listTableStatisticsDelegate(response);
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
     * @return the List&lt;USqlTableStatistics&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableStatistics>> listTableStatistics(final String accountName, final String databaseName, final String schemaName, final String tableName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableStatistics(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableStatistics>> response = listTableStatisticsDelegate(call.execute());
        PagedList<USqlTableStatistics> result = new PagedList<USqlTableStatistics>(response.getBody()) {
            @Override
            public Page<USqlTableStatistics> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTableStatisticsAsync(final String accountName, final String databaseName, final String schemaName, final String tableName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTableStatistics> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableStatistics(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableStatistics>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableStatistics>> result = listTableStatisticsDelegate(response);
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

    private ServiceResponse<PageImpl<USqlTableStatistics>> listTableStatisticsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableStatistics>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableStatistics>>() { }.getType())
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
     * @return the USqlTablePartition object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlTablePartition> getTablePartition(String accountName, String databaseName, String schemaName, String tableName, String partitionName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTablePartition(databaseName, schemaName, tableName, partitionName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getTablePartitionAsync(String accountName, String databaseName, String schemaName, String tableName, String partitionName, final ServiceCallback<USqlTablePartition> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTablePartition(databaseName, schemaName, tableName, partitionName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlTablePartition>(serviceCallback) {
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

    private ServiceResponse<USqlTablePartition> getTablePartitionDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlTablePartition, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlTablePartition>() { }.getType())
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
     * @return the List&lt;USqlTablePartition&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTablePartition>> listTablePartitions(final String accountName, final String databaseName, final String schemaName, final String tableName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTablePartitions(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTablePartition>> response = listTablePartitionsDelegate(call.execute());
        PagedList<USqlTablePartition> result = new PagedList<USqlTablePartition>(response.getBody()) {
            @Override
            public Page<USqlTablePartition> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTablePartitionsAsync(final String accountName, final String databaseName, final String schemaName, final String tableName, final ListOperationCallback<USqlTablePartition> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTablePartitions(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTablePartition>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTablePartition>> result = listTablePartitionsDelegate(response);
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
     * @return the List&lt;USqlTablePartition&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTablePartition>> listTablePartitions(final String accountName, final String databaseName, final String schemaName, final String tableName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTablePartitions(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTablePartition>> response = listTablePartitionsDelegate(call.execute());
        PagedList<USqlTablePartition> result = new PagedList<USqlTablePartition>(response.getBody()) {
            @Override
            public Page<USqlTablePartition> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTablePartitionsAsync(final String accountName, final String databaseName, final String schemaName, final String tableName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTablePartition> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTablePartitions(databaseName, schemaName, tableName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTablePartition>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTablePartition>> result = listTablePartitionsDelegate(response);
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

    private ServiceResponse<PageImpl<USqlTablePartition>> listTablePartitionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTablePartition>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTablePartition>>() { }.getType())
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
     * @return the List&lt;USqlType&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlType>> listTypes(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlType>> response = listTypesDelegate(call.execute());
        PagedList<USqlType> result = new PagedList<USqlType>(response.getBody()) {
            @Override
            public Page<USqlType> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTypesAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlType> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlType>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlType>> result = listTypesDelegate(response);
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
     * @return the List&lt;USqlType&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlType>> listTypes(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlType>> response = listTypesDelegate(call.execute());
        PagedList<USqlType> result = new PagedList<USqlType>(response.getBody()) {
            @Override
            public Page<USqlType> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTypesAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlType> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTypes(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlType>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlType>> result = listTypesDelegate(response);
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

    private ServiceResponse<PageImpl<USqlType>> listTypesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlType>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlType>>() { }.getType())
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
     * @return the USqlTableValuedFunction object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlTableValuedFunction> getTableValuedFunction(String accountName, String databaseName, String schemaName, String tableValuedFunctionName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableValuedFunction(databaseName, schemaName, tableValuedFunctionName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getTableValuedFunctionAsync(String accountName, String databaseName, String schemaName, String tableValuedFunctionName, final ServiceCallback<USqlTableValuedFunction> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getTableValuedFunction(databaseName, schemaName, tableValuedFunctionName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlTableValuedFunction>(serviceCallback) {
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

    private ServiceResponse<USqlTableValuedFunction> getTableValuedFunctionDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlTableValuedFunction, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlTableValuedFunction>() { }.getType())
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
     * @return the List&lt;USqlTableValuedFunction&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableValuedFunction>> listTableValuedFunctions(final String accountName, final String databaseName, final String schemaName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableValuedFunctions(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableValuedFunction>> response = listTableValuedFunctionsDelegate(call.execute());
        PagedList<USqlTableValuedFunction> result = new PagedList<USqlTableValuedFunction>(response.getBody()) {
            @Override
            public Page<USqlTableValuedFunction> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTableValuedFunctionsAsync(final String accountName, final String databaseName, final String schemaName, final ListOperationCallback<USqlTableValuedFunction> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableValuedFunctions(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableValuedFunction>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableValuedFunction>> result = listTableValuedFunctionsDelegate(response);
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
     * @return the List&lt;USqlTableValuedFunction&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlTableValuedFunction>> listTableValuedFunctions(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableValuedFunctions(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlTableValuedFunction>> response = listTableValuedFunctionsDelegate(call.execute());
        PagedList<USqlTableValuedFunction> result = new PagedList<USqlTableValuedFunction>(response.getBody()) {
            @Override
            public Page<USqlTableValuedFunction> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listTableValuedFunctionsAsync(final String accountName, final String databaseName, final String schemaName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlTableValuedFunction> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listTableValuedFunctions(databaseName, schemaName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableValuedFunction>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableValuedFunction>> result = listTableValuedFunctionsDelegate(response);
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

    private ServiceResponse<PageImpl<USqlTableValuedFunction>> listTableValuedFunctionsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableValuedFunction>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableValuedFunction>>() { }.getType())
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
     * @return the USqlAssembly object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlAssembly> getAssembly(String accountName, String databaseName, String assemblyName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getAssembly(databaseName, assemblyName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getAssemblyAsync(String accountName, String databaseName, String assemblyName, final ServiceCallback<USqlAssembly> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getAssembly(databaseName, assemblyName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlAssembly>(serviceCallback) {
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

    private ServiceResponse<USqlAssembly> getAssemblyDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlAssembly, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlAssembly>() { }.getType())
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
     * @return the List&lt;USqlAssemblyClr&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlAssemblyClr>> listAssemblies(final String accountName, final String databaseName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listAssemblies(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlAssemblyClr>> response = listAssembliesDelegate(call.execute());
        PagedList<USqlAssemblyClr> result = new PagedList<USqlAssemblyClr>(response.getBody()) {
            @Override
            public Page<USqlAssemblyClr> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listAssembliesAsync(final String accountName, final String databaseName, final ListOperationCallback<USqlAssemblyClr> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listAssemblies(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlAssemblyClr>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlAssemblyClr>> result = listAssembliesDelegate(response);
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
     * @return the List&lt;USqlAssemblyClr&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlAssemblyClr>> listAssemblies(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listAssemblies(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlAssemblyClr>> response = listAssembliesDelegate(call.execute());
        PagedList<USqlAssemblyClr> result = new PagedList<USqlAssemblyClr>(response.getBody()) {
            @Override
            public Page<USqlAssemblyClr> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listAssembliesAsync(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlAssemblyClr> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listAssemblies(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlAssemblyClr>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlAssemblyClr>> result = listAssembliesDelegate(response);
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

    private ServiceResponse<PageImpl<USqlAssemblyClr>> listAssembliesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlAssemblyClr>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlAssemblyClr>>() { }.getType())
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
     * @return the USqlSchema object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlSchema> getSchema(String accountName, String databaseName, String schemaName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getSchema(databaseName, schemaName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getSchemaAsync(String accountName, String databaseName, String schemaName, final ServiceCallback<USqlSchema> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getSchema(databaseName, schemaName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlSchema>(serviceCallback) {
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

    private ServiceResponse<USqlSchema> getSchemaDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlSchema, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlSchema>() { }.getType())
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
     * @return the List&lt;USqlSchema&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlSchema>> listSchemas(final String accountName, final String databaseName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listSchemas(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlSchema>> response = listSchemasDelegate(call.execute());
        PagedList<USqlSchema> result = new PagedList<USqlSchema>(response.getBody()) {
            @Override
            public Page<USqlSchema> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listSchemasAsync(final String accountName, final String databaseName, final ListOperationCallback<USqlSchema> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listSchemas(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlSchema>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlSchema>> result = listSchemasDelegate(response);
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
     * @return the List&lt;USqlSchema&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlSchema>> listSchemas(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listSchemas(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlSchema>> response = listSchemasDelegate(call.execute());
        PagedList<USqlSchema> result = new PagedList<USqlSchema>(response.getBody()) {
            @Override
            public Page<USqlSchema> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listSchemasAsync(final String accountName, final String databaseName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlSchema> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listSchemas(databaseName, filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlSchema>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlSchema>> result = listSchemasDelegate(response);
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

    private ServiceResponse<PageImpl<USqlSchema>> listSchemasDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlSchema>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlSchema>>() { }.getType())
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
     * @return the USqlDatabase object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<USqlDatabase> getDatabase(String accountName, String databaseName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getDatabase(databaseName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
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
    public ServiceCall getDatabaseAsync(String accountName, String databaseName, final ServiceCallback<USqlDatabase> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.getDatabase(databaseName, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<USqlDatabase>(serviceCallback) {
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

    private ServiceResponse<USqlDatabase> getDatabaseDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<USqlDatabase, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<USqlDatabase>() { }.getType())
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
     * @return the List&lt;USqlDatabase&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlDatabase>> listDatabases(final String accountName) throws CloudException, IOException, IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listDatabases(filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlDatabase>> response = listDatabasesDelegate(call.execute());
        PagedList<USqlDatabase> result = new PagedList<USqlDatabase>(response.getBody()) {
            @Override
            public Page<USqlDatabase> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listDatabasesAsync(final String accountName, final ListOperationCallback<USqlDatabase> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listDatabases(filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlDatabase>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlDatabase>> result = listDatabasesDelegate(response);
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
     * @return the List&lt;USqlDatabase&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<USqlDatabase>> listDatabases(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count) throws CloudException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlaCatalogDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlaCatalogDnsSuffix() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listDatabases(filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        ServiceResponse<PageImpl<USqlDatabase>> response = listDatabasesDelegate(call.execute());
        PagedList<USqlDatabase> result = new PagedList<USqlDatabase>(response.getBody()) {
            @Override
            public Page<USqlDatabase> nextPage(String nextPageLink) throws CloudException, IOException {
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
    public ServiceCall listDatabasesAsync(final String accountName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final ListOperationCallback<USqlDatabase> serviceCallback) throws IllegalArgumentException {
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
        String parameterizedHost = Joiner.on(", ").join("{accountName}", accountName, "{adlaCatalogDnsSuffix}", this.client.adlaCatalogDnsSuffix());
        Call<ResponseBody> call = service.listDatabases(filter, top, skip, expand, select, orderby, count, this.client.apiVersion(), this.client.acceptLanguage(), parameterizedHost, this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlDatabase>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlDatabase>> result = listDatabasesDelegate(response);
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

    private ServiceResponse<PageImpl<USqlDatabase>> listDatabasesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlDatabase>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlDatabase>>() { }.getType())
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
     * @return the List&lt;USqlExternalDataSource&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlExternalDataSource>> listExternalDataSourcesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listExternalDataSourcesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlExternalDataSource> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listExternalDataSourcesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlExternalDataSource>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlExternalDataSource>> result = listExternalDataSourcesNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlExternalDataSource>> listExternalDataSourcesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlExternalDataSource>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlExternalDataSource>>() { }.getType())
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
     * @return the List&lt;USqlCredential&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlCredential>> listCredentialsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listCredentialsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlCredential> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listCredentialsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlCredential>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlCredential>> result = listCredentialsNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlCredential>> listCredentialsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlCredential>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlCredential>>() { }.getType())
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
     * @return the List&lt;USqlProcedure&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlProcedure>> listProceduresNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listProceduresNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlProcedure> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listProceduresNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlProcedure>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlProcedure>> result = listProceduresNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlProcedure>> listProceduresNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlProcedure>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlProcedure>>() { }.getType())
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
     * @return the List&lt;USqlTable&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTable>> listTablesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listTablesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTable> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listTablesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTable>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTable>> result = listTablesNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlTable>> listTablesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTable>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTable>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Retrieves the list of table types from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;USqlTableType&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTableType>> listTableTypesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listTableTypesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listTableTypesNextDelegate(call.execute());
    }

    /**
     * Retrieves the list of table types from the Data Lake Analytics catalog.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listTableTypesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTableType> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listTableTypesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableType>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableType>> result = listTableTypesNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listTableTypesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<USqlTableType>> listTableTypesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableType>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableType>>() { }.getType())
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
     * @return the List&lt;USqlView&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlView>> listViewsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listViewsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlView> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listViewsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlView>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlView>> result = listViewsNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlView>> listViewsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlView>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlView>>() { }.getType())
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
     * @return the List&lt;USqlTableStatistics&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTableStatistics>> listTableStatisticsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listTableStatisticsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTableStatistics> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listTableStatisticsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableStatistics>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableStatistics>> result = listTableStatisticsNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlTableStatistics>> listTableStatisticsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableStatistics>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableStatistics>>() { }.getType())
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
     * @return the List&lt;USqlTablePartition&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTablePartition>> listTablePartitionsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listTablePartitionsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTablePartition> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listTablePartitionsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTablePartition>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTablePartition>> result = listTablePartitionsNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlTablePartition>> listTablePartitionsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTablePartition>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTablePartition>>() { }.getType())
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
     * @return the List&lt;USqlType&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlType>> listTypesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listTypesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlType> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listTypesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlType>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlType>> result = listTypesNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlType>> listTypesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlType>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlType>>() { }.getType())
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
     * @return the List&lt;USqlTableValuedFunction&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlTableValuedFunction>> listTableValuedFunctionsNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listTableValuedFunctionsNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlTableValuedFunction> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listTableValuedFunctionsNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlTableValuedFunction>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlTableValuedFunction>> result = listTableValuedFunctionsNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlTableValuedFunction>> listTableValuedFunctionsNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlTableValuedFunction>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlTableValuedFunction>>() { }.getType())
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
     * @return the List&lt;USqlAssemblyClr&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlAssemblyClr>> listAssembliesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listAssembliesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlAssemblyClr> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listAssembliesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlAssemblyClr>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlAssemblyClr>> result = listAssembliesNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlAssemblyClr>> listAssembliesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlAssemblyClr>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlAssemblyClr>>() { }.getType())
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
     * @return the List&lt;USqlSchema&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlSchema>> listSchemasNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listSchemasNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlSchema> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listSchemasNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlSchema>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlSchema>> result = listSchemasNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlSchema>> listSchemasNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlSchema>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlSchema>>() { }.getType())
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
     * @return the List&lt;USqlDatabase&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<USqlDatabase>> listDatabasesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
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
    public ServiceCall listDatabasesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<USqlDatabase> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listDatabasesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<USqlDatabase>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<USqlDatabase>> result = listDatabasesNextDelegate(response);
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

    private ServiceResponse<PageImpl<USqlDatabase>> listDatabasesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<USqlDatabase>, CloudException>(this.client.mapperAdapter())
                .register(200, new TypeToken<PageImpl<USqlDatabase>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
