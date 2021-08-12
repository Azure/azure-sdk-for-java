# Release History

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated `api-version` of features to `2021-07-01`.

### Features Added

- Added overload of `create(Context)` for `Creatable`.
- Added overload of `apply(Context)` for `Appliable`.
- Added overload of `beginCreate(Context)` for the definition flow of `Deployment`.
- Added support for `resourceProviderName` and `featureName` in `Feature`.

## 2.6.0 (2021-06-18)

- Added support for Challenge Based Authentication in `AuthenticationPolicy`.
- Added support for `parameters` in `PolicyDefinition` and `PolicyAssignment`.

## 2.5.0 (2021-05-28)
- Updated `api-version` of resources to `2021-01-01`
- Updated `api-version` of subscriptions to `2021-01-01`
- Updated `api-version` of policy to `2020-09-01`

## 2.4.0 (2021-04-28)

- Added shared interfaces and classes for Private Link.
- Updated parameter of `SupportsBatchCreation.create` from `List<Creatable<ResourceT>>` to `List<? extends Creatable<ResourceT>>`

## 2.3.0 (2021-03-30)

- Added client-side validation for `getByResourceGroup`, `listByResourceGroup`, `deleteByResourceGroup` methods.
- Added method overload of `getById` and `deleteById` in `GenericResources` to take `apiVersion` parameters. It is always recommended for user to provide the `apiVersion` parameter for consistency across service versions.
- Supported `TagOperations`

## 2.2.0 (2021-02-24)

- Supported locks with API version `2016-09-01`. Added `ManagementLock` and related classes.

## 2.1.0 (2020-11-24)

- Changed `ProviderRegistrationPolicy` using `Providers` endpoint directly.
- Changed `Tenants::list` to return new interface `Tenant` instead of `TenantIdDescriptionInner`.

## 2.0.0 (2020-10-19)

- Removed `Region`, as it is replaced by `com.azure.core.management.Region`.
- Removed `PagedList`
- Added `AggregatedManagementException` exception class for aggregation of multiple `ManagementException`.
- Changed `Creatabele.createAsync()` return `Mono<ResourceT>` and `SupportsBatchCreation.createAsync()` return `Flux<ResourceT>`

## 2.0.0-beta.4 (2020-09-02)

- Updated `azure-core-management` dependency
- Supported `beginCreate` and `beginDelete` for `VirtualMachine`, `Disk`, `NetworkInterface`, `PublicIpAddress`.
- Removed `DateTimeDeserializer`, as it is in azure-core.
- Added `ReturnRequestIdHeaderPolicy`. It is added to pipeline by default.
