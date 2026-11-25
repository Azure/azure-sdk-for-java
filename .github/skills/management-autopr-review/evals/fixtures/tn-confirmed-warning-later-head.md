# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-widgets]-generated-from-SDK Generation`
Author: `azure-sdk`
Base: `main`
Draft: `false`
Release Plan link: https://example.com/

PR: `Azure/azure-sdk-for-java#12345`
Head SHA: `4444444444444444444444444444444444444444`
Package: `azure-resourcemanager-widgets`
Package version: `2.0.0`

## Workflow comment at the first head

<!-- management-autopr-review -->
Head SHA: `1111111111111111111111111111111111111111`
Decision: `human attention requested`

### Warning

- `[MGMT-BREAKING]` New — `sdk/widgets/azure-resourcemanager-widgets/CHANGELOG.md`
  release `2.0.0` removes `WidgetManager.withLegacyMode(boolean)`. Confirm
  that the GA breaking change is intended.

## Author reply at the second head

Comment ID: `71106`
Author: `azure-sdk` (the PR author)
Head SHA when posted: `2222222222222222222222222222222222222222`

> The removal of `WidgetManager.withLegacyMode(boolean)` is intentional for
> version 2.0.0. The service team has confirmed this GA breaking change.

## Workflow comment at the second head

<!-- management-autopr-review -->
Head SHA: `2222222222222222222222222222222222222222`
Decision: `no high-confidence concerns`

### Warning

- `[MGMT-BREAKING]` Resolved — author reply `71106` confirms the removal of
  `WidgetManager.withLegacyMode(boolean)` in release `2.0.0` is intended.

## Latest workflow comment at the third head

<!-- management-autopr-review -->
Head SHA: `3333333333333333333333333333333333333333`
Package: `azure-resourcemanager-widgets`
Release type: `stable`
Decision: `no high-confidence concerns`

### Informational

- `[MGMT-API-VERSION]` Carried forward —
  `sdk/widgets/azure-resourcemanager-widgets/CHANGELOG.md` release `2.0.0`
  uses API version `2026-04-01`.

## Current PR diff and fourth head

The module exists under `sdk/widgets` on the base branch, whose latest release
is `1.0.0`. This PR adds the following release entry. It has not changed since
the second head:

`sdk/widgets/azure-resourcemanager-widgets/CHANGELOG.md`:

```markdown
## 2.0.0 (Unreleased)

### Breaking Changes

- Removed `WidgetManager.withLegacyMode(boolean)`.

### Other Changes

- Package api-version 2026-04-01.
```

The fourth head adds a `tags()` accessor to the existing public `WidgetManager`
class in
`sdk/widgets/azure-resourcemanager-widgets/src/main/java/com/azure/resourcemanager/widgets/WidgetManager.java`.
The earlier `withLegacyMode(boolean)` removal remains in the full PR diff.
