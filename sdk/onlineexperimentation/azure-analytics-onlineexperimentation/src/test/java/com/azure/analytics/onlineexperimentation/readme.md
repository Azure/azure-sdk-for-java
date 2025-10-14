# Azure Online Experimentation Test Suite

This directory contains unit tests for the Azure Analytics Online Experimentation client library.

## Overview

The test suite covers API operations related to:
- Creating, updating, and deleting experiment metrics
- Retrieving and listing metrics with various query parameters
- Validating metrics before creation
- Conditional operations using ETags

## Running Tests

To run the tests, use the following Maven command:

```bash
mvn test -Dtest=OnlineExperimentationClientTest
```

## Test Design

The tests follow the AAA (Arrange-Act-Assert) pattern and use the Azure Core testing infrastructure. 
They include:
- Live tests that interact with the actual service
- Recorded tests using the test-proxy package
- Parameterized tests that run against multiple service versions

The test structure follows the Azure SDK testing patterns, with these key components:
1. `OnlineExperimentationTestBase` - Base class providing common functionality
2. `OnlineExperimentationClientTest` - Main test class with test methods
3. `TestHelper` - Helper class providing test parameters and utilities

## Prerequisites

To run the live tests, you need:
1. An Azure subscription
2. Proper credentials and permissions to access the Online Experimentation service
3. Environment variables set up for authentication:
   - AZURE_ONLINEEXPERIMENTATION_ENDPOINT (optional, defaults to the standard endpoint)
   - Azure authentication information via DefaultAzureCredential
