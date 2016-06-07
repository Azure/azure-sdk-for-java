/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import retrofit2.Retrofit;
import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
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

/**
 * An instance of this class provides access to all the operations defined
 * in FileSystems.
 */
public final class FileSystemsInner {
    /** The Retrofit service to perform REST calls. */
    private FileSystemsService service;
    /** The service client containing this operation class. */
    private DataLakeStoreFileSystemManagementClientImpl client;

    /**
     * Initializes an instance of FileSystemsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public FileSystemsInner(Retrofit retrofit, DataLakeStoreFileSystemManagementClientImpl client) {
        this.service = retrofit.create(FileSystemsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for FileSystems to be
     * used by Retrofit to perform actually REST calls.
     */
    interface FileSystemsService {
        @Headers("Content-Type: application/octet-stream")
        @POST("WebHdfsExt/{filePath}")
        Call<ResponseBody> concurrentAppend(@Path("filePath") String filePath, @Body RequestBody streamContents, @Query("appendMode") AppendModeType appendMode, @Query("op") String op, @Header("Transfer-Encoding") String transferEncoding, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{path}")
        Call<ResponseBody> checkAccess(@Path("path") String path, @Query("fsaction") String fsaction, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{path}")
        Call<ResponseBody> mkdirs(@Path("path") String path, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @POST("webhdfs/v1/{destinationPath}")
        Call<ResponseBody> concat(@Path("destinationPath") String destinationPath, @Query("sources") String sources, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/octet-stream")
        @POST("webhdfs/v1/{msConcatDestinationPath}")
        Call<ResponseBody> msConcat(@Path("msConcatDestinationPath") String msConcatDestinationPath, @Query("deleteSourceDirectory") Boolean deleteSourceDirectory, @Body RequestBody streamContents, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{listFilePath}")
        Call<ResponseBody> listFileStatus(@Path("listFilePath") String listFilePath, @Query("listSize") Integer listSize, @Query("listAfter") String listAfter, @Query("listBefore") String listBefore, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/va/{getContentSummaryFilePath}")
        Call<ResponseBody> getContentSummary(@Path("getContentSummaryFilePath") String getContentSummaryFilePath, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{getFilePath}")
        Call<ResponseBody> getFileStatus(@Path("getFilePath") String getFilePath, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/octet-stream")
        @POST("webhdfs/v1/{directFilePath}")
        Call<ResponseBody> append(@Path("directFilePath") String directFilePath, @Body RequestBody streamContents, @Query("op") String op, @Query("append") String append, @Header("Transfer-Encoding") String transferEncoding, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/octet-stream")
        @PUT("webhdfs/v1/{directFilePath}")
        Call<ResponseBody> create(@Path("directFilePath") String directFilePath, @Body RequestBody streamContents, @Query("overwrite") Boolean overwrite, @Query("op") String op, @Query("write") String write, @Header("Transfer-Encoding") String transferEncoding, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{directFilePath}")
        @Streaming
        Call<ResponseBody> open(@Path("directFilePath") String directFilePath, @Query("length") Long length, @Query("offset") Long offset, @Query("op") String op, @Query("read") String read, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{setAclFilePath}")
        Call<ResponseBody> setAcl(@Path("setAclFilePath") String setAclFilePath, @Query("aclspec") String aclspec, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{modifyAclFilePath}")
        Call<ResponseBody> modifyAclEntries(@Path("modifyAclFilePath") String modifyAclFilePath, @Query("aclspec") String aclspec, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{removeAclFilePath}")
        Call<ResponseBody> removeAclEntries(@Path("removeAclFilePath") String removeAclFilePath, @Query("aclspec") String aclspec, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("webhdfs/v1/{aclFilePath}")
        Call<ResponseBody> getAclStatus(@Path("aclFilePath") String aclFilePath, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "webhdfs/v1/{filePath}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("filePath") String filePath, @Query("recursive") Boolean recursive, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{renameFilePath}")
        Call<ResponseBody> rename(@Path("renameFilePath") String renameFilePath, @Query("destination") String destination, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{setOwnerFilePath}")
        Call<ResponseBody> setOwner(@Path("setOwnerFilePath") String setOwnerFilePath, @Query("owner") String owner, @Query("group") String group, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("webhdfs/v1/{setPermissionFilePath}")
        Call<ResponseBody> setPermission(@Path("setPermissionFilePath") String setPermissionFilePath, @Query("permission") String permission, @Query("op") String op, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param streamContents The file contents to include when appending to the file.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> concurrentAppend(String accountName, String filePath, byte[] streamContents) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("Parameter filePath is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "CONCURRENTAPPEND";
        final String transferEncoding = "chunked";
        final AppendModeType appendMode = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.concurrentAppend(filePath, streamContentsConverted, appendMode, op, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return concurrentAppendDelegate(call.execute());
    }

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param streamContents The file contents to include when appending to the file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall concurrentAppendAsync(String accountName, String filePath, byte[] streamContents, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (filePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter filePath is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "CONCURRENTAPPEND";
        final String transferEncoding = "chunked";
        final AppendModeType appendMode = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.concurrentAppend(filePath, streamContentsConverted, appendMode, op, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(concurrentAppendDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param streamContents The file contents to include when appending to the file.
     * @param appendMode Indicates the concurrent append call should create the file if it doesn't exist or just open the existing file for append. Possible values include: 'autocreate'
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> concurrentAppend(String accountName, String filePath, byte[] streamContents, AppendModeType appendMode) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("Parameter filePath is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "CONCURRENTAPPEND";
        final String transferEncoding = "chunked";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.concurrentAppend(filePath, streamContentsConverted, appendMode, op, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return concurrentAppendDelegate(call.execute());
    }

    /**
     * Appends to the specified file. This method supports multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param filePath The Data Lake Store path (starting with '/') of the file to which to append using concurrent append.
     * @param streamContents The file contents to include when appending to the file.
     * @param appendMode Indicates the concurrent append call should create the file if it doesn't exist or just open the existing file for append. Possible values include: 'autocreate'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall concurrentAppendAsync(String accountName, String filePath, byte[] streamContents, AppendModeType appendMode, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (filePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter filePath is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "CONCURRENTAPPEND";
        final String transferEncoding = "chunked";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.concurrentAppend(filePath, streamContentsConverted, appendMode, op, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(concurrentAppendDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> concurrentAppendDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> checkAccess(String accountName, String path) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (path == null) {
            throw new IllegalArgumentException("Parameter path is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "CHECKACCESS";
        final String fsaction = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.checkAccess(path, fsaction, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return checkAccessDelegate(call.execute());
    }

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall checkAccessAsync(String accountName, String path, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (path == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter path is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "CHECKACCESS";
        final String fsaction = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.checkAccess(path, fsaction, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(checkAccessDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param fsaction File system operation read/write/execute in string form, matching regex pattern '[rwx-]{3}'
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> checkAccess(String accountName, String path, String fsaction) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (path == null) {
            throw new IllegalArgumentException("Parameter path is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "CHECKACCESS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.checkAccess(path, fsaction, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return checkAccessDelegate(call.execute());
    }

    /**
     * Checks if the specified access is available at the given path.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param path The Data Lake Store path (starting with '/') of the file or directory for which to check access.
     * @param fsaction File system operation read/write/execute in string form, matching regex pattern '[rwx-]{3}'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall checkAccessAsync(String accountName, String path, String fsaction, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (path == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter path is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "CHECKACCESS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.checkAccess(path, fsaction, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(checkAccessDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> checkAccessDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Creates a directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param path The Data Lake Store path (starting with '/') of the directory to create.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileOperationResultInner> mkdirs(String accountName, String path) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (path == null) {
            throw new IllegalArgumentException("Parameter path is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "MKDIRS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.mkdirs(path, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return mkdirsDelegate(call.execute());
    }

    /**
     * Creates a directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param path The Data Lake Store path (starting with '/') of the directory to create.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall mkdirsAsync(String accountName, String path, final ServiceCallback<FileOperationResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (path == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter path is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "MKDIRS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.mkdirs(path, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileOperationResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(mkdirsDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileOperationResultInner> mkdirsDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileOperationResultInner, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<FileOperationResultInner>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Concatenates the list of source files into the destination file, removing all source files upon success.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param destinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param sources A list of comma seperated Data Lake Store paths (starting with '/') of the files to concatenate, in the order in which they should be concatenated.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> concat(String accountName, String destinationPath, List<String> sources) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (destinationPath == null) {
            throw new IllegalArgumentException("Parameter destinationPath is required and cannot be null.");
        }
        if (sources == null) {
            throw new IllegalArgumentException("Parameter sources is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(sources);
        final String op = "CONCAT";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        String sourcesConverted = this.client.restClient().mapperAdapter().serializeList(sources, CollectionFormat.CSV);
        Call<ResponseBody> call = service.concat(destinationPath, sourcesConverted, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return concatDelegate(call.execute());
    }

    /**
     * Concatenates the list of source files into the destination file, removing all source files upon success.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param destinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param sources A list of comma seperated Data Lake Store paths (starting with '/') of the files to concatenate, in the order in which they should be concatenated.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall concatAsync(String accountName, String destinationPath, List<String> sources, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (destinationPath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter destinationPath is required and cannot be null."));
            return null;
        }
        if (sources == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter sources is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(sources, serviceCallback);
        final String op = "CONCAT";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        String sourcesConverted = this.client.restClient().mapperAdapter().serializeList(sources, CollectionFormat.CSV);
        Call<ResponseBody> call = service.concat(destinationPath, sourcesConverted, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(concatDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> concatDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> msConcat(String accountName, String msConcatDestinationPath, byte[] streamContents) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (msConcatDestinationPath == null) {
            throw new IllegalArgumentException("Parameter msConcatDestinationPath is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "MSCONCAT";
        final Boolean deleteSourceDirectory = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.msConcat(msConcatDestinationPath, deleteSourceDirectory, streamContentsConverted, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return msConcatDelegate(call.execute());
    }

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall msConcatAsync(String accountName, String msConcatDestinationPath, byte[] streamContents, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (msConcatDestinationPath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter msConcatDestinationPath is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "MSCONCAT";
        final Boolean deleteSourceDirectory = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.msConcat(msConcatDestinationPath, deleteSourceDirectory, streamContentsConverted, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(msConcatDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @param deleteSourceDirectory Indicates that as an optimization instead of deleting each individual source stream, delete the source stream folder if all streams are in the same folder instead. This results in a substantial performance improvement when the only streams in the folder are part of the concatenation operation. WARNING: This includes the deletion of any other files that are not source files. Only set this to true when source files are the only files in the source directory.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> msConcat(String accountName, String msConcatDestinationPath, byte[] streamContents, Boolean deleteSourceDirectory) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (msConcatDestinationPath == null) {
            throw new IllegalArgumentException("Parameter msConcatDestinationPath is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "MSCONCAT";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.msConcat(msConcatDestinationPath, deleteSourceDirectory, streamContentsConverted, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return msConcatDelegate(call.execute());
    }

    /**
     * Concatenates the list of source files into the destination file, deleting all source files upon success. This method accepts more source file paths than the Concat method. This method and the parameters it accepts are subject to change for usability in an upcoming version.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param msConcatDestinationPath The Data Lake Store path (starting with '/') of the destination file resulting from the concatenation.
     * @param streamContents A list of Data Lake Store paths (starting with '/') of the source files. Must be in the format: sources=&lt;comma separated list&gt;
     * @param deleteSourceDirectory Indicates that as an optimization instead of deleting each individual source stream, delete the source stream folder if all streams are in the same folder instead. This results in a substantial performance improvement when the only streams in the folder are part of the concatenation operation. WARNING: This includes the deletion of any other files that are not source files. Only set this to true when source files are the only files in the source directory.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall msConcatAsync(String accountName, String msConcatDestinationPath, byte[] streamContents, Boolean deleteSourceDirectory, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (msConcatDestinationPath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter msConcatDestinationPath is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "MSCONCAT";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.msConcat(msConcatDestinationPath, deleteSourceDirectory, streamContentsConverted, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(msConcatDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> msConcatDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Get the list of file status objects specified by the file path, with optional pagination parameters.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param listFilePath The Data Lake Store path (starting with '/') of the directory to list.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileStatusesResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileStatusesResultInner> listFileStatus(String accountName, String listFilePath) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (listFilePath == null) {
            throw new IllegalArgumentException("Parameter listFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "MSLISTSTATUS";
        final Integer listSize = null;
        final String listAfter = null;
        final String listBefore = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.listFileStatus(listFilePath, listSize, listAfter, listBefore, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return listFileStatusDelegate(call.execute());
    }

    /**
     * Get the list of file status objects specified by the file path, with optional pagination parameters.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param listFilePath The Data Lake Store path (starting with '/') of the directory to list.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFileStatusAsync(String accountName, String listFilePath, final ServiceCallback<FileStatusesResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (listFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter listFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "MSLISTSTATUS";
        final Integer listSize = null;
        final String listAfter = null;
        final String listBefore = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.listFileStatus(listFilePath, listSize, listAfter, listBefore, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileStatusesResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(listFileStatusDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Get the list of file status objects specified by the file path, with optional pagination parameters.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param listFilePath The Data Lake Store path (starting with '/') of the directory to list.
     * @param listSize Gets or sets the number of items to return. Optional.
     * @param listAfter Gets or sets the item or lexographical index after which to begin returning results. For example, a file list of 'a','b','d' and listAfter='b' will return 'd', and a listAfter='c' will also return 'd'. Optional.
     * @param listBefore Gets or sets the item or lexographical index before which to begin returning results. For example, a file list of 'a','b','d' and listBefore='d' will return 'a','b', and a listBefore='c' will also return 'a','b'. Optional.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileStatusesResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileStatusesResultInner> listFileStatus(String accountName, String listFilePath, Integer listSize, String listAfter, String listBefore) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (listFilePath == null) {
            throw new IllegalArgumentException("Parameter listFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "MSLISTSTATUS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.listFileStatus(listFilePath, listSize, listAfter, listBefore, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return listFileStatusDelegate(call.execute());
    }

    /**
     * Get the list of file status objects specified by the file path, with optional pagination parameters.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param listFilePath The Data Lake Store path (starting with '/') of the directory to list.
     * @param listSize Gets or sets the number of items to return. Optional.
     * @param listAfter Gets or sets the item or lexographical index after which to begin returning results. For example, a file list of 'a','b','d' and listAfter='b' will return 'd', and a listAfter='c' will also return 'd'. Optional.
     * @param listBefore Gets or sets the item or lexographical index before which to begin returning results. For example, a file list of 'a','b','d' and listBefore='d' will return 'a','b', and a listBefore='c' will also return 'a','b'. Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFileStatusAsync(String accountName, String listFilePath, Integer listSize, String listAfter, String listBefore, final ServiceCallback<FileStatusesResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (listFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter listFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "MSLISTSTATUS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.listFileStatus(listFilePath, listSize, listAfter, listBefore, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileStatusesResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(listFileStatusDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileStatusesResultInner> listFileStatusDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileStatusesResultInner, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<FileStatusesResultInner>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Gets the file content summary object specified by the file path.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param getContentSummaryFilePath The Data Lake Store path (starting with '/') of the file for which to retrieve the summary.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the ContentSummaryResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<ContentSummaryResultInner> getContentSummary(String accountName, String getContentSummaryFilePath) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (getContentSummaryFilePath == null) {
            throw new IllegalArgumentException("Parameter getContentSummaryFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "GETCONTENTSUMMARY";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getContentSummary(getContentSummaryFilePath, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getContentSummaryDelegate(call.execute());
    }

    /**
     * Gets the file content summary object specified by the file path.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param getContentSummaryFilePath The Data Lake Store path (starting with '/') of the file for which to retrieve the summary.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getContentSummaryAsync(String accountName, String getContentSummaryFilePath, final ServiceCallback<ContentSummaryResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (getContentSummaryFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter getContentSummaryFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "GETCONTENTSUMMARY";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getContentSummary(getContentSummaryFilePath, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<ContentSummaryResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getContentSummaryDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<ContentSummaryResultInner> getContentSummaryDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<ContentSummaryResultInner, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<ContentSummaryResultInner>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Get the file status object specified by the file path.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param getFilePath The Data Lake Store path (starting with '/') of the file or directory for which to retrieve the status.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileStatusResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileStatusResultInner> getFileStatus(String accountName, String getFilePath) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (getFilePath == null) {
            throw new IllegalArgumentException("Parameter getFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "GETFILESTATUS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getFileStatus(getFilePath, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getFileStatusDelegate(call.execute());
    }

    /**
     * Get the file status object specified by the file path.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param getFilePath The Data Lake Store path (starting with '/') of the file or directory for which to retrieve the status.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getFileStatusAsync(String accountName, String getFilePath, final ServiceCallback<FileStatusResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (getFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter getFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "GETFILESTATUS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getFileStatus(getFilePath, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileStatusResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getFileStatusDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileStatusResultInner> getFileStatusDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileStatusResultInner, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<FileStatusResultInner>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Appends to the specified file. This method does not support multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option. Use the ConcurrentAppend option if you would like support for concurrent appends.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to which to append.
     * @param streamContents The file contents to include when appending to the file.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> append(String accountName, String directFilePath, byte[] streamContents) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (streamContents == null) {
            throw new IllegalArgumentException("Parameter streamContents is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "APPEND";
        final String append = "true";
        final String transferEncoding = "chunked";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.append(directFilePath, streamContentsConverted, op, append, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return appendDelegate(call.execute());
    }

    /**
     * Appends to the specified file. This method does not support multiple concurrent appends to the file. NOTE: Concurrent append and normal (serial) append CANNOT be used interchangeably. Once a file has been appended to using either append option, it can only be appended to using that append option. Use the ConcurrentAppend option if you would like support for concurrent appends.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to which to append.
     * @param streamContents The file contents to include when appending to the file.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall appendAsync(String accountName, String directFilePath, byte[] streamContents, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (streamContents == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter streamContents is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "APPEND";
        final String append = "true";
        final String transferEncoding = "chunked";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        Call<ResponseBody> call = service.append(directFilePath, streamContentsConverted, op, append, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(appendDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> appendDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Creates a file with optionally specified content.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> create(String accountName, String directFilePath) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "CREATE";
        final String write = "true";
        final String transferEncoding = "chunked";
        final RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[0]);
        final Boolean overwrite = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.create(directFilePath, streamContentsConverted, overwrite, op, write, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return createDelegate(call.execute());
    }

    /**
     * Creates a file with optionally specified content.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createAsync(String accountName, String directFilePath, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "CREATE";
        final String write = "true";
        final String transferEncoding = "chunked";
        final RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[0]);
        final Boolean overwrite = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.create(directFilePath, streamContentsConverted, overwrite, op, write, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Creates a file with optionally specified content.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param streamContents The file contents to include when creating the file. This parameter is optional, resulting in an empty file if not specified.
     * @param overwrite The indication of if the file should be overwritten.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> create(String accountName, String directFilePath, byte[] streamContents, Boolean overwrite) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "CREATE";
        final String write = "true";
        final String transferEncoding = "chunked";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[0]);
        if (streamContents != null) {
            streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        }
        Call<ResponseBody> call = service.create(directFilePath, streamContentsConverted, overwrite, op, write, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return createDelegate(call.execute());
    }

    /**
     * Creates a file with optionally specified content.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to create.
     * @param streamContents The file contents to include when creating the file. This parameter is optional, resulting in an empty file if not specified.
     * @param overwrite The indication of if the file should be overwritten.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createAsync(String accountName, String directFilePath, byte[] streamContents, Boolean overwrite, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "CREATE";
        final String write = "true";
        final String transferEncoding = "chunked";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        RequestBody streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), new byte[0]);
        if (streamContents != null) {
            streamContentsConverted = RequestBody.create(MediaType.parse("application/octet-stream"), streamContents);
        }
        Call<ResponseBody> call = service.create(directFilePath, streamContentsConverted, overwrite, op, write, transferEncoding, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> createDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Opens and reads from the specified file.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<InputStream> open(String accountName, String directFilePath) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "OPEN";
        final String read = "true";
        final Long length = null;
        final Long offset = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.open(directFilePath, length, offset, op, read, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return openDelegate(call.execute());
    }

    /**
     * Opens and reads from the specified file.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall openAsync(String accountName, String directFilePath, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "OPEN";
        final String read = "true";
        final Long length = null;
        final Long offset = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.open(directFilePath, length, offset, op, read, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(openDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Opens and reads from the specified file.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param length the Long value
     * @param offset the Long value
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the InputStream object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<InputStream> open(String accountName, String directFilePath, Long length, Long offset) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (directFilePath == null) {
            throw new IllegalArgumentException("Parameter directFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "OPEN";
        final String read = "true";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.open(directFilePath, length, offset, op, read, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return openDelegate(call.execute());
    }

    /**
     * Opens and reads from the specified file.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param directFilePath The Data Lake Store path (starting with '/') of the file to open.
     * @param length the Long value
     * @param offset the Long value
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall openAsync(String accountName, String directFilePath, Long length, Long offset, final ServiceCallback<InputStream> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (directFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter directFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "OPEN";
        final String read = "true";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.open(directFilePath, length, offset, op, read, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<InputStream>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(openDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<InputStream> openDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<InputStream, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<InputStream>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Sets the Access Control List (ACL) for a file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setAclFilePath The Data Lake Store path (starting with '/') of the file or directory on which to set the ACL.
     * @param aclspec The ACL spec included in ACL creation operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setAcl(String accountName, String setAclFilePath, String aclspec) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (setAclFilePath == null) {
            throw new IllegalArgumentException("Parameter setAclFilePath is required and cannot be null.");
        }
        if (aclspec == null) {
            throw new IllegalArgumentException("Parameter aclspec is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "SETACL";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setAcl(setAclFilePath, aclspec, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return setAclDelegate(call.execute());
    }

    /**
     * Sets the Access Control List (ACL) for a file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setAclFilePath The Data Lake Store path (starting with '/') of the file or directory on which to set the ACL.
     * @param aclspec The ACL spec included in ACL creation operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setAclAsync(String accountName, String setAclFilePath, String aclspec, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (setAclFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setAclFilePath is required and cannot be null."));
            return null;
        }
        if (aclspec == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter aclspec is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "SETACL";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setAcl(setAclFilePath, aclspec, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setAclDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> setAclDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Modifies existing Access Control List (ACL) entries on a file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param modifyAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being modified.
     * @param aclspec The ACL specification included in ACL modification operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> modifyAclEntries(String accountName, String modifyAclFilePath, String aclspec) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (modifyAclFilePath == null) {
            throw new IllegalArgumentException("Parameter modifyAclFilePath is required and cannot be null.");
        }
        if (aclspec == null) {
            throw new IllegalArgumentException("Parameter aclspec is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "MODIFYACLENTRIES";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.modifyAclEntries(modifyAclFilePath, aclspec, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return modifyAclEntriesDelegate(call.execute());
    }

    /**
     * Modifies existing Access Control List (ACL) entries on a file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param modifyAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being modified.
     * @param aclspec The ACL specification included in ACL modification operations in the format '[default:]user|group|other::r|-w|-x|-'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall modifyAclEntriesAsync(String accountName, String modifyAclFilePath, String aclspec, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (modifyAclFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter modifyAclFilePath is required and cannot be null."));
            return null;
        }
        if (aclspec == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter aclspec is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "MODIFYACLENTRIES";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.modifyAclEntries(modifyAclFilePath, aclspec, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(modifyAclEntriesDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> modifyAclEntriesDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Removes existing Access Control List (ACL) entries for a file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param removeAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param aclspec The ACL spec included in ACL removal operations in the format '[default:]user|group|other'
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> removeAclEntries(String accountName, String removeAclFilePath, String aclspec) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (removeAclFilePath == null) {
            throw new IllegalArgumentException("Parameter removeAclFilePath is required and cannot be null.");
        }
        if (aclspec == null) {
            throw new IllegalArgumentException("Parameter aclspec is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "REMOVEACLENTRIES";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.removeAclEntries(removeAclFilePath, aclspec, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return removeAclEntriesDelegate(call.execute());
    }

    /**
     * Removes existing Access Control List (ACL) entries for a file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param removeAclFilePath The Data Lake Store path (starting with '/') of the file or directory with the ACL being removed.
     * @param aclspec The ACL spec included in ACL removal operations in the format '[default:]user|group|other'
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall removeAclEntriesAsync(String accountName, String removeAclFilePath, String aclspec, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (removeAclFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter removeAclFilePath is required and cannot be null."));
            return null;
        }
        if (aclspec == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter aclspec is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "REMOVEACLENTRIES";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.removeAclEntries(removeAclFilePath, aclspec, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(removeAclEntriesDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> removeAclEntriesDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Gets Access Control List (ACL) entries for the specified file or directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory for which to get the ACL.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the AclStatusResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<AclStatusResultInner> getAclStatus(String accountName, String aclFilePath) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (aclFilePath == null) {
            throw new IllegalArgumentException("Parameter aclFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "GETACLSTATUS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getAclStatus(aclFilePath, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getAclStatusDelegate(call.execute());
    }

    /**
     * Gets Access Control List (ACL) entries for the specified file or directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param aclFilePath The Data Lake Store path (starting with '/') of the file or directory for which to get the ACL.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAclStatusAsync(String accountName, String aclFilePath, final ServiceCallback<AclStatusResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (aclFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter aclFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "GETACLSTATUS";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.getAclStatus(aclFilePath, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<AclStatusResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getAclStatusDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<AclStatusResultInner> getAclStatusDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<AclStatusResultInner, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<AclStatusResultInner>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileOperationResultInner> delete(String accountName, String filePath) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("Parameter filePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "DELETE";
        final Boolean recursive = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.delete(filePath, recursive, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String accountName, String filePath, final ServiceCallback<FileOperationResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (filePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter filePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "DELETE";
        final Boolean recursive = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.delete(filePath, recursive, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileOperationResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param recursive The optional switch indicating if the delete should be recursive
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileOperationResultInner> delete(String accountName, String filePath, Boolean recursive) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (filePath == null) {
            throw new IllegalArgumentException("Parameter filePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "DELETE";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.delete(filePath, recursive, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes the requested file or directory, optionally recursively.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param filePath The Data Lake Store path (starting with '/') of the file or directory to delete.
     * @param recursive The optional switch indicating if the delete should be recursive
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String accountName, String filePath, Boolean recursive, final ServiceCallback<FileOperationResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (filePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter filePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "DELETE";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.delete(filePath, recursive, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileOperationResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileOperationResultInner> deleteDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileOperationResultInner, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<FileOperationResultInner>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Rename a file or directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param renameFilePath The Data Lake Store path (starting with '/') of the file or directory to move/rename.
     * @param destination The path to move/rename the file or folder to
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FileOperationResultInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FileOperationResultInner> rename(String accountName, String renameFilePath, String destination) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (renameFilePath == null) {
            throw new IllegalArgumentException("Parameter renameFilePath is required and cannot be null.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Parameter destination is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "RENAME";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.rename(renameFilePath, destination, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return renameDelegate(call.execute());
    }

    /**
     * Rename a file or directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param renameFilePath The Data Lake Store path (starting with '/') of the file or directory to move/rename.
     * @param destination The path to move/rename the file or folder to
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall renameAsync(String accountName, String renameFilePath, String destination, final ServiceCallback<FileOperationResultInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (renameFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter renameFilePath is required and cannot be null."));
            return null;
        }
        if (destination == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter destination is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "RENAME";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.rename(renameFilePath, destination, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FileOperationResultInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(renameDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FileOperationResultInner> renameDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FileOperationResultInner, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<FileOperationResultInner>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Sets the owner of a file or directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setOwner(String accountName, String setOwnerFilePath) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (setOwnerFilePath == null) {
            throw new IllegalArgumentException("Parameter setOwnerFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "SETOWNER";
        final String owner = null;
        final String group = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setOwner(setOwnerFilePath, owner, group, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return setOwnerDelegate(call.execute());
    }

    /**
     * Sets the owner of a file or directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setOwnerAsync(String accountName, String setOwnerFilePath, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (setOwnerFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setOwnerFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "SETOWNER";
        final String owner = null;
        final String group = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setOwner(setOwnerFilePath, owner, group, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setOwnerDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Sets the owner of a file or directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param owner The AAD Object ID of the user owner of the file or directory. If empty, the property will remain unchanged.
     * @param group The AAD Object ID of the group owner of the file or directory. If empty, the property will remain unchanged.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setOwner(String accountName, String setOwnerFilePath, String owner, String group) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (setOwnerFilePath == null) {
            throw new IllegalArgumentException("Parameter setOwnerFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "SETOWNER";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setOwner(setOwnerFilePath, owner, group, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return setOwnerDelegate(call.execute());
    }

    /**
     * Sets the owner of a file or directory.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setOwnerFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the owner.
     * @param owner The AAD Object ID of the user owner of the file or directory. If empty, the property will remain unchanged.
     * @param group The AAD Object ID of the group owner of the file or directory. If empty, the property will remain unchanged.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setOwnerAsync(String accountName, String setOwnerFilePath, String owner, String group, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (setOwnerFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setOwnerFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "SETOWNER";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setOwner(setOwnerFilePath, owner, group, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setOwnerDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> setOwnerDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

    /**
     * Sets the permission of the file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setPermission(String accountName, String setPermissionFilePath) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (setPermissionFilePath == null) {
            throw new IllegalArgumentException("Parameter setPermissionFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "SETPERMISSION";
        final String permission = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setPermission(setPermissionFilePath, permission, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return setPermissionDelegate(call.execute());
    }

    /**
     * Sets the permission of the file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setPermissionAsync(String accountName, String setPermissionFilePath, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (setPermissionFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setPermissionFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "SETPERMISSION";
        final String permission = null;
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setPermission(setPermissionFilePath, permission, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setPermissionDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Sets the permission of the file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param permission A string representation of the permission (i.e 'rwx'). If empty, this property remains unchanged.
     * @throws AdlsErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> setPermission(String accountName, String setPermissionFilePath, String permission) throws AdlsErrorException, IOException, IllegalArgumentException {
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            throw new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null.");
        }
        if (setPermissionFilePath == null) {
            throw new IllegalArgumentException("Parameter setPermissionFilePath is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final String op = "SETPERMISSION";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setPermission(setPermissionFilePath, permission, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return setPermissionDelegate(call.execute());
    }

    /**
     * Sets the permission of the file or folder.
     *
     * @param accountName The Azure Data Lake Store account to execute filesystem operations on.
     * @param setPermissionFilePath The Data Lake Store path (starting with '/') of the file or directory for which to set the permission.
     * @param permission A string representation of the permission (i.e 'rwx'). If empty, this property remains unchanged.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall setPermissionAsync(String accountName, String setPermissionFilePath, String permission, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.adlsFileSystemDnsSuffix() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.adlsFileSystemDnsSuffix() is required and cannot be null."));
            return null;
        }
        if (setPermissionFilePath == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter setPermissionFilePath is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final String op = "SETPERMISSION";
        this.client.restClient().setBaseUrl("{accountName}", accountName, "{adlsFileSystemDnsSuffix}", this.client.adlsFileSystemDnsSuffix());
        Call<ResponseBody> call = service.setPermission(setPermissionFilePath, permission, op, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(setPermissionDelegate(response));
                } catch (AdlsErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> setPermissionDelegate(Response<ResponseBody> response) throws AdlsErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, AdlsErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .registerError(AdlsErrorException.class)
                .build(response);
    }

}
