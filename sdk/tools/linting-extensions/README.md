# Client Core Linting Extensions shared library for Java

Linting Extensions is a shared library for Java that provides extensions to Checkstyle, Spotbugs, and RevApi to help
enforce the SDK for Java guidelines.

## Getting started

### Prerequisites

* [Java Development Kit (JDK)](https://learn.microsoft.com/azure/developer/java/fundamentals/java-jdk-install) with version 8 or above.

## Key concepts

### Consolidated Checkstyle rules

`PublicApiTypeCheck` checks public API signatures for implementation-package types and unapproved external
dependencies. Configure `allowedDependencyPrefixes` with the package prefixes allowed by the SDK. The `java.` and
`javax.` prefixes are always allowed. `checkImplementationTypes` and `checkExternalDependencies` both default to `true`.

`ExceptionHandlingCheck` validates exception logging, requires exceptions created through throwable logging events
to be thrown, and checks that throws in catch blocks preserve the caught cause. Set `loggingApi` to `azure-core` for Track2
logging or `clientcore` for ClientCore and VNext logging; the default is `clientcore`. The Azure Core policy preserves
its constructor and static-scope exemptions. The ClientCore policy preserves its immediate-validation exception
exemptions. `checkThrownExceptions`, `checkUnthrownExceptions`, and `checkCaughtExceptionCause` default to `true`;
the unthrown-event policy only applies to ClientCore logging.

The repository configurations register each consolidated rule once. The previous rule classes have been removed;
configure `PublicApiTypeCheck` and `ExceptionHandlingCheck` directly. Diagnostics identify the consolidated check as
their source:

| Removed checks | Replacement rule |
| --- | --- |
| `NoImplInPublicAPI`, `ExternalDependencyExposedCheck` | `PublicApiTypeCheck` |
| `ExceptionCreatedButNotThrownCheck`, `ThrowFromClientLoggerCheck`, `RawExceptionThrowCheck`, `UseCaughtExceptionCauseCheck` | `ExceptionHandlingCheck` |

Replace the old names in suppression `checks` selectors with the fully qualified replacement rule name, such as
`io.clientcore.linting.extensions.checkstyle.checks.PublicApiTypeCheck`. A suppression applies to the entire merged
rule. The suppression generator uses the merged rule names from Checkstyle XML reports without special identifiers.

Production SDK checks skip test and sample source files before performing their analysis when their diagnostics
would be suppressed by `SdkCheckFilter`. `StepVerifierCheck` remains active in test and shared-test source files.
The obsolete `JavadocInlineTagCheck` and `JavadocCodeSnippetCheck` have been removed.

## Examples

## Troubleshooting

## Next steps

## Contributing
This project welcomes contributions and suggestions. Most contributions require
you to agree to a Contributor License Agreement (CLA) declaring that you have
the right to, and actually do, grant us the rights to use your contribution.
For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether
you need to provide a CLA and decorate the PR appropriately (e.g., label,
comment). Simply follow the instructions provided by the bot. You will only
need to do this once across all repos using our CLA.

This project has adopted the
[Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct). For more information,
see the Code of Conduct FAQ or contact opencode@microsoft.com with any
additional questions or comments.

<!-- LINKS -->
