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
- Azure Event Hubs instance
    - Step-by-step guide for [creating an Event Hub using the Azure Portal][event_hubs_create]

## Building all the client libraries

Open a command prompt/terminal:
1. Execute `git clone https://github.com/Azure/azure-sdk-for-java.git`
1. Traverse to the repository root.
1. Execute `mvn compile -f pom.client.xml`
1. Install the tooling and build the product by executing:
    * `mvn install -Dinclude-non-shipping-modules -DskipTests -Dgpg.skip -f pom.client.xml`

## Building only the Azure SDK client library for Event Hubs

After building the tooling and solution once from the section, [Building all the client libraries](#building-all-the-client-libraries), you can build just the Azure SDK client library for Event Hubs by
executing:
1. `mvn compile -f eventhubs\client\pom.xml`

## Running tests

After following instructions in [Building all the client libraries](#building-all-the-client-libraries), you can run the
unit tests by executing:
1. `mvn test -f eventhubs\client\pom.xml`

For unit tests, there are no special considerations; these are self-contained and execute locally without any reliance
on external resources. These tests are run for all PR validations.

### Running integration tests

Integration tests have dependencies on live Azure resources and require setting up your development environment prior
to running. Known in the Azure SDK project commonly as "Live" tests, these tests only run when the
[`TestMode`][test-mode] is equal to [`TestMode.RECORD`][test-mode-record]. The Live tests read information from the
following environment variables:

- AZURE_TEST_MODE
    - Can be either "RECORD" or "PLAYBACK". If AZURE_TEST_MODE is equal to "RECORD", then the tests will run against the
      live service.
- AZURE_EVENT_HUBS_CONNECTION_STRING
    - The connection string to the Azure Event Hub instance. This is required when running tests in RECORD mode. It must
      contain the name of the Event Hub instance (the `EntityPath=` component must be in the connection string).

## Logging output

Log messages can be seen in the output window by:
1. Setting `AZURE_LOG_LEVEL` to the desired verbosity. Log levels can be found in [ClientLogger][log-level]
1. Adding an implementation of [slf4j][slf4j] to the classpath. Implementations can be found under section "[Binding with a
   logging framework at deployment time][slf4j-implementations]".

<!-- Links -->
[design-guidelines]: https://azuresdkspecs.z5.web.core.windows.net/DesignGuidelines.html
[event_hubs_create]: https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-create
[github-general]: https://github.com/Azure/azure-sdk
[java-spec]: https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html
[log-level]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java#L40
[maven]: https://maven.apache.org/
[slf4j]: https://www.slf4j.org/
[slf4j-implementations]: https://www.slf4j.org/manual.html#swapping
[test-mode]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core-test/src/main/java/com/azure/core/test/TestMode.java
[test-mode-record]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core-test/src/main/java/com/azure/core/test/TestMode.java#L12
