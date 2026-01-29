## Release History

### 4.43.0-beta.1 (Unreleased)

#### Features Added
* Added transactional batch support. See [PR 47478](https://github.com/Azure/azure-sdk-for-java/pull/47478) and [PR 47697](https://github.com/Azure/azure-sdk-for-java/pull/47697) and [47803](https://github.com/Azure/azure-sdk-for-java/pull/47803)

#### Breaking Changes

#### Bugs Fixed
* Fixed an issue for micro batch stream query where feed range starts with null or incorrect initial offset. **NOTE: This issue only happens when a partition split happened during initial offset calculation stage. - See [47742](https://github.com/Azure/azure-sdk-for-java/pull/47742)
* Fixed `java.lang.ClassCastException` during bulk write operations for write strategy `ItemPatch` or `ItemPatchIfExists`. - See [47748](https://github.com/Azure/azure-sdk-for-java/pull/47748)

#### Other Changes

### NOTE: See CHANGELOG.md in 3.3; 3.4 and 3.5 for scala 2.12 projects for changes prior to 4.43.0
