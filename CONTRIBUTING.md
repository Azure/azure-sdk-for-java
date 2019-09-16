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
