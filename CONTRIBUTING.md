# How to contribute

Thank you for your interest in contributing to Azure SDK for Java.

## About Azure SDK for java project

- For reporting bugs, requesting features or asking for support, please file an issue in the [issues](https://github.com/Azure/azure-sdk-for-java/issues) section of the project.

- If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.com/guidelines.html).

- To make code changes, or contribute something new, please follow the [GitHub Forks / Pull requests model](https://help.github.com/articles/fork-a-repo/): Fork the repo, make the change and propose it back by submitting a pull request.

- Refer to the [wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Building#testing-for-spotbugs-and-checkstyle-issues) to learn about how Azure SDK for java generates CheckStyle, SpotBugs, Jacoco, and JavaDoc reports.

- There are two Maven projects in the repo. Refer to the [wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Building#pomclientxml-vs-pomdataxml) to learn about project structure for each.

## Developer Guide

### Pre-requisites
- Install Java Development Kit 8
  - add `JAVA_HOME` to environment variables
- Install [Maven](http://maven.apache.org/download.cgi)
  - add `MAVEN_HOME` to environment variables


>**Note:** If you are on `Windows`, enable paths longer than 260 characters by: <br><br>
1.- Run this as Administrator on a command prompt:<br> 
`REG ADD HKLM\SYSTEM\CurrentControlSet\Control\FileSystem /v LongPathsEnabled /t REG_DWORD /d 1`<br>*(might need to type `yes` to override key if it already exists)*<br><br>
2.- Set up `git` by running:<br> `git config --system core.longpaths true`

### Building and Testing
The easiest way to build is by running the following command from the root folder:
```
mvn -f pom.client.xml -Dgpg.skip -DskipTests clean install
```
- `-f pom.client.xml`: tells maven to target latest Azure SDK for Java project.
- `-Dgpg.skip`: disables [gpg](https://mran.microsoft.com/snapshot/2016-12-19/web/packages/gpg/vignettes/intro.html) signing.
- `-DskipTests:` Building without running unit tests would speed operation up, however, make sure all tests pass before creating a new PR.
- `clean:` will remove any previous generated output.
- `install:`  compiles project and installs it in the local Maven cache.

>**Note**: Refer to [wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Building) for learning about how to build using Java 11

### Compiling one project only
```
mvn -f sdk/{projectForlderDir}/pom.xml -Dgpg.skip clean install

//example: mvn -f sdk/keyvault/azure-keyvault-keys/pom.xml clean install
```

## Versions and versioning
Tooling has been introduced to centralize versioning and help ease the pain of updating artifact versions in POM and README files. Under the eng\versioning directory there version text files, one for client (version_client.txt) and one for data (version_data.txt)*. The format of the version files is as follows:
groupId:artifactId;dependency-version;current-version
The dependency-version should be set to the most recent released version and the current-version is set to the next version to be released. 
For example:
com.azure:azure-identity;1.0.0-preview.4;1.0.0-preview.5
Note: In the case of a new or unreleased artifact both versions will be the same.

### Libraries vs External Dependencies
Libraries refer to things that are built and released as part of the Azure SDK. Libraries have a current version and a dependency version. 

External Dependencies refer to dependencies for things that are not built and released as part of the Azure SDK regardless of the source. External Dependencies will only ever have one version.

### Current version vs Depdendency version
Current version - This is the version we should be using when defining a component in its POM file and also when dependent components are built within the same pipeline. The current version is the version currently in development.
Depdendency version - This is the version we should be using when a given library is a dependency outside of a particular area. This should be the latest released version of the package.

For example: com.azure:azure-storage-blob-batch has dependencies on com.azure:azure-core, com.azure:azure-core-http-netty and com.azure:azure-storage-blob. Because com.azure:azure-core and com.azure:azure-core-http-netty are both built outside of azure-storage pipeline we should be using the released or Dependency versions of these when they're dependencies of another library. Similarly, libraries built as part of the same pipeline, that have interdependencies, should be using the Current version. Since com.azure:azure-storage-blob-batch and com.azure:azure-storage-blob are both built part of the azure-batch pipeline when com.azure:azure-storage-blob is declared as a dependency of com.azure:azure-storage-blob-batch it should be the Current version.

This is going to be especially important after GA when releases aren't going to be the entire Azure SDK every time. If we're releasing a patch for a targeted azure-storage fix then we shouldn't need to build and release azure-core, we should be targeting the released versions and only building/releasing that update to azure-storage. It's worth noting that right now, in the version_client.txt, the dependency/current versions are the same. This will change once we GA, at which point the current version should be ahead of the dependency version.

What about README files? Right now the README files in the repro end up getting into an odd state since things like samples and versions can get updated during the development process. We're in the process of versioning documentation with the releases which means that the docs are snapshot at the time of the release and then versioned and stored. This will allow the README files in the repro to have updated samples and versions that are setup for the next release.

### Tooling, version files and marker tags
All of the tooling lives under the eng\versioning directory.
version_client.txt - Contains the Client library and versions
version_data.txt - Contains Data library and versions
update_versions.py - This is just a basic python script that will climb through the source tree and update POM and README files. The script utilizies tags within the files to do replacements and the tags are slightly different between the POM and README files.
set_versions.py - This script should only be used by the build system when we start producing nightly ops builds. 

In POM files this is done by inserting a specifically formatted comment on the same line as the version element.
```xml
  <groupId>MyGroup</groupId>
  <artifactId>MyArtifact</artifactId>
  <version>1.0.0-preview.1</version> <!-- {x-version-update;MyGroup:MyArtifact;[current|dependency]} -->
```
The last element of the tag would be current or dependency depending on the criteria previously explained.

In README files this ends up being slightly different. Because the version tag is inside an XML element that we've explicitly telling a user to copy/paste into their product the comment tag really didn't make sense here. Instead there are tags before and after the XML element tags which effectively says "there's a version somewhere in between these two tags, when you find the line that matches replace it with the appropriate version of the group:artifact defined in the tag."

\[//]: # ({x-version-update-start;MyGroup:MyArtifact;dependency})
\```xml
  <groupId>MyGroup</groupId>
  <artifactId>MyArtifact</artifactId>
  <version>1.0.0-preview.1</version>
\```
\[//]: # ({x-version-update-end})


What if I've got something that, for whatever reason, shoudln't be updated? There's a tag for that
<!-- {x-version-exempt;<groupId>:<artifactId>;reason for excemption} -->
In theory, absence of an x-version-update tag would do the same thing but the tooling is still being developed and eventually there will be checkin blockers if xml has a version element with no tag.


### What does the process looks like?
Let's say we've GA'd and I need to tick up the version of azure-storage libraries how would I do it? 
1. I'd open up eng\versioning\version_client.txt and update the current-versions of the libraries are built and released as part of the azure storage pipeline. This list can be found in pom.service.xml under the sdk/storage directory. It's worth noting that any module entry starting with "../" isn't something that's released as part of the pipeline and once we GA these build dependencies for library components outside of a given area should go away and be replaced with downloading the appropriate dependency from Maven like we do for exxternal dependencies.
2. Execute the update_versions python script
python eng/versioning/update_versions.py --ut libary --bt client
This will go through the entire source tree and update all of the references in the POM and README files with the updated versions. Git status will show all of the modified files.
3. Review and submit a PR with the modified files.

### Next steps: External dependencies, Management plane and service pipeline changes
- External dependencies. Right now only there are only version files for client and data (eng\versioning\version_\[client|data\].txt) which only encompass the built binaries for their respective tracks. External dependencies for both client and data are next on the list which should allow modification of the parent/pom.xml to remove the list of version properties and dependency management sections which brings things one step closer to not having to publish the parent pom.
- Management plane. Management is in the process of being moved to service pipeline builds. The versioning work needs to wait until that work is finished.
- Service pipeline changes. The service pipelines currently have to build not only the libraries that are part of that pipeline but also the Azure SDK libraries that are dependencies. Once we GA and can start targeting the released version of those packages and pulling them from Maven instead of building them. An good example of this would be in sdk/appconfiguration/pom.service.xml where to build azure-data-appconfiguration we end up building azure-core, azure-core-test and azure-core-http-netty along with azure-data-appconfiguration instead of just building azure-data-appconfiguration.


*Management is TDB, pending changes to move to service pipelines.