# How to contribute

If you encounter any bugs with the library please file an issue in the [Issues](https://github.com/Azure/azure-sdk-for-java/issues) section of the project.

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.com/guidelines.html).



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
>
### Building and Testing

