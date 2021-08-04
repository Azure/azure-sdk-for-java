

$sourceSessionRecordPath = ".\target\test-classes\session-records\"
$targetSessionRecordPath = ".\src\test\resources\"

if (-not (Test-Path -Path $sourceSessionRecordPath))
{
    Write-Host("Ensure you have run the tests in Record mode. $sessionRecordPath does not exist`n")
}

Remove-Item -path $targetSessionRecordPath + "\session-records" -Recurse -Force
Copy-Item -Path $sourceSessionRecordPath -Destination $targetSessionRecordPath -Force -Recurse -Container
