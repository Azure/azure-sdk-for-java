# Guidance on ARM throttling

Azure Resource Manager applies [throttling][arm-throttling] on the number of requests sent from client within certain span of time.

Azure Management Libraries for Java provides some utilities to help on the challenge of throttling.

## Retry when Too Many Requests

By default, `RetryPolicy` is added to the HTTP pipeline. It will automatically re-attempt the request if it encounter Server error (500-599) as well as Too Many Requests (429).

## Information on remaining requests

HTTP policy `ResourceManagerThrottlingPolicy` can be added to HTTP pipeline, to provide real-time information on remaining requests, before throttling kick-in.

```java
AzureResourceManager azure = AzureResourceManager.configure()
    .withPolicy(new ResourceManagerThrottlingPolicy((response, throttlingInfo) -> {
        // throttlingInfo.getRateLimit()
    }))
    ...
```

Be aware that many resource providers do not give rate information in response. `ResourceManagerThrottlingPolicy` provides reliable information on Azure Resource Manager throttling limit and limit from compute resource provider.

[arm-throttling]: https://docs.microsoft.com/azure/azure-resource-manager/management/request-limits-and-throttling
