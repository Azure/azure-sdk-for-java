# Design for Azure Management Libraries for Java

## Namespace

Packages for Azure services are named as:

`com.azure.resourcemanager.<service>`

Within each package:

- `<package>` contains manager class for Azure service, as well as root client and client builder.
- `<package>.models` contains all core model interfaces and classes.
- `<package>.fluent` contains all client classes.
- `<package>.fluent.inner` contains additional model classes.
- `<package>.implementation` contains non-public classes. They are intended for internal use only.

In addition to packages for Azure services, a root package `com.azure.resourcemanager` is provided for the convenience of including all supported services.

## Maven

Group ID is:

`com.azure.resourcemanager`

Artifact ID for Azure services are:

`azure-resourcemanager-<service>`

In addition to artifacts for Azure services, an artifact `azure-resourcemanager` is provided for the convenience of including all supported services.

## Fluent interface

Azure Management Libraries for Java is object-oriented API for managing Azure resources.

There are 3 levels of object for Azure resource management.

Topmost, there is **service** API, e.g. `ComputeManager` for Azure compute service or `AzureResourceManager` for all supported services.
[Authentication][authenticate] is required to instantiate the service API.

Next, there is **resource collection** API, e.g. `VirtualMachines` to manage Azure virtual machine.
It can be instantiated from service API, e.g. as `computeManager.virtualMachines()` or `azure.virtualMachines()`.

Finally, there is **resource** instance, e.g. `VirtualMachine` as a client-side representation of Azure virtual machine.

### Resource provision

A resource instance can be instantiated as:

- Creating a new Azure resource from resource collection API, with method verb `define` till `create`, e.g.

```java
VirtualMachine virtualMachine = computeManager.virtualMachines()
    .define(name)...create();
```

- Getting an existing Azure resource from resource collection API, with method verb `get` or `list`, e.g.

```java
VirtualMachine virtualMachine = computeManager.virtualMachines()
    .getByResourceGroup(resourceGroupName, name);
```

After a resource instance is instantiated, it can be modified as:

- Updating the Azure resource, with method verb `update` till `apply`, e.g.

```java
virtualMachine.update()...apply();
```

After the Azure resource served its purpose, it can be deleted as:

- Deleting the Azure resource from resource collection API, with method verb `delete`, e.g.

```java
computeManager.virtualMachines().deleteByResourceGroup(resourceGroupName, name);
```

It is important to note that the resource instance, as client-side representation of the Azure resource, could be outdated as the Azure resource on service being modified or deleted by Portal, CLI, other SDKs, or even another thread in the same Java application.

### Lazy resource creation

Azure resource provision occurs when method verb `create` or `apply` is called.

Without calling `create`, the resource is a `Creatable<T>` instance, which can be provided to another resource as [dependent resource][sample_creatable_dependency], or be used in [batch creation][sample_creatable_batch].

For any resource that accept another dependent resource, consider supporting following methods:

- `withNewResource(Creatable<T> resource)` takes a `Creatable<T>` instance, and create it when provisioning current resource.
- `withNewResource(String name, Sku sku, ...)` takes a few required parameters for the dependent resource, and create it automatically when provisioning current resource.
- `withExistingResource(T resource)` takes the existing dependent resource.
- `withExistingResource(String id)` or `withExistingResource(SubResource resource)` takes the resource ID of the existing dependent resource, if only the ID is required by current resource.

### Action on resource

Other than management of life-cycle on Azure resource, resource collection API and resource instance supports action method, which could trigger change of state on the Azure resource.

Actions should be supported by resource API, e.g. `virtualMachine.restart()`.

For actions that is frequently called, consider providing it in resource collection API as well, e.g. `computeManager.virtualMachines().restart(resourceGroupName, name)`.
This saves the request to initiate the resource instance.

### List and pagination

The method verb `list` in resource collection API usually returns a `PagedIterable<T>` instance.

The results can be iterated by items, in a for-loop, or via `forEach`.
Alternatively, the results can be iterated by pages, e.g.
```java
computeManager.virtualMachines().list()
    .iterableByPage()
    .forEach(page -> ...);
```

It is worth noting that page is requested on demand. Therefore, after result found, it helps to `break` in for-loop, or to `findFirst` in stream, by avoiding unnecessary requests.

Also, there is no caching for requested pages or items in the `PagedIterable<T>` instance. Therefore, if you iterate `PagedIterable<T>` a second times, it will again request pages even if they are already iterated in the first iteration.

### Conditional request

For services support ETag and conditional request, consider supporting following methods in `define...create` and `update...apply`:

- `withETagCheck()` enables implicit ETag check, which fails creation if resource already exists on service, and fails modification if resource on service has been modified by others.
- `withETagCheck(String etag)` enables explicit ETag check on update.

## Exception

Exception on management of Azure resource is a `ManagementException` instance. Detail of the error is a `ManagementError` instance.

`managementError.getCode()` and `managementError.getMessage()` returns code and message of the error response.

<!-- LINKS -->
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[sample_creatable_dependency]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/README.md#dependency-across-azure-resources
[sample_creatable_batch]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/README.md#batch-azure-resource-provisioning
