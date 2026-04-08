# Credential Scan (CredScan)

> **Canonical source**: [`doc/dev/CredScan-Process.md`](../../doc/dev/CredScan-Process.md)  
> **Source**: Summarized for contributor reference (last reviewed April 2026).

This document summarises the key steps. For the full process, see the canonical source above.

---

## What Is CredScan?

CredScan (Credential Scanner) scans the repository nightly for accidental credential strings
(API keys, passwords, connection strings, etc.) that should not be committed to source control.

It runs in the `java - aggregate-reports` Azure DevOps pipeline.  
Findings appear in the "Post Analysis" → "ComplianceTools" job.

---

## Finding Your Package's CredScan Status

Look for lines in the build log like:

```
##[error]sdk/{service}/{package}/{file}.java:sdk/{service}/{package}/{file}.java(3,20)
```

The row and column identify where in the file the potential credential was detected.

---

## Resolving Warnings

### True Positives (real credentials)

Contact the EngSys team immediately at **azuresdkengsysteam@microsoft.com**.

### False Positives (fake strings flagged by mistake)

Suppress false positives in [`eng/CredScanSuppression.json`](../../eng/CredScanSuppression.json).

**Preferred strategies (most to least preferred):**

1. **Import from an already-suppressed file** — use a fake credential already in a suppressed source file.
2. **Use an already-suppressed string** — if the string is already in the `placeholder` list, reuse it.
3. **Create a `FakeCredentials.java` file** — move all fake credentials into this file and suppress the file path in the JSON.
4. **Add the string to the `placeholder` list** — last resort; avoid for lengthy strings.

### Suppression JSON structure

```json
{
  "tool": "CredScan",
  "suppressions": [
    {
      "placeholder": [
        "fakePassword1234"
      ],
      "file": [
        "sdk/myservice/mypackage/src/test/resources/FakeCredentials.java"
      ]
    }
  ]
}
```

---

## Guidelines

- Files that contain **only** fake credentials should be file-suppressed.
- Use test recording sanitizers to strip real credentials from recordings.
- String-value suppression disables the warning repo-wide — use sparingly.

---

## See Also

- [CredScan overview](https://aka.ms/credscan)
- [Suppression file](../../eng/CredScanSuppression.json)
