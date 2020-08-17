Hello! Thank you for being interested in contributing to our project! 
Please make sure you've followed the instructions provided in the [Azure Projects Contribution Guidelines](http://azure.github.io/guidelines/).
## Project Setup
The Azure Storage development team uses Intellij. However, any preferred IDE or other toolset should be usable.

### Install
* Java SE 8+
* [Maven](https://maven.apache.org/install.html)
* Clone the source code from GitHub

#### IntelliJ Installation
* [IntelliJ](https://www.jetbrains.com/idea/download)
* [Importing project from Maven for IntelliJ](https://www.jetbrains.com/help/idea//2017.1/importing-project-from-maven-model.html)

#### Eclipse Installation
* [Eclipse](https://eclipse.org/downloads/)
* [Maven plugin for Eclipse](http://www.eclipse.org/m2e/index.html). Some Eclipse packages (ex Eclipse IDE for Java Developers) may come with this plugin already installed.
* Open the project from Eclipse using File->Import->Maven->Existing Maven Projects and navigating to the azure-storage-java folder. Select the listed pom. This imports the source and the test files and downloads the required dependencies via Maven. If you'd like to import the samples, follow the same procedure but navigate to the azure-storage-java\microsoft-azure-storage-samples folder and select that pom. Both projects can be opened at the same time and will be shown in the Package Explorer.

## Tests

### Configuration
The only step to configure testing is to set the appropriate environment variables. Create environment variables named "ACCOUNT_NAME" and "ACCOUNT_KEY", holding your Azure storage account name and key respectively. This will satisfy most tests. 
To run any tests requiring two accounts (generally those testing copy-related apis), set environment variables "SECONDARY_ACCOUNT_NAME", and "SECONDARY_ACCOUNT_KEY".
To run any tests related to setting blob tiers on block blobs, set environment variables "BLOB_STORAGE_ACCOUNT_NAME" and "BLOB_STORAGE_ACCOUNT_KEY". Note that a GPV2 account is also sufficient here.
To run any tests related to setting blob tiers on page blobs, set environment variables "PREMIUM_ACCOUNT_NAME" and "PREMIUM_ACCOUNT_KEY".
It is valid to use a single account for multiple scenarios; a GPV2 account would work for both the primary account and the blob storage account, for instance. The only restriction is that the primary and secondary accounts must be distinct.

### Running
To actually run tests, right click on the test class in the Package Explorer or the individual test in the Outline and select Run As->GroovyTest. Alternatively, run mvn test from the command line.
### Testing Features
As you develop a feature, you'll need to write tests to ensure quality. You should also run existing tests related to your change to address any unexpected breaks.

## Pull Requests

### Guidelines
The following are the minimum requirements for any pull request that must be met before contributions can be accepted.
* Make sure you've signed the CLA before you start working on any change.
* Discuss any proposed contribution with the team via a GitHub issue **before** starting development.
* Code must be professional quality
	* No style issues
	* You should strive to mimic the style with which we have written the library
	* Clean, well-commented, well-designed code
	* Try to limit the number of commits for a feature to 1-2. If you end up having too many we may ask you to squash your changes into fewer commits.
* [ChangeLog.txt](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/microsoft-azure-storage-blob/ChangeLog.txt) needs to be updated describing the new change
* Thoroughly test your feature

### Branching Policy
Changes should be based on the **dev** branch for non-breaking changes and **dev_breaking** for breaking changes. Do not submit pull requests against master as master is considered publicly released code. Each breaking change should be recorded in [BreakingChanges.txt](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/microsoft-azure-storage-blob/BreakingChanges.txt). 

### Adding Features for Java 8+
We strive to release each new feature in a backward compatible manner. Therefore, we ask that all contributions be written to work in Java 8 and 9.

### Review Process
We expect all guidelines to be met before accepting a pull request. As such, we will work with you to address issues we find by leaving comments in your code. Please understand that it may take a few iterations before the code is accepted as we maintain high standards on code quality. Once we feel comfortable with a contribution, we will validate the change and accept the pull request.


Thank you for any contributions! Please let the team know if you have any questions or concerns about our contribution policy.