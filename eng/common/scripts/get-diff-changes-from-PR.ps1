<#
  .SYNOPSIS
  Returns git diff changes in pull request.
  .DESCRIPTION
  The script is to return diff changes in pull request.
  .PARAMETER SourceCommittish
  The branch committish PR merges from.
  Definition of committish: https://git-scm.com/docs/gitglossary#Documentation/gitglossary.txt-aiddefcommit-ishacommit-ishalsocommittish
  .PARAMETER TargetCommittish
  The branch committish PR targets to merge into.
  .PARAMETER DiffPath
  The files which git diff to scan against. Support regex match. E.g. "eng/common/*", "*.md"
  .PARAMETER DiffFilterType
  The filter type A(a)dd, D(d)elete, R(r)ename, U(u)pate. 
  E.g. 'ad' means filter out the newly added file and deleted file 
  E.g. '' means no filter on file mode.
#>
[CmdletBinding()]
param (
  [string] $SourceCommittish = "${env:BUILD_SOURCEVERSION}",
  [string] $TargetCommittish = ("origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH}" -replace "refs/heads/"),
  [string] $DiffPath = "",
  [string] $DiffFilterType = 'd'
)

#Set-StrictMode -Version 3

function Get-PullRequest-Diff-Changes($SourceCommittish, $TargetCommittish, $DiffPath, $DiffFilterType) {
  # If ${env:SYSTEM_PULLREQUEST_TARGETBRANCH} is empty, then return empty.
  if ($TargetCommittish -eq "origin/") {
    Write-Host "There is no target branch passed in. "
    return ""
  }

  # Git PR diff: https://docs.github.com/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/about-comparing-branches-in-pull-requests#three-dot-and-two-dot-git-diff-comparisons
  $command = "git -c core.quotepath=off -c i18n.logoutputencoding=utf-8 diff `"$TargetCommittish...HEAD`" --name-only --diff-filter=$DiffFilterType"
  if ($DiffPath) {
  $command = $command + " -- `'$DiffPath`'"
  }
  Write-Host $command
  $changedFiles = Invoke-Expression -Command $command
  if(!$changedFiles) {
    Write-Host "No changed files in git diff between $TargetCommittish and $SourceCommittish"
  }
  else {
  Write-Host "Here are the diff files:"
  foreach ($file in $changedFiles) {
      Write-Host "    $file"
  }
  }
  return $changedFiles
}

return Get-PullRequest-Diff-Changes -SourceCommittish $SourceCommittish `
        -TargetCommittish $TargetCommittish `
        -DiffPath $DiffPath `
        -DiffFilterType $DiffFilterType
