param (
    # The root repo we scaned with.
    [string[]] $RootRepo,
    # The markdown files from the PR.
    [string[]] $changedMarkdowns
)
$deletedFiles = (git diff origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH} HEAD --name-only --diff-filter=D)
$renamedFiles = (git diff origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH} HEAD --diff-filter=R)
$changedMarkdowns = (git diff origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH} HEAD --name-only -- '*.md')
# Removed the deleted markdowns. 
$changedMarkdowns = $changedMarkdowns |Where-Object { $deletedFiles -notcontains $_ }

$beforeRenameFiles = @()
# Retrieve the renamed from files.
foreach($file in $renamedFiles) {
    if ($file -match "^rename from (.*)$") {
        $beforeRenameFiles += $file -replace "^rename from (.*)$", '$1'
    }
}
# A combined list of deleted and renamed files.
$relativePathLinks += ($deletedFiles + $beforeRenameFiles)

# Scan all markdowns and find if it contains the deleted or renamed files.
$markdownContainLinks = @()
foreach ($f in (Get-ChildItem -Path $RootRepo -Recurse -Include *.md)) {
    $content = Get-Content -Path $f -Raw
    foreach($l in $fullPathLinks) {
        if ($content -match $l) {
            $markdownContainLinks += $f
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
    Write-Host "    $allMarkdowns"
}
return $allMarkdowns