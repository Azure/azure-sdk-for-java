# Design for Azure Management Libraries for Java (Preview)

## Fluent interface

### Fine control over long-running operation

Resource provision takes time, a typical solution adopted by Azure services is the [long-running operation (LRO)][lro].

Fluent interface does the polling operations in background, and only returns the final result.

Azure Management Libraries supports fine control over the polling for certain important resources, via `Accepted` and `SyncPoller` class. Method verb is `beginCreate` and `beginDelete`.

`Accepted` class provides following functionalities:
- `ActivationResponse` via `getActivationResponse` method provides the response of the first activation operation. Note that though it wraps a resource instance, some action on this resource instance will not work, since it is not provisioned yet.
- `SyncPoller` via `getSyncPoller` method provides the control of the polling operations. `SyncPoller.poll` can be called at desired time.
- Resource instance via `getFinalResult` method, after completion of the polling operations. The method will throw `ManagementException` if polling failed and resource cannot be provisioned.

Here is sample code for Deployment.

```java
// begin provision
Accepted<Deployment> acceptedDeployment = azure.deployments()
    .define(name)
    ...
    .beginCreate();
Deployment provisioningDeployment = acceptedDeployment.getActivationResponse().getValue();

LongRunningOperationStatus pollStatus = acceptedDeployment.getActivationResponse().getStatus();
int delayInMills = acceptedDeployment.getActivationResponse().getRetryAfter() == null
    ? 0
    : (int) acceptedDeployment.getActivationResponse().getRetryAfter().toMillis();
while (!pollStatus.isComplete()) {
    Thread.sleep(delayInMills);

    // poll
    PollResponse<?> pollResponse = acceptedDeployment.getSyncPoller().poll();
    pollStatus = pollResponse.getStatus();
    delayInMills = pollResponse.getRetryAfter() == null
        ? DEFAULT_DELAY_IN_MILLIS
        : (int) pollResponse.getRetryAfter().toMillis();
}
// pollStatus == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, if successful

// final result
Deployment deployment = acceptedDeployment.getFinalResult();
```

Supported Azure resources:
- `delete` for `ResourceGroup`
- `create` for `Deployment`
- `create` and `delete` for `GenericResource`
- `create` and `delete` for `VirtualMachine`
- `create` and `delete` for `Disk`
- `create` and `delete` for `NetworkInterface`
- `create` and `delete` for `PublicIpAddress`

[lro]: https://docs.microsoft.com/en-us/azure/architecture/patterns/async-request-reply
