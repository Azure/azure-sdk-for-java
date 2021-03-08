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
If you just want to run sample code with released version(not current), instead of cloning the project from master branch, you can clone project code from specific version tag.

- Clone from master branch  
  For master branch, the version number in dependencies is up to date, which means if you want to run a sample module, you may need to build the dependencies and install the module to you local maven repo manually, since the dependencies has not been published to maven central.  
  If you want to clone from master, jump to [Clone project from master branch](#clone-project-from-master-branch).

- Clone from released version tag
  For released version tag, the dependencies should have published to maven central, which means you can run sample code directly after necessary configuration.  
  If you want to clone from released version tag, jump to [Clone from specific version tag](#clone-from-specific-version-tag-recommend).

#### Clone project from master branch
It means cloning the master branch, all the code, dependencies, and readme files are up to date.

1. Clone code to local repo 

   ```
   git clone https://github.com/Azure/azure-sdk-for-java.git
   ```

2. Traverse to the root directory

3. Build the whole product by executing the following command which may take several minutes

   ```
   mvn clean install -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true -Drevapi.skip=true -Djacoco.skip=true -DskipTests -Dparallel-test-playback
   ```


#### Clone from specific version tag (recommend)

- Use this [link][azure_spring_release_tags] to query the *azure-spring* release tag you want to use. If there are too many returns, you can modify the query keyword value to accurately query.
- Replace the tag name in the following command and execute.
  
    ```shell script
    git clone -b <replace-the-tag> https://github.com/Azure/azure-sdk-for-java.git
    ```
    

<!-- Links -->
[maven]: https://maven.apache.org/
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_spring_release_tags]: https://github.com/Azure/azure-sdk-for-java/refs-tags/master?source_action=disambiguate&source_controller=files&tag_name=master&q=azure-spring
