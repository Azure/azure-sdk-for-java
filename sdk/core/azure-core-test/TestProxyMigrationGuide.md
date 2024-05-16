# Guide for migrating to the test proxy

This guide describes the changes that service SDKs should make to their test frameworks in order to take advantage of
the Azure SDK test proxy.

Documentation of the motivations and goals of the test proxy can be found [here][general_docs] in the azure-sdk-tools
GitHub repository and documentation of how to set up and use the proxy can be found [here][detailed_docs].

## Table of contents
- [Re-record existing test recordings](#re-record-existing-test-recordings)
    - [Perform a one-time setup](#perform-a-one-time-setup)
    - [Start the proxy server](#start-the-proxy-server)
    - [Record or playback tests](#record-or-playback-tests)
    - [Adding sanitizers](#adding-sanitizers)
- [Migrate management-plane tests](#migrate-management-plane-tests)
- [Next steps](#next-steps)
- [Advanced details](#advanced-details)
    - [What does the test proxy do?](#what-does-the-test-proxy-do)
    - [How does the test proxy know when and what to record or playback?](#how-does-the-test-proxy-know-when-and-what-to-record-or-playback)

## Re-record existing test recordings
Each SDK needs to re-record its test recordings using the test-proxy integration to ensure a consolidated recording format with serialized/sanitized requests and their matching responses.
#### Steps:
1) Run & update test recordings using the [test-proxy integration][test_proxy_integration].
   
   1.1) To use the proxy, test classes should extend from [`TestProxyTestBase`][test_proxy_base]

   ```java
   public abstract class DocumentAnalysisClientTestBase extends TestProxyTestBase {}
   ```
2) Run the tests in record mode and get the updated recordings.
3) Add [custom sanitizers][custom_sanitizer_example] if needed to address service-specific redactions.
[Default redaction][default_sanitizers] is already set up in Test Proxy for primary sanitization.

## Run tests
Test-Proxy maintains a _separate clone_ for each assets.json. The recording files will be located under your repo root under the `.assets` folder.
>>>>>>> cb044996f9bb89dd31aa8000cdd00500ce851530
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

### Perform a one-time setup

Test-proxy needs to be on the machine and in the path. Instructions for that are [here][test_proxy_installation]. 
For more details on proxy startup, please refer to the [proxy documentation][detailed_docs].

### Start the proxy server

The test proxy has to be available in order for tests to work; this is done automatically when the test is extended from
`[TestProxyTestBase]`[test_proxy_base].

The `com.azure.core.test.TestProxyTestBase#setupTestProxy()` method is responsible for starting the test proxy and
downloading if not present already.

```java
public class MyTest extends TestProxyTestBase {
    // method in TestProxyTestBase 
    @BeforeAll
    public static void setupTestProxy(TestInfo testInfo) {
        // Start the test proxy server
        testProxyManager.startProxy();
    }
}
```

The `testProxyManager.startProxy()` method will fetch the test proxy and start the test proxy.

### Record or playback tests

#### Running tests in `Playback` mode
When running tests in Playback mode, the `test-proxy` automatically checks out the appropriate tag in each local assets repo and performs testing.

#### Running tests in `Record` mode
1) After running tests in record mode, the newly updated recordings no longer be in the azure-sdk-for-java repo. 
   These updates will be reflected in a git-excluded `.assets folder` at the root of the repo.
2) You can `cd` into the folder containing your package's recordings and use `git status` to view the recording updates.
   Verify the updates, and use the following command to push these recordings to the `azure-sdk-assets` repo:

    ```ps
        C:/repo/sdk-for-java/>test-proxy push -a <path-to-assets-json-file>
    ```
    
3) The above command will push the updated recordings to the `azure-sdk-assets` repo.
How to set up and use the proxy can be found [here](https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md#installation).

### Adding sanitizers
Since the test proxy doesn't use [`RecordNetworkCallPolicy`][RecordNetworkCallPolicy], tests don't use the `RecordingRedactor` to sanitize values in recordings.
Instead, sanitizers (as well as matchers) can be registered on the proxy as detailed in
[this][sanitizers] section of the proxy documentation. Custom sanitizers can be registered using [`TestProxySanitizer`][test_proxy_sanitizer] respective SDK test classes.
[`Default sanitizers`][default_sanitizers], similar to the use of the `RecordingRedactor` are registered in the `TestProxyUtils`. 

For example, registering a custom sanitizer for redacting the value of JSON key `modelId` from the response body looks like the following:
```java readme-sample-add-sanitizer-matcher

List<TestProxySanitizer> customSanitizer = new ArrayList<>();
// sanitize value for key: "modelId" in response json body
customSanitizer.add(
    new TestProxySanitizer("$..modelId", "REPLACEMENT_TEXT", TestProxySanitizerType.BODY_KEY));

if (interceptorManager.isRecordMode()) {
    // Add a policy to record network calls.
    pipelineBuilder.policies(interceptorManager.getRecordPolicy());
}
if (interceptorManager.isPlaybackMode()) {
    // Use a playback client when running in playback mode
    pipelineBuilder.httpClient(interceptorManager.getPlaybackClient());
    // Add matchers only in playback mode
    interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
        .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-client-request-id"))));
}
if (!interceptorManager.isLiveMode()) {
    // Add sanitizers when running in playback or record mode
    interceptorManager.addSanitizers(customSanitizer);
}
```

For a more advanced scenario, where we want to sanitize the account names of all Tables endpoints in recordings, we
could instead use the `BODY_REGEX` sanitizer type:

```java
customSanitizer.add(new TestProxySanitizer("(?<=\\/\\/)[a-z]+(?=(?:|-secondary)\\.table\\.core\\.windows\\.net)", "REDACTED", TestProxySanitizerType.URI));
```
In the snippet above, any storage endpoint URIs that match the specified URL regex will have their account name replaced with
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

The [`TestProxyManager`][test_proxy_manager] does this for you.

### How does the test proxy know when and what to record or playback?

[TestProxyTestBase][test_proxy_base] provides a way to start and stop recording and playback for tests.
This is achieved by making POST requests to the proxy server that say whether to start or stop recording or playing
back, as well as what test is being run.

The [`TestProxyRecordPolicy`][test_proxy_record_policy] and [`TestProxyPlaybackClient`][test_proxy_playback_client] send 
the appropriate requests at the start and end of each test case.

More details can be found [here][detailed_docs].

[asset_sync_push]: https://github.com/Azure/azure-sdk-tools/tree/main/tools/test-proxy/documentation/asset-sync#pushing-new-recordings
[custom_sanitizer_example]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/test/java/com/azure/ai/formrecognizer/documentanalysis/TestUtils.java#L293

[default_sanitizers]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/utils/TestProxyUtils.java#L259
[detailed_docs]: https://github.com/Azure/azure-sdk-tools/tree/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md

[general_docs]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md

[recording_migration]: https://github.com/Azure/azure-sdk-for-java/blob/64de460d8080127a1e0c58fbfc7ab9e95f70a2c7/sdk/core/azure-core-test/RecordingMigrationGuide.md
[RecordNetworkCallPolicy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/policy/RecordNetworkCallPolicy.java

[sanitizers]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md#session-and-test-level-transforms-sanitiziers-and-matchers

[test_proxy_migration]: https://github.com/Azure/azure-sdk-for-java/wiki/Test-Proxy-Migration#3-using-test-proxy-going-forward
[test_proxy_sanitizer]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/models/TestProxySanitizer.java
[test_proxy_manager]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/utils/TestProxyManager.java
[test_proxy_base]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/TestProxyTestBase.java
[test_proxy_record_policy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/policy/TestProxyRecordPolicy.java
[test_proxy_playback_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/http/TestProxyPlaybackClient.java
[test_proxy_installation]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md#installation
[test_proxy_integration]: https://github.com/Azure/azure-sdk-for-java/pull/33902
[test_proxy_matcher]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/models/TestProxyRequestMatcher.java
