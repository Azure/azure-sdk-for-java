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

## Code style

Please [import](https://www.jetbrains.com/help/idea/copying-code-style-settings.html) `AzureSpringCodeStyle.xml` to your Intellij Idea to format your code.

## Getting started

Before working on a contribution, it would be beneficial to familiarize yourself with the project so that your
submission is consistent with the project standards and is ready to be accepted with fewer changes requested. 

- [Azure Spring Boot README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/README.md), to learn more about the overall project and processes used.

## Development environment setup

### Prerequisites

- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Maven][maven]
- Git
- Microsoft Azure subscription
    - You can create a free account at: https://azure.microsoft.com

## Building from source

If it's the first time you try to build the project or you pull new commits from github, then you need to build the whole SDK project with the below command:
1. Execute `git clone https://github.com/Azure/azure-sdk-for-java.git`
1. Traverse to the root directory
1. Build the whole product by executing the following command which may take several minutes:
    * `mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Drevapi.skip=true -Djacoco.skip=true -DskipTests -Dparallel-test-playback`

After executing the above steps, you can build the spring project only for the developing purpose:
1. Traverse to spring directory:
    * `cd sdk\spring`
1. Build the spring project:
    * `mvn clean install -DskipTests`


## Running tests

After following instructions above, you can run the
unit tests by executing: 
```shell
mvn test
```

For unit tests, there are no special considerations; these are self-contained and execute locally without any reliance
on external resources. These tests are run for all PR validations.


## Version management
Developing version naming convention is like `0.1.2-beta.1`. Release version naming convention is like `0.1.2`. 

## Contribute to code
Contribution is welcome. Please follow
[this instruction](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md) to contribute code.

<!-- Links -->
[maven]: https://maven.apache.org/
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
