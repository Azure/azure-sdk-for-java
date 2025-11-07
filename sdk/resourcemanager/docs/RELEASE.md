# API-version upgrade and SDK release

## Table of Contents

- [Prepare SDK PR](#1-prepare-sdk-pr)
  - [Swagger](#swagger)
    - [Generate from new release tag (API-version)](#generate-from-new-release-tag-api-version)
    - [Prepare release](#prepare-release)
  - [TypeSpec](#typespec)
- [Dev on SDK PR](#2-dev-on-sdk-pr)
  - [Review breaking changes (if any)](#review-breaking-changes-if-any)
  - [Record test](#record-test)
  - [Local verification](#local-verification)
  - [PR review](#pr-review)
- [Release](#3-release)
- [Post Release](#4-post-release)

## 1. Prepare SDK PR

### Swagger

Example PR: https://github.com/Azure/azure-sdk-for-java/pull/47139

#### Generate from new release tag (API-version)
- Confirm the library you want to release, e.g. `network`.
- Update `api-specs.json`, update the release tag for the library.
- Run `gulp codegen --projects <service>`. E.g. `gulp codegen --projects network`.

#### Prepare release
- Determine the next stable version to release.
- Update current version of the library to the new stable version, as well as the `unreleased` entry of it. E.g. https://github.com/Azure/azure-sdk-for-java/pull/46981/commits/cfc6d2a93cfab113103642f54023b8f3d9bfe3b1#diff-8c575b582f7315a913d70b6b0e50888a67a278187ccb7f8d4b8ceb2cb2d67959
- Run `python eng/versioning/update_versions.py --sr` to update library version for pom.xml.
- Run `python eng/versioning/update_versions.py` to update readme. Only take changes in target library directory. Revert others.
- Update CHANGELOG.md
  - In target library's directory's CHANGELOG.md, update unreleased version to the new version, as well as the release date.
  - In `sdk/resourcemanager/azure-resourcemanager`'s CHANGELOG.md, add an entry for the library to release. E.g. https://github.com/Azure/azure-sdk-for-java/pull/46981/commits/e2f550870ff3fd2fc972de6e66a46bd7eaad0f9d

### TypeSpec
Service usually do self-serve for already `TypeSpec`ed spec. If not, run [self-serve pipeline](https://dev.azure.com/azure-sdk/internal/_build?definitionId=7421).
Parameters are self-explained. Do ensure `Create SDK pull request` is checked.

Example PR: https://github.com/Azure/azure-sdk-for-java/pull/47100

## 2. Dev on SDK PR
Pull PR branch to your local repository. Further development will base on this branch.

### Review breaking changes (if any)
Pay attention to `CHANGELOG.md`'s `Breaking Changes` section. Check each breaking change's reason.
If it's acceptable, add suppression in [revapi.json](https://github.com/Azure/azure-sdk-for-java/blob/main/eng/lintingconfigs/revapi/track2/revapi.json).

### Record test
Refer [Record test](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager-test/README.md#record-test).

### Local verification
Run `mvn verify` and check for failures. If it passes, push local changes to the PR branch.

### PR review
Request for PR review. Merge PR after approval.

## 3. Release
In Azure DevOps's internal pipelines, look for `java - xx` pipeline. They can be either `java - containerservice` or `java - azure-resourcemanager-cosmos` depending on different services.
If former, there should be an e.g. `azure-resourcemanager-containerservice` choice unselected by default. Make sure to de-select data-plane SDK and select mgmt SDK.
If latter, there should be no choice.
Run and approve release.

## 4. Post Release
Once release is complete, there should be a follow-up `Increment versions after xx release` PR. E.g. https://github.com/Azure/azure-sdk-for-java/pull/46982
Approve and merge the PR.
