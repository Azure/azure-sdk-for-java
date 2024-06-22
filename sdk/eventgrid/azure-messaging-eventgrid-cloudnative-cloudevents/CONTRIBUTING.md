# Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For
details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repositories using our CLA.

## Code of conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact
[opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Getting started

Before working on a contribution, it would be beneficial to familiarize yourself with the process and guidelines used
for the Azure SDKs so that your submission is consistent with the project standards and is ready to be accepted with
fewer changes requested. In particular, it is recommended to review:

- [Azure SDK README][github-general], to learn more about the overall project and processes used.
- [Azure SDK Design Guidelines][design-guidelines], to understand the general guidelines for the SDKs across all
  languages and platforms
- [Azure SDK Design Guidelines for Java][java-spec], to understand the guidelines specific to the Azure SDKs for Java.

## Development environment setup

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]
- Git
- Microsoft Azure subscription
    - You can create a free account at: https://azure.microsoft.com
- Azure Event Grid instance
    - Step-by-step guide for [creating an Event Grid using the Azure Portal][event_grid_create]. 
      Make sure the instance resource accepts CloudEvent by choosing 'Cloud Event Schema' as the event schema. 

## Building all the client libraries

Open a command prompt/terminal:
1. Execute `git clone https://github.com/Azure/azure-sdk-for-java.git`
1. Traverse to the repository root.
1. Execute `mvn compile -f pom.client.xml`
1. Install the tooling and build the product by executing:
    * `mvn install -Dinclude-non-shipping-modules -DskipTests -Dgpg.skip -f pom.client.xml`

## Building only the Azure SDK client library for Event Grid Cloud Native Cloud Events

After building the tooling and solution once from the section, [Building all the client libraries](#building-all-the-client-libraries), 
you can build just the Azure SDK client library for Event Grid Cloud Native Cloud Events by executing:
1. `mvn -f pom.xml -pl com.azure:azure-messaging-eventgrid-cloudnative-cloudevents -am clean install -DskipTests`
   
## Testing

Please ensure all tests pass with any changes and additional tests are added to exercise any new features that you've
added.

### Frameworks

We use [JUnit 5](https://junit.org/junit5/docs/current/user-guide/) as our testing framework.

[azure-core-test][core_tests] provides a set of reusable primitives that simplify writing tests for new Azure SDK libraries.

### Recorded tests

Our testing framework supports recording service requests made during a unit test enabling them to be replayed later.
You can set the `AZURE_TEST_MODE` environment variable to `PLAYBACK` to run previously recorded tests, `RECORD` to
record or re-record tests, and `LIVE` to run tests against the live service.

Properly supporting recorded tests does require a few extra considerations. All random values should be obtained via
`TestResourceName` since we use the same seed on test playback to ensure our client code generates the same "random"
values each time. You can't share any state between tests or rely on ordering because you don't know the order they'll
be recorded or replayed. Any sensitive values are redacted via the recording sanitizers.

### Running tests

The easiest way to run the tests is via your IDE's unit test runner. You can also run tests via the command line
using `mvn test`.

The recorded tests are run automatically on every pull request. Live tests are run nightly. Contributors with write
access can ask Azure DevOps to run the live tests against a pull request by commenting 
`/azp run java - eventgrid-cloudnative-cloudevents - tests` in the PR.

### Live Test Resources

Before running or recording live tests you need to create
[live test resources](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/TestResources/README.md).
If recording tests, secrets will be sanitized from saved recordings. If you will be working on contributions over time,
you should consider persisting these variables.

### Samples

Our samples are structured as test source code in `/src/samples/` so we can easily verify they're up-to-date and
compile correctly.

<!-- Links -->
[design-guidelines]: https://aka.ms/azsdk/guide
[event_grid_create]: https://docs.microsoft.com/azure/event-grid/custom-event-quickstart-portal
[github-general]: https://github.com/Azure/azure-sdk
[java-spec]: https://aka.ms/azsdk/guide/java
[maven]: https://maven.apache.org/
[core_tests]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-test
