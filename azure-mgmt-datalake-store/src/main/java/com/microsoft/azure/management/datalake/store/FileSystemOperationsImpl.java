/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.datalake.store.models.AclStatusResult;
import com.microsoft.azure.management.datalake.store.models.AppendModeType;
import com.microsoft.azure.management.datalake.store.models.ContentSummaryResult;
import com.microsoft.azure.management.datalake.store.models.FileOperationResult;
import com.microsoft.azure.management.datalake.store.models.FileStatusesResult;
import com.microsoft.azure.management.datalake.store.models.FileStatusResult;
import com.microsoft.rest.serializer.CollectionFormat;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.Validator;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * An instance of this class provides access to all the operations defined
 * in FileSystemOperations.
 */
public final class FileSystemOperationsImpl implements FileSystemOperations {
    /** The Retrofit service to perform REST calls. */
    private FileSystemService service;
    /** The service client containing this operation class. */
    private DataLakeStoreFileSystemManagementClient client;

    /**
     * Initializes an instance of FileSystemOperations.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public FileSystemOperationsImpl(Retrofit retrofit, DataLakeStoreFileSystemManagementClient client) {
        this.service = retrofit.create(FileSystemService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for FileSystemOperations to be
     * used by Retrofit to perform actually REST calls.
     */
    interface FileSystemService {
        @Headers("Content-Type: application/octet-stream")
        @POST("WebHdfsExt/{filePath}")
        Call<ResponseBody> concurrentAppend(@Path("filePath") String filePath, @Body RequestBody streamContents, @Query("appendMode") AppendModeType appendMode, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{path}")
        Call<ResponseBody> checkAccess(@Path("path") String path, @Query("fsaction") String fsaction, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{path}")
        Call<ResponseBody> mkdirs(@Path("path") String path, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("webhdfs/v1/{destinationPath}")
        Call<ResponseBody> concat(@Path("destinationPath") String destinationPath, @Query("sources") String sources, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/octet-stream")
        @POST("webhdfs/v1/{msConcatDestinationPath}")
        Call<ResponseBody> msConcat(@Path("msConcatDestinationPath") String msConcatDestinationPath, @Query("deleteSourceDirectory") Boolean deleteSourceDirectory, @Body RequestBody streamContents, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{listFilePath}")
        Call<ResponseBody> listFileStatus(@Path("listFilePath") String listFilePath, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/va/{getContentSummaryFilePath}")
        Call<ResponseBody> getContentSummary(@Path("getContentSummaryFilePath") String getContentSummaryFilePath, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{getFilePath}")
        Call<ResponseBody> getFileStatus(@Path("getFilePath") String getFilePath, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/octet-stream")
        @POST("webhdfs/v1/{directFilePath}")
        Call<ResponseBody> append(@Path("directFilePath") String directFilePath, @Body RequestBody streamContents, @Query("op") String op, @Query("append") String append, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/octet-stream")
        @PUT("webhdfs/v1/{directFilePath}")
        Call<ResponseBody> create(@Path("directFilePath") String directFilePath, @Body RequestBody streamContents, @Query("overwrite") Boolean overwrite, @Query("op") String op, @Query("write") String write, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{directFilePath}")
        @Streaming
        Call<ResponseBody> open(@Path("directFilePath") String directFilePath, @Query("length") Long length, @Query("offset") Long offset, @Query("op") String op, @Query("read") String read, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{setAclFilePath}")
        Call<ResponseBody> setAcl(@Path("setAclFilePath") String setAclFilePath, @Query("aclspec") String aclspec, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{modifyAclFilePath}")
        Call<ResponseBody> modifyAclEntries(@Path("modifyAclFilePath") String modifyAclFilePath, @Query("aclspec") String aclspec, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{removeAclFilePath}")
        Call<ResponseBody> removeAclEntries(@Path("removeAclFilePath") String removeAclFilePath, @Query("aclspec") String aclspec, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{aclFilePath}")
        Call<ResponseBody> removeAcl(@Path("aclFilePath") String aclFilePath, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{aclFilePath}")
        Call<ResponseBody> getAclStatus(@Path("aclFilePath") String aclFilePath, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "webhdfs/v1/{filePath}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("filePath") String filePath, @Query("recursive") Boolean recursive, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{renameFilePath}")
        Call<ResponseBody> rename(@Path("renameFilePath") String renameFilePath, @Query("destination") String destination, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{setOwnerFilePath}")
        Call<ResponseBody> setOwner(@Path("setOwnerFilePath") String setOwnerFilePath, @Query("owner") String owner, @Query("group") String group, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{setPermissionFilePath}")
        Call<ResponseBody> setPermission(@Path("setPermissionFilePath") String setPermissionFilePath, @Query("permission") String permission, @Query("op") String op, @Query("api-version") String apiVersion, @Header("subscriptionId") String subscriptionId, @Header("accept-language") String acceptLanguage);

    }

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> concurrentAppend(String filePath, String accountName, byte[] streamContents) throws CloudException, IOException, IllegalArgumentException {
        if (filePath == null) {
            throw new IllegalArgumentException("Parameter filePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "CONCURRENTAPPEND";
        final AppendModeType appendMode = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.concurrentAppend(filePath, streamContentsConverted, appendMode, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return concurrentAppendDelegate(call.execute());
    }

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall concurrentAppendAsync(String filePath, String accountName, byte[] streamContents, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (filePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter filePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "CONCURRENTAPPEND";
        final AppendModeType appendMode = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.concurrentAppend(filePath, streamContentsConverted, appendMode, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(concurrentAppendDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @param appendMode Indicates the concurrent append call should create the file if it doesn't exist or just open the existing file for append. Possible values include: 'autocreate'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> concurrentAppend(String filePath, String accountName, byte[] streamContents, AppendModeType appendMode) throws CloudException, IOException, IllegalArgumentException {
        if (filePath == null) {
            throw new IllegalArgumentException("Parameter filePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "CONCURRENTAPPEND";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.concurrentAppend(filePath, streamContentsConverted, appendMode, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return concurrentAppendDelegate(call.execute());
    }

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @param appendMode Indicates the concurrent append call should create the file if it doesn't exist or just open the existing file for append. Possible values include: 'autocreate'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall concurrentAppendAsync(String filePath, String accountName, byte[] streamContents, AppendModeType appendMode, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (filePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter filePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "CONCURRENTAPPEND";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.concurrentAppend(filePath, streamContentsConverted, appendMode, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(concurrentAppendDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> concurrentAppendDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> checkAccess(String path, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("Parameter path is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "CHECKACCESS";
        final String fsaction = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.checkAccess(path, fsaction, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return checkAccessDelegate(call.execute());
    }

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall checkAccessAsync(String path, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (path == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter path is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "CHECKACCESS";
        final String fsaction = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.checkAccess(path, fsaction, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(checkAccessDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param fsaction File system operation read/write/execute in string form, matching regex pattern '[rwx-]{3}'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> checkAccess(String path, String accountName, String fsaction) throws CloudException, IOException, IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("Parameter path is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "CHECKACCESS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.checkAccess(path, fsaction, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return checkAccessDelegate(call.execute());
    }

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param fsaction File system operation read/write/execute in string form, matching regex pattern '[rwx-]{3}'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall checkAccessAsync(String path, String accountName, String fsaction, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (path == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter path is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "CHECKACCESS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.checkAccess(path, fsaction, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(checkAccessDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> checkAccessDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Creates a directory.
     *
     * @param path The Data Lake Store path (starting with '/') of the directory to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileOperationResult> mkdirs(String path, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("Parameter path is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "MKDIRS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.mkdirs(path, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return mkdirsDelegate(call.execute());
    }

    /**
     * Creates a directory.
     *
     * @param path The Data Lake Store path (starting with '/') of the directory to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall mkdirsAsync(String path, String accountName, final ServiceCallback<FileOperationResult> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (path == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter path is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "MKDIRS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.mkdirs(path, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileOperationResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(mkdirsDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileOperationResult> mkdirsDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileOperationResult, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<FileOperationResult>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Concatenates the list of source files into the destination file, removing all source files upon success.
     *
     * @param destinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param sources A list of comma seperated Data Lake Store paths (starting with '/') of the files to concatenate, in the order in which they should be concatenated.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> concat(String destinationPath, String accountName, List<String> sources) throws CloudException, IOException, IllegalArgumentException {
        if (destinationPath == null) {
            throw new IllegalArgumentException("Parameter destinationPath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (sources == null) {
            throw new IllegalArgumentException("Parameter sources is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        Validator.validate(sources);
        final String op = "CONCAT";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        String sourcesConverted = this.client.getMapperAdapter().serializeList(sources, CollectionFormat.CSV);
        Call<ResponseBody> call = service.concat(destinationPath, sourcesConverted, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return concatDelegate(call.execute());
    }

    /**
     * Concatenates the list of source files into the destination file, removing all source files upon success.
     *
     * @param destinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param sources A list of comma seperated Data Lake Store paths (starting with '/') of the files to concatenate, in the order in which they should be concatenated.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall concatAsync(String destinationPath, String accountName, List<String> sources, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (destinationPath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter destinationPath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (sources == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter sources is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        Validator.validate(sources, serviceCallback);
        final String op = "CONCAT";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        String sourcesConverted = this.client.getMapperAdapter().serializeList(sources, CollectionFormat.CSV);
        Call<ResponseBody> call = service.concat(destinationPath, sourcesConverted, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(concatDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> concatDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> msConcat(String msConcatDestinationPath, String accountName, byte[] streamContents) throws CloudException, IOException, IllegalArgumentException {
        if (msConcatDestinationPath == null) {
            throw new IllegalArgumentException("Parameter msConcatDestinationPath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "MSCONCAT";
        final Boolean deleteSourceDirectory = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.msConcat(msConcatDestinationPath, deleteSourceDirectory, streamContentsConverted, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return msConcatDelegate(call.execute());
    }

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall msConcatAsync(String msConcatDestinationPath, String accountName, byte[] streamContents, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (msConcatDestinationPath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter msConcatDestinationPath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "MSCONCAT";
        final Boolean deleteSourceDirectory = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.msConcat(msConcatDestinationPath, deleteSourceDirectory, streamContentsConverted, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(msConcatDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @param deleteSourceDirectory Indicates that as an optimization instead of deleting each individual source stream, delete the source stream folder if all streams are in the same folder instead. This results in a substantial performance improvement when the only streams in the folder are part of the concatenation operation. WARNING: This includes the deletion of any other files that are not source files. Only set this to true when source files are the only files in the source directory.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> msConcat(String msConcatDestinationPath, String accountName, byte[] streamContents, Boolean deleteSourceDirectory) throws CloudException, IOException, IllegalArgumentException {
        if (msConcatDestinationPath == null) {
            throw new IllegalArgumentException("Parameter msConcatDestinationPath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "MSCONCAT";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.msConcat(msConcatDestinationPath, deleteSourceDirectory, streamContentsConverted, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return msConcatDelegate(call.execute());
    }

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @param deleteSourceDirectory Indicates that as an optimization instead of deleting each individual source stream, delete the source stream folder if all streams are in the same folder instead. This results in a substantial performance improvement when the only streams in the folder are part of the concatenation operation. WARNING: This includes the deletion of any other files that are not source files. Only set this to true when source files are the only files in the source directory.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall msConcatAsync(String msConcatDestinationPath, String accountName, byte[] streamContents, Boolean deleteSourceDirectory, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (msConcatDestinationPath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter msConcatDestinationPath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "MSCONCAT";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.msConcat(msConcatDestinationPath, deleteSourceDirectory, streamContentsConverted, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(msConcatDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> msConcatDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Get the list of file status objects specified by the file path, with optional pagination parameters.
     *
     * @param listFilePath The Data Lake Store path (starting with '/') of the directory to list.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileStatusesResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileStatusesResult> listFileStatus(String listFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (listFilePath == null) {
            throw new IllegalArgumentException("Parameter listFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "LISTSTATUS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.listFileStatus(listFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return listFileStatusDelegate(call.execute());
    }

    /**
     * Get the list of file status objects specified by the file path, with optional pagination parameters.
     *
     * @param listFilePath The Data Lake Store path (starting with '/') of the directory to list.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFileStatusAsync(String listFilePath, String accountName, final ServiceCallback<FileStatusesResult> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (listFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter listFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "LISTSTATUS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.listFileStatus(listFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileStatusesResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(listFileStatusDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileStatusesResult> listFileStatusDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileStatusesResult, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<FileStatusesResult>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Gets the file content summary object specified by the file path.
     *
     * @param getContentSummaryFilePath The Data Lake Store path (starting with '/') of the file for which to retrieve the summary.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ContentSummaryResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ContentSummaryResult> getContentSummary(String getContentSummaryFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (getContentSummaryFilePath == null) {
            throw new IllegalArgumentException("Parameter getContentSummaryFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "GETCONTENTSUMMARY";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getContentSummary(getContentSummaryFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return getContentSummaryDelegate(call.execute());
    }

    /**
     * Gets the file content summary object specified by the file path.
     *
     * @param getContentSummaryFilePath The Data Lake Store path (starting with '/') of the file for which to retrieve the summary.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getContentSummaryAsync(String getContentSummaryFilePath, String accountName, final ServiceCallback<ContentSummaryResult> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (getContentSummaryFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter getContentSummaryFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "GETCONTENTSUMMARY";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getContentSummary(getContentSummaryFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ContentSummaryResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getContentSummaryDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ContentSummaryResult> getContentSummaryDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ContentSummaryResult, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<ContentSummaryResult>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Get the file status object specified by the file path.
     *
     * @param getFilePath The Data Lake Store path (starting with '/') of the file or directory for which to retrieve the status.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileStatusResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileStatusResult> getFileStatus(String getFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (getFilePath == null) {
            throw new IllegalArgumentException("Parameter getFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "GETFILESTATUS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getFileStatus(getFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return getFileStatusDelegate(call.execute());
    }

    /**
     * Get the file status object specified by the file path.
     *
     * @param getFilePath The Data Lake Store path (starting with '/') of the file or directory for which to retrieve the status.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getFileStatusAsync(String getFilePath, String accountName, final ServiceCallback<FileStatusResult> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (getFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter getFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "GETFILESTATUS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getFileStatus(getFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileStatusResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getFileStatusDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileStatusResult> getFileStatusDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileStatusResult, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<FileStatusResult>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Appends to the specified file. This method does not support multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option. Use the ConcurrentAppend option if you would like support for concurrent appends.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to which to append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> append(String directFilePath, String accountName, byte[] streamContents) throws CloudException, IOException, IllegalArgumentException {
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "APPEND";
        final String append = "true";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.append(directFilePath, streamContentsConverted, op, append, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return appendDelegate(call.execute());
    }

    /**
     * Appends to the specified file. This method does not support multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option. Use the ConcurrentAppend option if you would like support for concurrent appends.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to which to append.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when appending to the file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall appendAsync(String directFilePath, String accountName, byte[] streamContents, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "APPEND";
        final String append = "true";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.append(directFilePath, streamContentsConverted, op, append, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(appendDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> appendDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Creates a file with optionally specified content.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> create(String directFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "CREATE";
        final String write = "true";
        final RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[0]);
        final Boolean overwrite = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.create(directFilePath, streamContentsConverted, overwrite, op, write, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return createDelegate(call.execute());
    }

    /**
     * Creates a file with optionally specified content.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createAsync(String directFilePath, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "CREATE";
        final String write = "true";
        final RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[0]);
        final Boolean overwrite = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.create(directFilePath, streamContentsConverted, overwrite, op, write, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Creates a file with optionally specified content.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when creating the file. This parameter is optional, resulting in an empty file if not specified.
     * @param overwrite The indication of if the file should be overwritten.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> create(String directFilePath, String accountName, byte[] streamContents, Boolean overwrite) throws CloudException, IOException, IllegalArgumentException {
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "CREATE";
        final String write = "true";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[0]);
        if (streamContents != null) {
            streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        }
        Call<ResponseBody> call = service.create(directFilePath, streamContentsConverted, overwrite, op, write, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return createDelegate(call.execute());
    }

    /**
     * Creates a file with optionally specified content.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param streamContents The file contents to include when creating the file. This parameter is optional, resulting in an empty file if not specified.
     * @param overwrite The indication of if the file should be overwritten.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createAsync(String directFilePath, String accountName, byte[] streamContents, Boolean overwrite, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "CREATE";
        final String write = "true";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[0]);
        if (streamContents != null) {
            streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        }
        Call<ResponseBody> call = service.create(directFilePath, streamContentsConverted, overwrite, op, write, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> createDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Opens and reads from the specified file.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<InputStream> open(String directFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "OPEN";
        final String read = "true";
        final Long length = null;
        final Long offset = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.open(directFilePath, length, offset, op, read, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return openDelegate(call.execute());
    }

    /**
     * Opens and reads from the specified file.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall openAsync(String directFilePath, String accountName, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "OPEN";
        final String read = "true";
        final Long length = null;
        final Long offset = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.open(directFilePath, length, offset, op, read, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(openDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Opens and reads from the specified file.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param length the Long value
     * @param offset the Long value
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<InputStream> open(String directFilePath, String accountName, Long length, Long offset) throws CloudException, IOException, IllegalArgumentException {
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "OPEN";
        final String read = "true";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.open(directFilePath, length, offset, op, read, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return openDelegate(call.execute());
    }

    /**
     * Opens and reads from the specified file.
     *
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param length the Long value
     * @param offset the Long value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall openAsync(String directFilePath, String accountName, Long length, Long offset, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "OPEN";
        final String read = "true";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.open(directFilePath, length, offset, op, read, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(openDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<InputStream> openDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<InputStream, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Sets the Access Control List (ACL) for a file or folder.
     *
     * @param setAclFilePath The Data Lake Store path (starting with '/') of the file or directory on which to set the ACL.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL spec included in ACL creation operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setAcl(String setAclFilePath, String accountName, String aclspec) throws CloudException, IOException, IllegalArgumentException {
        if (setAclFilePath == null) {
            throw new IllegalArgumentException("Parameter setAclFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (aclspec == null) {
            throw new IllegalArgumentException("Parameter aclspec is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "SETACL";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setAcl(setAclFilePath, aclspec, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return setAclDelegate(call.execute());
    }

    /**
     * Sets the Access Control List (ACL) for a file or folder.
     *
     * @param setAclFilePath The Data Lake Store path (starting with '/') of the file or directory on which to set the ACL.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL spec included in ACL creation operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setAclAsync(String setAclFilePath, String accountName, String aclspec, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (setAclFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setAclFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (aclspec == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter aclspec is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "SETACL";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setAcl(setAclFilePath, aclspec, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setAclDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> setAclDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Modifies existing Access Control List (ACL) entries on a file or folder.
     *
     * @param modifyAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being modified.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL specification included in ACL modification operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> modifyAclEntries(String modifyAclFilePath, String accountName, String aclspec) throws CloudException, IOException, IllegalArgumentException {
        if (modifyAclFilePath == null) {
            throw new IllegalArgumentException("Parameter modifyAclFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (aclspec == null) {
            throw new IllegalArgumentException("Parameter aclspec is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "MODIFYACLENTRIES";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.modifyAclEntries(modifyAclFilePath, aclspec, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return modifyAclEntriesDelegate(call.execute());
    }

    /**
     * Modifies existing Access Control List (ACL) entries on a file or folder.
     *
     * @param modifyAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being modified.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL specification included in ACL modification operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall modifyAclEntriesAsync(String modifyAclFilePath, String accountName, String aclspec, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (modifyAclFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter modifyAclFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (aclspec == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter aclspec is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "MODIFYACLENTRIES";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.modifyAclEntries(modifyAclFilePath, aclspec, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(modifyAclEntriesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> modifyAclEntriesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Removes existing Access Control List (ACL) entries for a file or folder.
     *
     * @param removeAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL spec included in ACL removal operations in the format '[default:]user|group|other'
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> removeAclEntries(String removeAclFilePath, String accountName, String aclspec) throws CloudException, IOException, IllegalArgumentException {
        if (removeAclFilePath == null) {
            throw new IllegalArgumentException("Parameter removeAclFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (aclspec == null) {
            throw new IllegalArgumentException("Parameter aclspec is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "REMOVEACLENTRIES";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.removeAclEntries(removeAclFilePath, aclspec, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return removeAclEntriesDelegate(call.execute());
    }

    /**
     * Removes existing Access Control List (ACL) entries for a file or folder.
     *
     * @param removeAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclspec The ACL spec included in ACL removal operations in the format '[default:]user|group|other'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall removeAclEntriesAsync(String removeAclFilePath, String accountName, String aclspec, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (removeAclFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter removeAclFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (aclspec == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter aclspec is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "REMOVEACLENTRIES";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.removeAclEntries(removeAclFilePath, aclspec, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(removeAclEntriesDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> removeAclEntriesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Removes the existing Access Control List (ACL) of the specified file or directory.
     *
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> removeAcl(String aclFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (aclFilePath == null) {
            throw new IllegalArgumentException("Parameter aclFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "REMOVEACL";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.removeAcl(aclFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return removeAclDelegate(call.execute());
    }

    /**
     * Removes the existing Access Control List (ACL) of the specified file or directory.
     *
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall removeAclAsync(String aclFilePath, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (aclFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter aclFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "REMOVEACL";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.removeAcl(aclFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(removeAclDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> removeAclDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Gets Access Control List (ACL) entries for the specified file or directory.
     *
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory for which to get the ACL.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AclStatusResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<AclStatusResult> getAclStatus(String aclFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (aclFilePath == null) {
            throw new IllegalArgumentException("Parameter aclFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "GETACLSTATUS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getAclStatus(aclFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return getAclStatusDelegate(call.execute());
    }

    /**
     * Gets Access Control List (ACL) entries for the specified file or directory.
     *
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory for which to get the ACL.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAclStatusAsync(String aclFilePath, String accountName, final ServiceCallback<AclStatusResult> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (aclFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter aclFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "GETACLSTATUS";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getAclStatus(aclFilePath, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<AclStatusResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getAclStatusDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<AclStatusResult> getAclStatusDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<AclStatusResult, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<AclStatusResult>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileOperationResult> delete(String filePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (filePath == null) {
            throw new IllegalArgumentException("Parameter filePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "DELETE";
        final Boolean recursive = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.delete(filePath, recursive, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String filePath, String accountName, final ServiceCallback<FileOperationResult> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (filePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter filePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "DELETE";
        final Boolean recursive = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.delete(filePath, recursive, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileOperationResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param recursive The optional switch indicating if the delete should be recursive
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileOperationResult> delete(String filePath, String accountName, Boolean recursive) throws CloudException, IOException, IllegalArgumentException {
        if (filePath == null) {
            throw new IllegalArgumentException("Parameter filePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "DELETE";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.delete(filePath, recursive, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param recursive The optional switch indicating if the delete should be recursive
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String filePath, String accountName, Boolean recursive, final ServiceCallback<FileOperationResult> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (filePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter filePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "DELETE";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.delete(filePath, recursive, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileOperationResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileOperationResult> deleteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileOperationResult, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<FileOperationResult>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Rename a file or directory.
     *
     * @param renameFilePath The Data Lake Store path (starting with '/') of the file or directory to move/rename.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param destination The path to move/rename the file or folder to
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResult object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileOperationResult> rename(String renameFilePath, String accountName, String destination) throws CloudException, IOException, IllegalArgumentException {
        if (renameFilePath == null) {
            throw new IllegalArgumentException("Parameter renameFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Parameter destination is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "RENAME";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.rename(renameFilePath, destination, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return renameDelegate(call.execute());
    }

    /**
     * Rename a file or directory.
     *
     * @param renameFilePath The Data Lake Store path (starting with '/') of the file or directory to move/rename.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param destination The path to move/rename the file or folder to
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall renameAsync(String renameFilePath, String accountName, String destination, final ServiceCallback<FileOperationResult> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (renameFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter renameFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (destination == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter destination is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "RENAME";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.rename(renameFilePath, destination, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileOperationResult>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(renameDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileOperationResult> renameDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileOperationResult, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<FileOperationResult>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Sets the owner of a file or directory.
     *
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setOwner(String setOwnerFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (setOwnerFilePath == null) {
            throw new IllegalArgumentException("Parameter setOwnerFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "SETOWNER";
        final String owner = null;
        final String group = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setOwner(setOwnerFilePath, owner, group, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return setOwnerDelegate(call.execute());
    }

    /**
     * Sets the owner of a file or directory.
     *
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setOwnerAsync(String setOwnerFilePath, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (setOwnerFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setOwnerFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "SETOWNER";
        final String owner = null;
        final String group = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setOwner(setOwnerFilePath, owner, group, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setOwnerDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Sets the owner of a file or directory.
     *
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param owner The AAD Object ID of the user owner of the file or directory. If empty, the property will remain unchanged.
     * @param group The AAD Object ID of the group owner of the file or directory. If empty, the property will remain unchanged.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setOwner(String setOwnerFilePath, String accountName, String owner, String group) throws CloudException, IOException, IllegalArgumentException {
        if (setOwnerFilePath == null) {
            throw new IllegalArgumentException("Parameter setOwnerFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "SETOWNER";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setOwner(setOwnerFilePath, owner, group, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return setOwnerDelegate(call.execute());
    }

    /**
     * Sets the owner of a file or directory.
     *
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param owner The AAD Object ID of the user owner of the file or directory. If empty, the property will remain unchanged.
     * @param group The AAD Object ID of the group owner of the file or directory. If empty, the property will remain unchanged.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setOwnerAsync(String setOwnerFilePath, String accountName, String owner, String group, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (setOwnerFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setOwnerFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "SETOWNER";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setOwner(setOwnerFilePath, owner, group, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setOwnerDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> setOwnerDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Sets the permission of the file or folder.
     *
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setPermission(String setPermissionFilePath, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (setPermissionFilePath == null) {
            throw new IllegalArgumentException("Parameter setPermissionFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "SETPERMISSION";
        final String permission = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setPermission(setPermissionFilePath, permission, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return setPermissionDelegate(call.execute());
    }

    /**
     * Sets the permission of the file or folder.
     *
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setPermissionAsync(String setPermissionFilePath, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (setPermissionFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setPermissionFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "SETPERMISSION";
        final String permission = null;
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setPermission(setPermissionFilePath, permission, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setPermissionDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Sets the permission of the file or folder.
     *
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param permission A string representation of the permission (i.e 'rwx'). If empty, this property remains unchanged.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setPermission(String setPermissionFilePath, String accountName, String permission) throws CloudException, IOException, IllegalArgumentException {
        if (setPermissionFilePath == null) {
            throw new IllegalArgumentException("Parameter setPermissionFilePath is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        if (this.client.getSubscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null.");
        }
        final String op = "SETPERMISSION";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setPermission(setPermissionFilePath, permission, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        return setPermissionDelegate(call.execute());
    }

    /**
     * Sets the permission of the file or folder.
     *
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param permission A string representation of the permission (i.e 'rwx'). If empty, this property remains unchanged.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setPermissionAsync(String setPermissionFilePath, String accountName, String permission, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (setPermissionFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setPermissionFilePath is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.getAdlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getAdlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        if (this.client.getSubscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getSubscriptionId() is required and cannot be null."));
            return null;
        }
        final String op = "SETPERMISSION";
        this.client.getBaseUrl().set("{accountName}", accountName);
        this.client.getBaseUrl().set("{adlsFileSystemDnsSuffix}", this.client.getAdlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setPermission(setPermissionFilePath, permission, op, this.client.getApiVersion(), this.client.getSubscriptionId(), this.client.getAcceptLanguage());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setPermissionDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> setPermissionDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .build(response);
    }

}
