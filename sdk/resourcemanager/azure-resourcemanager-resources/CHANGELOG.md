# Release History

## 2.0.0-beta.5 (Unreleased)

- Added `AggregatedManagementException` exception class for aggregation of multiple `ManagementException`.

## 2.0.0-beta.4 (2020-09-02)

- Updated `azure-core-management` dependency
- Supported `beginCreate` and `beginDelete` for `VirtualMachine`, `Disk`, `NetworkInterface`, `PublicIpAddress`.
- Removed `DateTimeDeserializer`, as it is in azure-core.
- Added `ReturnRequestIdHeaderPolicy`. It is added to pipeline by default.
