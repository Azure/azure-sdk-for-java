The scripts in this folder are used to create(or update) [AzureDevOps] release work-items.

Steps to use these scripts:
1. Update `releaseDate` and `jcaReleaseVersion` in `Update-DevOps-Release-WorkItems-For-KeyVaultJCA.ps1`.
1. Execute `.\sdk\spring\scripts\Update-DevOps-Release-WorkItem\Update-DevOps-Release-WorkItems-For-KeyVaultJCA.ps1`
1. Do above several steps to other scripts: `Update-DevOps-Release-WorkItems-For-xxx.ps1`.
1. Check the result in [AzureDevOps].
   1. Confirm new item created(or updated).
   1. Update the new item's assignee to yourself.
   1. For the same artifact, set other items' status to `Next Release Unknown`.
1. Create PR to save the diff about these scripts.

Refs:
1. [AzureDevOps]
2. [Marking a Package as In Release]

[AzureDevOps]: https://dev.azure.com/azure-sdk/release/_queries/query/d6ab565e-5729-40ba-83a3-a1eb8962982c/
[Marking a Package as In Release]: https://dev.azure.com/azure-sdk/internal/_wiki/wikis/internal.wiki/444/Marking-a-Package-as-In-Release