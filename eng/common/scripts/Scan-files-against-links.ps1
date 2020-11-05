param (
    [string] $gitUrl,
    # These are the files relative paths which has been deleted from PR. Need to scan whether any markdowns include these links
    [string[]] $links,
    [string[]] $changedMarkdowns
)
$fullPathLinks = @()
foreach ($link in $links) {
    $fullPathLinks += ($gitUrl + $link)
}

$urls = @()
foreach ($f in (Get-ChildItem -Path ./ -Recurse -Include *.md)) {
    foreach($l in fullPathLinks) {
        $content = Get-Content -Path $f -Raw
        if ($content -match $l) {
            $urls += $f
            break
        }
    }
}
$urls += (git diff origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH} HEAD --name-only -- '*.md' | Get-Unique)
echo "##vso[task.setvariable variable=markdownsToScan]$urls"