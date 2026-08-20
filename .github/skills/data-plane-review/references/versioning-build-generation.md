# Versioning, Build, and Generation Rules

## `DP-VERSION-01`: preserve stable public API compatibility

- **Rule ID:** `DP-VERSION-01`
- **Severity:** Blocking

<!-- Sources: Java guidelines java-versioning-backwards-compatibility and the
Dependable design principle; general design service API version guidance. -->

For a GA package, block removed or renamed public types or methods, narrowed
visibility, incompatible signature or return-type changes, and newly closed
extensible value sets. Do not block beta-to-beta breaks.

**Correct form:** keep the existing API, add the new API additively, and
deprecate the old API when replacement is necessary.

## `DP-VERSION-02`: align package stability with service API stability

- **Rule ID:** `DP-VERSION-02`
- **Severity:** Blocking

<!-- Sources: General design general-service-apiversion-1,
general-service-apiversion-2, general-service-apiversion-3, and
general-service-apiversion-4; Java guidelines java-versioning-highest-api and
java-versioning-select-api-version. -->

A stable package must not default to or exclusively target a preview service
API. A beta package defaults to the latest public preview; a stable package
defaults to the latest GA service API and lets users select supported versions.

**Correct form:** use a `-beta.N` package version while the default API is
preview, or default the stable package to a GA `ServiceVersion`.

## `DP-VERSION-03`: keep repository version metadata synchronized

- **Rule ID:** `DP-VERSION-03`
- **Severity:** Warning

<!-- Sources: Repository CONTRIBUTING.md "Versions and versioning",
docs/contributor/versioning.md "Updating Versions", and
docs/contributor/adding-a-module.md "Versioning Management". -->

Package, dependency, README, and central version metadata must agree. For a new
module, require its `version_client.txt` entry and update markers.

**Correct form:** update `eng/versioning/version_client.txt`, then run from the
repository root:

```bash
python eng/versioning/update_versions.py --sr
```

## `DP-BUILD-01`: declare only required dependencies with correct scope

- **Rule ID:** `DP-BUILD-01`
- **Severity:** Warning

<!-- Sources: Java guidelines java-module-requires and
java-auth-azure-identity-dependency; repository CONTRIBUTING.md version-tag
guidance. -->

Remove unused dependencies, add required ones, use test scope for test-only
dependencies, and use repository version markers. Never add compile-scope
`azure-identity`.

**Correct form:** use the minimum POM and `module-info.java` dependencies with
the appropriate `{x-version-update;...}` marker.

## `DP-BUILD-02`: use repository-standard module and POM structure

- **Rule ID:** `DP-BUILD-02`
- **Severity:** Warning

<!-- Sources: Java guidelines java-maven-pom through java-maven-developers and
java-module-info; repository docs/contributor/adding-a-module.md. -->

Use the standard client-library parent and inherited plugins. New modules
include required package files and registrations; do not duplicate inherited
configuration.

**Correct form:** follow `docs/contributor/adding-a-module.md`, including
aggregate POM, JavaDoc, code-snippet, version, CI, and CODEOWNERS registration.

## `DP-SPEC-01`: fix generated API shape at its maintained source

- **Rule ID:** `DP-SPEC-01`
- **Severity:** Warning

<!-- Sources: Repository AGENTS.md "SDK Generation" and
docs/contributor/typespec-quickstart.md. -->

Do not request a hand edit to generated Java for an API-shape defect. Identify
whether the likely source is TypeSpec, `client.tsp`, generator configuration,
or supported customization.

**Correct form:** update the maintained source, then run `tsp-client update`
from the package directory and review the regenerated output.
