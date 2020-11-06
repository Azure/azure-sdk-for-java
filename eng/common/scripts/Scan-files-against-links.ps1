param (
    # The deleted files from the PR.
    [string[]] $deletedFiles,
    # The renamed files from the PR.
    [string[]] $renamedFiles,
    # The markdown files from the PR.
    [string[]] $changedMarkdowns
)

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
foreach ($f in (Get-ChildItem -Path ./ -Recurse -Include *.md)) {
    foreach($l in $fullPathLinks) {
        $content = Get-Content -Path $f -Raw
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
echo "##vso[task.setvariable variable=markdownsToScan]$allMarkdowns"