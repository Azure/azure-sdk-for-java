# Guide for migrating to the test proxy

This guide describes the changes that service SDKs should make to their test frameworks in order to take advantage of
the Azure SDK test proxy.

Documentation of the motivations and goals of the test proxy can be found [here][general_docs] in the azure-sdk-tools
GitHub repository, and documentation of how to set up and use the proxy can be found [here][detailed_docs].

## Table of contents
- [Re-record existing test recordings](#re-record-existing-test-recordings)
- [Run tests](#run-tests)
    - [Perform one-time setup](#perform-one-time-setup)
    - [Start the proxy server](#start-the-proxy-server)
    - [Record or play back tests](#record-or-play-back-tests)
    - [Adding sanitizers](#adding-sanitizers)
    - [Enable the test proxy in pipelines](#enable-the-test-proxy-in-pipelines)
    - [Record test variables](#record-test-variables)
- [Migrate management-plane tests](#migrate-management-plane-tests)
- [Next steps](#next-steps)
- [Advanced details](#advanced-details)
    - [What does the test proxy do?](#what-does-the-test-proxy-do)
    - [How does the test proxy know when and what to record or play back?](#how-does-the-test-proxy-know-when-and-what-to-record-or-play-back)
    - [Start the proxy manually](#start-the-proxy-manually)

## Re-record existing test recordings
Each SDK needs to re-record its test recordings using the test-proxy integration to ensure a consolidated recording format with serialized/sanitized requests and their matching responses.
#### Steps:
1) Run & update test recordings using the [test-proxy integration][test_proxy_integration].
2) Add [custom sanitizers][custom_sanitizer_example] if needed to address service-specific redactions.
[Default redaction][default_redaction] are already set up here in Test Proxy for primary sanitization.

## Run tests
Test-Proxy maintains a _separate clone_ for each assets.json. The recording files will be located under your repo root under the `.assets` folder.
```text
+-------------------------------+
|  azure-sdk-for-java/        |
|    sdk/                       |
|      storage/                 |
| +------assets.json            |
| |    appconfiguration/        |
| | +----assets.json            |
| | |  keyvault/                |
| | |    azure-keyvault-secrets |
| | |      assets.json-------+  |
| | |    azure-keyvault-keys |  |
| | |      assets.json---+   |  |
| | |                    |   |  |
| | |.assets/            |   |  |
| | +--AuN9me8zrT/       |   |  |
| |      <sparse clone>  |   |  |
| +----5hgHKwvMaN/       |   |  |
|        <sparse clone>  |   |  |
|      AuN9me8zrT--------+   |  |
|        <sparse clone>      |  |
|      BSdGcyN2XL------------+  |
|        <sparse clone>         |
+-------------------------------+
```

### Perform one-time setup

1. Test-proxy needs to be on the machine and in the path. Instructions for that are [here][test_proxy_installation].

### Start the proxy server

The test proxy has to be available in order for tests to work in live or playback mode.

The `com.azure.core.test.TestProxyTestBase` method `setupTestProxy()` is responsible for starting test proxy.

```java
 @BeforeAll
public static void setupTestProxy(TestInfo testInfo) {
    testMode = initializeTestMode();
    Path testClassPath = Paths.get(toURI(testInfo.getTestClass().get().getResource(testInfo.getTestClass().get().getSimpleName() + ".class"), LOGGER));
    if (isTestProxyEnabled() && (testMode == TestMode.PLAYBACK || testMode == TestMode.RECORD)) {
    testProxyManager = new TestProxyManager(testClassPath);
    testProxyManager.startProxy();
    }
}
```

The `testProxyManager.startProxy()` method will fetch the test proxy and start the test proxy.

### Record or play back tests

#### Running tests in `Playback` mode
When running tests in Playback mode, the `test-proxy` automatically checks out the appropriate tag in each local assets repo and performs testing.

#### Running tests in `Record` mode
After running tests in record mode, the newly updated recordings will be available within the associated `.assets` repository.
You can then, view the changes before [pushing the updated recordings][asset_sync_push] to the assets repo.
You can either use `CLI` command to push the recordings.
CLI

`test-proxy push <path-to-assets-json>`

### Adding sanitizers

Since the test proxy doesn't use [`RecordNetworkCallPolicy`][RecordNetworkCallPolicy], tests don't use the `RecordingRedactor` to sanitize values in recordings.
Instead, sanitizers (as well as matchers) can be registered on the proxy as detailed in
[this][sanitizers] section of the proxy documentation. Custom sanitizers can be registered using [`TestProxySanitizer`][test_proxy_sanitizer] respective SDK test classes.
[`Default sanitizers`][default_sanitizers], similar to use of the `RecordingRedactor` are registered in the `TestProxyUtils`. 

For example, registering a custom sanitizer for redacting the value of json key `modelId` from response body looks like following: 
```java
private static List<TestProxySanitizer> customSanitizer = new ArrayList<>();

public static final String REDACTED = "REDACTED";

static {
    // testing different sanitizer options
    customSanitizer.add(new TestProxySanitizer("$..modelId", REDACTED, TestProxySanitizerType.BODY_KEY));
}

@Override
protected void beforeTest() {
    // add sanitizer to Test Proxy Policy
    interceptorManager.addRecordSanitizers(customSanitizer);
}
```

For a more advanced scenario, where we want to sanitize the account names of all Tables endpoints in recordings, we
could instead use the `BODY_REGEX` sanitizer type:

```java
customSanitizer.add(new TestProxySanitizer("(?<=\\/\\/)[a-z]+(?=(?:|-secondary)\\.table\\.core\\.windows\\.net)", "REDACTED", TestProxySanitizerType.URI));
```
In the
snippet above, any storage endpoint URIs that match the specified URL regex will have their account name replaced with
"REDACTED". A request made to `https://tableaccount-secondary.table.core.windows.net` will be recorded as being
made to `https://REDACTED-secondary.table.core.windows.net`, and URLs will also be sanitized in bodies and headers.

For more details about sanitizers and their options, please refer to [TestProxySanitizer][test_proxy_sanitizer].

#### Note regarding body matching

In the old testing system, request and response bodies weren't compared in playback mode by default in
most packages. The test proxy system enables body matching by default, which can introduce failures for tests that
passed in the old system. For example, if a test sends a request that includes the current Unix time in its body, the
body will contain a new value when run in playback mode at a later time. This request might still match the recording if
body matching is disabled, but not if it's enabled.

Body matching can be turned off with the test proxy by calling the `setComparingBodies` method from
[TestProxyRequestMatcher][test_proxy_matcher] on the Interceptor Manager for that particular test.

### Enable the test proxy in pipelines

#### CI pipelines

To enable using the test proxy in CI, you need to set the parameter `TestProxy: true` in the `ci.yml` file in the
service-level folder. For example, in [sdk/formrecognizer/ci.yml][pipelines_ci]:

```diff
extends:
  template: ../../eng/pipelines/templates/stages/archetype-sdk-client.yml
  parameters:
    ServiceDirectory: formrecognizer
+   TestProxy: true
    ...
```

#### Live test pipelines

-- TODO --

### Record test variables

To run recorded tests successfully when there's an element of non-secret randomness to them, the test proxy provides a
[`variables` API][variables_api]. This makes it possible for a test to record the values of variables that were used
during recording and use the same values in playback mode without a sanitizer.

For example, imagine that a test uses a randomized `tableName` variable when creating resources. The same random value
for `tableName` can be used in playback mode by using this `Variables` API.

## Migrate management-plane tests

For management-plane packages, test classes should inherit from [TestProxyTestBase][test_proxy_base].

The rest of the information in this guide applies to management-plane packages as well, except for possible specifics
regarding test resource deployment.

## Next steps

Once your tests have been migrated to the test proxy, they can also have their recordings moved out of the
`azure-sdk-for-java` repo. Refer to the [recording migration guide][recording_migration] for more details.

After recordings are moved, you can refer to the instructions in [`TestProxyMigration.md`][test_proxy_migration] to manage them.

## Advanced details

### What does the test proxy do?

The basic idea is to have a test server that sits between the client being tested and the live endpoint. 
Instead of mocking out the communication with the server, the communication can be redirected to a test server.

For example, if an operation would typically make a GET request to
`https://fakeazsdktestaccount.table.core.windows.net/Tables`, that operation should now be sent to
`https://localhost:5001/Tables` instead. The original endpoint should be stored in an `x-recording-upstream-base-uri` --
the proxy will send the original request and record the result.

The [`TestProxyManager`][test_proxy_manager] does this for you.

### How does the test proxy know when and what to record or play back?

This is achieved by making POST requests to the proxy server that say whether to start or stop recording or playing
back, as well as what test is being run.

To start recording a test, the server should be primed with a POST request:

```
URL: https://localhost:5001/record/start
headers {
    "x-recording-file": "<path-to-test>/recordings/<testfile>.<testname>"
}
```

This will return a recording ID in an `x-recording-id` header. This ID should be sent as an `x-recording-id` header in
all further requests during the test.

After the test has finished, a POST request should be sent to indicate that recording is complete:

```
URL: https://localhost:5001/record/stop
headers {
    "x-recording-id": "<x-recording-id>"
}
```

Running tests in playback follows the same pattern, except that requests will be sent to `/playback/start` and
`/playback/stop` instead. A header, `x-recording-mode`, should be set to `record` for all requests when recording and
`playback` when playing recordings back. More details can be found [here][detailed_docs].

The [`TestProxyRecordPolicy`][test_proxy_record_policy] and [`TestProxyPlaybackClient`][test_proxy_playback_client] send 
the appropriate requests at the start and end of each test case.

### Start the proxy manually
Test-proxy needs to be on the machine and in the path. Instructions for that are [here][test_proxy_installation].

For more details on proxy startup, please refer to the [proxy documentation][detailed_docs].

[asset_sync_push]: https://github.com/Azure/azure-sdk-tools/tree/main/tools/test-proxy/documentation/asset-sync#pushing-new-recordings
[custom_sanitizer_example]: https://github.com/samvaity/azure-sdk-for-java/blob/ed6c25bd9521631080fbd0c2eeeac5180fac4fe4/sdk/formrecognizer/azure-ai-formrecognizer/src/test/java/com/azure/ai/formrecognizer/documentanalysis/administration/DocumentModelAdministrationClientTestBase.java#L52-L56

[default_sanitizers]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/utils/TestProxyUtils.java#L259
[detailed_docs]: https://github.com/Azure/azure-sdk-tools/tree/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md
[default_redaction]: https://github.com/Azure/azure-sdk-for-java/blob/caf484edbb1b679243ffa960f7960ddf643362d6/sdk/core/azure-core-test/src/main/java/com/azure/core/test/utils/TestProxyUtils.java#L89-L96

[general_docs]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/README.md

[pipelines_ci]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/sdk/formrecognizer/ci.yml
[proxy_cert_docs]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/documentation/test-proxy/trusting-cert-per-language.md

[recording_migration]: https://github.com/Azure/azure-sdk-for-java/blob/main/doc/dev/recording_migration_guide.md
[RecordNetworkCallPolicy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/policy/RecordNetworkCallPolicy.java

[sanitizers]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md#session-and-test-level-transforms-sanitiziers-and-matchers

[test_proxy_migration]: https://github.com/Azure/azure-sdk-for-java/wiki/Test-Proxy-Migration
[test_proxy_sanitizer]: https://github.com/Azure/azure-sdk-for-java/blob/caf484edbb1b679243ffa960f7960ddf643362d6/sdk/core/azure-core-test/src/main/java/com/azure/core/test/models/TestProxySanitizer.java
[test_base]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/TestBase.java
[test_proxy_manager]: https://github.com/Azure/azure-sdk-for-java/blob/caf484edbb1b679243ffa960f7960ddf643362d6/sdk/core/azure-core-test/src/main/java/com/azure/core/test/utils/TestProxyManager.java
[test_proxy_record_policy]: https://github.com/Azure/azure-sdk-for-java/blob/caf484edbb1b679243ffa960f7960ddf643362d6/sdk/core/azure-core-test/src/main/java/com/azure/core/test/policy/TestProxyRecordPolicy.java
[test_proxy_playback_client]: https://github.com/Azure/azure-sdk-for-java/blob/caf484edbb1b679243ffa960f7960ddf643362d6/sdk/core/azure-core-test/src/main/java/com/azure/core/test/http/TestProxyPlaybackClient.java
[test_proxy_installation]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md#installation
[test_proxy_integration]: https://github.com/Azure/azure-sdk-for-java/pull/33902

[variables]: https://github.com/Azure/azure-sdk-tools/tree/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy#storing-variables
