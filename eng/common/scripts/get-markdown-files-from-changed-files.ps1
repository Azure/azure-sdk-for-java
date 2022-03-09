param (
  # The root repo we scaned with.
  [string] $RootRepo = '$PSScriptRoot/../../..',
  # The target branch to compare with.
  [string] $targetBranch = ("origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH}" -replace "/refs/heads/")
)

$gitDiffChanges = Join-Path $PSScriptRoot "git-diff-changes.ps1"
$allMarkdownFiles = & $gitDiffChanges -TargetCommittish $targetBranch -IncludeRegex '*.md'


Write-Host "Here are all markdown files we need to check based on the changed files:"
foreach ($file in $allMarkdownFiles ) {
  Write-Host "    $file"
}
return $allMarkdownFiles 