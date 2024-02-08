// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.annotation;

import com.generic.core.annotation.ServiceInterface;
import com.generic.core.http.Response;
import com.generic.core.http.models.HttpMethod;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * Javadoc codesnippets for {@link com.azure.core.annotation} classes.
 */
@SuppressWarnings("ALL")
public interface AnnotationJavadocCodesnippets {
    void appeaseCheckstyle();

    /**
     * Examples for {@link BodyParam}.
     */
    interface BodyParamExamples {
        // BEGIN: com.generic.core.http.annotation.BodyParam.class1
        @HttpRequestInformation(method = HttpMethod.PUT, path = "/subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}",
            returnValueWireType = VirtualMachine.class)
        VirtualMachine createOrUpdate(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName,
                                      @PathParam("subscriptionId") String subscriptionId,
                                      @BodyParam("application/json") VirtualMachine vm);
        // END: com.generic.core.http.annotation.BodyParam.class1

        // BEGIN: com.generic.core.http.annotation.BodyParam.class2
        @HttpRequestInformation(method = HttpMethod.POST, path = "/formdata/stream/uploadfile",
            returnValueWireType = void.class)
        void uploadFileViaBody(@BodyParam("application/octet-stream") FileInputStream fileContent);
        // END: com.generic.core.http.annotation.BodyParam.class2
    }

    /**
     * Examples for {@link Delete}.
     */
    interface DeleteExamples {
        // BEGIN: com.generic.core.http.annotation.Delete.class1
        @HttpRequestInformation(method = HttpMethod.DELETE, path = "/subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}",
            returnValueWireType = void.class)
        void delete(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName,
                    @PathParam("subscriptionId") String subscriptionId);
        // END: com.generic.core.http.annotation.Delete.class1

        // BEGIN: com.generic.core.http.annotation.Delete.class2
        @HttpRequestInformation(method = HttpMethod.DELETE, path = "{vaultBaseUrl}/secrets/{secretName}",
            returnValueWireType = void.class)
        void delete(@PathParam(value = "vaultBaseUrl", encoded = true) String vaultBaseUrl,
                    @PathParam("secretName") String secretName);
        // END: com.generic.core.http.annotation.Delete.class2
    }

    /**
     * Examples for {@link ExpectedResponses}.
     */
    interface ExpectedResponsesExamples {
        // BEGIN: com.generic.core.http.annotation.ExpectedResponses.class
        @HttpRequestInformation(method = HttpMethod.POST, path = "/subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.CustomerInsights/hubs/{hubName}/images/"
            + "getEntityTypeImageUploadUrl",
            returnValueWireType = void.class, expectedStatusCodes = {200, 201})
        void getUploadUrlForEntityType(@PathParam("resourceGroupName") String resourceGroupName,
                                       @PathParam("hubName") String hubName,
                                       @PathParam("subscriptionId") String subscriptionId,
                                       @BodyParam("application/json") RequestBody parameters);
        // END: com.generic.core.http.annotation.ExpectedResponses.class
    }

    /**
     * Examples for {@link FormParam}.
     */
    interface FormParamExamples {
        // BEGIN: com.generic.core.http.annotation.FormParam.class
        @HttpRequestInformation(method = HttpMethod.POST, path = "spellcheck", returnValueWireType = ResponseBody.class)
        Response<ResponseBody> spellChecker(@HeaderParam("X-BingApis-SDK") String xBingApisSDK,
                                            @QueryParam("UserId") String userId, @FormParam("Text") String text);
        // END: com.generic.core.http.annotation.FormParam.class
    }

    /**
     * Examples for {@link Get}.
     */
    interface GetExamples {
        // BEGIN: com.generic.core.http.annotation.Get.class1
        @HttpRequestInformation(method = HttpMethod.GET, path = "/subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}",
            returnValueWireType = VirtualMachine.class)
        VirtualMachine getByResourceGroup(@PathParam("resourceGroupName") String rgName,
                                          @PathParam("vmName") String vmName,
                                          @PathParam("subscriptionId") String subscriptionId);
        // END: com.generic.core.http.annotation.Get.class1

        // BEGIN: com.generic.core.http.annotation.Get.class2
        @HttpRequestInformation(method = HttpMethod.GET, path = "{nextLink}",
            returnValueWireType = VirtualMachine.class)
        List<VirtualMachine> listNext(@PathParam("nextLink") String nextLink);
        // END: com.generic.core.http.annotation.Get.class2
    }

    /**
     * Examples for {@link Head}.
     */
    interface HeadExamples {
        // BEGIN: com.generic.core.http.annotation.Head.class1
        @HttpRequestInformation(method = HttpMethod.HEAD, path = "/subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}",
            returnValueWireType = boolean.class)
        boolean checkNameAvailability(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName,
                                      @PathParam("subscriptionId") String subscriptionId);
        // END: com.generic.core.http.annotation.Head.class1

        // BEGIN: com.generic.core.http.annotation.Head.class2
        @HttpRequestInformation(method = HttpMethod.HEAD, path = "{storageAccountId}",
            returnValueWireType = boolean.class)
        boolean checkNameAvailability(@PathParam("storageAccountId") String storageAccountId);
        // END: com.generic.core.http.annotation.Head.class2
    }

    /**
     * Examples for {@link HeaderParam}.
     */
    interface HeaderParamExamples {
        // BEGIN: com.generic.core.http.annotation.HeaderParam.class1
        @HttpRequestInformation(method = HttpMethod.PUT, path = "{functionId}",
            returnValueWireType = ResponseBody.class)
        Response<ResponseBody> createOrReplace(@PathParam(value = "functionId", encoded = true) String functionId,
            @BodyParam("application/json") RequestBody function, @HeaderParam("If-Match") String ifMatch);

        // "If-Match: user passed value" will show up as one of the headers.
        // END: com.generic.core.http.annotation.HeaderParam.class1

        // BEGIN: com.generic.core.http.annotation.HeaderParam.class2
        @HttpRequestInformation(method = HttpMethod.GET, path = "subscriptions/{subscriptionId}/providers/"
            + "Microsoft.ServiceBus/namespaces", returnValueWireType = ResponseBody.class)
        Response<ResponseBody> list(@PathParam("subscriptionId") String subscriptionId,
                                    @HeaderParam("accept-language") String acceptLanguage,
                                    @HeaderParam("User-Agent") String userAgent);

        // "accept-language" generated by the HTTP client will be overwritten by the user passed value.
        // END: com.generic.core.http.annotation.HeaderParam.class2

        // BEGIN: com.generic.core.http.annotation.HeaderParam.class3
        @HttpRequestInformation(method = HttpMethod.GET, path = "subscriptions/{subscriptionId}/providers/"
            + "Microsoft.ServiceBus/namespaces", returnValueWireType = ResponseBody.class)
        Response<ResponseBody> list(@PathParam("subscriptionId") String subscriptionId,
                                    @HeaderParam("Authorization") String token);

        // The token parameter will replace the effect of any credentials in the HttpPipeline.
        // END: com.generic.core.http.annotation.HeaderParam.class3

        // BEGIN: com.generic.core.http.annotation.HeaderParam.class4
        @HttpRequestInformation(method = HttpMethod.PUT, path = "{containerName}/{blob}",
            returnValueWireType = Void.class, expectedStatusCodes = {200})
        Response<Void> setMetadata(@PathParam("containerName") String containerName, @PathParam("blob") String blob,
                                   @HeaderParam("x-ms-meta-") Map<String, String> metadata);

        // The metadata parameter will be expanded out so that each entry becomes
        // "x-ms-meta-{@literal <entryKey>}: {@literal <entryValue>}".
        // END: com.generic.core.http.annotation.HeaderParam.class4
    }

    /**
     * Examples for {@link Headers}.
     */
    interface HeadersExamples {
        // BEGIN: com.generic.core.http.annotation.Headers.class
        @HttpRequestInformation(method = HttpMethod.POST, path = "/subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.CustomerInsights/hubs/{hubName}/images/"
            + "getEntityTypeImageUploadUrl",
            returnValueWireType = void.class,
            requestHeaders = {"Content-Type: application/json; charset=utf-8", "accept-language: en-US"})
        void getUploadUrlForEntityType(@PathParam("resourceGroupName") String resourceGroupName,
                                       @PathParam("hubName") String hubName,
                                       @PathParam("subscriptionId") String subscriptionId,
                                       @BodyParam("application/json") RequestBody parameters);
        // END: com.generic.core.http.annotation.Headers.class
    }

    /**
     * Examples for {@link Host}.
     */
    interface HostExamples {
        // BEGIN: com.generic.core.http.annotation.Host.class1
        @ServiceInterface(name = "VirtualMachinesService", host = "https://management.azure.com")
        interface VirtualMachinesService {
            @HttpRequestInformation(method = HttpMethod.GET, path = "/subscriptions/{subscriptionId}/resourceGroups/"
                + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}",
                returnValueWireType = VirtualMachine.class)
            VirtualMachine getByResourceGroup(@PathParam("resourceGroupName") String rgName,
                                              @PathParam("vmName") String vmName,
                                              @PathParam("subscriptionId") String subscriptionId);
        }
        // END: com.generic.core.http.annotation.Host.class1

        // BEGIN: com.generic.core.http.annotation.Host.class2
        @ServiceInterface(name = "KeyVaultService", host = "https://{vaultName}.vault.azure.net:443")
        interface KeyVaultService {
            @HttpRequestInformation(method = HttpMethod.GET, path = "secrets/{secretName}",
                returnValueWireType = Secret.class)
            Secret get(@HostParam("vaultName") String vaultName, @PathParam("secretName") String secretName);
        }
        // END: com.generic.core.http.annotation.Host.class2
    }

    /**
     * Example for {@link HostParam}.
     */
    // BEGIN: com.generic.core.http.annotation.HostParam.class1
    @ServiceInterface(name = "DatalakeService", host = "{accountName}.{suffix}")
    interface DatalakeService {
        @HttpRequestInformation(method = HttpMethod.GET, path = "jobs/{jobIdentity}", returnValueWireType = Job.class)
        Job getJob(@HostParam("accountName") String accountName, @HostParam("suffix") String suffix,
                   @PathParam("jobIdentity") String jobIdentity);
    }
    // END: com.generic.core.http.annotation.HostParam.class1

    /**
     * Examples for {@link HostParam}.
     */
    // BEGIN: com.generic.core.http.annotation.HostParam.class2
    String KEY_VAULT_ENDPOINT = "{vaultName}";

    @ServiceInterface(name = "KeyVaultService", host = KEY_VAULT_ENDPOINT)
    interface KeyVaultService {
        @HttpRequestInformation(method = HttpMethod.GET, path = "secrets/{secretName}",
            returnValueWireType = Secret.class)
        Secret get(@HostParam("vaultName") String vaultName, @PathParam("secretName") String secretName);
    }
    // END: com.generic.core.http.annotation.HostParam.class2

    /**
     * Examples for {@link Options}.
     */
    interface OptionsExamples {
        // BEGIN: com.generic.core.http.annotation.Options.class1
        @HttpRequestInformation(method = HttpMethod.OPTIONS, path = "subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}",
            returnValueWireType = ResponseBody.class)
        Response<ResponseBody> options(@PathParam("resourceGroupName") String rgName,
                                       @PathParam("vmName") String vmName,
                                       @PathParam("subscriptionId") String subscriptionId);
        // END: com.generic.core.http.annotation.Options.class1

        // BEGIN: com.generic.core.http.annotation.Options.class2
        @HttpRequestInformation(method = HttpMethod.OPTIONS, path = "{vaultBaseUrl}/secrets/{secretName}",
            returnValueWireType = ResponseBody.class)
        Response<ResponseBody> options(@PathParam(value = "vaultBaseUrl", encoded = true) String vaultBaseUrl,
                                       @PathParam("secretName") String secretName);
        // END: com.generic.core.http.annotation.Options.class2
    }

    /**
     * Examples for {@link Patch}.
     */
    interface PatchExamples {
        // BEGIN: com.generic.core.http.annotation.Patch.class1
        @HttpRequestInformation(method = HttpMethod.PATCH, path = "subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}",
            returnValueWireType = VirtualMachine.class)
        VirtualMachine patch(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName,
                             @PathParam("subscriptionId") String subscriptionId,
                             @BodyParam("application/json") VirtualMachineUpdateParameters updateParameters);
        // END: com.generic.core.http.annotation.Patch.class1

        // BEGIN: com.generic.core.http.annotation.Patch.class2
        @HttpRequestInformation(method = HttpMethod.PATCH, path = "{vaultBaseUrl}/secrets/{secretName}",
            returnValueWireType = Secret.class)
        Secret patch(@PathParam(value = "vaultBaseUrl", encoded = true) String vaultBaseUrl,
                     @PathParam("secretName") String secretName,
                     @BodyParam("application/json") SecretUpdateParameters updateParameters);
        // END: com.generic.core.http.annotation.Patch.class2
    }

    /**
     * Examples for {@link PathParam}.
     */
    interface PathParamExamples {
        // BEGIN: com.generic.core.http.annotation.PathParam.class1
        @HttpRequestInformation(method = HttpMethod.GET, path = "subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{foo}",
            returnValueWireType = VirtualMachine.class)
        VirtualMachine getByResourceGroup(@PathParam("subscriptionId") String subscriptionId,
                                          @PathParam("resourceGroupName") String rgName,
                                          @PathParam("foo") String bar);

        // The value of parameters subscriptionId, resourceGroupName will be encoded and used to replace the
        // corresponding path segments {subscriptionId}, {resourceGroupName} respectively.
        // END: com.generic.core.http.annotation.PathParam.class1

        // BEGIN: com.generic.core.http.annotation.PathParam.class2
        // It is possible that a path segment variable can be used to represent sub path:

        @HttpRequestInformation(method = HttpMethod.GET, path = "foo/{subpath}/value",
            returnValueWireType = String.class)
        String getValue(@PathParam("subpath") String param1);

        // In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks like:
        // "http://wq.com/foo/a%2Fb/value".
        // END: com.generic.core.http.annotation.PathParam.class2

        // BEGIN: com.generic.core.http.annotation.PathParam.class3
        // For such cases the encoded attribute can be used:

        @HttpRequestInformation(method = HttpMethod.GET, path = "foo/{subpath}/values",
            returnValueWireType = String.class)
        List<String> getValues(@PathParam(value = "subpath", encoded = true) String param1);

        // In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks as expected:
        // "http://wq.com/foo/a/b/values".
        // END: com.generic.core.http.annotation.PathParam.class3
    }

    /**
     * Examples for {@link Post}.
     */
    interface PostExamples {
        // BEGIN: com.generic.core.http.annotation.Post.class1
        @HttpRequestInformation(method = HttpMethod.POST, path = "subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}/restart",
            returnValueWireType = void.class)
        void restart(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName,
                     @PathParam("subscriptionId") String subscriptionId);
        // END: com.generic.core.http.annotation.Post.class1

        // BEGIN: com.generic.core.http.annotation.Post.class2
        @HttpRequestInformation(method = HttpMethod.POST, path = "https://{functionApp}.azurewebsites.net/admin/"
            + "functions/{name}/keys/{keyName}", returnValueWireType = KeyValuePair.class)
        KeyValuePair generateFunctionKey(@PathParam("functionApp") String functionApp, @PathParam("name") String name,
                                         @PathParam("keyName") String keyName);
        // END: com.generic.core.http.annotation.Post.class2
    }

    /**
     * Examples for {@link Put}.
     */
    interface PutExamples {
        // BEGIN: com.generic.core.http.annotation.Put.class1
        @HttpRequestInformation(method = HttpMethod.PUT, path = "subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}",
            returnValueWireType = VirtualMachine.class)
        VirtualMachine createOrUpdate(@PathParam("resourceGroupName") String rgName, @PathParam("vmName") String vmName,
                                      @PathParam("subscriptionId") String subscriptionId,
                                      @BodyParam("application/json") VirtualMachine vm);
        // END: com.generic.core.http.annotation.Put.class1

        // BEGIN: com.generic.core.http.annotation.Put.class2
        @HttpRequestInformation(method = HttpMethod.PUT, path = "{vaultBaseUrl}/secrets/{secretName}",
            returnValueWireType = Secret.class)
        Secret createOrUpdate(@PathParam(value = "vaultBaseUrl", encoded = true) String vaultBaseUrl,
                              @PathParam("secretName") String secretName, @BodyParam("application/json") Secret secret);
        // END: com.generic.core.http.annotation.Put.class2
    }

    /**
     * Examples for {@link QueryParam}.
     */
    interface QueryParamExamples {
        // BEGIN: com.generic.core.http.annotation.QueryParam.class1
        @HttpRequestInformation(method = HttpMethod.GET, path = "subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/resources", returnValueWireType = ResponseBody.class)
        Response<ResponseBody> listByResourceGroup(@PathParam("resourceGroupName") String resourceGroupName,
                                                   @PathParam("subscriptionId") String subscriptionId,
                                                   @QueryParam("$filter") String filter,
                                                   @QueryParam("$expand") String expand,
                                                   @QueryParam("$top") Integer top,
                                                   @QueryParam("api-version") String apiVersion);

        // The value of parameters filter, expand, top, apiVersion will be encoded and will be used to set the query
        // parameters {$filter}, {$expand}, {$top}, {api-version} on the HTTP URL.
        // END: com.generic.core.http.annotation.QueryParam.class1

        // BEGIN: com.generic.core.http.annotation.QueryParam.class2
        // It is possible that a query parameter will need to be encoded:
        @HttpRequestInformation(method = HttpMethod.GET, path = "foo/{subpath}/value",
            returnValueWireType = String.class)
        String getValue(@PathParam("subpath") String param, @QueryParam("query") String query);

        // In this case, if consumer pass "a=b" as the value for 'query' then the resolved url looks like:
        // "http://wq.com/foo/subpath/value?query=a%3Db"
        // END: com.generic.core.http.annotation.QueryParam.class2

        // BEGIN: com.generic.core.http.annotation.QueryParam.class3
        @HttpRequestInformation(method = HttpMethod.GET, path = "foo/{subpath}/values",
            returnValueWireType = String.class)
        List<String> getValues(@PathParam("subpath") String param,
                               @QueryParam(value = "query", encoded = true) String query);

        // In this case, if consumer pass "a=b" as the value for 'query' then the resolved url looks like:
        // "http://wq.com/foo/paramblah/values?connectionString=a=b"
        // END: com.generic.core.http.annotation.QueryParam.class3

        // BEGIN: com.generic.core.http.annotation.QueryParam.class4
        @HttpRequestInformation(method = HttpMethod.GET, path = "foo/multiple/params",
            returnValueWireType = String.class)
        String multipleParams(@QueryParam(value = "query", multipleQueryParams = true) List<String> query);

        // The value of parameter avoid would look like this:
        // "http://wq.com/foo/multiple/params?avoid%3Dtest1&avoid%3Dtest2&avoid%3Dtest3"
        // END: com.generic.core.http.annotation.QueryParam.class4
    }


    /**
     * Examples for {@link UnexpectedResponseExceptionType}.
     */
    interface UnexpectedResponseExceptionTypeExamples {
        // BEGIN: com.generic.core.http.annotation.UnexpectedResponseExceptionType.class
        // Set it so that all response exceptions use a custom exception type.
        @UnexpectedResponseExceptionInformation(exceptionBodyClass = MyCustomExceptionBody.class)
        @HttpRequestInformation(method = HttpMethod.POST, path = "subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.CustomerInsights/hubs/{hubName}/images/"
            + "getEntityTypeImageUploadUrl",
            returnValueWireType = void.class)
        void singleExceptionType(@PathParam("resourceGroupName") String resourceGroupName,
                                 @PathParam("hubName") String hubName,
                                 @PathParam("subscriptionId") String subscriptionId,
                                 @BodyParam("application/json") RequestBody parameters);

        // Set it so 404 uses a specific exception type while others use a generic exception type.
        @UnexpectedResponseExceptionInformation(statusCode = {404}, exceptionBodyClass = MyCustomExceptionBody.class)
        @HttpRequestInformation(method = HttpMethod.POST, path = "subscriptions/{subscriptionId}/resourceGroups/"
            + "{resourceGroupName}/providers/Microsoft.CustomerInsights/hubs/{hubName}/images/"
            + "getEntityTypeImageUploadUrl",
            returnValueWireType = void.class)
        void multipleExceptionTypes(@PathParam("resourceGroupName") String resourceGroupName,
                                    @PathParam("hubName") String hubName,
                                    @PathParam("subscriptionId") String subscriptionId,
                                    @BodyParam("application/json") RequestBody parameters);

        // If multiple annotations share the same HTTP status code or there is multiple default annotations the
        // exception, the last annotation in the top to bottom order will be used (so the bottom most annotation).
        // END: com.generic.core.http.annotation.UnexpectedResponseExceptionType.class
    }

    final class RequestBody {
    }

    final class ResponseBody {
    }

    final class ResponseHeaders {
    }

    final class Job {
    }

    final class MyCustomExceptionBody {
    }

    final class KeyValuePair {
    }

    final class Secret {
    }

    final class SecretUpdateParameters {
    }

    final class VirtualMachine {
    }

    final class VirtualMachineUpdateParameters {
    }
}
