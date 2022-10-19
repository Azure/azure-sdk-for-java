# How to Add Live Tests

Here is a guide to add live tests for management-plane SDK.

## Pre-requisites

Read [Developer Guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md#developer-guide).
It provides guide on how to build, run tests, and the context of live tests.

## Add Test Dependencies

Add following test dependencies to POM at `sdk/<service>/azure-resourcemanager-<service>/pom.xml`,
```
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-test</artifactId>
  <version>1.9.1</version> <!-- {x-version-update;com.azure:azure-core-test;dependency} -->
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.5.2</version> <!-- {x-version-update;com.azure:azure-identity;dependency} -->
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.azure.resourcemanager</groupId>
  <artifactId>azure-resourcemanager-resources</artifactId>
  <version>2.15.0</version> <!-- {x-version-update;com.azure.resourcemanager:azure-resourcemanager-resources;dependency} -->
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-engine</artifactId>
  <version>5.8.2</version> <!-- {x-version-update;org.junit.jupiter:junit-jupiter-engine;external_dependency} -->
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-simple</artifactId>
  <version>1.7.36</version> <!-- {x-version-update;org.slf4j:slf4j-simple;external_dependency} -->
  <scope>test</scope>
</dependency>
```

- `azure-core-test` and `junit-jupiter-engine` for test framework.
- `azure-identity` for authorization.
- `azure-resourcemanager-resources` for SDK to manage resource groups.
- One might add other required libraries in `test` scope.

And run
```
python eng/versioning/update_versions.py --ut library --bt client --sr
```
to update the versions in POM.

## Add Bicep Script for Test Environment

Add a [bicep](https://github.com/Azure/bicep) script at `sdk/<service>/test-resources.bicep` ([example](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/mediaservices/test-resources.bicep)).

No change to the bicep script is required.

- It adds [Contributor role](https://docs.microsoft.com/azure/role-based-access-control/built-in-roles#contributor) of the resource group to the service principal.
- It provides the name of the resource group, as well as credentials for the live tests.

## Add Live Tests

Add live tests ([example](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/mediaservices/azure-resourcemanager-mediaservices/src/test/java/com/azure/resourcemanager/mediaservices/MediaServicesTests.java)).

- `@DoNotRecord(skipInPlayback = true)` make it a live test, without recording and playback.
- It uses the `AZURE_RESOURCE_GROUP_NAME` environment variable if available.
- The `DefaultAzureCredential` uses `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_SUBSCRIPTION_ID` environment variable for credentials. 

All the environment variables are provided in live tests pipeline.

### Verify Locally

To verify your tests locally, one need to set these environment variables in local.

For credentials, please refer to [guide on authentication](https://docs.microsoft.com/azure/developer/java/sdk/get-started#set-up-authentication).

For the resource group, one can create a resource group, and set its name to `AZURE_RESOURCE_GROUP_NAME` environment variable.
Make sure your service principal above has Contributor role on the resource group.
And remember to delete it after local tests complete.

Run the tests locally,
```
mvn test -f sdk/<service>/azure-resourcemanager-<service>/pom.xml -DAZURE_TEST_MODE=LIVE
```

## Add Pipeline Configuration

Add a pipeline configuration for live tests at `sdk/<service>/tests.mgmt.yml` ([example](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/mediaservices/tests.mgmt.yml)).

### Add Pipeline

After pull request is ready, comment `/azp run prepare-pipelines` to let the automation create the pipeline from `tests.mgmt.yml`.

### Run Live Tests

The live tests will be automatically run, before SDK release.

In pull request, comment `/azp run java - <service> - tests.mgmt` to run it manually.
