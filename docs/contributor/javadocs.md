# JavaDoc and Code Snippets


This page covers the standards for writing JavaDoc in Azure SDK Java libraries and the automated code snippet injection workflow.

---

## 1. Why Good JavaDoc Matters

- **Readability**: Explains classes/methods/fields without opening source
- **IDE integration**: Tooltips and documentation pop-ups in IntelliJ, VS Code, Eclipse
- **Collaboration**: Reduces onboarding time for new contributors
- **Testing guidance**: Documents parameters, return values, and exceptions

---

## 2. What to Document

### Package-Level Documentation (`package-info.java`)

Structure:

1. **Service introduction** — brief description + link to Azure docs
2. **Getting Started** — authentication and client instantiation steps with code samples
3. **Key API scenarios** — one subsection per major operation with code samples
4. **`@see` tags** — link to `ClientBuilder`, `Client`, `AsyncClient`, and main model classes

**Example structure** (Azure App Configuration):

```java
/**
 * <p><a href="https://learn.microsoft.com/azure/azure-app-configuration/">Azure App Configuration</a>
 * is a managed service for centralizing application configuration settings.</p>
 *
 * <p>The Azure App Configuration library provides Java developers a simple interface for
 * accessing the Azure App Configuration Service.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>Authenticate via connection string or Azure Identity:</p>
 * <ol>
 *   <li>Connection string: {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#connectionString}</li>
 *   <li>AAD token: {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#credential}</li>
 * </ol>
 *
 * <p><strong>Sample: Construct Async Client</strong></p>
 * <!-- src_embed com.azure.data.applicationconfig.async.configurationclient.instantiation -->
 * <pre>
 * ConfigurationAsyncClient client = new ConfigurationClientBuilder()
 *     .connectionString(connectionString)
 *     .buildAsyncClient();
 * </pre>
 * <!-- end com.azure.data.applicationconfig.async.configurationclient.instantiation -->
 *
 * <h2>Add Configuration Setting</h2>
 * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * ConfigurationSetting setting = client.addConfigurationSetting(
 *     new ConfigurationSetting().setKey("prodDBConnection").setValue("db_connection"));
 * </pre>
 * <!-- end com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting -->
 *
 * @see com.azure.data.appconfiguration.ConfigurationClientBuilder
 * @see com.azure.data.appconfiguration.ConfigurationClient
 * @see com.azure.data.appconfiguration.ConfigurationAsyncClient
 */
package com.azure.data.appconfiguration;
```

### Client Class-Level Documentation

Include:
1. Brief client introduction (what operations it supports)
2. Getting Started section with constructor sample
3. Key operation subsections with code samples
4. `@see` tags to builder, models, and related clients

### Method-Level Documentation

- `@param` for every parameter
- `@return` describing the return value (not just "returns the value")
- `@throws` for every declared checked exception
- `@throws IllegalArgumentException` / `@throws NullPointerException` for validation failures

---

## 3. Code Snippet Injection (Codesnippet Maven Plugin)

The [Codesnippet Maven Plugin](https://github.com/Azure/azure-sdk-tools/blob/main/packages/java-packages/codesnippet-maven-plugin/README.md) keeps JavaDoc and README samples in sync with compilable Java code.

> **Note:** Projects using `azure-client-sdk-parent` as parent POM have the plugin pre-configured. You only need to configure it explicitly when you have additional snippet paths.

### How it works

1. Write live Java code in a `*Samples.java` file (under `src/samples/` or `src/test/`)
2. Wrap the snippet with markers:

   ```java
   // BEGIN: com.azure.myservice.MyClient.myMethod#String
   String result = client.myMethod("hello");
   System.out.println(result);
   // END: com.azure.myservice.MyClient.myMethod#String
   ```

3. Reference the snippet in JavaDoc or README:

   **In JavaDoc:**
   ```java
   /**
    * <!-- src_embed com.azure.myservice.MyClient.myMethod#String -->
    * <!-- end com.azure.myservice.MyClient.myMethod#String -->
    */
   ```

   **In README.md:**
   ````markdown
   ```java readme-sample-myMethodExample
   // snippet content auto-injected here
   ```
   ````

4. Run `mvn compile` — the plugin validates or injects the snippet content.

### Snippet ID conventions

Use fully qualified method signatures:

```
com.azure.<service>.<ClassName>.<methodName>#<ParamType>
```

For README samples, use `readme-sample-<descriptiveName>`.

### Adding codesnippets to aggregation

If your project needs custom snippet source paths, configure the plugin in your POM:

```xml
<plugin>
  <groupId>com.azure.tools</groupId>
  <artifactId>codesnippet-maven-plugin</artifactId>
  <configuration>
    <codesnippetRootDirectory>src/samples/java</codesnippetRootDirectory>
    <additionalCodesnippetDirectory>src/test/java</additionalCodesnippetDirectory>
  </configuration>
</plugin>
```

---

## 4. Javadoc Compilation

Use **JDK 21** (or your target LTS) for Javadoc generation to catch forward-compatibility issues:

```bash
mvn javadoc:javadoc -f sdk/<service>/<module>/pom.xml
```

Review the output at `target/site/apidocs/index.html`.

Add `-Dmaven.javadoc.skip` to skip Javadoc during iterative builds:

```bash
mvn install -f sdk/<service>/<module>/pom.xml -DskipTests -Dmaven.javadoc.skip
```

---

## 5. References

- [Identity Package-level Javadoc example](https://jogiles.z19.web.core.windows.net/identity/azure-identity/com/azure/identity/package-summary.html)
- [Identity Class-level Javadoc example](https://jogiles.z19.web.core.windows.net/identity/azure-identity/com/azure/identity/DefaultAzureCredential.html)
- [Codesnippet Maven Plugin README](https://github.com/Azure/azure-sdk-tools/blob/main/packages/java-packages/codesnippet-maven-plugin/README.md)

---

## See Also

- [TypeSpec Quickstart — Improve SDK Documentation](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/typespec-quickstart.md#5-improve-documentation)
- [Adding a Module](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/adding-a-module.md)
- [Code Quality](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/code-quality.md)
