# Working with AutoRest


> **Note:** For new services, prefer TypeSpec over AutoRest (OpenAPI 2.0). See [TypeSpec Quickstart](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/typespec-quickstart.md). Use this guide only when you must work with existing Swagger/OpenAPI 2.0 specifications.

AutoRest is the tool used to auto-generate the HTTP communication layer beneath the public API. The goal is to hide as much generated code from users as possible.

---

## Getting Started

1. Fork and clone [autorest.java](https://github.com/Azure/autorest.java):

   ```bash
   git clone https://github.com/Azure/autorest.java.git
   ```

2. Build it:

   ```bash
   mvn clean package -Dlocal
   ```

3. Install AutoRest (requires Node.js v10+):

   ```bash
   npm install
   npm install -g autorest
   ```

4. Run AutoRest:

   **From a local README.md:**

   ```bash
   autorest --java \
     --use=C:\work\autorest.java\ \
     C:\swagger\updated\Blob\readme.md \
     --output-folder=C:\work\azure-sdk-for-java\storage\client\ \
     --namespace=com.azure.storage.blob \
     --sync-methods=none \
     --generate-client-interfaces=false \
     --enable-xml \
     --required-parameter-client-methods
   ```

   **From a JSON/YAML swagger URL:**

   ```bash
   autorest --java \
     --use:./ \
     --input-file=https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/search/data-plane/Azure.Search/preview/2019-05-06-preview/searchservice.json \
     --namespace=com.azure.search.service \
     --output-folder=generated-code \
     --sync-methods=all \
     --add-context-parameter=true
   ```

---

## AutoRest Options Reference

| Option | Description |
|--------|-------------|
| `--namespace` | Java base package for generated code |
| `--output-folder` | Output directory |
| `--input-file` | JSON/YAML swagger spec (URL or local path); omit for README.md input |
| `--license-header` | License header type: `MICROSOFT_MIT`, `MICROSOFT_APACHE`, `NONE`, etc. |
| `--client-side-validations` | Enable client-side parameter validation (default: `false`) |
| `--generate-client-interfaces` | Generate interface for each client (default: `false`) |
| `--generate-client-as-impl` | Generate client as `*Impl` type |
| `--implementation-subpackage` | Sub-package for implementation classes |
| `--models-subpackage` | Sub-package for model classes |
| `--custom-types` | Comma-separated list of types to move to custom sub-package |
| `--custom-types-subpackage` | Sub-package for custom types |
| `--add-context-parameter` | Add `Context` to proxy methods |
| `--context-client-method-parameter` | Add `Context` to client methods |
| `--generate-sync-async-clients` | Generate both sync and async clients |
| `--required-fields-as-ctor-args` | Generate models with required fields as constructor arguments |
| `--enable-xml` | Enable XML support (Storage only) |
| `--fluent` | Generate fluent interface (Management libraries) |
| `--stream-style-serialization` | Enable `azure-json` stream-style serialization |

---

## Notes

1. `--use=<path to your autorest.java clone>` — points AutoRest to your local build
2. For JSON input use `--input-file=<path or URL>`, for README.md input just pass the README path directly
3. `--enable-xml --required-parameter-client-methods` are only needed for Storage
4. To reset all AutoRest extensions: `autorest --reset`
5. To pin a specific AutoRest core version: `autorest --version=<string>`

---

## Updating Generated Code

After changing a Swagger spec:

1. Re-run AutoRest with the same options
2. Review generated diffs carefully — especially breaking changes in existing type signatures
3. Run RevApi to check for API breaks:

   ```bash
   mvn verify -Drevapi
   ```

4. If you have breaking changes that are intentional, update `eng/code-quality-reports/src/main/resources/revapi/revapi.json`

---

## See Also

- [TypeSpec Quickstart](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/typespec-quickstart.md) — preferred for new services
- [Azure Json Migration](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/azure-json-migration.md) — move from Jackson to `azure-json`
- [Building](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/building.md)
