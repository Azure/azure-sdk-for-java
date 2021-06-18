# Release History

## 2.7.0-beta.1 (Unreleased)


## 2.6.0 (2021-06-18)

- Added support for Challenge Based Authentication in `AuthenticationPolicy`.

## 2.5.0 (2021-05-28)
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated parameter of `SupportsBatchCreation.create` from `List<Creatable<ResourceT>>` to `List<? extends Creatable<ResourceT>>`

## 2.3.0 (2021-03-30)

- Added client-side validation for `getByResourceGroup`, `listByResourceGroup`, `deleteByResourceGroup` methods.

## 2.2.0 (2021-02-24)

- Improved performance of `PagedIterable`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Removed non-GA packages

## 2.0.0-beta.5 (2020-10-19)

- Renamed `Azure` to `AzureResourceManager`

## 2.0.0-beta.4 (2020-09-02)

- Added `PrivateDns`
- Added `Redis`
- Added `EventHubs`
- Added `TrafficMananger`
- Exposed `subscriptions`, `tenants` and `tenantId` API
