#!/usr/bin/env pwsh -c

<#
.DESCRIPTION
Creates a GitHub pull request for a given branch if it doesn't already exist
.PARAMETER RepoOwner
The GitHub repository owner to create the pull request against.
.PARAMETER RepoName
The GitHub repository name to create the pull request against.
.PARAMETER BaseBranch
The base or target branch we want the pull request to be against.
.PARAMETER PROwner
The owner of the branch we want to create a pull request for.
.PARAMETER PRBranch
The branch which we want to create a pull request for.
.PARAMETER AuthToken
A personal access token
#>
[CmdletBinding(SupportsShouldProcess = $true)]
param(
  [Parameter(Mandatory = $true)]
  $RepoOwner,

  [Parameter(Mandatory = $true)]
  $RepoName,

  [Parameter(Mandatory = $true)]
  $BaseBranch,

  [Parameter(Mandatory = $true)]
  $PROwner,

  [Parameter(Mandatory = $true)]
  $PRBranch,

  [Parameter(Mandatory = $true)]
  $AuthToken,

  [Parameter(Mandatory = $true)]
  $PRTitle,
  $PRBody = $PRTitle
)

Write-Host $MyInvocation.Line

$query = "state=open&head=${PROwner}:${PRBranch}&base=${BaseBranch}"

Write-Host "Got Here 2"

$resp = Invoke-RestMethod "https://api.github.com/repos/$RepoOwner/$RepoName/pulls?$query"
$resp | Write-Verbose

Write-Host "Got Here 3"

if ($resp.Count -gt 0)
{
  Write-Host -f green "Pull request already exists $($resp[0].html_url)"
} else
{
  Write-Host "Got Here 4"
  $headers = @{
    Authorization = "bearer $AuthToken"
  }
  Write-Host "Got Here 5"

  $data = @{
    title                 = $PRTitle
    head                  = "${PROwner}:${PRBranch}"
    base                  = $BaseBranch
    body                  = $PRBody
    maintainer_can_modify = $true
  }
  Write-Host "Got Here 6"

  $resp = Invoke-RestMethod -Method POST -Headers $headers `
    https://api.github.com/repos/$RepoOwner/$RepoName/pulls `
    -Body ($data | ConvertTo-Json)
  Write-Host "Got Here 7"
  $resp | Write-Verbose
  Write-Host "Got Here 8"
  Write-Host -f green "Pull request created https://github.com/$RepoOwner/$RepoName/pull/$($resp.number)"
}
