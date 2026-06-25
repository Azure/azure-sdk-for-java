# RevAPI failure report

Baseline command:

```powershell
mvn clean install
```

Result: the build failed in `org.revapi:revapi-maven-plugin:0.15.1:check` while comparing
`com.azure:azure-ai-projects:2.1.0` against `2.2.0-beta.1`. Tests and coverage completed before the
RevAPI check.

## Version resolution

RevAPI resolves the old artifact from Maven's active repositories because this package does not set
`revapi.oldArtifacts` or `revapi.oldVersion`. The inherited parent config sets
`<versionFormat>^\d+\.\d+\.\d+$</versionFormat>`, so `RELEASE` resolves to the latest stable version
matching that pattern and excludes beta versions. In this build, that resolved to
`com.azure:azure-ai-projects:2.1.0`; the new artifact is the local project version,
`2.2.0-beta.1`.

## Beta-annotated surface

The 2.1.0 source jar contains no `@Beta` annotations. The only RevAPI failure on a type that is
`@Beta` in the current source is:

| Current beta type | RevAPI change |
| --- | --- |
| `RoutineRun` | `getStatus()` return type changed from `String` to `BinaryData`. |

`RoutineRun` is currently annotated with `@Beta(warningText = "Preview API. Routines=V1Preview")`,
but it was not `@Beta` in the 2.1.0 source.

## Other non-beta API changes

These RevAPI failures are outside the beta-annotated surface:

| Area | RevAPI changes |
| --- | --- |
| `ModelVersion` | Removed `getSystemData()`, which returned `SystemDataV3`. |
| Removed models | `SystemDataV3` was removed. It was not `@Beta` in 2.1.0. |

## Least-verbose exception shape

The concise exception set is three exact RevAPI rules:

1. `java.method.removed` for `ModelVersion.getSystemData()`.
2. `java.class.removed` for `SystemDataV3`.
3. `java.method.returnTypeChanged` for `RoutineRun.getStatus()`.

Those exceptions are in `revapi.json` and appended from `pom.xml` so the inherited RevAPI config remains active.
