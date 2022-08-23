# Troubleshoot Azure Form Recognizer client library issues

This troubleshooting guide contains instructions to diagnose frequently encountered issues while using the Azure Form Recognizer client library for Java.

## Table of Contents
* [Troubleshooting Errors & Exceptions](#troubleshooting-errors--exceptions)
    * [Handling HttpResponseException](#handling-httpResponseException)
    * [Generic Error](#generic-error)
    * [Build model error](#Build)
       * json files should be formatted from FR studio
       * Invalid model created
* Expected time to train a model using `buildMode: "neural"` 

* [Troubleshooting HttpResponseException Issues](#troubleshooting-HttpResponseException-issues)

## Troubleshooting Errors & Exceptions
### Handling HttpResponseException
Form Recognizer service methods throw a [HttpResponseException](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java) or its subclass on failure.
The HttpResponseException thrown by the Azure Form Recognizer client library includes detailed response error object
that provides specific useful insights into what went wrong and include corrective actions to fix common issues.
This error information can be found inside the message property of the HttpResponseException object.

Here's the example of how to catch it with synchronous client

```java readme-sample-handlingException
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRepository containerRepository = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .audience(ContainerRegistryAudience.AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD)
    .credential(credential)
    .buildClient()
    .getRepository(repositoryName);
try {
    containerRepository.getProperties();
} catch (HttpResponseException exception) {
    // Do something with the exception.
}
```

With async clients, you can catch and handle exceptions in the error callbacks:

```java readme-sample-async-handlingException
anonymousClient.deleteRepository(repositoryName)
    .doOnSuccess(
        ignored -> System.out.println("Unexpected Success: Delete is not allowed on anonymous access!"))
    .doOnError(
        error -> error instanceof ClientAuthenticationException,
        error -> System.out.println("Expected exception: Delete is not allowed on anonymous access"));
```

### Build model error
You may have seen a Build model error. 
It looks like this:

```text
com.azure.core.exception.HttpResponseException: Invalid model created with model Id [modelId], errorCode: [2012], message: <specific-reason>"
```

### Generic Error
A "Generic Error" in the SDK's is most often caused by heavy load on the service and throttling of the service and retrying after backoff time 
should help mitigate this.
The error looks like:

```text
com.azure.core.exception.HttpResponseException: Status code 200, Invalid model created with model Id [modelId], errorCode: [3014], message: Generic error during training."
```



## Troubleshooting authentication errors

### HTTP 401 Errors

HTTP 401 errors indicates problems authenticating. Check exception message or logs for more information. 

#### Anonymous access issues
You may see error similar to the one below, it indicates an attempt to perform operation that requires authentication without credentials.

```text
AcrErrorsException: Status code 401, "{"errors":[{"code":"UNAUTHORIZED","message":"authentication required, visit https://aka.ms/acr/authorization for more information."}]}"
```

Unauthorized access can only be enabled for read (pull) operations such as listing repositories, getting properties or tags.
Refer to [Anonymous pull access](https://docs.microsoft.com/azure/container-registry/anonymous-pull-access) to learn about anonymous access limitation. 

### HTTP 403 Errors

HTTP 403 errors indicate the user is not authorized to perform a specific operation in Azure Form Recognizer.

#### Insufficient permissions

If you see an error similar to the one below, it means that the provided credentials does not have permissions to access the registry.
```text
AcrErrorsException: Status code 403, "{"errors":[{"code":"DENIED","message":"retrieving permissions failed"}]}"
```

1. Check that the application or user that is making the request has sufficient permissions.
   Check [Troubleshoot registry login](https://docs.microsoft.com/azure/container-registry/container-registry-troubleshoot-login) for possible solutions.
2. If the user or application is granted sufficient privileges to query the workspace, make sure you are
   authenticating as that user/application. If you are authenticating using the
   [DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#authenticating-with-defaultazurecredential)
   then check the logs to verify that the credential used is the one you expected. To enable logging, see [enable
   client logging](#enable-client-logging) section above.

For more help on troubleshooting authentication errors, please see the Azure Identity client library [troubleshooting
guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TROUBLESHOOTING.md).

#### Network access issues

You may see an error similar to the one below, it indicates that public access to Azure Form Recognizer is disabled or restricted.
Refer to [Troubleshoot network issues with registry](https://docs.microsoft.com/azure/container-registry/container-registry-troubleshoot-access) for more information.
```text
AcrErrorsException: Status code 403, "{"errors":[{"code":"DENIED","message":"client with IP <> is not allowed access. Refer https://aka.ms/acr/firewall to grant access."}]}
```
 
## Dependency Conflicts

If you see `NoSuchMethodError` or `NoClassDefFoundError` during your application runtime, this is due to a
dependency version conflict. Please take a look at [troubleshooting dependency version conflicts](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict) for more information on
why this happens and [ways to mitigate this issue](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict#mitigate-version-mismatch-issues).
