# `_shared/` — library, not a skill

Pure-Java helpers used by the authoring skill scripts under
`.github/skills/cu-sdk-author-analyzer*/`.

The leading underscore marks this as a **library directory**, not a skill. It
is intentionally excluded from the Copilot skill picker.

Rules for code in `SchemaValidator.java` (the validator):

- **No `com.azure.*` imports.** No network calls. No I/O beyond reading /
  parsing caller-provided JSON.
- **No new runtime dependencies.** Jackson (`jackson-databind`) only.
- **Stable, small, well-tested.** Anything here is referenced by multiple skill
  scripts; breakage cascades.

The CLI command classes (`ExtractLayoutCommand.java`,
`CreateAndTestCommand.java`, `CreateAndTestRouterCommand.java`) wrap the
`ContentUnderstandingClient` from `azure-ai-contentunderstanding`. They are
allowed to import `com.azure.*` since they're the bridge between the
validator and the service.

Current modules:

- [`SchemaValidator.java`](src/main/java/com/azure/ai/contentunderstanding/skills/SchemaValidator.java) —
  validates analyzer schema JSON before any service call (catches
  `baseAnalyzerId` typos, missing `fieldSchema`, missing
  `contentCategories` analyzer routes, etc.). Pure Jackson.
- [`Cli.java`](src/main/java/com/azure/ai/contentunderstanding/skills/Cli.java) —
  subcommand dispatcher.
- [`ExtractLayoutCommand.java`](src/main/java/com/azure/ai/contentunderstanding/skills/ExtractLayoutCommand.java) —
  Stage 1: extract document layout.
- [`CreateAndTestCommand.java`](src/main/java/com/azure/ai/contentunderstanding/skills/CreateAndTestCommand.java) —
  Stage 2 (single-type).
- [`CreateAndTestRouterCommand.java`](src/main/java/com/azure/ai/contentunderstanding/skills/CreateAndTestRouterCommand.java) —
  Stage 2 (classify-and-route).

## Build

The Maven module is **intentionally NOT a child of azure-client-sdk-parent**
and is NOT referenced from the package POM — it lives outside the published
source tree so it has zero effect on the published
`azure-ai-contentunderstanding` artifact.

```bash
mvn -B -q -f .github/skills/_shared/pom.xml -DskipTests compile
```

Dependencies are pulled from Maven Central using published versions; the tool
builds against the public SDK rather than the local reactor so contributors
can iterate without first running `mvn install` on the parent.

## Run

```bash
mvn -B -q -f .github/skills/_shared/pom.xml exec:java \
    -Dexec.args="extract-layout --input <file-or-folder> --output <dir>"
```

Or, after one `mvn package`, invoke directly:

```bash
java -cp .github/skills/_shared/target/cu-skill-0.1.0.jar:<deps> \
    com.azure.ai.contentunderstanding.skills.Cli extract-layout ...
```
