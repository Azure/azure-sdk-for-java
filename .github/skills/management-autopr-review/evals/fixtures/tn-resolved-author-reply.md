# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-widgets]-generated-from-SDK Generation`
Author: `azure-sdk`
Base: `main`
Draft: `false`
Release Plan link: https://example.com/

PR: `Azure/azure-sdk-for-java#12345`
Head SHA: `2222222222222222222222222222222222222222`
Package: `azure-resourcemanager-widgets`
Package version: `2.0.0`

## Latest workflow comment

<!-- management-autopr-review -->
Head SHA: `1111111111111111111111111111111111111111`
Decision: `human attention requested`

### Warning

- `[MGMT-BREAKING]` New — `sdk/widgets/azure-resourcemanager-widgets/CHANGELOG.md`
  release `2.0.0` removes `WidgetManager.withLegacyMode(boolean)`. Confirm
  that the GA breaking change is intended.

### Informational

- `[MGMT-API-VERSION]` New — the same release uses API version `2026-04-01`.

## Author reply

Comment ID: `71106`
Author: `azure-sdk` (the PR author)
Head SHA when posted: `2222222222222222222222222222222222222222`

> The removal of `WidgetManager.withLegacyMode(boolean)` is intentional for
> version 2.0.0. The service team has confirmed this GA breaking change.

## Current PR diff and head

The module already exists under `sdk/widgets` on the base branch, whose latest
release is `1.0.0`. This PR adds the following release entry. It remains at the
current head:

`sdk/widgets/azure-resourcemanager-widgets/CHANGELOG.md`:

```markdown
## 2.0.0 (Unreleased)

### Breaking Changes

- Removed `WidgetManager.withLegacyMode(boolean)`.

### Other Changes

- Package api-version 2026-04-01.
```

The PR removes this public method from the existing `WidgetManager` class in
`sdk/widgets/azure-resourcemanager-widgets/src/main/java/com/azure/resourcemanager/widgets/WidgetManager.java`:

```diff
- public WidgetManager withLegacyMode(boolean enabled);
```
