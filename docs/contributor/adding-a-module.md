# Adding a Module


Adding a new client module to the Azure SDK for Java requires changes to Maven and Azure Pipeline configurations. A module may be added to an existing group (e.g. `sdk/storage`) or to a brand-new group.

---

## 1. Create the Module Directory

- Place the module under `sdk/<group>/<new-module>/`
- Directory name must match the package name without the leading `com`, e.g. package `com.azure.newservice.newpackage` → directory `azure-newservice-newpackage`

**Required files:**

| File | Notes |
|------|-------|
| `CHANGELOG.md` | Follow [changelog guidance](https://github.com/Azure/azure-sdk/blob/master/docs/policies/releases.md#changelog-guidance) |
| `pom.xml` | Parent: `sdk/parents/azure-client-sdk-parent/pom.xml` |
| `README.md` | Use the [README template](https://github.com/Azure/azure-sdk/blob/master/docs/policies/README-TEMPLATE.md) |
| `src/` | Java source |

> **Note:** Failing to follow the `CHANGELOG` or `README` specifications causes PR validation failures.

---

## 2. Group Changes

If the module starts a **new** service group, you must create a `sdk/<new-group>/` directory and a group-level `pom.xml` that aggregates the modules within it.

---

## 3. Parent POM Changes (`sdk/parents/azure-client-sdk-parent`)

### Adding to `modules` configuration

Add your module to the root `pom.xml` `<modules>` section **alphabetically**:

```xml
<modules>
  <!-- ... alphabetically sorted existing modules ... -->
  <module>sdk/myservice/azure-myservice/</module>
  <!-- ... -->
</modules>
```

### Adding to Javadoc generation

Add an entry to the aggregate Javadoc POM.

### Adding to codesnippets generation

See [JavaDoc with CodeSnippet](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/javadocs.md).

---

## 4. Versioning Management

Add a line to `eng/versioning/version_client.txt`:

```
com.azure:azure-myservice;1.0.0-beta.1;1.0.0-beta.1
```

Run the version-update script to propagate versions across POMs:

```bash
python eng/versioning/update_versions.py --sr
```

See [versioning.md](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/versioning.md) for full details.

---

## 5. Aggregate POM Changes

Add the module to aggregate POMs used by SpotBugs and JaCoCo (usually `pom.client.xml`).

---

## 6. Miscellaneous Changes

### Suppressing Swagger transform README failures

If your module uses code generation, add an entry to `eng/.docsettings.yml` under `known_content_issues` to suppress the generated README from failing PR structure validation.

### Configuring CODEOWNERS

For a new service group directory, add an entry to `.github/CODEOWNERS`:

```
/sdk/myservice/ @githubuser1 @githubuser2
```

---

## 7. Common Issues

- **Missing version in `version_client.txt`**: Run `python eng/versioning/update_versions.py --sr` to auto-fix.
- **Module not found in IntelliJ**: Ensure you added it to the root `pom.xml` `<modules>` section.
- **Checkstyle / SpotBugs failures**: Ensure parent POM is `azure-client-sdk-parent`.

---

## Examples

### Adding to an existing group (e.g. azure-core-mqtt)

1. Create `sdk/core/azure-core-mqtt/`
2. Add `pom.xml` with `azure-client-sdk-parent` as parent
3. Add to root `pom.xml` modules alphabetically
4. Add version entry to `eng/versioning/version_client.txt`
5. Add to aggregate POM `pom.client.xml`

### Adding to a new group (e.g. azure-newservice)

All steps above plus:
1. Create `sdk/newservice/` with group-level `pom.xml`
2. Add to `.github/CODEOWNERS`
3. Create `sdk/newservice/ci.yml` pipeline file

---

## See Also

- [versioning.md](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/versioning.md)
- [javadocs.md](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/javadocs.md)
- [TypeSpec Quickstart](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/typespec-quickstart.md)
- [Building](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/building.md)
