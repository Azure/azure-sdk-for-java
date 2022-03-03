param (
  # The root repo we scaned with.
  [string] $RootRepo = '$PSScriptRoot/../../..',
  # The target branch to compare with.
  [string] $targetBranch = ("origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH}" -replace "/refs/heads/")
)

$gitDiffChanges = Join-Path $PSScriptRoot "git-diff-changes.ps1"
$allMarkdownFiles = & $gitDiffChanges -IncludeRegex "*.md"
# Scan all markdowns and find if it contains the deleted or renamed files.
$markdownContainLinks = @()
foreach ($f in $allMarkdownFiles) {
  $filePath = $f.FullName
  $content = Get-Content -Path $filePath -Raw
  foreach ($l in $relativePathLinks) {
    if ($content -match $l) {
      $markdownContainLinks += $filePath
      break
    }
  }
}

# Convert markdowns path of the PR to absolute path.
$adjustedReadmes = $changedMarkdowns | Foreach-Object { Resolve-Path $_ }
$markdownContainLinks += $adjustedReadmes

# Get rid of any duplicated ones.
$allMarkdowns = [string[]]($markdownContainLinks | Sort-Object | Get-Unique)

Write-Host "Here are all markdown files we need to check based on the changed files:"
foreach ($file in $allMarkdowns) {
  Write-Host "    $file"
}
return $allMarkdowns
