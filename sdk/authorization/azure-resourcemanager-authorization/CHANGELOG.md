# Release History

## 2.54.0-beta.1 (2026-04-09)

### Breaking Changes

#### `models.PermissionGetResult` was removed

#### `models.ProviderOperationsMetadataListResult` was removed

#### `models.RoleEligibilityScheduleListResult` was removed

#### `models.RoleManagementPolicyAssignmentListResult` was removed

#### `models.RoleEligibilityScheduleRequestListResult` was removed

#### `models.RoleAssignmentScheduleListResult` was removed

#### `models.RoleEligibilityScheduleInstanceListResult` was removed

#### `models.EligibleChildResourcesListResult` was removed

#### `models.RoleAssignmentScheduleRequestListResult` was removed

#### `models.DenyAssignmentListResult` was removed

#### `models.RoleAssignmentScheduleInstanceListResult` was removed

#### `models.RoleDefinitionListResult` was removed

#### `models.RoleManagementPolicyListResult` was removed

#### `models.ClassicAdministratorListResult` was removed

#### `models.RoleAssignmentListResult` was removed

#### `models.PolicyAssignmentPropertiesRoleDefinition` was modified

* `PolicyAssignmentPropertiesRoleDefinition()` was changed to private access
* `withId(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.PolicyPropertiesScope` was modified

* `PolicyPropertiesScope()` was changed to private access
* `withType(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.PolicyAssignmentPropertiesScope` was modified

* `PolicyAssignmentPropertiesScope()` was changed to private access
* `withId(java.lang.String)` was removed
* `withType(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.PolicyProperties` was modified

* `PolicyProperties()` was changed to private access

#### `models.PolicyAssignmentPropertiesPolicy` was modified

* `PolicyAssignmentPropertiesPolicy()` was changed to private access
* `withId(java.lang.String)` was removed
* `withLastModifiedDateTime(java.time.OffsetDateTime)` was removed

#### `models.ExpandedPropertiesPrincipal` was modified

* `ExpandedPropertiesPrincipal()` was changed to private access
* `withType(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withEmail(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.ResourceType` was modified

* `ResourceType()` was changed to private access
* `withOperations(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.PolicyAssignmentProperties` was modified

* `PolicyAssignmentProperties()` was changed to private access
* `withRoleDefinition(models.PolicyAssignmentPropertiesRoleDefinition)` was removed
* `withPolicy(models.PolicyAssignmentPropertiesPolicy)` was removed
* `withScope(models.PolicyAssignmentPropertiesScope)` was removed

#### `models.ExpandedProperties` was modified

* `ExpandedProperties()` was changed to private access
* `withPrincipal(models.ExpandedPropertiesPrincipal)` was removed
* `withScope(models.ExpandedPropertiesScope)` was removed
* `withRoleDefinition(models.ExpandedPropertiesRoleDefinition)` was removed

#### `models.ProviderOperation` was modified

* `ProviderOperation()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `withOrigin(java.lang.String)` was removed
* `withIsDataAction(java.lang.Boolean)` was removed
* `withDescription(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withProperties(java.lang.Object)` was removed

#### `models.Principal` was modified

* `Principal()` was changed to private access
* `withType(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withEmail(java.lang.String)` was removed

#### `models.ExpandedPropertiesRoleDefinition` was modified

* `ExpandedPropertiesRoleDefinition()` was changed to private access
* `withId(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.ExpandedPropertiesScope` was modified

* `ExpandedPropertiesScope()` was changed to private access
* `withType(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

### Features Added

* `models.AccessReviewScheduleDefinitionStatus` was added

* `models.OperationDisplay` was added

* `models.DenyAssignmentEffect` was added

* `models.AzureRolesAssignedOutsidePimAlertIncidentProperties` was added

* `models.AccessReviewDecisionInsight` was added

* `models.AccessReviewHistoryDefinitionStatus` was added

* `models.AccessReviewActorIdentityType` was added

* `models.AccessReviewDecisionInsightProperties` was added

* `models.AccessReviewDecisionIdentity` was added

* `models.AttributeNamespaceCreateRequest` was added

* `models.AccessReviewDecisionInsightType` was added

* `models.AccessReviewInstanceStatus` was added

* `models.AccessReviewRecurrencePatternType` was added

* `models.DecisionTargetType` was added

* `models.RecordAllDecisionsResult` was added

* `models.AccessReviewScheduleDefinitionReviewersType` was added

* `models.SettableResource` was added

* `models.AccessRecommendationType` was added

* `models.AzureRolesAssignedOutsidePimAlertConfigurationProperties` was added

* `models.SeverityLevel` was added

* `models.DefaultDecisionType` was added

* `models.TooManyPermanentOwnersAssignedToResourceAlertConfigurationProperties` was added

* `models.PIMOnlyModeSettings` was added

* `models.TooManyPermanentOwnersAssignedToResourceAlertIncidentProperties` was added

* `models.UsersOrServicePrincipalSet` was added

* `models.AccessReviewScopePrincipalType` was added

* `models.DenyAssignmentPrincipal` was added

* `models.UsersOrServicePrincipalSetUserType` was added

* `models.RoleManagementPolicyPimOnlyModeRule` was added

* `models.RecordAllDecisionsProperties` was added

* `models.AccessReviewReviewer` was added

* `models.AccessReviewInstanceReviewersType` was added

* `models.AccessReviewResult` was added

* `models.AccessReviewRecurrenceRangeType` was added

* `models.TooManyOwnersAssignedToResourceAlertConfigurationProperties` was added

* `models.AccessReviewReviewerType` was added

* `models.ExcludedPrincipalTypes` was added

* `models.DuplicateRoleCreatedAlertConfigurationProperties` was added

* `models.AccessReviewScopeAssignmentState` was added

* `models.DuplicateRoleCreatedAlertIncidentProperties` was added

* `models.AccessReviewDecisionServicePrincipalIdentity` was added

* `models.PIMOnlyMode` was added

* `models.AccessReviewApplyResult` was added

* `models.AccessReviewDecisionUserSignInInsightProperties` was added

* `models.DecisionResourceType` was added

* `models.AccessReviewDecisionUserIdentity` was added

* `models.AlertIncidentProperties` was added

* `models.AccessReviewDecisionPrincipalResourceMembershipType` was added

* `models.TooManyOwnersAssignedToResourceAlertIncidentProperties` was added

#### `models.RoleManagementPolicyRuleType` was modified

* `ROLE_MANAGEMENT_POLICY_PIM_ONLY_MODE_RULE` was added

#### `models.RoleManagementPolicyExpirationRule` was modified

* `exceptionMembers()` was added
* `withExceptionMembers(java.util.List)` was added

## 2.53.8 (2026-03-30)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.53.5` to version `2.54.0`.


## 2.53.7 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded core dependencies.

## 2.53.6 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.5 (2025-11-14)

### Other Changes

- Improved Javadoc on classes.

## 2.53.4 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.3 (2025-09-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.2 (2025-08-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.1 (2025-08-05)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.0 (2025-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.52.0 (2025-06-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.51.0 (2025-05-26)

### Features Added

- Added `ACR_PULL` to `BuiltInRole`.

## 2.50.0 (2025-04-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.48.0 (2025-02-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.47.0 (2025-01-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.46.0 (2024-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.45.0 (2024-11-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.44.0 (2024-10-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.43.0 (2024-09-27)

### Features Added

- Added `Quota Request Operator` role to `BuiltInRole`.

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.42.0 (2024-08-23)

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

## 2.41.0 (2024-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.40.0 (2024-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.39.0 (2024-05-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.38.0 (2024-04-16)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.0 (2024-02-29)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.35.0 (2024-01-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.34.0 (2023-12-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.33.0 (2023-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.32.0 (2023-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.31.0 (2023-09-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.30.0 (2023-08-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.29.0 (2023-07-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.29.0-beta.1 (2023-07-19)

- Preview release for `api-version` `2022-05-01-preview`.

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.24.0 (2023-02-17)

### Bugs Fixed

- Fixed a bug that `Permission.dataActions()` and `Permission.notDataActions()` return wrong results.

## 2.23.0 (2023-01-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.22.0 (2022-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.21.0 (2022-11-24)

### Breaking Changes

- `filter` parameters in list API from `RoleAssignmentsClient` is required to be encoded by user.

### Other Changes

#### Dependency Updates

- Updated `api-version` of authorization to `2022-04-01`.

## 2.20.0 (2022-10-26)

### Features Added

- Supported description in role assignment.

## 2.19.0 (2022-09-23)

### Bugs Fixed

- Supported delayed retry on 404 for eventual consistency, after creating AAD service principal.
- Improved the delayed retry on 400 for service principal, when creating role assignment. Now the retry will continue for only about a minute.

## 2.18.0 (2022-08-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.17.0 (2022-07-25)

### Bugs Fixed

- Supported delayed retry on 404 for eventual consistency, after creating AAD application.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Breaking Changes

- Removed `DenyAssignmentsClient` as it is preview feature.

### Other Changes

#### Dependency Updates

- Updated `api-version` of authentication to `2020-10-01`.

## 2.14.0 (2022-04-11)

### Features Added

- Supported Azure Kubernetes Service related roles to `BuiltInRole`.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Other Changes

#### Dependency Updates

- Updated `api-version` of authentication to `2020-08-01-preview`

## 2.10.0 (2021-11-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated core dependency from resources

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Supported key vault data related roles to `BuiltInRole`, for RBAC authorization of data access to data in `Vault`

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Supported `listByServicePrincipal` in `RoleAssignments`
- Updated API from `AAD Graph` to `Microsoft Graph`. New permission needs to be granted before calling the API, [Reference](https://docs.microsoft.com/graph/permissions-reference)
- Removed `applicationPermissions` in `ActiveDirectoryApplication`
- Removed `signInName` in `ActiveDirectoryUser`
- Removed `withPasswordValue` in `PasswordCredential.Definition`
- Supported `withPasswordConsumer` in `PasswordCredential.Definition` to consume the password value.

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Supported `listByFilter` in `ActiveDirectoryApplications`, `ActiveDirectoryGroups`, `ActiveDirectoryUsers`, `ServicePrincipals`

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
