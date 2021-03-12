# Release History

## 2.3.0-beta.1 (Unreleased)

- Added client-side validation for `getByResourceGroup`, `listByResourceGroup`, `deleteByResourceGroup` methods.

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
