# Path Release Pipeline

The patch release pipeline is a pipeline designed to release a patch of updated azure-sdk-for-java Artifacts in preparation for release of the Azure Java SDK BOM (Bill of Materials).

## Prerequisites

The patch release pipeline differs from service directory pipelines in that the Artifacts list should change, every time, prior to this pipeline being run. The list of what Artifacts and external dependencies that needs updated, built, and released, to enable the BOM release, is computed by other Java scripts. The last part of this computation feeds in the computed artifacts list to the [Update-Artifacts-List-For-Patch-Release.ps1][update_for_release_script] script which, in turn, updates the [patch-release.yml][patch_release_yml] file's Artifacts and AdditionalModules lists. The updated patch-release.yml should be checked in along with the version_client.txt, pom, CHANGELOG and README updates when prepping for the patch release.

### [Update-Artifacts-List-For-Patch-Release.ps1][update_for_release_script]

The script uses the powershell-yml module to load up all the ci.yml files in the repository to create a dictionary. It then uses the input project list to output the Artifacts and AdditionalModules into the YmlToUpdate with the correct metadata. For example; If a library had skipPublishDocMs: true it would be preserved here. There's one additional thing that's being done by the script which makes the Artifacts list it generates different from other ci.yml files and that's the addition of the ServiceDirectory on each Artifact. The reason for this is because there's common tooling that needs the ServiceDirectory as part of it's input. Things like README and Changelog verification are good examples.

  **arguments:**
    **SourcesDirectory** The root of the repository.
    **YmlToUpdate** The yml file to update. *This should be the [patch-release.yml][patch_release_yml] file for the patch release pipeline*.
    **ProjectList** Comma separated list of artifacts.

**Example**:
./eng/scripts/Update-Artifacts-List-For-Patch-Release.ps1 -SourcesDirectory \$\(Build.SourcesDirectory\) -YmlToUpdate $(Build.SourcesDirectory)/eng/pipelines/patch-release.yml -ProjectList com.azure:azure-sdk-template,com.azure:azure-sdk-template-two,com.azure:azure-sdk-template-three

### [patch-release.yml][patch_release_yml]

This is the yml file that belongs to the [java - patch-release][java_patch_release] pipeline. It's updated by running [Update-Artifacts-List-For-Patch-Release.ps1][update_for_release_script] and it's expected that this will be checked into the branch where the patch release is being prepped prior to running.

### [java - patch-release][java_patch_release] pipeline

The [java - patch-release][java_patch_release] pipeline is manual only. There is no purpose to running this pipeline other than than to release a patch build. The pipeline will build, analyze, verify and release the set of Artifacts that are in the patch-release.yml.

### How build works in the patch build pipeline

Normal service directory pipelines are only concerned with the set of Artifacts that live within that service directory. Any interdependencies within that service directory use current versions of each other and any dependencies outside of that service directory use the dependency versions. When a artifacts from a service directory are released, the released version becomes the new dependency version and everything that depends on it gets the updated version. This is why monthly releases have a release order. The From Source runs, which are part of every test matrix, is how we know this process works.

The patch-release pipeline utilizes the same tools that the From Source runs use. The repository is prepped in such a way that everything is built from source, regardless of the service directory. This is what allows us to release a set of Artifacts, regardless of their original service directories, to be released as part of the same release job instead of having to order the releases.

## What the patch release pipeline does and doesn't do

**Release verifications are done in the AnalyzeAndVerify Job.** In the patch-release pipeline, verifications normally done as part of the release task in service directories pipelines are now being done as part of the AnalyzeAndVerify job. This can be done since there's no ambiguity as to whether or not the artifacts are going to be released.

**Testing is not done in the patch-release pipeline.** Unlike service directory pipelines, there are no tests being run. We have a highly customizable test matrix which many service directories have made changes to. Because of this, it's nearly impossible to rectify a test matrix for a set of artifacts that spans the entire repository. There is a mitigation for this; as part of the preparation for patch release versions, poms, CHANGELOG and README files all need to be updated. These updates will cause each individual service directory pipeline to get run as part of the process for the update PR. In between the update PR and kicking off the patch-release pipeline, there should be no outside changes that aren't related to the aforementioned update. The matrix of FromSource and non-FromSource runs should provide enough verification. We already make the same assumptions when we release service directory, update the dependency version and then release a service directory that depends on what was previously released.

## Testing changes/updates to the patch-release pipeline

The easiest way to test the patch release pipeline is to use the example above which sets the Artifacts list to the
list of template libraries. After running, add the following parameter **TestPipeline**: **true**. This will allow the release of the template libraries without any version preparation, just run and release. The parameter will cause the test pipeline version code, which sets template libraries to a new beta version, based upon the buildId, for release and then auto closes the version increment PR.

<!-- LINKS -->
[java_patch_release]: https://dev.azure.com/azure-sdk/internal/_build?definitionId=5015&_a=summary
[update_for_release_script]: https://github.com/Azure/azure-sdk-for-java/blob/main/eng/scripts/Update-Artifacts-List-For-Patch-Release.ps1
[patch_release_yml]: https://github.com/Azure/azure-sdk-for-java/blob/main/eng/pipelines/patch-release.yml
