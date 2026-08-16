# Versioning

> **Source**: Consolidated from [CONTRIBUTING.md](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md) (last reviewed April 2026).  
> **See also**: [Package Versioning Policy](https://azure.github.io/azure-sdk/policies_releases.html#package-versioning)

---

## Version Files

All library versions are managed centrally in `eng/versioning/`:

| File | Contents |
|------|---------|
| `eng/versioning/version_client.txt` | Client library versions |
| `eng/versioning/external_dependencies.txt` | External (third-party) dependency versions |

### Format

```
groupId:artifactId;dependency-version;current-version
```

**Example:**
```
com.azure:azure-identity;1.5.0;1.6.0-beta.1
```

- **dependency-version** – Latest released version (used when this library is a dependency *outside* its own pipeline)
- **current-version** – Version currently in development (used within the same pipeline)

For a new, not-yet-released artifact both versions are the same.

---

## Version Types

| Type | Purpose | Tag Prefix |
|------|---------|-----------|
| Current | In-development; used by libraries built in the same pipeline | *(none)* |
| Dependency | Latest released; used by libraries in other pipelines | *(none)* |
| Unreleased Dependency | Unreleased additive change needed cross-pipeline | `unreleased_` |
| Released Beta Dependency | Depends on a released beta of a library | `beta_` |

A library **cannot reach GA** while it has released beta dependencies (except test-scoped ones).

---

## Version Tags in POM Files

Insert a comment tag on the same line as the `<version>` element:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core</artifactId>
  <version>1.30.0</version> <!-- {x-version-update;com.azure:azure-core;dependency} -->
</dependency>
```

Use `current` when the dependency is built in the same pipeline; use `dependency` otherwise.

---

## Version Tags in README Files

Use surrounding marker comments (because `<version>` appears inside user-visible XML):

```markdown
[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>1.5.0</version>
</dependency>
```
[//]: # ({x-version-update-end})
```

---

## Updating Versions

After a release, increment versions for the next cycle:

1. Open `eng/versioning/version_client.txt`.
2. Bump the `current-version` of all libraries in the affected service pipeline.
3. Run the update script from the repo root:

   ```bash
   python eng/versioning/update_versions.py
   ```

4. Review the modified POM and README files, then open a PR.

Guidelines for what to increment (major/minor/patch) are in the
[Package Versioning Policy](https://azure.github.io/azure-sdk/policies_releases.html#java).

---

## Unreleased Dependency Workflow

When library A needs an unreleased additive change from library B:

1. Make the additive change to library B.
2. In `version_client.txt`, add to the unreleased section:
   ```
   unreleased_com.azure:azure-core;<current-version-of-core>
   ```
3. In library A's `pom.xml`, change the dependency tag:
   ```xml
   <!-- Before -->
   <version>...</version> <!-- {x-version-update;com.azure:azure-core;dependency} -->
   <!-- After -->
   <version>...</version> <!-- {x-version-update;unreleased_com.azure:azure-core;dependency} -->
   ```
4. Once library B is released, remove the `unreleased_` entry and revert to the normal tag.

---

## Supported GroupIds for Publishing

Libraries must use one of these Maven group IDs to be publishable via ESRP:

- `com.azure` / `com.azure.*`
- `com.microsoft`
- `com.microsoft.azure`
- `com.microsoft.azure.cognitiveservices`
- `com.microsoft.azure.functions`
- `com.microsoft.azure.kusto`
- `com.microsoft.azure.sdk.iot`
- `com.microsoft.azure.sdk.iot.provisioning`
- `com.microsoft.commondatamodel`
- `com.microsoft.rest`
- `com.microsoft.servicefabric`
- `com.microsoft.spring`
- `com.microsoft.sqlserver`
- `com.windowsazure`
- `io.clientcore`
