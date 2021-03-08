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

<!-- embedme ../azure-resourcemanager/src/samples/java/com/azure/resourcemanager/DesignPreviewSamples.java#L62-L89 -->
```java
Duration defaultDelay = Duration.ofSeconds(10);
// begin provision
Accepted<Deployment> acceptedDeployment = azure.deployments()
    .define("deployment")
    .withNewResourceGroup(rgName, Region.US_WEST)
    .withTemplateLink(templateUri, contentVersion)
    .withParametersLink(parametersUri, contentVersion)
    .withMode(DeploymentMode.COMPLETE)
    .beginCreate();
// get deployment before provision complete
Deployment provisioningDeployment = acceptedDeployment.getActivationResponse().getValue();
// get status of activation response
LongRunningOperationStatus pollStatus = acceptedDeployment.getActivationResponse().getStatus();
Duration delay = acceptedDeployment.getActivationResponse().getRetryAfter() == null
    ? defaultDelay
    : acceptedDeployment.getActivationResponse().getRetryAfter();
while (!pollStatus.isComplete()) {
    Thread.sleep(delay.toMillis());
    // poll and get status
    PollResponse<?> pollResponse = acceptedDeployment.getSyncPoller().poll();
    pollStatus = pollResponse.getStatus();
    delay = pollResponse.getRetryAfter() == null
        ? defaultDelay
        : pollResponse.getRetryAfter();
}
// pollStatus == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, if successful
// get final result
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

[lro]: https://docs.microsoft.com/azure/architecture/patterns/async-request-reply
