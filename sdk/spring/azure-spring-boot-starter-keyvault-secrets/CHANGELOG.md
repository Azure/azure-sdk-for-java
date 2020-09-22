# Release History

## 3.0.0-beta.1 (Unreleased)
### Breaking Changes 
 - Change configure item from `azure.keyvault.secret.keys` to `azure.keyvault.secret-keys`
 - Change configure item from `azure.keyvault.allow.telemetry` to `azure.keyvault.allow-telemetry`


## 2.3.5 (2020-09-14)
### Key Bug Fixes 
- Exclude disabled secrets when getting Key Vault secrets

## 2.3.3 (2020-08-13)
### New Features
- Support connection to multiple Key Vault from a single application configuration file 
- Support case sensitive keys in Key Vault 
- Key Vault Spring Boot Actuator 

### Breaking Changes 
- Revamp KeyVault refreshing logic to avoid unnecessary updates. 
 
### Key Bug Fixes 
- Address CVEs and cleaned up all warnings at build time. 
