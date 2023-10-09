## Release History

### 4.23.0 (2023-10-09)

#### Features Added
* Added configuration option to control the maximum batch size used - by default the batch size is determined automatically based on the throttling rate - and will auto-adjust to meet the throughput control limits when applied. This setting is mostly added to simplify Spark 2.4 migrations where it was possible to specify a fixed batch size. This setting should only be used when not enabling throughput control - and for new workloads not being migrated from Spark 2.4 using throughput control is preferred. See [PR 37072](https://github.com/Azure/azure-sdk-for-java/pull/37072)

#### Bugs Fixed
* Fixed an issue with backpressure when using WriteStrategy `ItemBulkUpdate` - with this write strategy a Reactor operator `bufferTimeout` was used, which has issues when backpressure happens and can result in an error `verflowException: Could not emit buffer due to lack of requests`. See [PR 37072](https://github.com/Azure/azure-sdk-for-java/pull/37072)

### 4.22.0 (2023-09-19)

#### Features Added
* Added throughput control support for `gateway mode`. See [PR 36687](https://github.com/Azure/azure-sdk-for-java/pull/36687)

#### Other Changes
* Reduce noisy log in `ThroughputControlHelper` from `INFO` to `DEBUG` - See [PR 36653](https://github.com/Azure/azure-sdk-for-java/pull/36653)

### 4.21.1 (2023-08-28)

#### Bugs Fixed
* Fixed an issue where spark job failed due to 409 when `ItemBulkUpdate` is being configured - See [PR 36541](https://github.com/Azure/azure-sdk-for-java/pull/36541)

### 4.21.0 (2023-08-09)

#### Features Added
* Spark 3.4 support: - See [PR 35176](https://github.com/Azure/azure-sdk-for-java/pull/35176).
* Added a new configuration setting `spark.cosmos.write.bulk.initialBatchSize` to allow specifying the initial micro batch size for bulk operations. The batch size will be tuned automatically based on the throttling rate afterwards - by default it starts initially with 100 documents per batch. This can lead to exceeding the requested throughput when using throughput control in the first few seconds of a Spark job. This usually isn't a problem - but if there is the desire to avoid this, reducing the initial micro batch size - for example setting it to `1` - would avoid the initial spike in RU/s usage. - See [PR 36068](https://github.com/Azure/azure-sdk-for-java/pull/36068)
* Added new strategy `ItemBulkUpdate` to allow patch with more than 10 columns - See [PR 35977](https://github.com/Azure/azure-sdk-for-java/pull/35977)

#### Bugs Fixed
* Fixed schema reference issue for empty array - See [PR 35746](https://github.com/Azure/azure-sdk-for-java/pull/35746)

#### Other Changes

### NOTE: See CHANGELOG.md in 3.1, 3.2 and 3.3 projects for changes prior to 4.21.0
