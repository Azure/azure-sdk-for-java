$sourcesDirectory = $env:BUILD_SOURCESDIRECTORY
$files = Get-ChildItem -Path $sourcesDirectory -Filter test-proxy.log
Write-Host "###### Found $($files.Count) test-proxy.log file(s) under '$sourcesDirectory'"
foreach ($file in $files) {
    Write-Host "##[group]$file"
    Get-Content $file
    Write-Host "##[endgroup]"
}

Write-Host "###### Found test-proxy-error.log file(s) under '$sourcesDirectory'"
$files = Get-ChildItem -Path $sourcesDirectory -Filter test-proxy-error.log
foreach ($file in $files) {
    Write-Host "##[group]$file"
    Get-Content $file
    Write-Host "##[endgroup]"
}