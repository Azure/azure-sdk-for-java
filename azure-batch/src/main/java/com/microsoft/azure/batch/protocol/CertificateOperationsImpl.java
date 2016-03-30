/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.Certificate;
import com.microsoft.azure.batch.protocol.models.CertificateAddHeaders;
import com.microsoft.azure.batch.protocol.models.CertificateAddOptions;
import com.microsoft.azure.batch.protocol.models.CertificateAddParameter;
import com.microsoft.azure.batch.protocol.models.CertificateCancelDeletionHeaders;
import com.microsoft.azure.batch.protocol.models.CertificateCancelDeletionOptions;
import com.microsoft.azure.batch.protocol.models.CertificateDeleteHeaders;
import com.microsoft.azure.batch.protocol.models.CertificateDeleteOptions;
import com.microsoft.azure.batch.protocol.models.CertificateGetHeaders;
import com.microsoft.azure.batch.protocol.models.CertificateGetOptions;
import com.microsoft.azure.batch.protocol.models.CertificateListHeaders;
import com.microsoft.azure.batch.protocol.models.CertificateListNextOptions;
import com.microsoft.azure.batch.protocol.models.CertificateListOptions;
import com.microsoft.azure.batch.protocol.models.PageImpl;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.DateTimeRfc1123;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import org.joda.time.DateTime;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * An instance of this class provides access to all the operations defined
 * in CertificateOperations.
 */
public final class CertificateOperationsImpl implements CertificateOperations {
    /** The Retrofit service to perform REST calls. */
    private CertificateService service;
    /** The service client containing this operation class. */
    private BatchServiceClient client;

    /**
     * Initializes an instance of CertificateOperations.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public CertificateOperationsImpl(Retrofit retrofit, BatchServiceClient client) {
        this.service = retrofit.create(CertificateService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for CertificateOperations to be
     * used by Retrofit to perform actually REST calls.
     */
    interface CertificateService {
        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("certificates")
        Call<ResponseBody> add(@Body CertificateAddParameter certificate, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("certificates")
        Call<ResponseBody> list(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("$select") String select, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @POST("certificates(thumbprintAlgorithm={thumbprintAlgorithm},thumbprint={thumbprint})/canceldelete")
        Call<ResponseBody> cancelDeletion(@Path("thumbprintAlgorithm") String thumbprintAlgorithm, @Path("thumbprint") String thumbprint, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @HTTP(path = "certificates(thumbprintAlgorithm={thumbprintAlgorithm},thumbprint={thumbprint})", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("thumbprintAlgorithm") String thumbprintAlgorithm, @Path("thumbprint") String thumbprint, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("certificates(thumbprintAlgorithm={thumbprintAlgorithm},thumbprint={thumbprint})")
        Call<ResponseBody> get(@Path("thumbprintAlgorithm") String thumbprintAlgorithm, @Path("thumbprint") String thumbprint, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$select") String select, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

    }

    /**
     * Adds a certificate to the specified account.
     *
     * @param certificate Specifies the certificate to be added.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, CertificateAddHeaders> add(CertificateAddParameter certificate) throws BatchErrorException, IOException, IllegalArgumentException {
        if (certificate == null) {
            throw new IllegalArgumentException("Parameter certificate is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(certificate);
        final CertificateAddOptions certificateAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(certificate, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addDelegate(call.execute());
    }

    /**
     * Adds a certificate to the specified account.
     *
     * @param certificate Specifies the certificate to be added.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(CertificateAddParameter certificate, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (certificate == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter certificate is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(certificate, serviceCallback);
        final CertificateAddOptions certificateAddOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.add(certificate, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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
     * Adds a certificate to the specified account.
     *
     * @param certificate Specifies the certificate to be added.
     * @param certificateAddOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, CertificateAddHeaders> add(CertificateAddParameter certificate, CertificateAddOptions certificateAddOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (certificate == null) {
            throw new IllegalArgumentException("Parameter certificate is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(certificate);
        Validator.validate(certificateAddOptions);
        Integer timeout = null;
        if (certificateAddOptions != null) {
            timeout = certificateAddOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateAddOptions != null) {
            clientRequestId = certificateAddOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateAddOptions != null) {
            returnClientRequestId = certificateAddOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateAddOptions != null) {
            ocpDate = certificateAddOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.add(certificate, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return addDelegate(call.execute());
    }

    /**
     * Adds a certificate to the specified account.
     *
     * @param certificate Specifies the certificate to be added.
     * @param certificateAddOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall addAsync(CertificateAddParameter certificate, CertificateAddOptions certificateAddOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (certificate == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter certificate is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(certificate, serviceCallback);
        Validator.validate(certificateAddOptions, serviceCallback);
        Integer timeout = null;
        if (certificateAddOptions != null) {
            timeout = certificateAddOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateAddOptions != null) {
            clientRequestId = certificateAddOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateAddOptions != null) {
            returnClientRequestId = certificateAddOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateAddOptions != null) {
            ocpDate = certificateAddOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.add(certificate, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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

    private ServiceResponseWithHeaders<Void, CertificateAddHeaders> addDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.getMapperAdapter())
                .register(201, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, CertificateAddHeaders.class);
    }

    /**
     * Lists all of the certificates that have been added to the specified account.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;Certificate&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<Certificate>, CertificateListHeaders> list() throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final CertificateListOptions certificateListOptions = null;
        String filter = null;
        String select = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> response = listDelegate(call.execute());
        PagedList<Certificate> result = new PagedList<Certificate>(response.getBody()) {
            @Override
            public Page<Certificate> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the certificates that have been added to the specified account.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final ListOperationCallback<Certificate> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final CertificateListOptions certificateListOptions = null;
        String filter = null;
        String select = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.list(this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<Certificate>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> result = listDelegate(response);
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
     * Lists all of the certificates that have been added to the specified account.
     *
     * @param certificateListOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;Certificate&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<Certificate>, CertificateListHeaders> list(final CertificateListOptions certificateListOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(certificateListOptions);
        String filter = null;
        if (certificateListOptions != null) {
            filter = certificateListOptions.getFilter();
        }
        String select = null;
        if (certificateListOptions != null) {
            select = certificateListOptions.getSelect();
        }
        Integer maxResults = null;
        if (certificateListOptions != null) {
            maxResults = certificateListOptions.getMaxResults();
        }
        Integer timeout = null;
        if (certificateListOptions != null) {
            timeout = certificateListOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateListOptions != null) {
            clientRequestId = certificateListOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateListOptions != null) {
            returnClientRequestId = certificateListOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateListOptions != null) {
            ocpDate = certificateListOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.list(this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> response = listDelegate(call.execute());
        PagedList<Certificate> result = new PagedList<Certificate>(response.getBody()) {
            @Override
            public Page<Certificate> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                CertificateListNextOptions certificateListNextOptions = null;
                if (certificateListOptions != null) {
                    certificateListNextOptions = new CertificateListNextOptions();
                    certificateListNextOptions.setClientRequestId(certificateListOptions.getClientRequestId());
                    certificateListNextOptions.setReturnClientRequestId(certificateListOptions.getReturnClientRequestId());
                    certificateListNextOptions.setOcpDate(certificateListOptions.getOcpDate());
                }
                return listNext(nextPageLink, certificateListNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all of the certificates that have been added to the specified account.
     *
     * @param certificateListOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final CertificateListOptions certificateListOptions, final ListOperationCallback<Certificate> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(certificateListOptions, serviceCallback);
        String filter = null;
        if (certificateListOptions != null) {
            filter = certificateListOptions.getFilter();
        }
        String select = null;
        if (certificateListOptions != null) {
            select = certificateListOptions.getSelect();
        }
        Integer maxResults = null;
        if (certificateListOptions != null) {
            maxResults = certificateListOptions.getMaxResults();
        }
        Integer timeout = null;
        if (certificateListOptions != null) {
            timeout = certificateListOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateListOptions != null) {
            clientRequestId = certificateListOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateListOptions != null) {
            returnClientRequestId = certificateListOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateListOptions != null) {
            ocpDate = certificateListOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.list(this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, select, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<Certificate>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        CertificateListNextOptions certificateListNextOptions = null;
                        if (certificateListOptions != null) {
                            certificateListNextOptions = new CertificateListNextOptions();
                            certificateListNextOptions.setClientRequestId(certificateListOptions.getClientRequestId());
                            certificateListNextOptions.setReturnClientRequestId(certificateListOptions.getReturnClientRequestId());
                            certificateListNextOptions.setOcpDate(certificateListOptions.getOcpDate());
                        }
                        listNextAsync(result.getBody().getNextPageLink(), certificateListNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> listDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<Certificate>, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<Certificate>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, CertificateListHeaders.class);
    }

    /**
     * Cancels a failed deletion of a certificate from the specified account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate being deleted.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, CertificateCancelDeletionHeaders> cancelDeletion(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException, IllegalArgumentException {
        if (thumbprintAlgorithm == null) {
            throw new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null.");
        }
        if (thumbprint == null) {
            throw new IllegalArgumentException("Parameter thumbprint is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final CertificateCancelDeletionOptions certificateCancelDeletionOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.cancelDeletion(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return cancelDeletionDelegate(call.execute());
    }

    /**
     * Cancels a failed deletion of a certificate from the specified account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate being deleted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall cancelDeletionAsync(String thumbprintAlgorithm, String thumbprint, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (thumbprintAlgorithm == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null."));
            return null;
        }
        if (thumbprint == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprint is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final CertificateCancelDeletionOptions certificateCancelDeletionOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.cancelDeletion(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(cancelDeletionDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    /**
     * Cancels a failed deletion of a certificate from the specified account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate being deleted.
     * @param certificateCancelDeletionOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, CertificateCancelDeletionHeaders> cancelDeletion(String thumbprintAlgorithm, String thumbprint, CertificateCancelDeletionOptions certificateCancelDeletionOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (thumbprintAlgorithm == null) {
            throw new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null.");
        }
        if (thumbprint == null) {
            throw new IllegalArgumentException("Parameter thumbprint is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(certificateCancelDeletionOptions);
        Integer timeout = null;
        if (certificateCancelDeletionOptions != null) {
            timeout = certificateCancelDeletionOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateCancelDeletionOptions != null) {
            clientRequestId = certificateCancelDeletionOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateCancelDeletionOptions != null) {
            returnClientRequestId = certificateCancelDeletionOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateCancelDeletionOptions != null) {
            ocpDate = certificateCancelDeletionOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.cancelDeletion(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return cancelDeletionDelegate(call.execute());
    }

    /**
     * Cancels a failed deletion of a certificate from the specified account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate being deleted.
     * @param certificateCancelDeletionOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall cancelDeletionAsync(String thumbprintAlgorithm, String thumbprint, CertificateCancelDeletionOptions certificateCancelDeletionOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (thumbprintAlgorithm == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null."));
            return null;
        }
        if (thumbprint == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprint is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(certificateCancelDeletionOptions, serviceCallback);
        Integer timeout = null;
        if (certificateCancelDeletionOptions != null) {
            timeout = certificateCancelDeletionOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateCancelDeletionOptions != null) {
            clientRequestId = certificateCancelDeletionOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateCancelDeletionOptions != null) {
            returnClientRequestId = certificateCancelDeletionOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateCancelDeletionOptions != null) {
            ocpDate = certificateCancelDeletionOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.cancelDeletion(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(cancelDeletionDelegate(response));
                } catch (BatchErrorException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponseWithHeaders<Void, CertificateCancelDeletionHeaders> cancelDeletionDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.getMapperAdapter())
                .register(204, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, CertificateCancelDeletionHeaders.class);
    }

    /**
     * Deletes a certificate from the specified account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to be deleted.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, CertificateDeleteHeaders> delete(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException, IllegalArgumentException {
        if (thumbprintAlgorithm == null) {
            throw new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null.");
        }
        if (thumbprint == null) {
            throw new IllegalArgumentException("Parameter thumbprint is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final CertificateDeleteOptions certificateDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.delete(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a certificate from the specified account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to be deleted.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String thumbprintAlgorithm, String thumbprint, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (thumbprintAlgorithm == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null."));
            return null;
        }
        if (thumbprint == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprint is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final CertificateDeleteOptions certificateDeleteOptions = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.delete(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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
     * Deletes a certificate from the specified account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to be deleted.
     * @param certificateDeleteOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponseWithHeaders} object if successful.
     */
    public ServiceResponseWithHeaders<Void, CertificateDeleteHeaders> delete(String thumbprintAlgorithm, String thumbprint, CertificateDeleteOptions certificateDeleteOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (thumbprintAlgorithm == null) {
            throw new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null.");
        }
        if (thumbprint == null) {
            throw new IllegalArgumentException("Parameter thumbprint is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(certificateDeleteOptions);
        Integer timeout = null;
        if (certificateDeleteOptions != null) {
            timeout = certificateDeleteOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateDeleteOptions != null) {
            clientRequestId = certificateDeleteOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateDeleteOptions != null) {
            returnClientRequestId = certificateDeleteOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateDeleteOptions != null) {
            ocpDate = certificateDeleteOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.delete(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return deleteDelegate(call.execute());
    }

    /**
     * Deletes a certificate from the specified account.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to be deleted.
     * @param certificateDeleteOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteAsync(String thumbprintAlgorithm, String thumbprint, CertificateDeleteOptions certificateDeleteOptions, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (thumbprintAlgorithm == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null."));
            return null;
        }
        if (thumbprint == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprint is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(certificateDeleteOptions, serviceCallback);
        Integer timeout = null;
        if (certificateDeleteOptions != null) {
            timeout = certificateDeleteOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateDeleteOptions != null) {
            clientRequestId = certificateDeleteOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateDeleteOptions != null) {
            returnClientRequestId = certificateDeleteOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateDeleteOptions != null) {
            ocpDate = certificateDeleteOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.delete(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
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

    private ServiceResponseWithHeaders<Void, CertificateDeleteHeaders> deleteDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, BatchErrorException>(this.client.getMapperAdapter())
                .register(202, new TypeToken<Void>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, CertificateDeleteHeaders.class);
    }

    /**
     * Gets information about the specified certificate.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to get.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Certificate object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<Certificate, CertificateGetHeaders> get(String thumbprintAlgorithm, String thumbprint) throws BatchErrorException, IOException, IllegalArgumentException {
        if (thumbprintAlgorithm == null) {
            throw new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null.");
        }
        if (thumbprint == null) {
            throw new IllegalArgumentException("Parameter thumbprint is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final CertificateGetOptions certificateGetOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.get(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified certificate.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to get.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String thumbprintAlgorithm, String thumbprint, final ServiceCallback<Certificate> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (thumbprintAlgorithm == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null."));
            return null;
        }
        if (thumbprint == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprint is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final CertificateGetOptions certificateGetOptions = null;
        String select = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.get(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Certificate>(serviceCallback) {
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
     * Gets information about the specified certificate.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to get.
     * @param certificateGetOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the Certificate object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<Certificate, CertificateGetHeaders> get(String thumbprintAlgorithm, String thumbprint, CertificateGetOptions certificateGetOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (thumbprintAlgorithm == null) {
            throw new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null.");
        }
        if (thumbprint == null) {
            throw new IllegalArgumentException("Parameter thumbprint is required and cannot be null.");
        }
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(certificateGetOptions);
        String select = null;
        if (certificateGetOptions != null) {
            select = certificateGetOptions.getSelect();
        }
        Integer timeout = null;
        if (certificateGetOptions != null) {
            timeout = certificateGetOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateGetOptions != null) {
            clientRequestId = certificateGetOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateGetOptions != null) {
            returnClientRequestId = certificateGetOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateGetOptions != null) {
            ocpDate = certificateGetOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.get(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        return getDelegate(call.execute());
    }

    /**
     * Gets information about the specified certificate.
     *
     * @param thumbprintAlgorithm The algorithm used to derive the thumbprint parameter. This must be sha1.
     * @param thumbprint The thumbprint of the certificate to get.
     * @param certificateGetOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String thumbprintAlgorithm, String thumbprint, CertificateGetOptions certificateGetOptions, final ServiceCallback<Certificate> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (thumbprintAlgorithm == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprintAlgorithm is required and cannot be null."));
            return null;
        }
        if (thumbprint == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter thumbprint is required and cannot be null."));
            return null;
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(certificateGetOptions, serviceCallback);
        String select = null;
        if (certificateGetOptions != null) {
            select = certificateGetOptions.getSelect();
        }
        Integer timeout = null;
        if (certificateGetOptions != null) {
            timeout = certificateGetOptions.getTimeout();
        }
        String clientRequestId = null;
        if (certificateGetOptions != null) {
            clientRequestId = certificateGetOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateGetOptions != null) {
            returnClientRequestId = certificateGetOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateGetOptions != null) {
            ocpDate = certificateGetOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.get(thumbprintAlgorithm, thumbprint, this.client.getApiVersion(), this.client.getAcceptLanguage(), select, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Certificate>(serviceCallback) {
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

    private ServiceResponseWithHeaders<Certificate, CertificateGetHeaders> getDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Certificate, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<Certificate>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, CertificateGetHeaders.class);
    }

    /**
     * Lists all of the certificates that have been added to the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;Certificate&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> listNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final CertificateListNextOptions certificateListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the certificates that have been added to the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<Certificate> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final CertificateListNextOptions certificateListNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<Certificate>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> result = listNextDelegate(response);
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
     * Lists all of the certificates that have been added to the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param certificateListNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;Certificate&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> listNext(final String nextPageLink, final CertificateListNextOptions certificateListNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(certificateListNextOptions);
        String clientRequestId = null;
        if (certificateListNextOptions != null) {
            clientRequestId = certificateListNextOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateListNextOptions != null) {
            returnClientRequestId = certificateListNextOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateListNextOptions != null) {
            ocpDate = certificateListNextOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNextDelegate(call.execute());
    }

    /**
     * Lists all of the certificates that have been added to the specified account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param certificateListNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final CertificateListNextOptions certificateListNextOptions, final ServiceCall serviceCall, final ListOperationCallback<Certificate> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(certificateListNextOptions, serviceCallback);
        String clientRequestId = null;
        if (certificateListNextOptions != null) {
            clientRequestId = certificateListNextOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (certificateListNextOptions != null) {
            returnClientRequestId = certificateListNextOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (certificateListNextOptions != null) {
            ocpDate = certificateListNextOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = new DateTimeRfc1123(ocpDate);
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<Certificate>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), certificateListNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<Certificate>, CertificateListHeaders> listNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<Certificate>, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<Certificate>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, CertificateListHeaders.class);
    }

}
