<#
.SYNOPSIS
Converts the files changed in a PR to what sparse checkout needs to check out for CredScan running.

.DESCRIPTION
Given a list of file paths modified in a PR this script will:

- Add a leading '/' to the file path for git sparse checkout.
- When many files have changed this will compact individual files to whole directory check outs.

At the end of processing this will set the DevOps variable 'SparseCheckoutDirectories'.
#>

$changedFiles = eng/common/scripts/get-changedfiles.ps1

# Loop over each file changed and determine which directory to check out and add a leading '/' if needed.
$changedFiles = $changedFiles | ForEach-Object {
    $changedFile = $_
    if ($changedFile -match "sdk/.*?/") {
        $changedFile = $matches[0]
    } elseif ($changedFile -match ".*/.*?/") {
        $changedFile = $matches[0]
    } elseif ($changedFile -match ".*/.*?") {
        $changedFile = $matches[0]
    }

    if ($changedFile.StartsWith("/")) {
        $changedFile
    } else {
        "/$changedFile"
    }
}

$tmp = ConvertTo-Json @($changedFiles | Sort-Object | Get-Unique) -Compress
Write-Host "##vso[task.setvariable variable=SparseCheckoutDirectories;]$tmp"
