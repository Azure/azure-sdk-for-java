---
name: samples
description: 'Generate Java samples for azure-ai-agents or azure-ai-projects by referencing existing Java samples for format and equivalent Python samples for CRUD flows. WHEN: write samples for a feature area; generate Java samples; create sample code; write agent samples; new feature samples.'
---

# Generate Java Samples

Write Java sample files for the Azure AI Agents SDK (`azure-ai-agents`) or Azure AI Projects SDK (`azure-ai-projects`), following the established format and referencing equivalent Python samples for feature coverage.

## Preconditions

- You must be working within `sdk/ai/azure-ai-agents` or `sdk/ai/azure-ai-projects`.
- The user must specify the **feature area** (e.g. toolboxes, hosted agents, sessions, skills, conversations, memory), OR ask to write samples for "new features" — in which case, use the **api-diff** skill first to identify what functionality areas have new API additions that need samples.
- The user must provide a **live service endpoint** for testing.

## Integration with api-diff

When the user asks to write samples for new or changed functionality without specifying a feature area:

1. Use the **api-diff** skill to identify new API additions and their functionality buckets.
2. Cross-reference the bucketed results against existing sample directories:
   ```bash
   ls src/samples/java/com/azure/ai/agents/
   ```
3. Write samples for any new functionality bucket that has **no corresponding sample directory or file**.
4. For existing buckets with new methods, add samples covering the new operations.

## Reference Materials

### Existing Java samples (format reference)

| Package | Samples location |
|---------|-----------------|
| `azure-ai-agents` | `sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/` |
| `azure-ai-projects` | `sdk/ai/azure-ai-projects/src/samples/java/com/azure/ai/projects/` |

Two styles exist:

**Style A — One operation per file** (used in `azure-ai-agents`):
- Separate file per CRUD operation (e.g. `CreateAgent.java`, `GetAgent.java`, `DeleteAgent.java`)
- Organized in subdirectories by feature (e.g. `agents/`, `toolboxes/`, `hostedagents/`, `conversations/`)
- `public static void main(String[] args)` entry point
- Javadoc class comment explaining the sample

**Style B — All operations in one file** (used in `azure-ai-projects`):
- Single file per feature with multiple methods (e.g. `SkillsSample.java`)
- Methods wrapped with `// BEGIN:` / `// END:` codesnippet markers
- Client constructed as a class-level field

Use whichever style matches the target package.

### Python samples (feature/flow reference)

Before writing a sample for a specific operation, search the Python SDK for an equivalent sample demonstrating the same functionality.

#### Locating the Python SDK

Try these approaches in order:

1. **Ask the user** for the path to their local `azure-sdk-for-python` checkout.
2. **Check common locations**:
   ```bash
   # Sibling to the Java SDK repo
   ls -d $(dirname $(git rev-parse --show-toplevel))/azure-sdk-for-python 2>/dev/null
   # Home directory
   ls -d ~/azure-sdk-for-python 2>/dev/null
   # Search for it
   find /home -maxdepth 3 -name "azure-sdk-for-python" -type d 2>/dev/null | head -3
   ```
3. **Browse the web** if no local checkout is available — check the GitHub repository at `https://github.com/Azure/azure-sdk-for-python/tree/main/sdk/ai/azure-ai-projects/samples/` for equivalent Python samples.

#### Searching for equivalent samples

Once the Python SDK is located (locally or via web):

```bash
find <python-sdk-root>/sdk/ai -name "sample_*.py" | xargs grep -l "<feature_keyword>"
```

Use matching Python samples to determine:
- What operations to cover
- The expected CRUD flow and ordering
- What fields/parameters to demonstrate
- Cleanup/teardown patterns

If no Python sample exists for the operation, write the Java sample based on the API surface alone.

## Java Sample Format Rules

1. **License header**: `// Copyright (c) Microsoft Corporation. All rights reserved.` + `// Licensed under the MIT License.`
2. **Package**: matches the directory (e.g. `package com.azure.ai.agents.toolboxes;`)
3. **Endpoint from env**: `Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT")`
4. **Auth**: `new DefaultAzureCredentialBuilder().build()`
5. **Client construction**: `new AgentsClientBuilder().credential(...).endpoint(...).build<X>Client()`
6. **No hardcoded endpoints or secrets**
7. **Print results**: use `System.out.println` to show key fields from response objects
8. **Imports**: only import what's used

## Workflow

### 1. Identify the feature area and target package

Determine which package (`azure-ai-agents` or `azure-ai-projects`) and which feature subdirectory to use.

### 2. Find equivalent Python samples

Locate the Python SDK (ask the user, check common paths, or browse GitHub — see "Locating the Python SDK" above). Then search for samples covering the same feature:

```bash
find <python-sdk-root>/sdk/ai -name "sample_*.py" | xargs grep -l "<feature>"
```

If no local checkout is available, check `https://github.com/Azure/azure-sdk-for-python/tree/main/sdk/ai/azure-ai-projects/samples/` for equivalent samples.

Read matching Python samples to understand the intended CRUD flow and what operations to demonstrate.

### 3. Discover available Java API

Search the generated client classes for available methods:

```bash
grep -n "public.*<ReturnType>.*methodName" src/main/java/com/azure/ai/agents/*Client.java
```

Check the models package for request/response types:

```bash
find src/main/java -name "*FeatureName*"
```

### 4. Check for preview feature flags

Some operations require `Foundry-Features` headers. Look for `AgentDefinitionOptInKeys` parameters or check the TypeSpec routes for `required_previews`:

```bash
grep -n "foundryFeatures\|OptInKeys\|Foundry-Features" src/main/java/com/azure/ai/agents/*Client.java
```

If the convenience method doesn't have a `foundryFeatures` parameter, use the protocol method with `RequestOptions`:

```java
RequestOptions requestOptions = new RequestOptions()
    .setHeader(HttpHeaderName.fromString("Foundry-Features"), "HostedAgents=V1Preview");
```

Known preview headers:
- `HostedAgents=V1Preview` — sessions, hosted agent operations
- `AgentEndpoints=V1Preview` — agent endpoint configuration
- `ContainerAgents=V1Preview` — container agent operations

### 5. Write the samples

Create sample files following the appropriate style (A or B). Each sample should:
- Be self-contained (can be copy-pasted and run)
- Use placeholder values for resource names (e.g. `"my-toolbox-name"`) with comments indicating what to replace
- Handle both convenience methods and protocol methods as needed
- Mirror the flow demonstrated in the equivalent Python sample (if one exists)

### 6. Verify compilation

```bash
cd <sdk-root> && mvn compile -pl sdk/ai/<package> -am \
  -DskipTests -Dcheckstyle.skip -Dspotbugs.skip -Drevapi.skip -Djacoco.skip \
  -Denforcer.skip -Dcodesnippet.skip -Dcompile.samples=true -T 4
```

### 7. Test against live service

Build the package and run each sample:

```bash
# Build without tests
mvn package -pl sdk/ai/<package> -am \
  -DskipTests -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Dsource.skip=true \
  -Dcheckstyle.skip -Dspotbugs.skip -Drevapi.skip -Djacoco.skip -Denforcer.skip \
  -Dgpg.skip -Dcodesnippet.skip -T 4

# Compile samples against the packaged jar
AGENTS_JAR="sdk/ai/<package>/target/<artifact>-<version>.jar"
CP=$(mvn -pl sdk/ai/<package> dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)
javac -cp "$AGENTS_JAR:$CP" -d /tmp/samples src/samples/java/com/azure/ai/agents/<feature>/*.java

# Run with endpoint set
export FOUNDRY_PROJECT_ENDPOINT="<user-provided-endpoint>"
java -cp "/tmp/samples:$AGENTS_JAR:$CP" com.azure.ai.agents.<feature>.<SampleClass>
```

### 8. Handle errors

Common issues:
- **403 `preview_feature_required`**: Add the required `Foundry-Features` header
- **404 `not_found`**: Resource doesn't exist yet (expected for Get/Delete samples with placeholder IDs)
- **424 `session_not_ready`**: Container image doesn't implement `/readiness` endpoint (expected for non-production images)
- **Compilation errors with modular JARs**: Use `mvn package` and compile against the packaged jar (not `target/classes`)

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Samples fail `mvn compile` | codesnippet plugin scans `TempTypeSpecFiles/node_modules` | Add `-Dcodesnippet.skip` |
| `module-info.class` errors with `javac` | Modular JARs on classpath | Use packaged jar approach (step 7) |
| Java 8 base-testCompile overwrites classes | Multi-release compilation | Compile samples against packaged jar instead of `target/test-classes` |
| Method not found on client | API might be protocol-only | Check for `*WithResponse` methods that take `BinaryData` + `RequestOptions` |
