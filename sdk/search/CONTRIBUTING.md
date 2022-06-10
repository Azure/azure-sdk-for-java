# Contributing (for `azure-search-documents`)

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License 
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For
details, visit [cla.microsoft.com](https://cla.microsoft.com).

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/)
or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Azure SDK Design Guidelines for Java

These libraries follow the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html)
and share a number of core features such as HTTP retries, logging, transport protocols, authentication protocols, etc., 
so that once you learn how to use these features in one client library, you will know how to use them in other client 
libraries. You can learn about these shared features in the
[azure-core README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/README.md).

## Code Generation

Please do not edit any of the code that contains the code generation header at the beginning of the file. If you need 
to update a swagger file or change the generator, you can regenerate by running the steps outlined in the 
[swagger folder](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/search/azure-search-documents/swagger).

## Testing

Please ensure all tests pass with any changes and additional tests are added to exercise any new features that you've 
added.

### Frameworks

We use [JUnit 5](https://junit.org/junit5/docs/current/user-guide/) as our testing framework.

[azure-core-test][core_tests] provides a set of reusable primitives that simplify writing tests for new Azure SDK libraries.

### Recorded tests

Our testing framework supports recording service requests made during a unit test enabling them to be replayed later. 
You can set the `AZURE_TEST_MODE` environment variable to `PLAYBACK` to run previously recorded tests, `RECORD` to 
record or re-record tests, and `LIVE` to run tests against the live service.

Properly supporting recorded tests does require a few extra considerations. All random values should be obtained via 
`TestResourceNamer` since we use the same seed on test playback to ensure our client code generates the same "random" 
values each time. You can't share any state between tests or rely on ordering because you don't know the order they'll 
be recorded or replayed. Any sensitive values are redacted via the recording sanitizers.

### Running tests

The easiest way to run the tests is via your IDE's unit test runner. You can also run tests via the command line 
using `mvn test`.

The recorded tests are run automatically on every pull request. Live tests are run nightly. Contributors with write 
access can ask Azure DevOps to run the live tests against a pull request by commenting `/azp run java - search - tests`
in the PR.

### Live Test Resources

Before running or recording live tests you need to create
[live test resources](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/TestResources/README.md).
If recording tests, secrets will be sanitized from saved recordings. If you will be working on contributions over time, 
you should consider persisting these variables.

### Samples

Our samples are structured as test source code in `/src/samples/` so we can easily verify they're up-to-date and 
compile correctly.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fsearch%2FCONTRIBUTING.png)

<!-- LINKS -->
[core_tests]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-test