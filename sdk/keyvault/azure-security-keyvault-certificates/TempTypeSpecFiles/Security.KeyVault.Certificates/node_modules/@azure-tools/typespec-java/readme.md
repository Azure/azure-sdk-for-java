# Prerequisite

Install [Node.js](https://nodejs.org/en/download/) 16 or above. (Verify by `node --version`)

Install [Java](https://docs.microsoft.com/java/openjdk/download) 11 or above. (Verify by `java --version`)

Install [TypeSpec](https://github.com/microsoft/typespec/) 0.59.

# Initialize TypeSpec Project

Follow [TypeSpec Getting Started](https://github.com/microsoft/typespec/#using-node--npm) to initialize your TypeSpec project.

Make sure `npx tsp compile .` runs correctly.

# Add TypeSpec-Java

Make sure the version of [TypeSpec-java release](https://github.com/Azure/autorest.java/releases) depends on same version of "@typespec/compiler" as in your TypeSpec project.

Modify `package.json`, add one line under `dependencies`:
```diff
    "dependencies": {
      "@typespec/compiler": "latest",
      "@typespec/rest": "latest",
      "@azure-tools/typespec-azure-core": "latest",
+      "@azure-tools/typespec-java": "latest"
    },
```

Run `npm install` again to install `@azure-tools/typespec-java`.

Modify (or create) `tspconfig.yaml`, specify emit as `@azure-tools/typespec-java`:
```diff
emit:
  - "@azure-tools/typespec-java"
```

# Generate Java

`npx tsp compile client.tsp --emit=@azure-tools/typespec-java` or `npx tsp compile client.tsp --emit=@azure-tools/typespec-java --options='@azure-tools/typespec-java.emitter-output-dir=<target-folder>'`.

If `emitter-output-dir` option is not provided, generated Java code will be under `tsp-output/@azure-tools/typespec-java` folder.

# Optional Configuration

## SDK

One can further configure the SDK generated, using the emitter options on `@azure-tools/typespec-java`.

```yaml
emit:
  - "@azure-tools/typespec-java"
options:
  "@azure-tools/typespec-java":
    emitter-output-dir: "{project-root}/azure-ai-language-authoring"
    namespace: "com.azure.ai.language.authoring"
    service-name: "Authoring"
    service-versions:
      - "2022-05-15-preview"
    enable-sync-stack: true
    stream-style-serialization: true
    generate-samples: true
    generate-tests: true
    examples-directory: "./examples"
    partial-update: false
    models-subpackage: "models"
    custom-types-subpackage: "implementation.models"
    custom-types: InternalModel1,InternalModel2
    api-version: "2023-11-01"
```

## Convenience API

By default, TypeSpec-Java generates all protocol APIs and convenience APIs.
A few exceptions are API of JSON Merge Patch, and API of long-running operation with ambiguous response type.

See "convenientAPI" decorator from [typespec-client-generator-core](https://github.com/Azure/typespec-azure/tree/main/packages/typespec-client-generator-core).


# Customization
All post-code customizations listed in this [documentation](https://github.com/Azure/autorest.java/tree/main/customization-base/README.md) are supported for code generated from TypeSpec.

To configure customization with TypeSpec, Java's emitter options should include a `customization-class`. The `customization-class` option should specify the path to the file containing the customization code relative to `emitter-output-dir`. Note that the path should end with `src/main/java/<YourCustomizationClassName>.java`. The recommended practice is to place the customization class in `<output-dir>/customization/src/main/java/<YourCustomizationClassName>.java` and the `customization-class` option will have the value of `customization-class: customization/src/main/java/<YourCustomizationClassName>.java`. See example `tspconfig.yaml` below:

```yaml
emit:
  - "@azure-tools/typespec-java"
options:
  "@azure-tools/typespec-java":
    emitter-output-dir: "{project-root}/azure-ai-language-authoring"
    namespace: "com.azure.ai.language.authoring"
    customization-class: customization/src/main/java/MyCustomization.java
```

# Changelog

See [changelog](https://github.com/Azure/autorest.java/blob/main/typespec-extension/changelog.md).

# Troubleshooting

### Enable logging in Java code

To enable logging, use `tspconfig.yaml` to add the `loglevel: ` option. Typically, `tspconfig.yaml` file will be
located in the same directory as the `<target.tsp>` file. The `loglevel` setting is a developer option and should be set under `options->dev-options`. The acceptable values for `loglevel` are
`off`, `debug`, `info`, `warn` and `error`. A sample `tspconfig.yaml` is shown below that enables logging at `info` level. By default,
logging is enabled at `error` level.

```yaml
emit:
  - "@azure-tools/typespec-java"
options:
  "@azure-tools/typespec-java":
    emitter-output-dir: "{project-root}/tsp-output"
    namespace: "com.azure.ai.language.authoring"
    dev-options:
      loglevel: info
```

### Debugging Java code

In order to set breakpoints and debug Java code locally on your development workspace, use the `tspconfig.yaml` file to
set the `debug` option to `true` under `options->dev-options` as shown in the example below. If the `debug` option is set
to `true`, then `tsp compile <target.tsp>` command will start the emitter which then invokes the Java process but the process
will be suspended until a debugger is attached to the process. The process listens on port 5005. Run the remote debugger
with this port on your IntelliJ or VS Code to connect to the Java process. This should now run the Java code generator
and breaks at all applicable breakpoints.

The remote debugger configuration is shown below for reference.

![img.png](https://raw.githubusercontent.com/Azure/autorest.java/main/docs/images/remote-debugger-config.png)

```yaml
emit:
  - "@azure-tools/typespec-java"
options:
  "@azure-tools/typespec-java":
    emitter-output-dir: "{project-root}/tsp-output"
    namespace: "com.azure.ai.language.authoring"
    dev-options:
      debug: true
```

### New version of `@typespec/compiler` etc.

Force an installation of new version via deleting `package-lock.json` and `node_modules` in `./typespec-extension` folder.

```shell
rm -rf node_modules
rm package-lock.json
```
