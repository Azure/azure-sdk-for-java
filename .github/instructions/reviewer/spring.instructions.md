---
applyTo: "sdk/spring/**"
description: "Review Spring Cloud for Azure changes using Spring-specific guidance where it overrides general Java SDK rules."
---

# Spring Cloud for Azure review

Apply the [Spring guidelines](https://azure.github.io/azure-sdk/java_spring.html)
where they differ from the general Java client guidelines. Review the actual
Spring module and its supported Spring/JDK versions first: existing modules
use `spring-cloud-azure-*` artifact names and some target Java 17. Do not
propose renaming them to match historical guideline examples or forcing a
Java 8 target.

- New consumer-facing packages should use `com.azure.spring.<group>.<service>`
  (with an optional feature); keep non-public implementation under
  `implementation`. New Spring artifacts use the `com.azure.spring` group.
  Flag a naming change only if it creates a real discoverability or API break.
- Check that new or changed dependencies have the required approval, remain
  minimal, do not conflict with supported Spring dependency versions, and
  use `com.azure` rather than legacy `com.microsoft.azure` client libraries.
  Preserve compatibility with the module's existing Spring BOM/dependency
  management; do not demand an explicit BOM import in every child POM.
- Do **not** apply the Java client-library `ClientLogger` requirement to
  Spring libraries: Spring guidance explicitly prohibits `ClientLogger`.
  Check instead that new logging does not expose credentials or other secrets.
- Preserve context propagation between Spring and Azure client tracing.
  Flag a concrete loss of spans or context, not a missing integration with
  an obsolete Spring tracing package. Verify support for the Spring API
  versions the changed module actually targets.
- Check starter/autoconfiguration changes for broken defaults, bean
  conditions, and configuration binding that would prevent an application
  from using the underlying Azure client. Require a demonstrated regression,
  not a theoretical performance or style concern.

Sources: [Spring namespaces](https://azure.github.io/azure-sdk/java_spring.html#java-spring-namespaces),
[dependencies](https://azure.github.io/azure-sdk/java_spring.html#java-spring-dependency-approval),
[BOM](https://azure.github.io/azure-sdk/java_spring.html#java-spring-bom),
[supported versions](https://azure.github.io/azure-sdk/java_spring.html#java-spring-supported-versions),
[logging](https://azure.github.io/azure-sdk/java_spring.html#java-spring-logging),
and [tracing](https://azure.github.io/azure-sdk/java_spring.html#java-spring-tracing).
