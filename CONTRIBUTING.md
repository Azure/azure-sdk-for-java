If you intend to contribute to the project, please make sure you've followed the instructions provided in the [Azure Projects Contribution Guidelines](http://azure.github.io/guidelines/).
## Project Setup
The Azure Storage development team uses Intellij. However, any preferred IDE or other toolset should be usable.

### Install
* Java SE 6+
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
The only step to configure testing is to setup a configuration file or connection string via environment variables. To use the connection string route, create an environment variable named "storageConnection". To use the configuration file route, create an environment variable named "storageTestConfiguration" with the path to a TestConfigurations.xml file with this [template](https://github.com/Azure/azure-storage-java/blob/master/microsoft-azure-storage-test/res/TestConfigurations.xml).
Alternatively, you can fill in microsoft-azure-storage-test/res/TestConfigurations.xml with the appropriate information.

### Running
To actually run tests, right click on the test class in the Package Explorer or the individual test in the Outline and select Run As->JUnitTest. All tests or tests grouped by service can be run using the test runners in the com.microsoft.azure.storage package TestRunners file. Running all tests from the top of the package explorer will result in each test being run multiple times as the package explorer will also run every test runner.

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
* [ChangeLog.md](ChangeLog.md) needs to be updated describing the new change
* Thoroughly test your feature

### Branching Policy
Changes should be based on the **dev** branch for non-breaking changes and **dev_breaking** for breaking changes. Do not submit pull requests against master as master is considered publicly released code. Each breaking change should be recorded in [BreakingChanges.md](BreakingChanges.md). 

### Adding Features for Java 6+
We strive to release each new feature in a backward compatible manner. Therefore, we ask that all contributions be written to work in Java 6, 7 and 8.

### Review Process
We expect all guidelines to be met before accepting a pull request. As such, we will work with you to address issues we find by leaving comments in your code. Please understand that it may take a few iterations before the code is accepted as we maintain high standards on code quality. Once we feel comfortable with a contribution, we will validate the change and accept the pull request.


Thank you for any contributions! Please let the team know if you have any questions or concerns about our contribution policy.