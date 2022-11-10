$granularity = 'package' # 'package' or 'service'
# Excluding glob suffix for .NET public API: sdk/**/!(api)/*
$globSuffix = '**' # '**' for all files
$scanType = "" # or "" for all files

$parallel = 12
$spellingErrorCountThreshold = 50
$quickWinsErrorCountThreshold = 5
$packageSlashSplit = 4
if ($granularity -eq 'service') {
    $packageSlashSplit = 3
}

# Public API surface checking:
# Exclude packages named @azure-rest/*
# Exclude packages named *-rest as well
# ONLY include packages which have swagger/README.md with yaml key
#   `source-code-folder-path:` with value including ./src/generated

$serviceDirectories = Get-ChildItem -Path 'sdk' -Directory
$packageDirectories = $serviceDirectories | Foreach-Object { Get-ChildItem -Path $_  -Directory }

$directores = $packageDirectories
if ($granularity -eq 'service') {
    $directores = $serviceDirectories
}

$tempPath = Join-Path ([System.IO.Path]::GetTempPath()) 'cspell-parallel-configs'
$configDirectory = New-Item -Path $tempPath -ItemType Directory -Force

./eng/common/spelling/Invoke-Cspell.ps1 -ScanGlobs "README.md" -LeavePackageInstallCache

$spellingOutput = $directores `
| foreach-object {
    [IO.Path]::GetRelativePath('./', $_)
}
| foreach-object -ThrottleLimit $parallel -Parallel {
$globSuffix = $using:globSuffix
$configDirectory = $using:configDirectory
$output = @{ Directory = $_ }

# Copy cspell config to another path to enable parallel execution
$cspellConfigPath = Join-Path $configDirectory "$([System.IO.Path]::GetRandomFileName()).json"
Copy-Item ./.vscode/cspell.json $cspellConfigPath

Write-Host "Invoke-Cspell.ps1 -CSpellConfigPath $cspellConfigPath -ScanGlobs $_/$globSuffix"
$output.CspellOutput = ./eng/common/spelling/Invoke-Cspell.ps1 `
    -CSpellConfigPath $cspellConfigPath `
    -ScanGlobs "$_/$globSuffix" `
    -LeavePackageInstallCache

Remove-Item -Force -Path $cspellConfigPath

$output.SpellingErrors = $output.CspellOutput
| Where-Object { $_ -and $_.Trim() } `
  | ForEach-Object {
$line = $_;
$_ -match 'Unknown word \((.*)\)' | Out-Null;

$file = $line.Substring(0, $line.LastIndexOf(':', $line.LastIndexOf(':') - 1));
$word = $Matches[1];

return @{
Line = $line;
File = $file;
Word = $word;
}
}

if ($LASTEXITCODE) {
$output.Error = $true
}
else {
$output.Error = $false
}
Write-Host "Finished $_"
$output
}

$global:optOut = $spellingOutput | Where-Object { $_.Error }
$global:optIn = $spellingOutput | Where-Object { !$_.Error }

Write-Host "Opt Out: $($optOut.Count) (may be inaccurate if single object)"
$optOut | Foreach-Object { Write-Host "  $($_.Directory)" }
Write-Host "Opt In: $($optIn.Count) (may be inaccurate if single object)"
$optIn | Foreach-Object { Write-Host "  $($_.Directory)" }


foreach ($item in $optOut) {
    $glob = "sdk/$($item.Directory)/$globSuffix"
    if ($granularity -eq 'package') {
        $glob = "$($item.Directory)/$globSuffix"
    }

    $item.Glob = $glob
}

Write-Host "Ignore globs: "
$optOut | Foreach-Object { Write-Host "`"$($_.Glob)`"," }

Write-Host "Spelling Error Words:"
$optOut.SpellingErrors `
| Group-Object -Property Word `
| Sort-Object -Property Count -Descending `
| Where-Object { $_.Count -ge $spellingErrorCountThreshold }
| Out-String `
| Write-Host

Write-Host "Spelling Error Files:"
$optOut.SpellingErrors.File `
| ForEach-Object { @{ Extension = [System.IO.Path]::GetExtension($_); File = $_ } } `
| Group-Object -Property Extension `
| Sort-Object -Property Count -Descending `
| Out-String `
| Write-Host

Write-Host "Possible quick wins:"
$optOut.SpellingErrors `
| ForEach-Object { @{ Package = (($_.File.Split([IO.Path]::DirectorySeparatorChar) | Select-Object -First $packageSlashSplit) -join [IO.Path]::DirectorySeparatorChar); Word = $_.Word } } `
| Group-Object -Property Package `
| ForEach-Object { $wordGroups = $_.Group | Group-Object -Property Word; @{ Count = $_.Count; Package = $_.Name; WordCount = ($wordGroups | Measure-Object).Count;  Words = $wordGroups.Name } } `
| Sort-Object -Property WordCount `
| Select-Object -Property WordCount,Package,Words `
| Where-Object { $_.WordCount -le $quickWinsErrorCountThreshold }
| Out-String `
| Write-Host

exit 0
Pause
$prNumber = Read-Host "PR Number"

foreach ($item in $optOut) {
    $spellCheckOutputNote = @"
``````
$($item.CspellOutput -join "`n")
``````
"@
    if ($spellCheckOutputNote.Length + 1029 -gt 65536) {
        $spellCheckOutputNote = "Output too large to add to bug, follow instructions in 'What to do' section to get a list of the spelling errors"
    }

    $fileOutput = @"
Spell check scanning of $granularity at ``$($item.Directory)`` detected spelling errors in the public API surface. This directory is opted out of PR spell checking in PR #$prNumber to keep PRs unblocked.

## What to do

1. Ensure Node.js is installed (https://nodejs.org/en/download/).
1. Delete the entry in ``.vscode/cspell.json``'s ``ignorePaths`` field. It will look like: ``$($item.Glob)``. You need to do this to enable checking the files.
1. From the root of the repo run spell check using ``./eng/common/spelling/Invoke-Cspell.ps1 -ScanGlobs "$($item.Glob)"``
1. Fix detections according to http://aka.ms/azsdk/engsys/spellcheck use the "False positives" section to fix false positives
1. Check in changes (including the change to ``.vscode/cspell.json`` where the ``ignorePaths`` is updated to remove the entry for this service.). You may need to run ``git add -f .vscode/cspell.json`` to force adding the changes to the file in git.

## Spell checking output

$spellCheckOutputNote

"@

    $outputFileName = "spelling-errors-$($item.Directory.Replace('/','--')).md"
    $fileOutput | Set-Content $outputFileName

    $titleHeader = $item.Directory
    if (Test-Path "$($item.Directory)/package.json") {
        $packageJson = Get-Content "$($item.Directory)/package.json" | ConvertFrom-Json
        $titleHeader = $packageJson.name
        if ($packageJson.'sdk-type' -ne "client") {
            Write-Host "Non-client SDK found ($($packageJson.'sdk-type')): $($item.Directory)"
        }
    }

    $title = "$titleHeader - cspell found spelling errors in package"

    $searchResults = gh issue list --json number --search "`\`"$title`\`" in:title" | ConvertFrom-Json

    if (!$searchResults) {
        Write-Host "Issue ($title) does not exist... creating"
        gh issue create `
      --title $title `
      --body-file $outputFileName `
      --label Client
    }
    else {
        $issueNumber = $searchResults[0].number
        Write-Host "Issue ($title) DOES exist (#$issueNumber)... updating"
        gh issue edit $issueNumber --body-file $outputFileName
    }
}
