# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-03-20)

- Azure Resource Manager reservations client library for Java. This package contains Microsoft Azure SDK for reservations Management SDK. This API describe Azure Reservation. Package tag package-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Returns` was modified

* `models.RefundResponse post(java.lang.String,models.RefundRequest)` -> `models.ReservationOrderResponse post(java.lang.String,models.RefundRequest)`
* `models.RefundResponse post(java.lang.String,models.RefundRequest,com.azure.core.util.Context)` -> `models.ReservationOrderResponse post(java.lang.String,models.RefundRequest,com.azure.core.util.Context)`

### Features Added

#### `models.CatalogMsrp` was modified

* `p5Y()` was added
* `p3Y()` was added
* `withP5Y(models.Price)` was added
* `withP3Y(models.Price)` was added

## 1.0.0-beta.1 (2023-02-07)

- Azure Resource Manager reservations client library for Java. This package contains Microsoft Azure SDK for reservations Management SDK. This API describe Azure Reservation. Package tag package-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
