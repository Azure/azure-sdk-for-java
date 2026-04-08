# Agentic Workflows

This repository uses [GitHub Agentic Workflows](https://github.github.com/gh-aw/introduction/overview/)
(`gh-aw`) to automate detection and triaging of documentation gaps — the same approach used in
[azure-sdk-for-net](https://github.com/Azure/azure-sdk-for-net).

## What runs automatically

| Workflow | Trigger | What it does |
|----------|---------|-------------|
| **Update Docs** | Push to `main` | Detects docs/sample gaps in the changed `sdk/<service>/<package>/` directory and files a GitHub issue with a full implementation guide |

## How it works

1. A push lands on `main` and touches Java source files under `sdk/`
2. The agent examines the diff and identifies new/changed public APIs or types
3. It maps the changed file path to a service using that service's `ci.yml`
4. It checks the affected `README.md` and `ReadmeSamples.java` for gaps
5. It files **one** issue titled `[<Service>] Docs: <description>` with:
   - A description of what changed
   - A list of gaps
   - A collapsible `Implementation Guide` with exact file paths, content to add, and Maven commands to validate
6. It dispatches the issue triage workflow which routes the issue to the correct owner

**To have Copilot implement the fix:** assign the issue to `@copilot`.

## Java snippet system

When the agent or a Copilot coding agent adds a new code sample, it must use the Java snippet system:

**In the Java source file** (`*Samples.java` or `ReadmeSamples.java`):
```java
public void myFeatureExample() {
    // BEGIN: readme-sample-myFeatureExample
    MyClient client = new MyClientBuilder()
        .endpoint("<endpoint>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
    client.doSomething();
    // END: readme-sample-myFeatureExample
}
```

**In `README.md`** (at the location where the code should appear):
````markdown
```java readme-sample-myFeatureExample
MyClient client = new MyClientBuilder()
    .endpoint("<endpoint>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
client.doSomething();
```
````

The content inside the README fence is kept in sync with the Java source by CI tooling.
See [sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/ReadmeSamples.java](../../sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/ReadmeSamples.java)
as a canonical example.

## Editing the workflow prompt

The LLM prompt that drives the agent lives in
[`.github/workflows/update-samples-and-docs.md`](../../.github/workflows/update-samples-and-docs.md).

To change agent behavior:
1. Edit the `.md` file
2. Run `gh aw compile .github/workflows/update-samples-and-docs.md`
3. Commit both the `.md` and the updated `.lock.yml`

See [`.github/workflows/README.md`](../../.github/workflows/README.md) for full `gh-aw` setup
instructions including the required repository secrets.
