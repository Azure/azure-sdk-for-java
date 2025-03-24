# Azure Core Test V2 shared library for Java

This library contains core classes used to test Azure SDK client libraries.

Newer SDK tests utilize the [Azure SDK Tools Test Proxy][test-proxy-readme] to record and playback HTTP interactions.
To migrate from existing [TestBase][TestBase.java] to use the test proxy, or to learn more about using the test proxy,
refer to the [test proxy migration guide][test-proxy-migration-guide].

## Table of contents

- [Getting started](#getting-started)
- [Key concepts](#key-concepts)
- [Troubleshooting](#troubleshooting)
- [Next steps](#next-steps)
- [Contributing](#contributing)

## Getting started

To use this package, add the following to your _pom.xml_.

[//]: # ({x-version-update-start;com.azure.v2:azure-core-test;current})

```xml

<dependency>
  <groupId>com.azure.v2</groupId>
  <artifactId>azure-core-test</artifactId>
  <version>2.0.0-beta.1</version>
</dependency>
```

[//]: # ({x-version-update-end})

## Key concepts

## Examples

## Troubleshooting

If you encounter any bugs with these SDKs, please file issues via
[Issues](https://github.com/Azure/azure-sdk-for-java/issues) or checkout
[StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Next steps

Other useful packages are:

* [azure-core](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core): Contains core classes and
  functionality used by all client libraries.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit <https://cla.microsoft.com>.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repositories using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact <opencode@microsoft.com> with any additional questions or comments.

[azure_portal]: https://portal.azure.com/
[azure_cli_service_principal]: https://learn.microsoft.com/cli/azure/ad/sp?view=azure-cli-latest#az-ad-sp-create-for-rbac
[default_sanitizers]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/utils/TestProxyUtils.java#L259
[git_setup]: https://git-scm.com/book/en/v2/Getting-Started-First-Time-Git-Setup
[git_token]: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token
[powershell]: https://learn.microsoft.com/powershell/scripting/install/installing-powershell?view=powershell-latest
[recording-migration]: https://github.com/Azure/azure-sdk-for-java/blob/64de460d8080127a1e0c58fbfc7ab9e95f70a2c7/sdk/core/azure-core-test/RecordingMigrationGuide.md
[sanitize-secrets]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md#session-and-test-level-transforms-sanitizers-and-matchers
[test-proxy-readme]: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/README.md
[test-proxy-migration-guide]: https://github.com/Azure/azure-sdk-for-java/blob/64de460d8080127a1e0c58fbfc7ab9e95f70a2c7/sdk/core/azure-core-test/TestProxyMigrationGuide.md
[InterceptorManager.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/InterceptorManager.java
[TestProxyPlaybackClient.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/http/TestProxyPlaybackClient.java
[TestProxyRecordPolicy.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/policy/TestProxyRecordPolicy.java
[TestBase.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/TestProxyTestBase.java
[TestProxyTestBase.java]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/TestProxyTestBase.java
[TableClientTestBase]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/src/test/java/com/azure/data/tables/TableClientTestBase.java#L61
[tables-test-resources]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/test-resources.json
[tables-test-resources-resources]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/test-resources.json#L42
[tables-test-resources-outputs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/test-resources.json#L115
[test-resources]: https://github.com/Azure/azure-sdk-for-java/tree/main/eng/common/TestResources#readme
[test_proxy_sanitizer]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/src/main/java/com/azure/core/test/models/TestProxySanitizer.java
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/


