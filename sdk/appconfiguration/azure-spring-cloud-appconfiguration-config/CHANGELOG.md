# Release History

## 2.0.0-beta.2 (Unreleased)


## 2.0.0-beta.1 (2021-05-04)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-cloud-azure-appconfiguration-config` to `azure-spring-cloud-appconfiguration-config`.
- Format and options of library configuration has completely changed. See Readme in Starter
- Use of a Watch Key is now required see `spring.cloud.azure.appconfiguration.stores[0].monitoring.triggers`
- Added support for JSON content type
- Feature Management config loading is no longer on by default.
- Users can now select multiple groups of keys from one store see `spring.cloud.azure.appconfiguration.stores[0].selects`. Same default select happens as before.
- By default, `spring.profiles.active` is used as the label of all filters. This can be overridden using selects. If no profile is set `\0` is used i.e. `(No Label)
