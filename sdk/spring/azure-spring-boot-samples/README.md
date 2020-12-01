# Azure Spring Sample shared client library for Java

## Ready to run checklist
### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Maven][maven] 3.0 or above
- A Windows machine (necessary if you want to run the app on Windows)
- An OS X machine (necessary if you want to run the app on Mac)
- A Linux machine (necessary if you want to run the app on Linux)
- Git

### Clone source
Recommend users to clone specific version tag, the difference form cloning master branch is that there's no need to install local dependencies of the whole project, and all the dependencies can be downloaded from the Maven repository.

#### Clone from master branch
It means cloning the master branch, all the code, dependencies, and readme files are up to date.

```shell script
git clone https://github.com/Azure/azure-sdk-for-java.git
```

#### Clone from specific version tag
It means cloning the special release tag, all the code, dependencies, and readme files are versions of the corresponding release version.
- Use this [link][azure_spring_release_tags] to query the *azure-spring* release tag you want to use. If there are too many returns, you can modify the query keyword value to accurately query.
- Replace the tag name in the following command and execute.
    
    ```shell script
    git clone -b <replace-the-tag> https://github.com/Azure/azure-sdk-for-java.git
    ```
    
- After cloning the code, you may be able to skip step **Compile the project** if all dependencies can be found.

### Install local dependencies
1. Traverse to the root directory
1. Build the whole product by executing the following command which may take several minutes:
   
   ```shell script
   mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Drevapi.skip=true -Djacoco.skip=true​ -DskipTests -Dparallel-test-playback
   ```

### Update maven dependencies
Reload the `pom.xml` file of the sample to update the dependencies.

<!-- Links -->
[maven]: https://maven.apache.org/
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_spring_release_tags]: https://github.com/Azure/azure-sdk-for-java/refs-tags/master?source_action=disambiguate&source_controller=files&tag_name=master&q=azure-spring
