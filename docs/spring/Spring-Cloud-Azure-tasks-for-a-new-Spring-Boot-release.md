# Background

When Spring Boot release a new version, Spring Cloud Azure team should do something to supported latest release version.

Spring boot have the following types of versions:
 - BUILD-SNAPSHOT: Current development release.
 - M[number]: Milestone release.
 - RC[number]: Release Candidate.
 - RELEASE: GA, for General Availability.

You can refer to [Spring Projects Version Naming](https://www.baeldung.com/spring-projects-version-naming) to get more information.


# Tasks For Each Release 

## Snapshot Release
We don't need to do anything about this kind of release.

## Milestone Release And Release Candidate
- [ ] Create a new issue to track the following tasks. Sample: [#27864](https://github.com/Azure/azure-sdk-for-java/issues/27864). And add the new created issue into [#28518](https://github.com/Azure/azure-sdk-for-java/issues/28518)
- [ ] Create **draft** PR to upgrade external dependencies' version according to spring-boot-dependencies and spring-cloud-dependencies. Refer to [related readme](https://github.com/Azure/azure-sdk-for-java/blob/aec4c6247ba7ba4de57dd866e3f5511ca4fbd387/sdk/spring/scripts/README.md)
- [ ] Run tests by commands:
  - `/azp run java - spring - tests`
  - `/azp run java - cosmos - tests`
  - `/azp run java - keyvault - tests`
- [ ] Fix the test failures.
- [ ] Close draft PR.

Sample PR: [#24670](https://github.com/Azure/azure-sdk-for-java/pull/24670).

## GA Versions
- Before azure-sdk-for-java in band release
  - [ ] Create a new issue to track the following tasks. Sample: [#28498](https://github.com/Azure/azure-sdk-for-java/issues/28498). And add the new created issue into [#28518](https://github.com/Azure/azure-sdk-for-java/issues/28518)
  - [ ] Read spring boot release notes. Sample: [Spring-Boot-2.6-Release-Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.6-Release-Notes). Write down items we(Spring Cloud Azure team members) should care about. Just write these items by adding a new comment in the GitHub issue created in previous step.
  - [ ] Upgrade external dependencies' version according to spring-boot-dependencies and spring-cloud-dependencies. Refer to [related readme](https://github.com/Azure/azure-sdk-for-java/blob/aec4c6247ba7ba4de57dd866e3f5511ca4fbd387/sdk/spring/scripts/README.md). **NOTE**: This step should be finished before `Core code complete`. You can get more information about `Core code complete` in the Email with subject like `APRIL Release Kickoff`.

- After azure-sdk-for-java in band release.

  Update spring-boot and spring-cloud version in these places:

  - [ ] [azure-spring-boot-samples](https://github.com/Azure-Samples/azure-spring-boot-samples)
  - [ ] [Reference doc](https://github.com/microsoft/spring-cloud-azure/tree/docs/docs/src/main/asciidoc).
  - [ ] [MS docs](https://docs.microsoft.com/azure/developer/java/spring-framework/).
  - [ ] Doc: Spring Cloud Azure Timeline
  - [ ] Doc: Spring Versions Mapping
