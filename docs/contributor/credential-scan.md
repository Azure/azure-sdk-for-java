# Credential Scan (CredScan)

This guide describes how package owners can monitor their package's Credential Scanner (CredScan) status
and resolve findings. General information is available at [CredScan documentation][credscan_doc].

---

## What Is CredScan?

CredScan (Credential Scanner) scans source files for accidental credential strings
(API keys, passwords, connection strings, etc.) that should not be committed to source control.

Current pipeline configuration integrates credential scanning through [1ES SDL configuration][credscan_config].
Scan scope depends on the run and template configuration. The [shared CredScan step template][credscan_template]
supports changed-file scanning for pull requests and service- or repository-scoped scanning for other runs.
Findings can fail a build, not just produce warnings.

---

## Finding Your Package's CredScan Status

Open the relevant SDK pipeline run in Azure DevOps and inspect its security/SDL stages or jobs.
Stage and job names vary by pipeline and template version. Search the logs for `CredScan`, `Credential Scanner`,
or `CredScan result analysis`, and inspect any published security reports.

Findings include the affected file and location. A build log may contain a line such as:

```
##[error]sdk/{service}/{package}/{file}.java:sdk/{service}/{package}/{file}.java(3,20)
```

The line and column identify where in the file the potential credential was detected.

---

## Resolving Findings

### True Positives (real credentials)

Treat a committed real credential as compromised:

1. Contact the credential owner and the EngSys team immediately at **azuresdkengsysteam@microsoft.com**.
2. Coordinate urgent revocation or rotation with the owner, and update services that depend on the credential.
3. Remove the leaked value from current source files, recordings, and other affected artifacts. Coordinate any
  Git history cleanup with repository maintainers.

Deleting a file or adding a suppression does not invalidate the credential or remove it from Git history.
Never suppress a real credential or paste it into a public issue, PR comment, or build log. Keep incident details
in private channels. For security vulnerabilities, follow the repository's [Reporting Security Issues][security_policy]
instructions. See also GitHub's [guidance for fixing exposed secrets][secret_remediation].

### False Positives (fake strings flagged by mistake)

Suppress false positives in [`eng/CredScanSuppression.json`](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/CredScanSuppression.json).

**Preferred strategies (most to least preferred):**

1. **Reuse an already-suppressed source file:** Import fake credentials from an accessible helper in the same SDK.
2. **Reuse an already-suppressed string:** Use a known-fake value already covered by a `placeholder` suppression.
3. **Create a `FakeCredentials.java` helper:** Isolate fake test credentials under `src/test/java/`, following the
   Java package structure, and add a narrowly scoped file suppression. Java helpers under `src/test/resources/`
   are not compiled as test sources.
4. **Add a string to the `placeholder` list:** Use this only when the other strategies are unsuitable; avoid lengthy strings.

### Suppression JSON structure

Add entries to the existing `suppressions` array; do not replace the file or remove unrelated entries.
Include `_justification` on each new entry to explain why the credential is fake or test-only.

The following example shows file and string suppressions as separate entries. Choose the appropriate
strategy and replace the illustrative path or value with your SDK's actual test helper or fake string:

```json
{
  "tool": "Credential Scanner",
  "suppressions": [
    {
      "file": [
        "sdk/myservice/mypackage/src/test/java/com/azure/myservice/FakeCredentials.java"
      ],
      "_justification": "File contains only fake credentials used by unit tests."
    },
    {
      "placeholder": [
        "fakePassword1234"
      ],
      "_justification": "Artificial password used by unit tests; not a real credential."
    }
  ]
}
```

After editing, validate the JSON and rerun the relevant pipeline scan to confirm that the intended
false positive is resolved without broadening the suppression to unrelated files or values.

---

## Guidelines

- Files that contain **only** fake credentials should be file-suppressed.
- Use test recording sanitizers to strip real credentials before recordings are committed.
- String-value suppression applies repo-wide; use it sparingly and only for verified fake values.
- File suppressions hide findings in the entire matched file. Keep helpers limited to fake test credentials
  and review later additions carefully.

---

## See Also

- [CredScan overview](https://aka.ms/credscan)
- [Suppression file](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/CredScanSuppression.json)

[credscan_doc]: https://aka.ms/credscan
[credscan_config]: https://github.com/Azure/azure-sdk-for-java/blob/main/eng/pipelines/templates/stages/1es-redirect.yml
[credscan_template]: https://github.com/Azure/azure-sdk-for-java/blob/main/eng/common/pipelines/templates/steps/credscan.yml
[security_policy]: https://github.com/Azure/azure-sdk-for-java/blob/main/SECURITY.md#reporting-security-issues
[secret_remediation]: https://docs.github.com/code-security/secret-scanning/managing-alerts-from-secret-scanning/resolving-alerts#fixing-alerts
