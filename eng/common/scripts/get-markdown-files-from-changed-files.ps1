param (
  # The root repo we scaned with.
  [string] $RootRepo = '$PSScriptRoot/../../..',
  # The target branch to compare with.
  [string] $targetBranch = ("origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH}" -replace "/refs/heads/")
)

$gitDiffChanges = Join-Path $PSScriptRoot "git-diff-changes.ps1"
return & $gitDiffChanges -TargetCommittish $targetBranch -IncludeRegex '*.md'
