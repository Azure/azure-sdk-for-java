/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

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
import retrofit2.Callback;
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
 * in Accounts.
 */
public final class AccountsInner {
    /** The Retrofit service to perform REST calls. */
    private AccountsService service;
    /** The service client containing this operation class. */
    private DataLakeStoreAccountManagementClientImpl client;

    /**
     * Initializes an instance of AccountsInner.
     *
     * @param retrofit the Retrofit instance built from a Retrofit Builder.
     * @param client the instance of the service client containing this operation class.
     */
    public AccountsInner(Retrofit retrofit, DataLakeStoreAccountManagementClientImpl client) {
        this.service = retrofit.create(AccountsService.class);
        this.client = client;
    }

    /**
     * The interface defining all the services for Accounts to be
     * used by Retrofit to perform actually REST calls.
     */
    interface AccountsService {
        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{accountName}/firewallRules/{firewallRuleName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> deleteFirewallRule(@Path("resourceGroupName") String resourceGroupName, @Path("accountName") String accountName, @Path("firewallRuleName") String firewallRuleName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{accountName}/firewallRules/{firewallRuleName}")
        Call<ResponseBody> getFirewallRule(@Path("resourceGroupName") String resourceGroupName, @Path("accountName") String accountName, @Path("firewallRuleName") String firewallRuleName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{accountName}/firewallRules")
        Call<ResponseBody> listFirewallRules(@Path("resourceGroupName") String resourceGroupName, @Path("accountName") String accountName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{accountName}/firewallRules/{name}")
        Call<ResponseBody> createOrUpdateFirewallRule(@Path("resourceGroupName") String resourceGroupName, @Path("accountName") String accountName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body FirewallRuleInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{name}")
        Call<ResponseBody> create(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body DataLakeStoreAccountInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{name}")
        Call<ResponseBody> beginCreate(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body DataLakeStoreAccountInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PATCH("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{name}")
        Call<ResponseBody> update(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body DataLakeStoreAccountInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @PATCH("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{name}")
        Call<ResponseBody> beginUpdate(@Path("resourceGroupName") String resourceGroupName, @Path("name") String name, @Path("subscriptionId") String subscriptionId, @Body DataLakeStoreAccountInner parameters, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{accountName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> delete(@Path("resourceGroupName") String resourceGroupName, @Path("accountName") String accountName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @HTTP(path = "subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{accountName}", method = "DELETE", hasBody = true)
        Call<ResponseBody> beginDelete(@Path("resourceGroupName") String resourceGroupName, @Path("accountName") String accountName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts/{accountName}")
        Call<ResponseBody> get(@Path("resourceGroupName") String resourceGroupName, @Path("accountName") String accountName, @Path("subscriptionId") String subscriptionId, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.DataLakeStore/accounts")
        Call<ResponseBody> listByResourceGroup(@Path("resourceGroupName") String resourceGroupName, @Path("subscriptionId") String subscriptionId, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("$search") String search, @Query("$format") String format, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET("subscriptions/{subscriptionId}/providers/Microsoft.DataLakeStore/accounts")
        Call<ResponseBody> list(@Path("subscriptionId") String subscriptionId, @Query("$filter") String filter, @Query("$top") Integer top, @Query("$skip") Integer skip, @Query("$expand") String expand, @Query("$select") String select, @Query("$orderby") String orderby, @Query("$count") Boolean count, @Query("$search") String search, @Query("$format") String format, @Query("api-version") String apiVersion, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listFirewallRulesNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listByResourceGroupNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

        @Headers("Content-Type: application/json; charset=utf-8")
        @GET
        Call<ResponseBody> listNext(@Url String nextPageLink, @Header("accept-language") String acceptLanguage, @Header("User-Agent") String userAgent);

    }

    /**
     * Deletes the specified firewall rule from the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account from which to delete the firewall rule.
     * @param firewallRuleName The name of the firewall rule to delete.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> deleteFirewallRule(String resourceGroupName, String accountName, String firewallRuleName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (firewallRuleName == null) {
            throw new IllegalArgumentException("Parameter firewallRuleName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.deleteFirewallRule(resourceGroupName, accountName, firewallRuleName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return deleteFirewallRuleDelegate(call.execute());
    }

    /**
     * Deletes the specified firewall rule from the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account from which to delete the firewall rule.
     * @param firewallRuleName The name of the firewall rule to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall deleteFirewallRuleAsync(String resourceGroupName, String accountName, String firewallRuleName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (firewallRuleName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter firewallRuleName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.deleteFirewallRule(resourceGroupName, accountName, firewallRuleName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(deleteFirewallRuleDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> deleteFirewallRuleDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .register(204, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Gets the specified Data Lake Store firewall rule.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account from which to get the firewall rule.
     * @param firewallRuleName The name of the firewall rule to retrieve.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FirewallRuleInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FirewallRuleInner> getFirewallRule(String resourceGroupName, String accountName, String firewallRuleName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (firewallRuleName == null) {
            throw new IllegalArgumentException("Parameter firewallRuleName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.getFirewallRule(resourceGroupName, accountName, firewallRuleName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getFirewallRuleDelegate(call.execute());
    }

    /**
     * Gets the specified Data Lake Store firewall rule.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account from which to get the firewall rule.
     * @param firewallRuleName The name of the firewall rule to retrieve.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getFirewallRuleAsync(String resourceGroupName, String accountName, String firewallRuleName, final ServiceCallback<FirewallRuleInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (firewallRuleName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter firewallRuleName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.getFirewallRule(resourceGroupName, accountName, firewallRuleName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FirewallRuleInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getFirewallRuleDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FirewallRuleInner> getFirewallRuleDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FirewallRuleInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<FirewallRuleInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists the Data Lake Store firewall rules within the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account from which to get the firewall rules.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;FirewallRuleInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<FirewallRuleInner>> listFirewallRules(final String resourceGroupName, final String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listFirewallRules(resourceGroupName, accountName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<FirewallRuleInner>> response = listFirewallRulesDelegate(call.execute());
        PagedList<FirewallRuleInner> result = new PagedList<FirewallRuleInner>(response.getBody()) {
            @Override
            public Page<FirewallRuleInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listFirewallRulesNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Lists the Data Lake Store firewall rules within the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account from which to get the firewall rules.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFirewallRulesAsync(final String resourceGroupName, final String accountName, final ListOperationCallback<FirewallRuleInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listFirewallRules(resourceGroupName, accountName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<FirewallRuleInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<FirewallRuleInner>> result = listFirewallRulesDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFirewallRulesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<FirewallRuleInner>> listFirewallRulesDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<FirewallRuleInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<FirewallRuleInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Creates or updates the specified firewall rule.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account to which to add the firewall rule.
     * @param name The name of the firewall rule to create or update.
     * @param parameters Parameters supplied to create the create firewall rule.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the FirewallRuleInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<FirewallRuleInner> createOrUpdateFirewallRule(String resourceGroupName, String accountName, String name, FirewallRuleInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Call<ResponseBody> call = service.createOrUpdateFirewallRule(resourceGroupName, accountName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return createOrUpdateFirewallRuleDelegate(call.execute());
    }

    /**
     * Creates or updates the specified firewall rule.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account to which to add the firewall rule.
     * @param name The name of the firewall rule to create or update.
     * @param parameters Parameters supplied to create the create firewall rule.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall createOrUpdateFirewallRuleAsync(String resourceGroupName, String accountName, String name, FirewallRuleInner parameters, final ServiceCallback<FirewallRuleInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
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
        Call<ResponseBody> call = service.createOrUpdateFirewallRule(resourceGroupName, accountName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<FirewallRuleInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(createOrUpdateFirewallRuleDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<FirewallRuleInner> createOrUpdateFirewallRuleDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<FirewallRuleInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<FirewallRuleInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Creates the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param name The name of the Data Lake Store account to create.
     * @param parameters Parameters supplied to create the Data Lake Store account.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the DataLakeStoreAccountInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<DataLakeStoreAccountInner> create(String resourceGroupName, String name, DataLakeStoreAccountInner parameters) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Response<ResponseBody> result = service.create(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<DataLakeStoreAccountInner>() { }.getType());
    }

    /**
     * Creates the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param name The name of the Data Lake Store account to create.
     * @param parameters Parameters supplied to create the Data Lake Store account.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall createAsync(String resourceGroupName, String name, DataLakeStoreAccountInner parameters, final ServiceCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(parameters, serviceCallback);
        Call<ResponseBody> call = service.create(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<DataLakeStoreAccountInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Creates the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param name The name of the Data Lake Store account to create.
     * @param parameters Parameters supplied to create the Data Lake Store account.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DataLakeStoreAccountInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DataLakeStoreAccountInner> beginCreate(String resourceGroupName, String name, DataLakeStoreAccountInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Call<ResponseBody> call = service.beginCreate(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginCreateDelegate(call.execute());
    }

    /**
     * Creates the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param name The name of the Data Lake Store account to create.
     * @param parameters Parameters supplied to create the Data Lake Store account.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginCreateAsync(String resourceGroupName, String name, DataLakeStoreAccountInner parameters, final ServiceCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
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
        Call<ResponseBody> call = service.beginCreate(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DataLakeStoreAccountInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginCreateDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<DataLakeStoreAccountInner> beginCreateDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<DataLakeStoreAccountInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(201, new TypeToken<DataLakeStoreAccountInner>() { }.getType())
                .register(200, new TypeToken<DataLakeStoreAccountInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Updates the specified Data Lake Store account information.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param name The name of the Data Lake Store account to update.
     * @param parameters Parameters supplied to update the Data Lake Store account.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the DataLakeStoreAccountInner object wrapped in ServiceResponse if successful.
     */
    public ServiceResponse<DataLakeStoreAccountInner> update(String resourceGroupName, String name, DataLakeStoreAccountInner parameters) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Response<ResponseBody> result = service.update(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPutOrPatchResult(result, new TypeToken<DataLakeStoreAccountInner>() { }.getType());
    }

    /**
     * Updates the specified Data Lake Store account information.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param name The name of the Data Lake Store account to update.
     * @param parameters Parameters supplied to update the Data Lake Store account.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall updateAsync(String resourceGroupName, String name, DataLakeStoreAccountInner parameters, final ServiceCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (parameters == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter parameters is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Validator.validate(parameters, serviceCallback);
        Call<ResponseBody> call = service.update(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPutOrPatchResultAsync(response, new TypeToken<DataLakeStoreAccountInner>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Updates the specified Data Lake Store account information.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param name The name of the Data Lake Store account to update.
     * @param parameters Parameters supplied to update the Data Lake Store account.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DataLakeStoreAccountInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DataLakeStoreAccountInner> beginUpdate(String resourceGroupName, String name, DataLakeStoreAccountInner parameters) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (name == null) {
            throw new IllegalArgumentException("Parameter name is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (parameters == null) {
            throw new IllegalArgumentException("Parameter parameters is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Validator.validate(parameters);
        Call<ResponseBody> call = service.beginUpdate(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginUpdateDelegate(call.execute());
    }

    /**
     * Updates the specified Data Lake Store account information.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param name The name of the Data Lake Store account to update.
     * @param parameters Parameters supplied to update the Data Lake Store account.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginUpdateAsync(String resourceGroupName, String name, DataLakeStoreAccountInner parameters, final ServiceCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (name == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter name is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
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
        Call<ResponseBody> call = service.beginUpdate(resourceGroupName, name, this.client.subscriptionId(), parameters, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DataLakeStoreAccountInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginUpdateDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<DataLakeStoreAccountInner> beginUpdateDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<DataLakeStoreAccountInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<DataLakeStoreAccountInner>() { }.getType())
                .register(201, new TypeToken<DataLakeStoreAccountInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Deletes the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account to delete.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    public ServiceResponse<Void> delete(String resourceGroupName, String accountName) throws CloudException, IOException, IllegalArgumentException, InterruptedException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Response<ResponseBody> result = service.delete(resourceGroupName, accountName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent()).execute();
        return client.getAzureClient().getPostOrDeleteResult(result, new TypeToken<Void>() { }.getType());
    }

    /**
     * Deletes the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link ServiceCall} object
     */
    public ServiceCall deleteAsync(String resourceGroupName, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
        }
        Call<ResponseBody> call = service.delete(resourceGroupName, accountName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                serviceCallback.failure(t);
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                client.getAzureClient().getPostOrDeleteResultAsync(response, new TypeToken<Void>() { }.getType(), serviceCall, serviceCallback);
            }
        });
        return serviceCall;
    }

    /**
     * Deletes the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account to delete.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the {@link ServiceResponse} object if successful.
     */
    public ServiceResponse<Void> beginDelete(String resourceGroupName, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.beginDelete(resourceGroupName, accountName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return beginDeleteDelegate(call.execute());
    }

    /**
     * Deletes the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account to delete.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall beginDeleteAsync(String resourceGroupName, String accountName, final ServiceCallback<Void> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.beginDelete(resourceGroupName, accountName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<Void>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(beginDeleteDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<Void> beginDeleteDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<Void, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<Void>() { }.getType())
                .register(404, new TypeToken<Void>() { }.getType())
                .register(204, new TypeToken<Void>() { }.getType())
                .register(202, new TypeToken<Void>() { }.getType())
                .build(response);
    }

    /**
     * Gets the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account to retrieve.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the DataLakeStoreAccountInner object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<DataLakeStoreAccountInner> get(String resourceGroupName, String accountName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (accountName == null) {
            throw new IllegalArgumentException("Parameter accountName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.get(resourceGroupName, accountName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        return getDelegate(call.execute());
    }

    /**
     * Gets the specified Data Lake Store account.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account.
     * @param accountName The name of the Data Lake Store account to retrieve.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall getAsync(String resourceGroupName, String accountName, final ServiceCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (accountName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter accountName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.get(resourceGroupName, accountName, this.client.subscriptionId(), this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<DataLakeStoreAccountInner>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    serviceCallback.success(getDelegate(response));
                } catch (CloudException | IOException exception) {
                    serviceCallback.failure(exception);
                }
            }
        });
        return serviceCall;
    }

    private ServiceResponse<DataLakeStoreAccountInner> getDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<DataLakeStoreAccountInner, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<DataLakeStoreAccountInner>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists the Data Lake Store accounts within a specific resource group. The response includes a link to the next page of results, if any.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account(s).
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;DataLakeStoreAccountInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<DataLakeStoreAccountInner>> listByResourceGroup(final String resourceGroupName) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
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
        final String search = null;
        final String format = null;
        Call<ResponseBody> call = service.listByResourceGroup(resourceGroupName, this.client.subscriptionId(), filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<DataLakeStoreAccountInner>> response = listByResourceGroupDelegate(call.execute());
        PagedList<DataLakeStoreAccountInner> result = new PagedList<DataLakeStoreAccountInner>(response.getBody()) {
            @Override
            public Page<DataLakeStoreAccountInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listByResourceGroupNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Lists the Data Lake Store accounts within a specific resource group. The response includes a link to the next page of results, if any.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account(s).
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listByResourceGroupAsync(final String resourceGroupName, final ListOperationCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
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
        final String search = null;
        final String format = null;
        Call<ResponseBody> call = service.listByResourceGroup(resourceGroupName, this.client.subscriptionId(), filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<DataLakeStoreAccountInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<DataLakeStoreAccountInner>> result = listByResourceGroupDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listByResourceGroupNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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
     * Lists the Data Lake Store accounts within a specific resource group. The response includes a link to the next page of results, if any.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account(s).
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count A Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param search A free form search. A free-text search expression to match for whether a particular entry should be included in the feed, e.g. Categories?$search=blue OR green. Optional.
     * @param format The desired return format. Return the response in particular formatxii without access to request headers for standard content-type negotiation (e.g Orders?$format=json). Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;DataLakeStoreAccountInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<DataLakeStoreAccountInner>> listByResourceGroup(final String resourceGroupName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final String search, final String format) throws CloudException, IOException, IllegalArgumentException {
        if (resourceGroupName == null) {
            throw new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null.");
        }
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listByResourceGroup(resourceGroupName, this.client.subscriptionId(), filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<DataLakeStoreAccountInner>> response = listByResourceGroupDelegate(call.execute());
        PagedList<DataLakeStoreAccountInner> result = new PagedList<DataLakeStoreAccountInner>(response.getBody()) {
            @Override
            public Page<DataLakeStoreAccountInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listByResourceGroupNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Lists the Data Lake Store accounts within a specific resource group. The response includes a link to the next page of results, if any.
     *
     * @param resourceGroupName The name of the Azure resource group that contains the Data Lake Store account(s).
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count A Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param search A free form search. A free-text search expression to match for whether a particular entry should be included in the feed, e.g. Categories?$search=blue OR green. Optional.
     * @param format The desired return format. Return the response in particular formatxii without access to request headers for standard content-type negotiation (e.g Orders?$format=json). Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listByResourceGroupAsync(final String resourceGroupName, final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final String search, final String format, final ListOperationCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (resourceGroupName == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter resourceGroupName is required and cannot be null."));
            return null;
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listByResourceGroup(resourceGroupName, this.client.subscriptionId(), filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<DataLakeStoreAccountInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<DataLakeStoreAccountInner>> result = listByResourceGroupDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listByResourceGroupNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<DataLakeStoreAccountInner>> listByResourceGroupDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<DataLakeStoreAccountInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<DataLakeStoreAccountInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists the Data Lake Store accounts within the subscription. The response includes a link to the next page of results, if any.
     *
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;DataLakeStoreAccountInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<DataLakeStoreAccountInner>> list() throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
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
        final String search = null;
        final String format = null;
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<DataLakeStoreAccountInner>> response = listDelegate(call.execute());
        PagedList<DataLakeStoreAccountInner> result = new PagedList<DataLakeStoreAccountInner>(response.getBody()) {
            @Override
            public Page<DataLakeStoreAccountInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Lists the Data Lake Store accounts within the subscription. The response includes a link to the next page of results, if any.
     *
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final ListOperationCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
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
        final String search = null;
        final String format = null;
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<DataLakeStoreAccountInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<DataLakeStoreAccountInner>> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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
     * Lists the Data Lake Store accounts within the subscription. The response includes a link to the next page of results, if any.
     *
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param search A free form search. A free-text search expression to match for whether a particular entry should be included in the feed, e.g. Categories?$search=blue OR green. Optional.
     * @param format The desired return format. Return the response in particular formatxii without access to request headers for standard content-type negotiation (e.g Orders?$format=json). Optional.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;DataLakeStoreAccountInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PagedList<DataLakeStoreAccountInner>> list(final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final String search, final String format) throws CloudException, IOException, IllegalArgumentException {
        if (this.client.subscriptionId() == null) {
            throw new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null.");
        }
        if (this.client.apiVersion() == null) {
            throw new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null.");
        }
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        ServiceResponse<PageImpl<DataLakeStoreAccountInner>> response = listDelegate(call.execute());
        PagedList<DataLakeStoreAccountInner> result = new PagedList<DataLakeStoreAccountInner>(response.getBody()) {
            @Override
            public Page<DataLakeStoreAccountInner> nextPage(String nextPageLink) throws CloudException, IOException {
                return listNext(nextPageLink).getBody();
            }
        };
        return new ServiceResponse<>(result, response.getResponse());
    }

    /**
     * Lists the Data Lake Store accounts within the subscription. The response includes a link to the next page of results, if any.
     *
     * @param filter OData filter. Optional.
     * @param top The number of items to return. Optional.
     * @param skip The number of items to skip over before returning elements. Optional.
     * @param expand OData expansion. Expand related resources in line with the retrieved resources, e.g. Categories/$expand=Products would expand Product data in line with each Category entry. Optional.
     * @param select OData Select statement. Limits the properties on each entry to just those requested, e.g. Categories?$select=CategoryName,Description. Optional.
     * @param orderby OrderBy clause. One or more comma-separated expressions with an optional "asc" (the default) or "desc" depending on the order you'd like the values sorted, e.g. Categories?$orderby=CategoryName desc. Optional.
     * @param count The Boolean value of true or false to request a count of the matching resources included with the resources in the response, e.g. Categories?$count=true. Optional.
     * @param search A free form search. A free-text search expression to match for whether a particular entry should be included in the feed, e.g. Categories?$search=blue OR green. Optional.
     * @param format The desired return format. Return the response in particular formatxii without access to request headers for standard content-type negotiation (e.g Orders?$format=json). Optional.
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listAsync(final String filter, final Integer top, final Integer skip, final String expand, final String select, final String orderby, final Boolean count, final String search, final String format, final ListOperationCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (this.client.subscriptionId() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.subscriptionId() is required and cannot be null."));
            return null;
        }
        if (this.client.apiVersion() == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter this.client.apiVersion() is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.list(this.client.subscriptionId(), filter, top, skip, expand, select, orderby, count, search, format, this.client.apiVersion(), this.client.acceptLanguage(), this.client.userAgent());
        final ServiceCall serviceCall = new ServiceCall(call);
        call.enqueue(new ServiceResponseCallback<List<DataLakeStoreAccountInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<DataLakeStoreAccountInner>> result = listDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<DataLakeStoreAccountInner>> listDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<DataLakeStoreAccountInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<DataLakeStoreAccountInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists the Data Lake Store firewall rules within the specified Data Lake Store account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;FirewallRuleInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<FirewallRuleInner>> listFirewallRulesNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listFirewallRulesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listFirewallRulesNextDelegate(call.execute());
    }

    /**
     * Lists the Data Lake Store firewall rules within the specified Data Lake Store account.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listFirewallRulesNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<FirewallRuleInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listFirewallRulesNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<FirewallRuleInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<FirewallRuleInner>> result = listFirewallRulesNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listFirewallRulesNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<FirewallRuleInner>> listFirewallRulesNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<FirewallRuleInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<FirewallRuleInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists the Data Lake Store accounts within a specific resource group. The response includes a link to the next page of results, if any.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;DataLakeStoreAccountInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<DataLakeStoreAccountInner>> listByResourceGroupNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listByResourceGroupNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listByResourceGroupNextDelegate(call.execute());
    }

    /**
     * Lists the Data Lake Store accounts within a specific resource group. The response includes a link to the next page of results, if any.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listByResourceGroupNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listByResourceGroupNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<DataLakeStoreAccountInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<DataLakeStoreAccountInner>> result = listByResourceGroupNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listByResourceGroupNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<DataLakeStoreAccountInner>> listByResourceGroupNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<DataLakeStoreAccountInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<DataLakeStoreAccountInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

    /**
     * Lists the Data Lake Store accounts within the subscription. The response includes a link to the next page of results, if any.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @return the List&lt;DataLakeStoreAccountInner&gt; object wrapped in {@link ServiceResponse} if successful.
     */
    public ServiceResponse<PageImpl<DataLakeStoreAccountInner>> listNext(final String nextPageLink) throws CloudException, IOException, IllegalArgumentException {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        return listNextDelegate(call.execute());
    }

    /**
     * Lists the Data Lake Store accounts within the subscription. The response includes a link to the next page of results, if any.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @param serviceCall the ServiceCall object tracking the Retrofit calls
     * @param serviceCallback the async ServiceCallback to handle successful and failed responses.
     * @throws IllegalArgumentException thrown if callback is null
     * @return the {@link Call} object
     */
    public ServiceCall listNextAsync(final String nextPageLink, final ServiceCall serviceCall, final ListOperationCallback<DataLakeStoreAccountInner> serviceCallback) throws IllegalArgumentException {
        if (serviceCallback == null) {
            throw new IllegalArgumentException("ServiceCallback is required for async calls.");
        }
        if (nextPageLink == null) {
            serviceCallback.failure(new IllegalArgumentException("Parameter nextPageLink is required and cannot be null."));
            return null;
        }
        Call<ResponseBody> call = service.listNext(nextPageLink, this.client.acceptLanguage(), this.client.userAgent());
        serviceCall.newCall(call);
        call.enqueue(new ServiceResponseCallback<List<DataLakeStoreAccountInner>>(serviceCallback) {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    ServiceResponse<PageImpl<DataLakeStoreAccountInner>> result = listNextDelegate(response);
                    serviceCallback.load(result.getBody().getItems());
                    if (result.getBody().getNextPageLink() != null
                            && serviceCallback.progress(result.getBody().getItems()) == ListOperationCallback.PagingBahavior.CONTINUE) {
                        listNextAsync(result.getBody().getNextPageLink(), serviceCall, serviceCallback);
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

    private ServiceResponse<PageImpl<DataLakeStoreAccountInner>> listNextDelegate(Response<ResponseBody> response) throws CloudException, IOException, IllegalArgumentException {
        return new AzureServiceResponseBuilder<PageImpl<DataLakeStoreAccountInner>, CloudException>(this.client.restClient().mapperAdapter())
                .register(200, new TypeToken<PageImpl<DataLakeStoreAccountInner>>() { }.getType())
                .registerError(CloudException.class)
                .build(response);
    }

}
