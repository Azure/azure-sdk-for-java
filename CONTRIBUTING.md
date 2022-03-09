Azure SDK for Java Contributing Guide
-------------------------------------

Thank you for your interest in contributing to Azure SDK for Java.

- For reporting bugs, requesting features, or asking for support, please file an issue in the [issues](https://github.com/Azure/azure-sdk-for-java/issues) section of the project.

- If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](https://opensource.microsoft.com/collaborate).

- To make code changes, or contribute something new, please follow the [GitHub Forks / Pull requests model](https://help.github.com/articles/fork-a-repo/): Fork the repo, make the change and propose it back by submitting a pull request.

- Refer to the [wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Building#testing-for-spotbugs-and-checkstyle-issues) to learn about how Azure SDK for java generates CheckStyle, SpotBugs, Jacoco, and JavaDoc reports.

- There are two Maven projects in the repo. Refer to the [wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Building#pomclientxml-vs-pomdataxml) to learn about project structure for each.

Pull Requests
-------------

* **DO** submit all code changes via pull requests (PRs) rather than through a direct commit. PRs will be reviewed and potentially merged by the repo maintainers after a peer review that includes at least one maintainer.
* **DO NOT** submit "work in progress" PRs.  A PR should only be submitted when it is considered ready for review and subsequent merging by the contributor.
* **DO** give PRs short-but-descriptive names (e.g. "Improve code coverage for Azure.Core by 10%", not "Fix #1234")
* **DO** refer to any relevant issues, and include [keywords](https://help.github.com/articles/closing-issues-via-commit-messages/) that automatically close issues when the PR is merged.
* **DO** tag any users that should know about and/or review the change.
* **DO** ensure each commit successfully builds.  The entire PR must pass all tests in the Continuous Integration (CI) system before it'll be merged.
* **DO** address PR feedback in an additional commit(s) rather than amending the existing commits, and only rebase/squash them when necessary.  This makes it easier for reviewers to track changes.
* **DO** assume that ["Squash and Merge"](https://github.com/blog/2141-squash-your-commits) will be used to merge your commit unless you request otherwise in the PR.
* **DO NOT** fix merge conflicts using a merge commit. Prefer `git rebase`.
* **DO NOT** mix independent, unrelated changes in one PR. Separate real product/test code changes from larger code formatting/dead code removal changes. Separate unrelated fixes into separate PRs, especially if they are in different assemblies.

Merging Pull Requests (for project contributors with write access)
----------------------------------------------------------

* **DO** use ["Squash and Merge"](https://github.com/blog/2141-squash-your-commits) by default for individual contributions unless requested by the PR author.
  Do so, even if the PR contains only one commit. It creates a simpler history than "Create a Merge Commit".
  Reasons that PR authors may request "Merge and Commit" may include (but are not limited to):

  - The change is easier to understand as a series of focused commits. Each commit in the series must be buildable so as not to break `git bisect`.
  - Contributor is using an e-mail address other than the primary GitHub address and wants that preserved in the history. Contributor must be willing to squash
    the commits manually before acceptance.



## Developer Guide

### Pre-requisites

- Install Java Development Kit 8 or 11
  - add `JAVA_HOME` to environment variables
- Install [Maven](https://maven.apache.org/download.cgi)
  - add `MAVEN_HOME` to environment variables

>**Note:** If you ran into "long path" issue on `Windows`, enable paths longer than 260 characters by: <br><br>
1.- Run this as Administrator on a command prompt:<br> 
`REG ADD HKLM\SYSTEM\CurrentControlSet\Control\FileSystem /v LongPathsEnabled /t REG_DWORD /d 1`<br>*(might need to type `yes` to override key if it already exists)*<br><br>
2.- Set up `git` by running:<br> `git config --system core.longpaths true`

### Building and Unit Testing

Refer to the [build wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Building) for learning how to build Java SDKs
and the [unit testing wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Unit-Testing) for guidelines on unit 
testing.

### Live testing

Live tests assume a live resource has been created and appropriate environment
variables have been set for the test process. To automate setting up live
resources we use created a script called `New-TestResources.ps1` that deploys
resources for a given service.

To see what resources will be deployed for a live service, check the
`test-resources.json` ARM template files in the service you wish to deploy for
testing, for example `sdk\keyvault\test-resources.json`.

To deploy live resources for testing use the steps documented in [`Example 1 of New-TestResources.ps1`](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/TestResources/New-TestResources.ps1.md#example-1)
to set up a service principal and deploy live testing resources.

The script will provide instructions for setting environment variables before
running live tests.

To run live tests against a service after deploying live resources:

```
mvn -f sdk/keyvault/pom.xml -Dmaven.wagon.http.pool=false --batch-mode --fail-at-end --settings eng/settings.xml test
```

Some live tests may have additional steps for setting up live testing resources.
See the CONTRIBUTING.md file for the service you wish to test for additional
information or instructions.

### Workaround for Checkstyle error

When building locally you might run into a Checkstyle such as the following:

```
Execution default of goal org.apache.maven.plugins:maven-checkstyle-plugin:3.1.0:check failed:
Plugin org.apache.maven.plugins:maven-checkstyle-plugin:3.1.0 or one of its dependencies could not be resolved: 
Could not find artifact com.azure:sdk-build-tools:jar:1.0.0 in ossrh (https://oss.sonatype.org/content/repositories/snapshots/)
```

This is because the `sdk-build-tools` project isn't released to Maven. To resolve this issue you'll need to copy the `eng` folder locally then install `sdk-build-tools`.

`mvn clean install -f eng/code-quality-reports/pom.xml`

All code in the Azure SDKs for Java repository must pass Checkstyle before being merged. The `sdk-build-tools` is updated periodically, so if a new branch fails Checkstyle you'll need to reinstall.

## Versions and versioning

Tooling has been introduced to centralize versioning and help ease the pain of updating artifact versions in POM and README files. Under the eng\versioning directory there exists version text files, one for client ([version_client.txt](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/versioning/version_client.txt)) and one for data ([version_data.txt](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/versioning/version_data.txt)). The format of the version files is as follows:

`groupId:artifactId;dependency-version;current-version`

The dependency-version should be set to the most recent released version and the current-version is set to the next version to be released. For example:

`com.azure:azure-identity;1.0.0-beta.4;1.0.0-beta.5`

Note: In the case of a new artifact both versions will be the same. In the case of a released artifact, the dependecny version should be the latest released version.

### Libraries vs External Dependencies

Libraries refer to things that are built and released as part of the Azure SDK. Libraries have a current version and a dependency version.

External Dependencies refer to dependencies for things that are not built and released as part of the Azure SDK regardless of the source. External Dependencies will only ever have a dependency version.

### Current version, Dependency version, Unreleased Dependency version and Released Beta Dependency version

Current version - This is the version we should be using when defining a component in its POM file and also when dependent components are built within the same pipeline. The current version is the version currently in development.
Dependency version - This is the version we should be using when a given library is a dependency outside of a particular area. This should be the latest released version of the package whenever possible.
Unreleased Dependency version – Whenever possible, libraries should be using the latest released version for dependencies but there is the case where active development in one library is going to be needed by another library or libraries that are built in separate pipelines. These types of changes are specifically additive and not breaking. Once a library has GA’d, nothing short of breaking changes should ever force the dependency versions across the repo to an unreleased version. The reason for this is that it would prevent other libraries, that don’t need this change, from releasing. Unreleased dependcies of scope test will not prevent a library from being released.
Released Beta Dependency version – This is for when a library, which has already GA'd, is being released as a Beta version and we need to keep the dependency version to the latest GA. This particular tag will be used to allow other libraries to depend on the released Beta version. Libraries with released Beta dependencies can only be released as Beta, themselves, as a library cannot GA with Beta dependencies. An exception to the previous rule would be if the Beta dependency has a scope of test as this will not prevent a library from being released as GA.

An example of Current vs Dependency versions: `com.azure:azure-storage-blob-batch` has dependencies on `com.azure:azure-core`, `com.azure:azure-core-http-netty` and `com.azure:azure-storage-blob`. Because `com.azure:azure-core` and `com.azure:azure-core-http-netty` are both built outside of azure-storage pipeline we should be using the released or *Dependency* versions of these when they're dependencies of another library. Similarly, libraries built as part of the same pipeline, that have interdependencies, should be using the Current version. Since `com.azure:azure-storage-blob-batch` and `com.azure:azure-storage-blob` are both built part of the azure-batch pipeline when `com.azure:azure-storage-blob` is declared as a dependency of `com.azure:azure-storage-blob-batch` it should be the *Current* version.

An example of an Unreleased Dependency version: Additive, not breaking, API changes have been made to `com.azure:azure-core`. `com.azure:azure-storage-blob` has a dependency on `com.azure:azure-core` and requires the additive API change that has not yet been released. An unreleased entry needs to be created in [version_client.txt](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/versioning/version_client.txt), under the unreleased section, with the following format: `unreleased_<groupId>:<artifactId>;dependency-version`, in this example that would be `unreleased_com.azure:azure-core;1.2.0` (this should match the 'current' version of core). The dependency update tags in the pom files that required this dependency would now reference `{x-version-update;unreleased_com.azure:azure-core;dependency}`. Once the updated library has been released the unreleased dependency version should be removed and the POM file update tags should be referencing the released version.

### Tooling, version files and marker tags

All of the tooling lives under the **eng\versioning** directory.

- version_client.txt - Contains the Client library and versions
- version_data.txt - Contains Data library and versions
- external_dependencies.txt - Contains the external dependency versions
- update_versions.py - This is just a basic python script that will climb through the source tree and update POM and README files. The script utilizes tags within the files to do replacements and the tags are slightly different between the POM and README files.
- set_versions.py - This script should only be used by the build system when we start producing nightly ops builds.

In POM files this is done by inserting a specifically formatted comment on the same line as the version element.

```xml
  <groupId>MyGroup</groupId>
  <artifactId>MyArtifact</artifactId>
  <version>1.0.0-beta.1</version> <!-- {x-version-update;MyGroup:MyArtifact;[current|dependency]} -->
```

The last element of the tag would be current or dependency depending on the criteria previously explained.

In README files this ends up being slightly different. Because the version tag is inside an XML element that we're explicitly telling a user to copy/paste into their product the comment tag really didn't make sense here. Instead there are tags before and after the XML element tags which effectively says "there's a version somewhere in between these two tags, when you find the line that matches replace it with the appropriate version of the group:artifact defined in the tag."

    [//]: # ({x-version-update-start;MyGroup:MyArtifact;dependency})
    ```xml
      <groupId>MyGroup</groupId>
      <artifactId>MyArtifact</artifactId>
      <version>1.0.0-beta.1</version>
    ```
    [//]: # ({x-version-update-end})

### What does the process look like?

Let's say we've GA'd and I need to tick up the version of azure-storage libraries how would I do it? Guidelines for incrementing versions after release can be found [here](https://github.com/Azure/azure-sdk/blob/main/docs/policies/releases.md#incrementing-after-release).

1. I'd open up eng\versioning\version_client.txt and update the current-versions of the libraries that are built and released as part of the azure storage pipeline. This list can be found in pom.service.xml under the sdk/storage directory. It's worth noting that any module entry starting with "../" are external module dependencies and not something that's released as part of the pipeline. Dependencies for library components outside of a given area would be downloading the appropriate dependency from Maven like we do for external dependencies.
2. Execute the update_versions python script from the root of the enlistment. The exact syntax and commands will vary based upon what is being changed and some examples can be found in the use cases in the [update_versions.py](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/versioning/update_versions.py#L6) file.
3. Review and submit a PR with the modified files.

### Next steps: Management plane

- Management plane. Management is in the process of being moved to service pipeline builds. The versioning work needs to wait until that work is finished.

### Making changes to an already GA'd library that require other libraries to depend on the unreleased version

This is where the `unreleased_` dependency tags come into play. Using the Unreleased Dependency example above, where `com.azure:azure-storage-blob` has a dependency on an unreleased `com.azure:azure-core`:

- [ ] Make the additive changes to `com.azure:azure-core`
- [ ] In [version_client.txt](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/versioning/version_client.txt) add the entry for the unreleased azure core in the unreleased section at the bottom of the file. The entry would look like `unreleased_com.azure:azure-core;<version>`.
      Note: The version of the library referenced in the unreleased version tag should match the current version of that library.
- [ ] In the pom.xml file for `com.azure:azure-storage-blob`, the dependency tag for `com.azure:azure-core` which was originally `{x-version-update;com.azure:azure-core-test;dependency}` would now become `{x-version-update;unreleased_com.azure:azure-core-test;dependency}`
After the unreleased version of `com.azure:azure-core` was released but before `com.azure:azure-storage-blob` has been released.
- [ ] In [version_client.txt](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/versioning/version_client.txt) the the dependency version of `com.azure:azure-core` would become the released version and the "unreleased_" entry, at this time, would be removed.
- [ ] In the pom.xml file for `com.azure:azure-storage-blob`, the dependency tag for `com.azure:azure-core` would get changed back to `{x-version-update;com.azure:azure-core-test;dependency}`

## Packaging Versioning

For general packaging versioning policies see [Package Versioning](https://azure.github.io/azure-sdk/policies_releases.html#package-versioning) and see [Java](https://azure.github.io/azure-sdk/policies_releases.html#java) for specific rules used in this repo.

While some Java projects use SNAPSHOT versions for nightly builds, we have opted not to use that convention because it has proven to be very unreliable in our scenarios. For example, if we use SNAPSHOT versions in our pom.xml files that usually ends up becoming viral throughout our entire repo and we want to more tightly control our versioning, especially our dependency versioning. On top of the viral nature, we have experienced a lot of network reliability issues when consuming SNAPSHOT versions from Maven central so we want to avoid this reliability issue in our build pipelines. 

Given we don't use SNAPSHOT versions in our pom.xml files we generally have the version currently under development committed to the repo in the pom.xml file. This means if you are looking at our active development code for the version you will see a version that is not yet published to Maven central. If you want to try out our packages under development, you should look for our latest alpha build (see Dev Feed section below).

### Dev Feed

Every day our engineering system produces a set of packages for each component of the SDK. These can be used by other projects to test updated builds of our libraries prior to their release. The packages are published to an Azure Artifacts public feed hosted at the following URL:

> [https://dev.azure.com/azure-sdk/public/_packaging?_a=feed&feed=azure-sdk-for-java](https://dev.azure.com/azure-sdk/public/_packaging?_a=feed&feed=azure-sdk-for-java)

For developers working within the repo, refer to the instructions above for updating versions numbers correctly. The parent POM for the Azure SDK already contains a repository reference to the daily feed and can download the packages.

For developers wishing to use the daily packages for other purposes, refer to the [connect to feed instructions](https://dev.azure.com/azure-sdk/public/_packaging?_a=connect&feed=azure-sdk-for-java) in Azure Artifacts.

Note: the daily package feed is considered volatile and taking dependencies on a daily package should be considered a temporary arrangement. We reserve the right to remove packages from this feed at any point in time.
