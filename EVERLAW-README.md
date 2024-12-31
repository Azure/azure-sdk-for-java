## Updating this package

This repository is a fork of Azure's OpenAI Java SDK (among other things).

The specific package we use is located in `sdk/openai/azure-ai-openai`.

### Merging upstream updates

If the original repository has changes that need to be included in our fork, follow the instructions [here](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/syncing-a-fork) for syncing our fork.

After the changes are merged, follow the instructions below for updating the version of this package and publishing a new version.

### Making local changes

If something specific to our fork needs to be changed, open a pull request targeting the `origin/main` branch of this repository.

Make your changes and find an appropriate reviewer to review them.

Follow testing instructions below.

Publish a new version according to the instructions below.

### Testing

If you build this package, it will install the built artifacts to your local Maven cache (usually located at `~/.m2/repository`).

You can then reference the version you just built in another project, and as long as you clean-build that project, it will pull in your updated code.

Follow the instructions for building from [the upstream's wiki](https://github.com/Azure/azure-sdk-for-java/wiki/Building#pomclientxml-vs-pomdataxml).

**Note:** Make sure you're using Java 11 to build the package and that you change back to 17 (or whatever version we're using) before working on the `servers` repo again.

**Note:** The most useful commands from the wiki are:

 - `mvn install -f eng/code-quality-reports/pom.xml ` for installing build tools (one-time setup usually)
 - `mvn install -f sdk/openai/pom.xml` for building the package we use

### Updating the version and publishing the package to Github

Once any changes are made and finalized, you'll want to bump the version we're hosting in our Github artifactory.

There are two steps:

1. Update the version

Our self-hosted version of this package started on `1.0.0-everlaw.0`.

Any time we pull in upstream changes or make changes ourselves, we should bump the suffix one time.

Pulling in upstream changes will often cause a merge conflict with the version number.

Track what the version number was before syncing the fork, and make sure it's updated _after_ you've resolved any conflicts and made any new local changes after syncing.

2. Publish the package

There's a `pyutil` script in the `servers` repository that will help you publish a new version of this library to our Github artifactory.

a. Activate the `pyutil` virtual environment and make sure all `pyutil` packages are installed.

b. Ensure you have a Github PAT that will allow you to read and write to our Github artifactory. Helpful instructions [here](https://everlaw.atlassian.net/wiki/spaces/ENG/pages/403963963/Github+Packages).

c. Ensure your local Maven cache has the current version of this package installed in it. It should be at `~/.m2/repository/com/azure/azure-ai-openai/<version>`.

d. Run `upload-maven-artifacts-to-github-packages --dir <path-to-artifact-version> --dry-run`. If no output is shown, rerun it without the `--dry-run` flag.

e. Go to [our artifactory](https://github.com/orgs/Everlaw/packages?repo_name=servers) and find the package. Ensure the latest version matches what you just uploaded.

f. Delete the artifacts located in your local Maven cache and try pulling in the new version from a project that uses it. This will ensure you correctly uploaded the package to Github.
