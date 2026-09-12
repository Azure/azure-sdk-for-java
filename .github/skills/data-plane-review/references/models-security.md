# Model and Security Rules

## `DP-MODEL-01`: make public models usable and contract-aligned

- **Rule ID:** `DP-MODEL-01`
- **Severity:** Warning

<!-- Sources: Java guidelines java-models-builder, java-models-constructors,
java-models-constructors-args, java-models-constructors-args-required,
java-models-fluent-api, java-models-javabeans, and java-models-interface. -->

Constructible models expose constructors for required properties and fluent
JavaBean setters for optional properties. Output-only models do not expose
unusable constructors or setters. Generated fields and types must match the
service contract.

**Correct form:** require mandatory values in the constructor and return
`this` from optional setters.

## `DP-ENUM-01`: keep service-defined value sets extensible

- **Rule ID:** `DP-ENUM-01`
- **Severity:** Warning

<!-- Sources: Java guidelines java-enums and java-expandable-enums. -->

Do not expose a Java `enum` for values the service may extend. A Java enum is
valid only when values are permanently fixed or input-only with low break risk.

**Correct form:** extend `ExpandableStringEnum<T>` and provide known constants
plus `fromString`.

## `DP-SECURITY-01`: keep credentials refreshable and out of output

- **Rule ID:** `DP-SECURITY-01`
- **Severity:** Blocking

<!-- Sources: Java guidelines auth-client-no-token-persistence,
java-auth-fluent-builder, java-auth-use-core, java-auth-azure-identity-dependency,
and java-auth-connection-strings; Java implementation
java-logging-no-sensitive-info; general design auth-client-no-token-persistence. -->

Block newly introduced credential persistence, readable credential access,
compile-scope `azure-identity`, unapproved connection-string construction, or
logging of non-allowlisted secret values. Do not report ordinary credential
references or redacted logs.

**Correct form:** accept `TokenCredential` or `AzureKeyCredential` in the
builder, use the Azure Core authentication policy, and let the credential
refresh tokens.
