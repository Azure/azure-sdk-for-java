# Java API Review Hub tools

The build-tools wrapper builds `sdk/tools/api-review-maven-plugin` from the pinned tooling checkout and stages its executable JAR plus `Generate-ApiReviewArtifact.ps1` into `bin`. No binary is checked in.

Both generation jobs reuse that bundle. Historical SDK tags need no review-tool sources or engineering scripts.

For local generation with prebuilt sources:

```powershell
pwsh eng\scripts\Stage-ApiReviewTools.ps1 -SourceDirectory . -BinOutputDirectory artifacts\api-review-tools
pwsh eng\scripts\Generate-ApiReviewArtifact.ps1 -RepositoryRoot . -PackageName azure-json -ToolJar artifacts\api-review-tools\api-review-maven-plugin.jar -SourcesJar sdk\serialization\azure-json\target\azure-json-1.6.0-beta.1-sources.jar -OutputDirectory artifacts\api-review
```

Tool builds require Maven and JDK 11+. Standalone generation requires Java 8+; SDK package builds also require Maven.
