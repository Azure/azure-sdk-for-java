# Azure Core Test shared library for Java

This library contains core classes used to test Azure SDK client libraries.

Newer SDK tests utilize the [Azure SDK Tools Test Proxy][test-proxy-readme] to record and playback HTTP interactions.
To migrate from existing [TestBase][TestBase.java] to use the test proxy, or to learn more about using the test proxy,
refer to the [test proxy migration guide][test-proxy-migration-guide].

## Table of contents

- [Getting started](#getting-started)
- [Key concepts](#key-concepts)
- [Write or run tests](#write-or-run-tests)
    - [Set up test resources](#set-up-test-resources)
    - [Configure credentials](#configure-credentials)
    - [Start the test proxy server](#start-the-test-proxy-server)
    - [Write your tests](#write-your-tests)
    - [Configure live or playback testing mode](#configure-live-or-playback-testing-mode)
    - [Run and record tests](#run-and-record-tests)
    - [Run tests with out-of-repo recordings](#run-tests-with-out-of-repo-recordings)
    - [Sanitize secrets](#sanitize-secrets)
      - [Customizing what gets recorded](#customizing-what-gets-recorded)
- [Troubleshooting](#troubleshooting)
- [Next steps](#next-steps)
- [Contributing](#contributing)

## Getting started

To use this package, add the following to your _pom.xml_.

[//]: # ({x-version-update-start;com.azure:azure-core-test;current})

```xml

<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-test</artifactId>
  <version>1.20.0</version>
</dependency>
```

[//]: # ({x-version-update-end})

## Key concepts

* Run tests in `Record` mode:
  To record means to intercept any HTTP request, store it in a file, then store the response received from the live
  resource that was originally targeted.
* Run tests in `Playback` mode:
  To playback means to intercept any HTTP request and to respond  to it with the stored response of a previously recorded
  matching request.
* Run tests in `Live` mode:
  To run live means to not intercept any HTTP request and to send them to the Azure service directly.
* Sanitize sensitive information:
  Sensitive information means content like passwords, unique identifiers or personal information should be cleaned up
  from the recordings.
* [TestProxyTestBase][TestProxyTestBase.java]: Base test class that creates an `InterceptorManager` and enables the test
  to use [`test-proxy`][test-proxy-readme] for running the test. It either plays back test session data or records the
  test session.
* [InterceptorManager][InterceptorManager.java]: A class that keeps track of network calls by either reading the data
  from an existing test session record or recording the network calls in memory. Test session records are saved or read
  from "<i>.assets/{library-level}/src/test/resources/session-records/TestFileName.testName}.json</i>".
* [TestProxyRecordPolicy][TestProxyRecordPolicy.java]: Pipeline policy that records network calls using
  the [`test-proxy`][test-proxy-readme].
* [TestProxyPlaybackClient][TestProxyPlaybackClient.java]: HTTP client that plays back responses from the recorded data
  of session-record using test proxy.

## Write or run tests

### Set up test resources

Live Azure resources will be necessary in order to run live tests and produce recordings.

If you haven't yet set up a `test-resources.json` file for test resource deployment and/or want to use test resources of
your own, you can just configure credentials to target these resources instead.

To create a `test-resources.json` file:

1. Create an Azure Resource Management Template for your specific service and the configuration you need. This can be
   done in the [Portal][azure_portal] by creating a resource, and at the very last step (Review + Create), clicking
   "Download a template for automation".
2. Save this template to a `test-resources.json` file under the directory that contains your package
   (`sdk/<my-service>/test-resources.json`) file. You can refer to
   [Table's][tables-test-resources] as an example.
3. Add templates for any additional resources in a grouped `"resources"` section of `test-resources.json`
   ([example][tables-test-resources-resources]).
4. Add an `"outputs"` section to `test-resources.json` that describes any environment variables necessary for accessing
   these resources ([example][tables-test-resources-outputs]).

### Configure credentials

Java SDK tests uses `EnvironmentVariables` to store test credentials.

If using a `New-TestResources` script from [/eng/common/TestResources][test-resources], the script should output any
environment variables necessary to run live tests for the service. After storing these variables in your local
environment
variables -- with appropriate formatting -- your credentials and test configuration variables will be set in your
environment when running tests.

If your service doesn't have a `test-resources.json` file for test deployment, you'll need to set environment variables
for `AZURE_SUBSCRIPTION_ID`, `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, and `AZURE_CLIENT_SECRET` at minimum.

1. Set the `AZURE_SUBSCRIPTION_ID` variable to your organization's subscription ID. You can find it in the "Overview"
   section of the "Subscriptions" blade in the [Azure Portal][azure_portal].
2. Define the `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, and `AZURE_CLIENT_SECRET` of a test service principal. If you do not
   have a service principal, use the Azure CLI's [az ad sp create-for-rbac][azure_cli_service_principal] command (
   ideally,
   using your alias as the service principal's name prefix):

```
az login
az ad sp create-for-rbac --name "{your alias}-tests" --role Contributor
```

The command will output a set of credentials. Set values for `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`,
and `AZURE_CLIENT_SECRET` in your environment variables.

### Start the test proxy server

The test proxy has to be available in order for tests to work; this is done automatically when the test is extended from
`TestProxyTestBase`.
The `com.azure.core.test.TestProxyTestBase#setupTestProxy()` method is responsible for starting test proxy.

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

For more details about how this starts up the test proxy, or the test proxy itself, refer to the
[test proxy migration guide][test-proxy-migration-guide].

## Write your tests

Each of the SDKs should include client sync and async testing in their `tests` directory (`sdk/{service}/{package}/tests`)
with the naming pattern `{ServiceName}ClientTest.java` and `{ServiceName}AsyncClientTest.java`.
The `{ServiceName}ClientTest` will be responsible for testing the synchronous client, and
the `{ServiceName}AsyncClientTest` will be responsible for testing the asynchronous client. 
The `{ServiceName}ClientTest` and the `{ServiceName}AsyncClientTest` both will extend the `{ServiceName}ClientTestBase` 
which then extends the `TestProxyTestBase` class.
The `{ServiceName}ClientTestBase` will be responsible for initializing the clients, preparing test data, registering
sanitizers/matchers etc. (in this example we use Tables SDK for the sake of demonstration):

```java readme-sample-createATestClass

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * record. By default, tests are run in playback mode.
 */
public static class ClientTests extends TestProxyTestBase {

    /**
     * Use JUnit annotation here for your testcase
     */
    public void testMethodName() {
        HttpPipelineBuilder pipelineBuilder = new HttpPipelineBuilder();
        if (interceptorManager.isRecordMode()) {
            // Add a policy to record network calls.
            pipelineBuilder.policies(interceptorManager.getRecordPolicy());
        }
        if (interceptorManager.isPlaybackMode()) {
            // Use a playback client when running in playback mode
            pipelineBuilder.httpClient(interceptorManager.getPlaybackClient());
        }

        Mono<HttpResponse> response =
            pipelineBuilder.build().send(new HttpRequest(HttpMethod.GET, "http://bing.com"));

        // Validate test results.
        assertEquals(200, response.block().getStatusCode());
    }
```

## Configure live or playback testing mode

"Live" tests refer to tests that make requests to actual Azure resources. "Playback" tests require a recording for each
test; the test proxy will compare the requests/responses that would be made during each test with requests/responses in
the recording.

To run live tests, set the environment variable `AZURE_TEST_MODE` to `LIVE`.
To run tests in playback, set `AZURE_TEST_MODE` to `PLAYBACK` or leave it unset.

## Run and record tests

Set the environment variable `AZURE_TEST_MODE` to `RECORD` to run your test(s) in record mode.

After tests finish running, there should be a folder called `src/test/resources/session-records` in your package
directory.
Each recording in this folder will be a `.json` file that captures the HTTP traffic that was
generated while running the test matching the file's name. If you set the `AZURE_TEST_MODE` environment variable to "
PLAYBACK"
and re-run tests, they should pass again -- this time, in playback mode (i.e. without making actual HTTP requests, using
the recorded data from json recording file).

### Run tests with out-of-repo recordings

If the package being tested stores its recordings outside the `azure-sdk-for-java` repository -- i.e. the
[recording migration guide][recording-migration] has been followed and the package contains an `assets.json` file --
there won't be a `src/test/resources/session-records` folder in the `tests` directory. Instead, 
the package's `assets.json` file will point to a tag in the `azure-sdk-assets` repository that contains the recordings. 
This is the preferred recording configuration.

Running live or playback tests is the same in this configuration as it was in the previous section. The only changes are
to the process of updating recordings.

### Update test recordings

#### Prerequisites

- The targeted library is already migrated to use the test proxy.
- Git version > 2.30.0 is to on the machine and in the path. Git is used by the script and test proxy.
- Global [git config settings][git_setup] are configured for `user.name` and `user.email`.
    - These settings are also set with environment variables `GIT_COMMIT_OWNER` and `GIT_COMMIT_EMAIL`, respectively (in
      your environment or your local `.env` file).
- Membership in the `azure-sdk-write` GitHub group.

Test recordings will be updated if tests are run and the environment variable `AZURE_TEST_MODE` is set to `RECORD`.
Since the recordings themselves are no longer in the `azure-sdk-for-java` repo, though, these updates will be reflected
in a git-excluded `.assets` folder at the root of the repo.

The `.assets` folder contains one or more directories with random names, which each are a git directory containing
recordings. If you `cd` into the folder containing your package's recordings, you can use `git status` to view the
recording updates you've made. You can also use other `git` commands; for example, `git diff {file name}` to see
specific file changes, or `git restore {file name}` to undo changes you don't want to keep.

To find the directory containing your package's recordings, open the `.breadcrumb` file in the `.assets` folder. This
file lists a package name on each line, followed by the recording directory name; for example:

```
sdk/{service}/{package}/assets.json;2Wm2Z87545;java/{service}/{package}_<10-character-commit-SHA>
```

The recording directory in this case is `2Wm2Z8745`, the string between the two semicolons.

After verifying that your recording updates look correct, you can use the `test-proxy push -a assets.json` command
to push these recordings to the `azure-sdk-assets` repo. This command should be provided a **relative** path to your
package's `assets.json` file. For example, from the root of the `azure-sdk-for-java` repo:

```
test-proxy push -a sdk/{service}/{package}/assets.json
```

The verbs that can be provided to this script are "push", "restore", and "reset":

- **push**: pushes recording updates to a new assets repo tag and updates the tag pointer in `assets.json`.
- **restore**: fetches recordings from the assets repo, based on the tag pointer in `assets.json`.
- **reset**: discards any pending changes to recordings, based on the tag pointer in `assets.json`.

After pushing your recordings, the `assets.json` file for your package will be updated to point to a new `Tag` that
contains the updates. Include this `assets.json` update in any pull request to update the recordings pointer in the
upstream repo.

### Sanitize secrets

The `.json` files created from running tests in record mode can include authorization details, account names, shared
access signatures, and other secrets. The recordings are included in our public GitHub repository, making it important
for us to remove any secrets from these recordings before committing them to the repository.

There are two primary ways to keep secrets from being written into recordings:

1. [Default sanitizers][default_sanitizers], similar to the use of the RecordingRedactor are already registered in the
   TestProxyUtils for default redactions.
2. Custom sanitizers can be added
   using `[TestProxySanitizer]`[test_proxy_sanitizer] & `interceptorManager.addSanitizers()` method for addressing
   specific service sanitization needs.
   For example, registering a custom sanitizer for redacting the value of json key modelId from the response body looks
   like the following:.
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

Note: Sanitizers must only be added once the playback client or record policy is registered.
Look at the [TableClientTestBase][TableClientTestBase] class for example.

Detailed information about the sanitizers supported by Test Proxy can be found [here][sanitize-secrets].

#### Customizing what gets recorded
Some tests send large request bodies that are not meaningful and should not be stored in the session records. 
In order to disable storing the request body for a specific request, add the `RecordWithoutRequestBody` annotation to 
the test method.

## Examples

## Troubleshooting

If you encounter any bugs with these SDKs, please file issues via
[Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout
[StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Next steps

Other useful packages are:

* [azure-core](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core): Contains core classes and
  functionality used by all client libraries.

## Contributing

For details on contributing to this repository, see the [contributing guide][cg].

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit <https://cla.microsoft.com>.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact <opencode@microsoft.com> with any additional questions or comments.

[azure_portal]: https://portal.azure.com/
[azure_cli_service_principal]: https://docs.microsoft.com/cli/azure/ad/sp?view=azure-cli-latest#az-ad-sp-create-for-rbac
[default_sanitizers]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/utils/TestProxyUtils.java#L259
[git_setup]: https://git-scm.com/book/en/v2/Getting-Started-First-Time-Git-Setup
[git_token]: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token
[powershell]: https://learn.microsoft.com/powershell/scripting/install/installing-powershell?view=powershell-latest
[recording-migration]: https://github.com/Azure/azure-sdk-for-java/blob/64de460d8080127a1e0c58fbfc7ab9e95f70a2c7/sdk/core/azure-core-test/RecordingMigrationGuide.md
[sanitize-secrets]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md#session-and-test-level-transforms-sanitizers-and-matchers
[test-proxy-readme]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md
[test-proxy-migration-guide]: https://github.com/Azure/azure-sdk-for-java/blob/64de460d8080127a1e0c58fbfc7ab9e95f70a2c7/sdk/core/azure-core-test/TestProxyMigrationGuide.md
[InterceptorManager.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/InterceptorManager.java
[TestProxyPlaybackClient.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/http/TestProxyPlaybackClient.java
[TestProxyRecordPolicy.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/policy/TestProxyRecordPolicy.java
[TestBase.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/TestProxyTestBase.java
[TestProxyTestBase.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/TestProxyTestBase.java
[TableClientTestBase]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/src/test/java/com/azure/data/tables/TableClientTestBase.java#L61
[tables-test-resources]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/test-resources.json
[tables-test-resources-resources]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/test-resources.json#L42
[tables-test-resources-outputs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/test-resources.json#L115
[test-resources]: https://github.com/Azure/azure-sdk-for-java/tree/main/eng/common/TestResources#readme
[test_proxy_sanitizer]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/models/TestProxySanitizer.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core-test%2FREADME.png)
