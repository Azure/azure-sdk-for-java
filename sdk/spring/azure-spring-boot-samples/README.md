# Azure Spring Sample shared client library for Java

## Environment setup

### Prerequisites

- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Maven][maven] 3.0 and above
- An Internet connection
- A Windows machine (necessary if you want to run the app on Windows)
- An OS X machine (necessary if you want to run the app on Mac)
- A Linux machine (necessary if you want to run the app on Linux)
- Git
- Microsoft Azure subscription
    - You can create a free account at: https://azure.microsoft.com

### Clone source
There are two scenarios for demonstrating.

#### Clone from master branch
It means run the master branch, all the code, dependencies, and readme files are up to date.
Execute `git clone https://github.com/Azure/azure-sdk-for-java.git`.

#### Clone source from specific version tag
It means run the special release branch, all the code, dependencies, and readme files are versions of the corresponding release version.
Find out which release tag you want to use, then replace tag name and execute `git clone -b <replace-the-tag-you-want-to-use> https://github.com/Azure/azure-sdk-for-java.git`.
After cloning the code, you may be able to skip step **Compile the project** if all dependencies can be found.

>**Note:** 
There's a [link][azure_spring_release_tags] to query the `azure-spring` release tag, if there are too many returns, you can modify the query keyword value to accurately query. 

### Compile the project
1. Traverse to the root directory
1. Build the whole product by executing the following command which may take several minutes:
    * `mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Drevapi.skip=true -Djacoco.skip=true​ -DskipTests -Dparallel-test-playback`

After executing the above steps, you can build the spring project only for the developing purpose:
1. Traverse to spring directory:
    * `cd sdk\spring`
1. Build the spring project:
    * `mvn clean install -DskipTests`

### Run Sample
Reload the `pom.xml` file of the sample to update the dependencies, then you can start demonstrating.

<!-- Links -->
[maven]: https://maven.apache.org/
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_spring_release_tags]: https://github.com/Azure/azure-sdk-for-java/refs-tags/master?source_action=disambiguate&source_controller=files&tag_name=master&q=azure-spring

