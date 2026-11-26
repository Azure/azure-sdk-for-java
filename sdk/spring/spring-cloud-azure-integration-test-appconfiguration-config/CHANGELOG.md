# Release History

## 1.0.0 (Unreleased)

### Other Changes

- Migrate integration tests to ConfigData imports and bootstrap-registry client customization.
- Use DefaultAzureCredential, including Azure CLI authentication for local live runs.
- Evaluate remote feature flags through FeatureManager and verify Key Vault reference resolution.
- Separate credential-free Surefire checks from opt-in Failsafe live tests, and enable live execution in CI.
- Provision 101 pagination settings and verify pages of 100 and 1, continuation handling, and all bound values in credential-free and live tests.
- Cover labeled and unlabeled settings, comma-separated labels, overlapping selects, and profile-based label priority with shared provisioning fixtures and credential-free/live scenarios.
