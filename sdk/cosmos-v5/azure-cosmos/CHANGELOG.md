## Release History

### 5.0.0-beta.1 (Unreleased)

#### Other Changes

* Added an isolated, nonpublishing Java 17 foundation in `sdk/cosmos-v5`.
  This is not a functional SDK and must not be used by applications. - See [PR 51181](https://github.com/Azure/azure-sdk-for-java/pull/51181)
* Added Core v2 dependency wiring, JUnit 5 foundation checks, module metadata,
  and opt-in build/version configuration without changing the existing v4 SDK.
  Native integration and service client functionality will follow separately. - See [PR 51181](https://github.com/Azure/azure-sdk-for-java/pull/51181)
