param (
    # The root repo we scaned with.
    [string[]] $RootRepo = "./"
)
$deletedFiles = (git diff origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH} HEAD --name-only --diff-filter=D)
$renamedFiles = (git diff origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH} HEAD --diff-filter=R)
$changedMarkdowns = (git diff origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH} HEAD --name-only -- '*.md')
# These are for local testing.
# $deletedFiles = (git diff origin/master HEAD --name-only --diff-filter=D)
# $renamedFiles = (git diff origin/master HEAD --diff-filter=R)
# $changedMarkdowns = (git diff origin/master HEAD --name-only -- '*.md')

$beforeRenameFiles = @()
# Retrieve the renamed from files.
foreach($file in $renamedFiles) {
    if ($file -match "^rename from (.*)$") {
        $beforeRenameFiles += $file -replace "^rename from (.*)$", '$1'
    }
}
# A combined list of deleted and renamed files.
$relativePathLinks = ($deletedFiles + $beforeRenameFiles)
# Removed the deleted markdowns. 
$changedMarkdowns = $changedMarkdowns | Where-Object { $relativePathLinks -notcontains $_ }
# Scan all markdowns and find if it contains the deleted or renamed files.
$markdownContainLinks = @()
$allMarkdownFiles = Get-ChildItem -Path $RootRepo -Recurse -Include *.md
foreach ($f in $allMarkdownFiles) {
    $filePath = $f.ToString()
    $content = Get-Content -Path $filePath -Raw
    foreach($l in $relativePathLinks) {
        if ($content -match $l) {
            $markdownContainLinks += $filePath
            break
        }
    }
}

# Convert markdowns path of the PR to absolute path.
$adjustedReadmes = $changedMarkdowns | Foreach-Object {Resolve-Path $_}
$markdownContainLinks += $adjustedReadmes

# Get rid of any duplicated ones.
$allMarkdowns = [string[]]($markdownContainLinks | Sort-Object | Get-Unique)

Write-Host "Here are all markdown files need to scan against:"
foreach ($file in $allMarkdowns) {
    Write-Host "    $file"
}
return $allMarkdowns