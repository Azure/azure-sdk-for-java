/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol;

import com.google.common.reflect.TypeToken;
import com.microsoft.azure.AzureServiceResponseBuilder;
import com.microsoft.azure.batch.protocol.models.AccountListNodeAgentSkusHeaders;
import com.microsoft.azure.batch.protocol.models.AccountListNodeAgentSkusNextOptions;
import com.microsoft.azure.batch.protocol.models.AccountListNodeAgentSkusOptions;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.azure.batch.protocol.models.NodeAgentSku;
import com.microsoft.azure.batch.protocol.models.PageImpl;
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
import retrofit2.Retrofit;

/**
 * An instance of this class provides access to all the operations defined
 * in AccountOperations.
 */
public final class AccountOperationsImpl implements AccountOperations {
    /** The Retrofit service to perform REST calls. */
    private AccountService service;
    /** The service client containing this operation class. */
    private BatchServiceClient client;

    /**
     * Initializes an instance of AccountOperations.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public AccountOperationsImpl(Retrofit retrofit, BatchServiceClient client) {
        this.service = retrofit.create(AccountService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for AccountOperations to be
     * used by Retrofit to perform actually REST calls.
     */
    interface AccountService {
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
     * @return the List&lt;NodeAgentSku&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkus() throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        final AccountListNodeAgentSkusOptions accountListNodeAgentSkusOptions = null;
        String filter = null;
        if (accountListNodeAgentSkusOptions != null) {
            filter = accountListNodeAgentSkusOptions.getFilter();
        }
        Integer maxResults = null;
        if (accountListNodeAgentSkusOptions != null) {
            maxResults = accountListNodeAgentSkusOptions.getMaxResults();
        }
        Integer timeout = null;
        if (accountListNodeAgentSkusOptions != null) {
            timeout = accountListNodeAgentSkusOptions.getTimeout();
        }
        String clientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            clientRequestId = accountListNodeAgentSkusOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusOptions != null) {
            ocpDate = accountListNodeAgentSkusOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkus(this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> response = listNodeAgentSkusDelegate(call.execute());
        PagedList<NodeAgentSku> result = new PagedList<NodeAgentSku>(response.getBody()) {
            @Override
            public Page<NodeAgentSku> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions = null;
                if (accountListNodeAgentSkusOptions != null) {
                    accountListNodeAgentSkusNextOptions = new AccountListNodeAgentSkusNextOptions();
                    accountListNodeAgentSkusNextOptions.setClientRequestId(accountListNodeAgentSkusOptions.getClientRequestId());
                    accountListNodeAgentSkusNextOptions.setReturnClientRequestId(accountListNodeAgentSkusOptions.getReturnClientRequestId());
                    accountListNodeAgentSkusNextOptions.setOcpDate(accountListNodeAgentSkusOptions.getOcpDate());
                }
                return listNodeAgentSkusNext(nextPageLink, accountListNodeAgentSkusNextOptions).getBody();
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
    public ServiceCall listNodeAgentSkusAsync(final ListOperationCallback<NodeAgentSku> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        final AccountListNodeAgentSkusOptions accountListNodeAgentSkusOptions = null;
        String filter = null;
        if (accountListNodeAgentSkusOptions != null) {
            filter = accountListNodeAgentSkusOptions.getFilter();
        }
        Integer maxResults = null;
        if (accountListNodeAgentSkusOptions != null) {
            maxResults = accountListNodeAgentSkusOptions.getMaxResults();
        }
        Integer timeout = null;
        if (accountListNodeAgentSkusOptions != null) {
            timeout = accountListNodeAgentSkusOptions.getTimeout();
        }
        String clientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            clientRequestId = accountListNodeAgentSkusOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusOptions != null) {
            ocpDate = accountListNodeAgentSkusOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkus(this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeAgentSku>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> result = listNodeAgentSkusDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions = null;
                        if (accountListNodeAgentSkusOptions != null) {
                            accountListNodeAgentSkusNextOptions = new AccountListNodeAgentSkusNextOptions();
                            accountListNodeAgentSkusNextOptions.setClientRequestId(accountListNodeAgentSkusOptions.getClientRequestId());
                            accountListNodeAgentSkusNextOptions.setReturnClientRequestId(accountListNodeAgentSkusOptions.getReturnClientRequestId());
                            accountListNodeAgentSkusNextOptions.setOcpDate(accountListNodeAgentSkusOptions.getOcpDate());
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

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param accountListNodeAgentSkusOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSku&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PagedList<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkus(final AccountListNodeAgentSkusOptions accountListNodeAgentSkusOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (this.client.getApiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null.");
        }
        Validator.validate(accountListNodeAgentSkusOptions);
        String filter = null;
        if (accountListNodeAgentSkusOptions != null) {
            filter = accountListNodeAgentSkusOptions.getFilter();
        }
        Integer maxResults = null;
        if (accountListNodeAgentSkusOptions != null) {
            maxResults = accountListNodeAgentSkusOptions.getMaxResults();
        }
        Integer timeout = null;
        if (accountListNodeAgentSkusOptions != null) {
            timeout = accountListNodeAgentSkusOptions.getTimeout();
        }
        String clientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            clientRequestId = accountListNodeAgentSkusOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusOptions != null) {
            ocpDate = accountListNodeAgentSkusOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkus(this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> response = listNodeAgentSkusDelegate(call.execute());
        PagedList<NodeAgentSku> result = new PagedList<NodeAgentSku>(response.getBody()) {
            @Override
            public Page<NodeAgentSku> nextPage(String nextPageLink) throws BatchErrorException, IOException {
                AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions = null;
                if (accountListNodeAgentSkusOptions != null) {
                    accountListNodeAgentSkusNextOptions = new AccountListNodeAgentSkusNextOptions();
                    accountListNodeAgentSkusNextOptions.setClientRequestId(accountListNodeAgentSkusOptions.getClientRequestId());
                    accountListNodeAgentSkusNextOptions.setReturnClientRequestId(accountListNodeAgentSkusOptions.getReturnClientRequestId());
                    accountListNodeAgentSkusNextOptions.setOcpDate(accountListNodeAgentSkusOptions.getOcpDate());
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
    public ServiceCall listNodeAgentSkusAsync(final AccountListNodeAgentSkusOptions accountListNodeAgentSkusOptions, final ListOperationCallback<NodeAgentSku> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.getApiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.getApiVersion() is required and cannot be null."));
            return null;
        }
        Validator.validate(accountListNodeAgentSkusOptions, serviceCallback);
        String filter = null;
        if (accountListNodeAgentSkusOptions != null) {
            filter = accountListNodeAgentSkusOptions.getFilter();
        }
        Integer maxResults = null;
        if (accountListNodeAgentSkusOptions != null) {
            maxResults = accountListNodeAgentSkusOptions.getMaxResults();
        }
        Integer timeout = null;
        if (accountListNodeAgentSkusOptions != null) {
            timeout = accountListNodeAgentSkusOptions.getTimeout();
        }
        String clientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            clientRequestId = accountListNodeAgentSkusOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusOptions != null) {
            ocpDate = accountListNodeAgentSkusOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkus(this.client.getApiVersion(), this.client.getAcceptLanguage(), filter, maxResults, timeout, clientRequestId, returnClientRequestId, ocpDateConverted);
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeAgentSku>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> result = listNodeAgentSkusDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions = null;
                        if (accountListNodeAgentSkusOptions != null) {
                            accountListNodeAgentSkusNextOptions = new AccountListNodeAgentSkusNextOptions();
                            accountListNodeAgentSkusNextOptions.setClientRequestId(accountListNodeAgentSkusOptions.getClientRequestId());
                            accountListNodeAgentSkusNextOptions.setReturnClientRequestId(accountListNodeAgentSkusOptions.getReturnClientRequestId());
                            accountListNodeAgentSkusNextOptions.setOcpDate(accountListNodeAgentSkusOptions.getOcpDate());
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

    private ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkusDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<NodeAgentSku>, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<NodeAgentSku>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, AccountListNodeAgentSkusHeaders.class);
    }

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSku&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkusNext(final String nextPageLink) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        final AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions = null;
        String clientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            clientRequestId = accountListNodeAgentSkusNextOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusNextOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            ocpDate = accountListNodeAgentSkusNextOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkusNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
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
    public ServiceCall listNodeAgentSkusNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<NodeAgentSku> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        final AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions = null;
        String clientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            clientRequestId = accountListNodeAgentSkusNextOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusNextOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            ocpDate = accountListNodeAgentSkusNextOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkusNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeAgentSku>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> result = listNodeAgentSkusNextDelegate(response);
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

    /**
     * Lists all the node agent SKUs supported by Azure Batch Service.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param accountListNodeAgentSkusNextOptions Additional parameters for the operation
     * @throws BatchErrorException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;NodeAgentSku&gt; object wrapped in {@link ServiceResponseWithHeaders} if successful.
     */
    public ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkusNext(final String nextPageLink, final AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions) throws BatchErrorException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Validator.validate(accountListNodeAgentSkusNextOptions);
        String clientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            clientRequestId = accountListNodeAgentSkusNextOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusNextOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            ocpDate = accountListNodeAgentSkusNextOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkusNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
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
    public ServiceCall listNodeAgentSkusNextAsync(final String nextPageLink, final AccountListNodeAgentSkusNextOptions accountListNodeAgentSkusNextOptions, final ServiceCall serviceCall, final ListOperationCallback<NodeAgentSku> serviceCallback) throws IllegalArgumentException {
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
            clientRequestId = accountListNodeAgentSkusNextOptions.getClientRequestId();
        }
        Boolean returnClientRequestId = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            returnClientRequestId = accountListNodeAgentSkusNextOptions.getReturnClientRequestId();
        }
        DateTime ocpDate = null;
        if (accountListNodeAgentSkusNextOptions != null) {
            ocpDate = accountListNodeAgentSkusNextOptions.getOcpDate();
        }
        DateTimeRfc1123 ocpDateConverted = null;
        if (ocpDate != null) {
            ocpDateConverted = new DateTimeRfc1123(ocpDate);
        }
        Call<ResponseBody> call = service.listNodeAgentSkusNext(nextPageLink, this.client.getAcceptLanguage(), clientRequestId, returnClientRequestId, ocpDateConverted);
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<NodeAgentSku>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> result = listNodeAgentSkusNextDelegate(response);
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

    private ServiceResponseWithHeaders<PageImpl<NodeAgentSku>, AccountListNodeAgentSkusHeaders> listNodeAgentSkusNextDelegate(Response<ResponseBody> response) throws BatchErrorException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<NodeAgentSku>, BatchErrorException>(this.client.getMapperAdapter())
                .register(200, new TypeToken<PageImpl<NodeAgentSku>>() { }.getType())
                .registerError(BatchErrorException.class)
                .buildWithHeaders(response, AccountListNodeAgentSkusHeaders.class);
    }

}
