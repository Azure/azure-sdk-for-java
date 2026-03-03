## Release History

### 4.44.0 (2026-02-27)

#### Features Added
* Added config entry `spark.cosmos.account.azureEnvironment.management.scope` to allow specifying the Entra ID scope/audience to be used when retrieving tokens to authenticate against the ARM/management endpoint of non-public clouds. - See [PR 48137](https://github.com/Azure/azure-sdk-for-java/pull/48137)

### 4.43.1 (2026-02-25)

#### Bugs Fixed
* Fixed an issue where `TransientIOErrorsRetryingIterator` would trigger extra query during retries and on close. - See [PR 47996](https://github.com/Azure/azure-sdk-for-java/pull/47996)

#### Other Changes
* Added status code history in `BulkWriterNoProgressException` error message. - See [PR 48022](https://github.com/Azure/azure-sdk-for-java/pull/48022)
* Reduced the log noise level for frequent transient errors - for example throttling - in Gateway mode - [PR 48112](https://github.com/Azure/azure-sdk-for-java/pull/48112)

### 4.43.0 (2026-02-10)

#### Features Added
* Initial release of Spark 4.0 connector with Scala 2.13 support
* Added transactional batch support. See [PR 47478](https://github.com/Azure/azure-sdk-for-java/pull/47478) and [PR 47697](https://github.com/Azure/azure-sdk-for-java/pull/47697) and [47803](https://github.com/Azure/azure-sdk-for-java/pull/47803)
* Added support for throughput bucket. - See [47856](https://github.com/Azure/azure-sdk-for-java/pull/47856)

#### Other Changes

### NOTE: See CHANGELOG.md in 3.3, 3.4, and 3.5 projects for changes in prior Spark versions
