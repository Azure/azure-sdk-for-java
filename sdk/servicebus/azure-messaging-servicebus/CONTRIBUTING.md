# Contributing (for `azure-messaging-servicebus`)

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For
details, visit [cla.microsoft.com](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repositories using our CLA.

## Code of conduct
This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/)
or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

##Pull Requests

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

## Developer Guide
### Getting Started
Before working on a contribution, it would be beneficial to familiarize yourself with the process and guidelines used
for the Azure SDKs so that your submission is consistent with the project standards and is ready to be accepted with
fewer changes requested. In particular, it is recommended to review:

- [Azure SDK README][github-general], to learn more about the overall project and processes used.
- [Azure SDK Design Guidelines][design-guidelines], to understand the general guidelines for the SDKs across all
  languages and platforms
- [Azure SDK Design Guidelines for Java][java-spec], to understand the guidelines specific to the Azure SDKs for Java.

### Pre-requisites

- Install Java Development Kit 8 or 11
    - add `JAVA_HOME` to environment variables
- Install [Maven][maven]
    - add `MAVEN_HOME` to environment variables

>**Note:** If you ran into "long path" issue on `Windows`, enable paths longer than 260 characters by: <br><br>
1.- Run this as Administrator on a command prompt:<br>
`REG ADD HKLM\SYSTEM\CurrentControlSet\Control\FileSystem /v LongPathsEnabled /t REG_DWORD /d 1`<br>*(might need to type `yes` to override key if it already exists)*<br><br>
2.- Set up `git` by running:<br> `git config --system core.longpaths true`
  
### Building and Unit Testing

#### Building all the client libraries

Open a command prompt/terminal:
1. Execute `git clone https://github.com/Azure/azure-sdk-for-java.git`
1. Traverse to the repository root.
1. Execute `mvn compile -f pom.client.xml`
1. Install the tooling and build the product by executing:
    * `mvn install -Dinclude-non-shipping-modules -DskipTests -Dgpg.skip -f pom.client.xml`

#### Building only the Azure SDK client library for Event Hubs

After building the tooling and solution once from the section, [Building all the client libraries](#building-all-the-client-libraries), you can build just the Azure SDK client library for Service Bus by
executing:
```
mvn compile -f servicebus\azure-messaging-servicebus\pom.xml
```

#### Running unit tests

After following instructions in [Building all the client libraries](#building-all-the-client-libraries), you can run the
unit tests by executing:
```
mvn test -f servicebus\azure-messaging-servicebus\pom.xml
```

For unit tests, there are no special considerations; these are self-contained and execute locally without any reliance
on external resources. These tests are run for all PR validations.
### Live testing

Live tests assume a live resource has been created and appropriate environment
variables have been set for the test process. To automate setting up live
resources we use created a script called `New-TestResources.ps1` that deploys
resources for a given service.

To see what resources will be deployed for a live service, check the
`sdk\servicebus\test-resources.json` ARM template file.

To deploy live resources for testing use the steps documented in [`Example 1 of New-TestResources.ps1`](https://github.com/Azure/azure-sdk-for-java/blob/master/eng/common/TestResources/New-TestResources.ps1.md#example-1)
to set up a service principal and deploy live testing resources.

The script will provide instructions for setting environment variables before
running live tests.

To run live tests against a service after deploying live resources:

```
mvn -f sdk/servicebus/azure-messaging-servicebus/pom.xml -Dmaven.wagon.http.pool=false --batch-mode --fail-at-end --settings eng/settings.xml test
```
<!-- Links -->
[design-guidelines]: https://azuresdkspecs.z5.web.core.windows.net/DesignGuidelines.html
[github-general]: https://github.com/Azure/azure-sdk
[java-spec]: https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html
[maven]: https://maven.apache.org/
