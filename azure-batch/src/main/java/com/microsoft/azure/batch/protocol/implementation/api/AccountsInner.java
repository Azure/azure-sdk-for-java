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
import com.microsoft.rest.ServiceResponseCallback;
import com.microsoft.rest.ServiceResponseWithHeaders;
import com.microsoft.rest.Validator;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import org.joda.time.DateTime;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Url;
import retrofit2.Response;

/**
 * An instance of this class provides access to all the operations defined
 * in Accounts.
 */
public final class AccountsInner {
    /** The Retrofit service to perform REST calls. */
    private AccountsService service;
    /** The service client containing this operation class. */
    private BatchServiceClientImpl client;

    /**
     * Initializes an instance of AccountsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public AccountsInner(Retrofit retrofit, BatchServiceClientImpl client) {
        this.service = retrofit.create(AccountsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Accounts to be
     * used by Retrofit to perform actually REST calls.
     */
    interface AccountsService {
        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET("nodeagentskus")
        Call<ResponseBody> listNodeAgentSkus(@Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Query("$filter") String filter, @Query("maxresults") Integer maxResults, @Query("timeout") Integer timeout, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

        @Headers("Content-Type: application/json; odata=minimalmetadata; charset=utf-8")
        @GET
        Call<ResponseBody> listNodeAgentSkusNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("client-request-id") String clientRequestId, @Header("return-client-request-id") Boolean returnClientRequestId, @Header("ocp-date") DateTimeRfc1123 ocpDate);

    }

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSkuInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> listNodeAgentSkus() throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        final AccountListNodeAgentSkusOptionsInner accountListNodeAgentSkusOptions = null;
        String filter = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNodeAgentSkus(this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> response = listNodeAgentSkusDelegate(call.execute());
        PagedList<NodeAgentSkuInner> result = new PagedList<NodeAgentSkuInner>(response.getBody()) {
            @Override
            public Page<NodeAgentSkuInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                return listNodeAgentSkusNext(nextPageLink, null).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNodeAgentSkusAsync(final ListOperationCallback<NodeAgentSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        final AccountListNodeAgentSkusOptionsInner accountListNodeAgentSkusOptions = null;
        String filter = null;
        Integer maxResults = null;
        Integer timeout = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNodeAgentSkus(this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeAgentSkuInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> result = listNodeAgentSkusDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNodeAgentSkusNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
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
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param accountListNodeAgentSkusOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSkuInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> listNodeAgentSkus(final AccountListNodeAgentSkusOptionsInner accountListNodeAgentSkusOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(accountListNodeAgentSkusOptions);
        String filter = null;
        if (accountListNodeAgentSkusOptions != null) {
            filter = accountListNodeAgentSkusOptions.filter();
        }
        Integer maxResults = null;
        if (accountListNodeAgentSkusOptions != null) {
            maxResults = accountListNodeAgentSkusOptions.maxResults();
        }
        Integer timeout = null;
        if (accountListNodeAgentSkusOptions != null) {
            timeout = accountListNodeAgentSkusOptions.timeout();
        }
        String clientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            clientRequestId = accountListNodeAgentSkusOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusOptions != null) {
            ocpDate = accountListNodeAgentSkusOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkus(this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> response = listNodeAgentSkusDelegate(call.execute());
        PagedList<NodeAgentSkuInner> result = new PagedList<NodeAgentSkuInner>(response.getBody()) {
            @Override
            public Page<NodeAgentSkuInner> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                AccountListNodeAgentSkusNextOptionsInner accountListNodeAgentSkusNextOptions = null;
                if (accountListNodeAgentSkusOptions != null) {
                    accountListNodeAgentSkusNextOptions = new AccountListNodeAgentSkusNextOptionsInner();
                    accountListNodeAgentSkusNextOptions.setClientRequestId(accountListNodeAgentSkusOptions.clientRequestId());
                    accountListNodeAgentSkusNextOptions.setReturnClientRequestId(accountListNodeAgentSkusOptions.returnClientRequestId());
                    accountListNodeAgentSkusNextOptions.setOcpDate(accountListNodeAgentSkusOptions.ocpDate());
                }
                return listNodeAgentSkusNext(nextPageLink, accountListNodeAgentSkusNextOptions).getBody();
            }
        };
        return new ServiceResponseWithHeaders<>(result, response.getHeaders(), response.getResponse());
    }

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param accountListNodeAgentSkusOptions Additional parameters for the operation
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNodeAgentSkusAsync(final AccountListNodeAgentSkusOptionsInner accountListNodeAgentSkusOptions, final ListOperationCallback<NodeAgentSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(accountListNodeAgentSkusOptions, serviceCallback);
        String filter = null;
        if (accountListNodeAgentSkusOptions != null) {
            filter = accountListNodeAgentSkusOptions.filter();
        }
        Integer maxResults = null;
        if (accountListNodeAgentSkusOptions != null) {
            maxResults = accountListNodeAgentSkusOptions.maxResults();
        }
        Integer timeout = null;
        if (accountListNodeAgentSkusOptions != null) {
            timeout = accountListNodeAgentSkusOptions.timeout();
        }
        String clientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            clientRequestId = accountListNodeAgentSkusOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusOptions != null) {
            ocpDate = accountListNodeAgentSkusOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkus(this.client.apiVersion(), this.client.acceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeAgentSkuInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> result = listNodeAgentSkusDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        AccountListNodeAgentSkusNextOptionsInner accountListNodeAgentSkusNextOptions = null;
                        if (accountListNodeAgentSkusOptions != null) {
                            accountListNodeAgentSkusNextOptions = new AccountListNodeAgentSkusNextOptionsInner();
                            accountListNodeAgentSkusNextOptions.setClientRequestId(accountListNodeAgentSkusOptions.clientRequestId());
                            accountListNodeAgentSkusNextOptions.setReturnClientRequestId(accountListNodeAgentSkusOptions.returnClientRequestId());
                            accountListNodeAgentSkusNextOptions.setOcpDate(accountListNodeAgentSkusOptions.ocpDate());
                        }
                        listNodeAgentSkusNextAsync(result.getBody().getNextPageLink(), accountListNodeAgentSkusNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> listNodeAgentSkusDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<NodeAgentSkuInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<NodeAgentSkuInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, AccountListNodeAgentSkusHeadersInner.class);
    }

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSkuInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> listNodeAgentSkusNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final AccountListNodeAgentSkusNextOptionsInner accountListNodeAgentSkusNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNodeAgentSkusNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNodeAgentSkusNextDelegate(call.execute());
    }

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNodeAgentSkusNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<NodeAgentSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final AccountListNodeAgentSkusNextOptionsInner accountListNodeAgentSkusNextOptions = null;
        String clientRequestId = null;
        Boolean returnClientRequestId = null;
        DateTimeRfc1123 ocpDateConverted = null;
        Call<ResponseBody> call = service.listNodeAgentSkusNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeAgentSkuInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> result = listNodeAgentSkusNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNodeAgentSkusNextAsync(result.getBody().getNextPageLink(), null, serviceCall, serviceCallback);
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
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param accountListNodeAgentSkusNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSkuInner&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> listNodeAgentSkusNext(final String nextPageLink, final AccountListNodeAgentSkusNextOptionsInner accountListNodeAgentSkusNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(accountListNodeAgentSkusNextOptions);
        String clientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            clientRequestId = accountListNodeAgentSkusNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            ocpDate = accountListNodeAgentSkusNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkusNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        return listNodeAgentSkusNextDelegate(call.execute());
    }

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param accountListNodeAgentSkusNextOptions Additional parameters for the operation
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNodeAgentSkusNextAsync(final String nextPageLink, final AccountListNodeAgentSkusNextOptionsInner accountListNodeAgentSkusNextOptions, final ServiceCall serviceCall, final ListOperationCallback<NodeAgentSkuInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Validator.validate(accountListNodeAgentSkusNextOptions, serviceCallback);
        String clientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            clientRequestId = accountListNodeAgentSkusNextOptions.clientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusNextOptions.returnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            ocpDate = accountListNodeAgentSkusNextOptions.ocpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkusNext(nextPageLink, this.client.acceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeAgentSkuInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> result = listNodeAgentSkusNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNodeAgentSkusNextAsync(result.getBody().getNextPageLink(), accountListNodeAgentSkusNextOptions, serviceCall, serviceCallback);
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

    private ServiceResponseWithHeaders<PageImpl<NodeAgentSkuInner>, AccountListNodeAgentSkusHeadersInner> listNodeAgentSkusNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<NodeAgentSkuInner>, BatchErrorException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<NodeAgentSkuInner>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, AccountListNodeAgentSkusHeadersInner.class);
    }

}
