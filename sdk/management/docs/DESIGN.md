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

In addition to packages for Azure services, a root package `com.azure.resourcemanager` is provided for convenience.

## Maven

TBD

## Fluent interface

Azure Management Libraries for Java is object-oriented API for managing Azure resources.
