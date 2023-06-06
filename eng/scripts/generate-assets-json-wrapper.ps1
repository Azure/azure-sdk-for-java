# get the temp directory
$temp = [System.IO.Path]::GetTempPath()
if (!(test-path $temp)) {
    throw "Unable to find temp directory"
}
# Need the path separator for search paths, not file paths.
$pathsep = if ($env:OS -eq "Windows_NT") { ";" } else { ":" }
$env:Path = $env:PATH + $pathsep + (join-path $temp "test-proxy")
Invoke-Expression "$(join-path $PSScriptRoot ../common/testproxy/transition-scripts/generate-assets-json.ps1) -TestProxyExe Azure.Sdk.Tools.TestProxy $args"