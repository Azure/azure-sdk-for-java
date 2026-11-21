# Java API Review Hub test tools

This test branch contains the locally built executable `api-review-maven-plugin-1.0.0-beta.1.jar` from Azure/azure-sdk-tools#17586. It is temporary: replace the checked-in JAR with a pinned published artifact before merging the integration.

The build-tools wrapper stages the JAR and `Generate-ApiReviewArtifact.ps1` into `bin`. Both generation jobs use those staged files, so historical package tags need no processor sources or generation scripts. The generator uses only PowerShell, Maven for the SDK package build, and Java; it does not build or resolve review tooling.

For a local run with prebuilt sources:

```powershell
pwsh eng\scripts\Generate-ApiReviewArtifact.ps1 -RepositoryRoot . -PackageName azure-json -ToolJar eng\tools\api-review\api-review-maven-plugin-1.0.0-beta.1.jar -SourcesJar sdk\serialization\azure-json\target\azure-json-1.6.0-beta.1-sources.jar -OutputDirectory artifacts\api-review
```

For ARH, use the Azure-owned branch as `AZSDK_API_REVIEW_LANGUAGE_TOOLS_REF`; the shared pipeline and tools refs can remain `refs/heads/main`. The architect mapping is copied from Azure/azure-sdk-for-java#51236.
