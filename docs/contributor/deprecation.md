# Deprecation Process

> **Short link**: [aka.ms/azsdk/java/deprecation-process](https://aka.ms/azsdk/java/deprecation-process)

This document describes how to mark a package deprecated on Maven.

---

## Overview

Maven has no official deprecation mechanism. The recommended approach is:

1. Add a deprecation notice to `README.md`, the POM description, and `CHANGELOG.md`.
2. Push a final release to Maven.

---

## Step 1 – Update Files in the Repository

### `README.md` — Add a deprecation disclaimer

```markdown
> Please note, this package has been deprecated and will no longer be maintained
> after `<EOLDate>`. We encourage you to upgrade to the replacement package,
> `<Replacement>`, to continue receiving updates. Refer to the migration guide
> `<MigrationGuideURL>` for guidance on upgrading. Refer to our
> [deprecation policy](https://aka.ms/azsdk/support-policies) for more details.
```

### `CHANGELOG.md` — Add a version entry with the same disclaimer

```markdown
## 1.0.1 (2024-01-15)

> Please note, this package has been deprecated ...
```

### `pom.xml` — Add the disclaimer to `<description>`

```xml
<description>Please note, this package has been deprecated ...</description>
```

### `eng/versioning/version_client.txt` — Align current-version with the release

If `current-version` is ahead of the intended release version, set it to the release version:

```diff
- com.azure:azure-example;1.0.0;1.1.0-beta.1
+ com.azure:azure-example;1.0.0;1.0.1
```

Submit a PR to `main` and post it in the [Java language channel](https://teams.microsoft.com/l/channel/19%3a5e673e41085f4a7eaaf20823b85b2b53%40thread.skype/Language%2520-%2520Java?groupId=3e17dcb0-4257-4a30-b843-77f47f1d4121&tenantId=72f988bf-86f1-41af-91ab-2d7cd011db47).

---

## Step 2 – Trigger a Release

Trigger the library's standard release pipeline as documented in:

<https://aka.ms/azsdk/release-checklist>

---

## See Also

- [Release Checklist](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/release-checklist.md)
- [Versioning](https://github.com/g2vinay/azure-sdk-for-java/blob/consolidate-docs-v2/docs/contributor/versioning.md)
- [Azure SDK Support Policies](https://aka.ms/azsdk/support-policies)
