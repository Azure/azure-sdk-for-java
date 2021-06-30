# Contributing Guide

This is a contributing guide made specifically for the Azure Communication Services SDK. The Azure SDK repository also has a contributing guide that might help you in some other general processes. If you haven't checked that one out yet, you can find it [here](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md). This guide assumes you have set up your development environment for this repository.

The Azure Communication Services SDK for Java currently consists of 5 different packages. While each package has its own set of environment variables to make their tests run successfully, all of them follow a similar structure that allows a smooth onboarding process.

Let's get started with additional setup steps for this package.

## Installation process

To get started with any of the packages, change directory to the root communication folder and run the `mvn verify -DskipTests` command. This will install all of the local files necessary for you to run the corresponding tests for all packages. 

Once the packages have been installed on your machine, let's jump on how to run the tests to see that everything is in order.

## Testing

Tests run against a target resource, which is nothing more than an Azure Communication Services instance that can be acquired from the Azure Portal itself. The target resource can manage phone numbers and identities depending on which SDK is used. Because these tests run against this instance, we must first create the resource from the Azure Portal and then obtain the connection string and set it up in an environment variable which we'll talk about later. 

There are three modes these tests can run in. One of them is RECORD mode, where we test our code against the resource directly and record the response in a series of recording files. These recording files can then be used to run the tests in PLAYBACK mode and avoid running them against the instance every time we may want to verify everything is in order with our code. The last mode is called LIVE mode, where we test against the resource directly but not record the response from the server.

When you go inside the tests folder of the package you are working with, you will see a folder called `resources` that has another folder named `session-records`. This folder contains, as its name suggests, recordings of successful calls to the API that allow us to run the tests in PLAYBACK mode and remove the necessity of hitting the actual resources every time we may want to test.

### Playback mode

To run the tests in PLAYBACK mode, set an environment variable called `AZURE_TEST_MODE` and set its value to `PLAYBACK` (If the variable if not set, the default will be `PLAYBACK`). After your variable has been set, change directory to the root folder of the package you're working on and run the `mvn verify` command.

### Live mode

Because in LIVE mode we are hitting an actual resource, we must set the appropriate environment variable to make sure the code tests against the resource we want. Set up two env variables called `COMMUNICATION_SAMPLES_CONNECTION_STRING` and `COMMUNICATION_LIVETEST_DYNAMIC_CONNECTION_STRING` and set them to the connection string of the target communication resource you want to test against. Note that the value of `COMMUNICATION_SAMPLES_CONNECTION_STRING` is currently set in the Key Vault, no need to set it up here for now. The value of the connection strings can be obtained from Azure portal. [Access your connection strings and service endpoints](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp)

Depending on which package you are testing, it may need special environment variables to test successfully. All packages have a *TestBase.java file inside their corresponding test folder and each one of these contain the special environment variables the tests need in order to run. Make sure to set these variables before running the tests themselves. You may need to restart your development environment after creating or updating these environment variables.

You can run the `mvn verify` command after setting the `AZURE_TEST_MODE` variable to `LIVE`.

### Record mode

RECORD mode is similar to LIVE mode because it also hits an actual resource. In addition to hitting the resource, RECORD mode will also record the successful calls to the service in json format. As mentioned, the recordings are stored in the package root folder under `target/test-classes/session-records`.

These newly generated files will have to be copied to the previosly mentioned `resources/session-records` after completion to make sure the PLAYBACK tests run with an updated version of the calls we made. Make sure to change the name of the recording files to match the names of the ones that are already in the `resources/session-records` folder. 

If you would like to generate new recordings for a single test, setting `AZURE_TEST_MODE` to `RECORD`, reopen Visual Studio, and run the test in Visual Studio normally.
### Managed Identity Tests

If you ran the tests in LIVE mode, you may have noticed that the files inside the recordings folder were updated. If any of the tests failed, you will see the error message right there in the recording file as well as in your terminal logs.

The most probable thing is that the managed identity tests will fail at first. This is because we haven't set up any managed identity credentials for the DefaultAzureCredential object inside the tests to reference to. There are multiple ways of creating a managed identity credential.

One of the easiest ways is to install the [Azure CLI](https://docs.microsoft.com/cli/azure/install-azure-cli) and run the `az login` command. If you are listed as a contributor of the resource you are testing against, this should be enough for the DefaultAzureCredential object to get the corresponding Azure Active Directory credentials you need.

Another way to authenticate is to set up 3 environment variables called `AZURE_CLIENT_ID`, `AZURE_TENANT_ID` and `AZURE_CLIENT_SECRET` and set their values to the ones from a registered Azure Active Directory application that is linked to the resource you are testing against.

If you are testing against a personal resource, you can check the [Managed Identity Quickstart Guide for ACS](https://docs.microsoft.com/azure/communication-services/quickstarts/managed-identity-from-cli) for an easy ramp-up process.

For a more in-depth look on how to authenticate using managed identity, refer to the [Azure Identity client library for Java](https://docs.microsoft.com/java/api/overview/azure/identity-readme) documentation. This document also has more ways for you to authenticate using the DefaultAzureCredential object besides the ones we discussed in this contributing file.

### Samples and building

As your code changes, the samples inside the `samples` folder should also change. The README file extracts code from the samples to avoid duplicated code. For a more in-depth look on how to extract the code from the samples into the README file, check this [guide](https://github.com/Azure/azure-sdk-for-java/wiki/Building#code-snippets-in-readme)

That guide also serves as a great starting point for ideas on how to run the code locally to avoid problems on the pipeline whenever you upload your changes. Make sure to give that document a read too.

## Submitting a Pull Request

Follow the general [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md) for instructions on the GitHub Forks / Pull requests model we use for our submissions. Make sure to name your PR with the following format once you are ready to submit it: [Communication] - `package-you-are-updating` - `pr-description`.

Additionally, write a good description about what your PR does in the description section of the PR itself. This will help your reviewers have a better understanding of what you are trying to accomplish in your PR.
