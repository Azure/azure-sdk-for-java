# Pull request snapshot

Title: `[AutoPR azure-resourcemanager-containerservicepreparedimgspec]-generated-from-SDK Generation`
Author: `app/azure-sdk-automation`
Base: `main`
Draft: `false`
Release Plan link: https://azsdk-releaseplan-dashboard-hveph5aqhhcfhtgu.westus-01.azurewebsites.net/?releaseplan=35926

New module:
`sdk/containerservice/azure-resourcemanager-containerservicepreparedimgspec/pom.xml`

Before this PR, `sdk/containerservice` already contains
`azure-resourcemanager-containerservice`, with its own service-level POM and CI
configuration. The new module is added into those same service-level files.

Changed Java adds the new `containerservicepreparedimgspec` client.
