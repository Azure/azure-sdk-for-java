// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.annotation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.rest.Response;
import io.clientcore.core.http.rest.ResponseBase;
import joptsimple.util.KeyValuePair;
import reactor.core.publisher.Mono;

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
        // BEGIN: com.azure.core.annotation.BodyParam.class1
        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
            + "virtualMachines/{vmName}")
        VirtualMachine createOrUpdate(@PathParam("resourceGroupName") String rgName,
            @PathParam("vmName") String vmName,
            @PathParam("subscriptionId") String subscriptionId,
            @BodyParam("application/json") VirtualMachine vm);
        // END: com.azure.core.annotation.BodyParam.class1

        // BEGIN: com.azure.core.annotation.BodyParam.class2
        @Post("formdata/stream/uploadfile")
        void uploadFileViaBody(@BodyParam("application/octet-stream") FileInputStream fileContent);
        // END: com.azure.core.annotation.BodyParam.class2
    }


    /**
     * Examples for {@link Delete}.
     */
    interface DeleteExamples {
        // BEGIN: com.azure.core.annotation.Delete.class1
        @Delete("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
            + "virtualMachines/{vmName}")
        void delete(@PathParam("resourceGroupName") String rgName,
            @PathParam("vmName") String vmName,
            @PathParam("subscriptionId") String subscriptionId);
        // END: com.azure.core.annotation.Delete.class1

        // BEGIN: com.azure.core.annotation.Delete.class2
        @Delete("{vaultBaseUrl}/secrets/{secretName}")
        void delete(@PathParam(value = "vaultBaseUrl", encoded = true) String vaultBaseUrl,
            @PathParam("secretName") String secretName);
        // END: com.azure.core.annotation.Delete.class2
    }


    /**
     * Examples for {@link ExpectedResponses}.
     */
    interface ExpectedResponsesExamples {
        // BEGIN: com.azure.core.annotation.ExpectedResponses.class
        @ExpectedResponses({200, 201})
        @Post("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.CustomerInsights/"
            + "hubs/{hubName}/images/getEntityTypeImageUploadUrl")
        void getUploadUrlForEntityType(@PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("hubName") String hubName,
            @PathParam("subscriptionId") String subscriptionId,
            @BodyParam("application/json") RequestBody parameters);
        // END: com.azure.core.annotation.ExpectedResponses.class
    }


    /**
     * Examples for {@link FormParam}.
     */
    interface FormParamExamples {
        // BEGIN: com.azure.core.annotation.FormParam.class
        @Post("spellcheck")
        Mono<Response<ResponseBody>> spellChecker(@HeaderParam("X-BingApis-SDK") String xBingApisSDK,
            @QueryParam("UserId") String userId,
            @FormParam("Text") String text);
        // END: com.azure.core.annotation.FormParam.class
    }


    /**
     * Examples for {@link Get}.
     */
    interface GetExamples {
        // BEGIN: com.azure.core.annotation.Get.class1
        @Get("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
            + "virtualMachines/{vmName}")
        VirtualMachine getByResourceGroup(@PathParam("resourceGroupName") String rgName,
            @PathParam("vmName") String vmName,
            @PathParam("subscriptionId") String subscriptionId);
        // END: com.azure.core.annotation.Get.class1

        // BEGIN: com.azure.core.annotation.Get.class2
        @Get("{nextLink}")
        List<VirtualMachine> listNext(@PathParam("nextLink") String nextLink);
        // END: com.azure.core.annotation.Get.class2
    }


    /**
     * Examples for {@link Head}.
     */
    interface HeadExamples {
        // BEGIN: com.azure.core.annotation.Head.class1
        @Head("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
            + "virtualMachines/{vmName}")
        boolean checkNameAvailability(@PathParam("resourceGroupName") String rgName,
            @PathParam("vmName") String vmName,
            @PathParam("subscriptionId") String subscriptionId);
        // END: com.azure.core.annotation.Head.class1

        // BEGIN: com.azure.core.annotation.Head.class2
        @Head("{storageAccountId}")
        boolean checkNameAvailability(@PathParam("storageAccountId") String storageAccountId);
        // END: com.azure.core.annotation.Head.class2
    }


    /**
     * Examples for {@link HeaderParam}.
     */
    interface HeaderParamExamples {
        // BEGIN: com.azure.core.annotation.HeaderParam.class1
        @Put("{functionId}")
        Mono<ResponseBase<ResponseHeaders, ResponseBody>> createOrReplace(
            @PathParam(value = "functionId", encoded = true) String functionId,
            @BodyParam("application/json") RequestBody function,
            @HeaderParam("If-Match") String ifMatch);

        // "If-Match: user passed value" will show up as one of the headers.
        // END: com.azure.core.annotation.HeaderParam.class1

        // BEGIN: com.azure.core.annotation.HeaderParam.class2
        @Get("subscriptions/{subscriptionId}/providers/Microsoft.ServiceBus/namespaces")
        Mono<ResponseBase<ResponseHeaders, ResponseBody>> list(@PathParam("subscriptionId") String subscriptionId,
            @HeaderParam("accept-language") String acceptLanguage,
            @HeaderParam("User-Agent") String userAgent);

        // "accept-language" generated by the HTTP client will be overwritten by the user passed value.
        // END: com.azure.core.annotation.HeaderParam.class2

        // BEGIN: com.azure.core.annotation.HeaderParam.class3
        @Get("subscriptions/{subscriptionId}/providers/Microsoft.ServiceBus/namespaces")
        Mono<ResponseBase<ResponseHeaders, ResponseBody>> list(@PathParam("subscriptionId") String subscriptionId,
            @HeaderParam("Authorization") String token);

        // The token parameter will replace the effect of any credentials in the HttpPipeline.
        // END: com.azure.core.annotation.HeaderParam.class3

        // BEGIN: com.azure.core.annotation.HeaderParam.class4
        @Put("{containerName}/{blob}")
        @ExpectedResponses({200})
        Mono<ResponseBase<ResponseHeaders, Void>> setMetadata(@PathParam("containerName") String containerName,
            @PathParam("blob") String blob,
            @HeaderParam("x-ms-meta-") Map<String, String> metadata);

        // The metadata parameter will be expanded out so that each entry becomes
        // "x-ms-meta-{@literal <entryKey>}: {@literal <entryValue>}".
        // END: com.azure.core.annotation.HeaderParam.class4
    }


    /**
     * Examples for {@link Headers}.
     */
    interface HeadersExamples {
        // BEGIN: com.azure.core.annotation.Headers.class
        @Headers({"Content-Type: application/json; charset=utf-8", "accept-language: en-US"})
        @Post("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.CustomerInsights/"
            + "hubs/{hubName}/images/getEntityTypeImageUploadUrl")
        void getUploadUrlForEntityType(@PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("hubName") String hubName,
            @PathParam("subscriptionId") String subscriptionId,
            @BodyParam("application/json") RequestBody parameters);
        // END: com.azure.core.annotation.Headers.class
    }


    /**
     * Examples for {@link Host}.
     */
    interface HostExamples {
        // BEGIN: com.azure.core.annotation.Host.class1
        @Host("https://management.azure.com")
        interface VirtualMachinesService {
            @Get("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
                + "virtualMachines/{vmName}")
            VirtualMachine getByResourceGroup(@PathParam("resourceGroupName") String rgName,
                @PathParam("vmName") String vmName,
                @PathParam("subscriptionId") String subscriptionId);
        }
        // END: com.azure.core.annotation.Host.class1

        // BEGIN: com.azure.core.annotation.Host.class2
        @Host("https://{vaultName}.vault.azure.net:443")
        interface KeyVaultService {
            @Get("secrets/{secretName}")
            Secret get(@HostParam("vaultName") String vaultName, @PathParam("secretName") String secretName);
        }
        // END: com.azure.core.annotation.Host.class2
    }


    /**
     * Example for {@link HostParam}.
     */
    // BEGIN: com.azure.core.annotation.HostParam.class1
    @Host("{accountName}.{suffix}")
    interface DatalakeService {
        @Get("jobs/{jobIdentity}")
        Job getJob(@HostParam("accountName") String accountName,
            @HostParam("suffix") String suffix,
            @PathParam("jobIdentity") String jobIdentity);
    }
    // END: com.azure.core.annotation.HostParam.class1

    /**
     * Examples for {@link HostParam}.
     */
    // BEGIN: com.azure.core.annotation.HostParam.class2
    String KEY_VAULT_ENDPOINT = "{vaultName}";

    @Host(KEY_VAULT_ENDPOINT)
    interface KeyVaultService {
        @Get("secrets/{secretName}")
        Secret get(@HostParam("vaultName") String vaultName, @PathParam("secretName") String secretName);
    }
    // END: com.azure.core.annotation.HostParam.class2


    /**
     * Examples for {@link Options}.
     */
    interface OptionsExamples {
        // BEGIN: com.azure.core.annotation.Options.class1
        @Options("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
            + "virtualMachines/{vmName}")
        ResponseBase<ResponseHeaders, ResponseBody> options(@PathParam("resourceGroupName") String rgName,
            @PathParam("vmName") String vmName,
            @PathParam("subscriptionId") String subscriptionId);
        // END: com.azure.core.annotation.Options.class1

        // BEGIN: com.azure.core.annotation.Options.class2
        @Options("{vaultBaseUrl}/secrets/{secretName}")
        ResponseBase<ResponseHeaders, ResponseBody> options(
            @PathParam(value = "vaultBaseUrl", encoded = true) String vaultBaseUrl,
            @PathParam("secretName") String secretName);
        // END: com.azure.core.annotation.Options.class2
    }


    /**
     * Examples for {@link Patch}.
     */
    interface PatchExamples {
        // BEGIN: com.azure.core.annotation.Patch.class1
        @Patch("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
            + "virtualMachines/{vmName}")
        VirtualMachine patch(@PathParam("resourceGroupName") String rgName,
            @PathParam("vmName") String vmName,
            @PathParam("subscriptionId") String subscriptionId,
            @BodyParam("application/json") VirtualMachineUpdateParameters updateParameters);
        // END: com.azure.core.annotation.Patch.class1

        // BEGIN: com.azure.core.annotation.Patch.class2
        @Patch("{vaultBaseUrl}/secrets/{secretName}")
        Secret patch(@PathParam(value = "vaultBaseUrl", encoded = true) String vaultBaseUrl,
            @PathParam("secretName") String secretName,
            @BodyParam("application/json") SecretUpdateParameters updateParameters);
        // END: com.azure.core.annotation.Patch.class2
    }


    /**
     * Examples for {@link PathParam}.
     */
    interface PathParamExamples {
        // BEGIN: com.azure.core.annotation.PathParam.class1
        @Get("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
            + "virtualMachines/")
        VirtualMachine getByResourceGroup(@PathParam("subscriptionId") String subscriptionId,
            @PathParam("resourceGroupName") String rgName,
            @PathParam("foo") String bar);

        // The value of parameters subscriptionId, resourceGroupName will be encoded and used to replace the
        // corresponding path segments {subscriptionId}, {resourceGroupName} respectively.
        // END: com.azure.core.annotation.PathParam.class1

        // BEGIN: com.azure.core.annotation.PathParam.class2
        // It is possible that a path segment variable can be used to represent sub path:

        @Get("http://wq.com/foo/{subpath}/value")
        String getValue(@PathParam("subpath") String param1);

        // In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks like:
        // "http://wq.com/foo/a%2Fb/value".
        // END: com.azure.core.annotation.PathParam.class2

        // BEGIN: com.azure.core.annotation.PathParam.class3
        // For such cases the encoded attribute can be used:

        @Get("http://wq.com/foo/{subpath}/values")
        List<String> getValues(@PathParam(value = "subpath", encoded = true) String param1);

        // In this case, if consumer pass "a/b" as the value for param1 then the resolved url looks as expected:
        // "http://wq.com/foo/a/b/values".
        // END: com.azure.core.annotation.PathParam.class3
    }


    /**
     * Examples for {@link Post}.
     */
    interface PostExamples {
        // BEGIN: com.azure.core.annotation.Post.class1
        @Post("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
            + "virtualMachines/{vmName}/restart")
        void restart(@PathParam("resourceGroupName") String rgName,
            @PathParam("vmName") String vmName,
            @PathParam("subscriptionId") String subscriptionId);
        // END: com.azure.core.annotation.Post.class1

        // BEGIN: com.azure.core.annotation.Post.class2
        @Post("https://{functionApp}.azurewebsites.net/admin/functions/{name}/keys/{keyName}")
        KeyValuePair generateFunctionKey(@PathParam("functionApp") String functionApp,
            @PathParam("name") String name,
            @PathParam("keyName") String keyName);
        // END: com.azure.core.annotation.Post.class2
    }


    /**
     * Examples for {@link Put}.
     */
    interface PutExamples {
        // BEGIN: com.azure.core.annotation.Put.class1
        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/Microsoft.Compute/"
            + "virtualMachines/{vmName}")
        VirtualMachine createOrUpdate(@PathParam("resourceGroupName") String rgName,
            @PathParam("vmName") String vmName,
            @PathParam("subscriptionId") String subscriptionId,
            @BodyParam("application/json") VirtualMachine vm);
        // END: com.azure.core.annotation.Put.class1

        // BEGIN: com.azure.core.annotation.Put.class2
        @Put("{vaultBaseUrl}/secrets/{secretName}")
        Secret createOrUpdate(@PathParam(value = "vaultBaseUrl", encoded = true) String vaultBaseUrl,
            @PathParam("secretName") String secretName,
            @BodyParam("application/json") Secret secret);
        // END: com.azure.core.annotation.Put.class2
    }


    /**
     * Examples for {@link QueryParam}.
     */
    interface QueryParamExamples {
        // BEGIN: com.azure.core.annotation.QueryParam.class1
        @Get("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/resources")
        Mono<ResponseBase<ResponseHeaders, ResponseBody>> listByResourceGroup(
            @PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("subscriptionId") String subscriptionId,
            @QueryParam("$filter") String filter,
            @QueryParam("$expand") String expand,
            @QueryParam("$top") Integer top,
            @QueryParam("api-version") String apiVersion);

        // The value of parameters filter, expand, top, apiVersion will be encoded and will be used to set the query
        // parameters {$filter}, {$expand}, {$top}, {api-version} on the HTTP URL.
        // END: com.azure.core.annotation.QueryParam.class1

        // BEGIN: com.azure.core.annotation.QueryParam.class2
        // It is possible that a query parameter will need to be encoded:
        @Get("http://wq.com/foo/{subpath}/value")
        String getValue(@PathParam("subpath") String param,
            @QueryParam("query") String query);

        // In this case, if consumer pass "a=b" as the value for 'query' then the resolved url looks like:
        // "http://wq.com/foo/subpath/value?query=a%3Db"
        // END: com.azure.core.annotation.QueryParam.class2

        // BEGIN: com.azure.core.annotation.QueryParam.class3
        @Get("http://wq.com/foo/{subpath}/values")
        List<String> getValues(@PathParam("subpath") String param,
            @QueryParam(value = "query", encoded = true) String query);

        // In this case, if consumer pass "a=b" as the value for 'query' then the resolved url looks like:
        // "http://wq.com/foo/paramblah/values?connectionString=a=b"
        // END: com.azure.core.annotation.QueryParam.class3

        // BEGIN: com.azure.core.annotation.QueryParam.class4
        @Get("http://wq.com/foo/multiple/params")
        String multipleParams(@QueryParam(value = "query", multipleQueryParams = true) List<String> query);

        // The value of parameter avoid would look like this:
        // "http://wq.com/foo/multiple/params?avoid%3Dtest1&avoid%3Dtest2&avoid%3Dtest3"
        // END: com.azure.core.annotation.QueryParam.class4
    }


    /**
     * Examples for {@link UnexpectedResponseExceptionType}.
     */
    interface UnexpectedResponseExceptionTypeExamples {
        // BEGIN: com.azure.core.annotation.UnexpectedResponseExceptionType.class
        // Set it so that all response exceptions use a custom exception type.

        @UnexpectedResponseExceptionType(MyCustomExceptionHttpResponseException.class)
        @Post("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/"
            + "Microsoft.CustomerInsights/hubs/{hubName}/images/getEntityTypeImageUploadUrl")
        void singleExceptionType(@PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("hubName") String hubName,
            @PathParam("subscriptionId") String subscriptionId,
            @BodyParam("application/json") RequestBody parameters);


        // Set it so 404 uses a specific exception type while others use a generic exception type.

        @UnexpectedResponseExceptionType(code = {404}, value = ResourceNotFoundException.class)
        @UnexpectedResponseExceptionType(HttpResponseException.class)
        @Post("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/"
            + "Microsoft.CustomerInsights/hubs/{hubName}/images/getEntityTypeImageUploadUrl")
        void multipleExceptionTypes(@PathParam("resourceGroupName") String resourceGroupName,
            @PathParam("hubName") String hubName,
            @PathParam("subscriptionId") String subscriptionId,
            @BodyParam("application/json") RequestBody parameters);

        // If multiple annotations share the same HTTP status code or there is multiple default annotations the
        // exception, the last annotation in the top to bottom order will be used (so the bottom most annotation).
        // END: com.azure.core.annotation.UnexpectedResponseExceptionType.class
    }

    final class RequestBody {
    }

    final class ResponseBody {
    }

    final class ResponseHeaders {
    }

    final class Job {
    }

    final class MyCustomExceptionHttpResponseException extends HttpResponseException {
        public MyCustomExceptionHttpResponseException(Response<?> response) {
            super(response);
        }
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
