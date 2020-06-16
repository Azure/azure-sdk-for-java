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

A parent POM `azure-resourcemanager-parent` is provided for common configurations and dependencies.

## Fluent interface

Azure Management Libraries for Java is object-oriented API for managing Azure resources.

There are 3 levels of object for Azure resource management.

Topmost, there is service management API, e.g. `ComputeManager` for Azure compute service or `Azure` for all supported services.
Authentication is required to instantiate the service management API.

Next, there is resource management API, e.g. `VirtualMachines` under Azure compute service, to manage Azure virtual machine.
It can be instantiated from service management API, e.g. as `computeManager.virtualMachines()` or `azure.virtualMachines()`.

Finally, there is resource instance, e.g. `VirtualMachine` as a client-side representation of Azure virtual machine.
