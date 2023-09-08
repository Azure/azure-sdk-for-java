# get the temp directory
$temp = [System.IO.Path]::GetTempPath()
if (!(test-path $temp)) {
    throw "Unable to find temp directory"
}

$testProxyPath = join-path $temp "test-proxy"
if (!(test-path $testProxyPath)) {
    throw "Unable to find test proxy directory. Run a test proxy test to install it."
}
# Need the path separator for search paths, not file paths.
$pathsep = if ($env:OS -eq "Windows_NT") { ";" } else { ":" }
$env:Path = $env:PATH + $pathsep + $testProxyPath
Invoke-Expression "$(join-path $PSScriptRoot ../common/testproxy/transition-scripts/generate-assets-json.ps1) -TestProxyExe Azure.Sdk.Tools.TestProxy $args"
