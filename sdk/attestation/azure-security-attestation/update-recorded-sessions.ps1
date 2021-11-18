

$sourceSessionRecordPath = ".\target\test-classes\session-records\"
$targetSessionRecordPath = ".\src\test\resources\"
$targetSessionRecordsPattern = $targetSessionRecordPath + "\session-records"

if (-not (Test-Path -Path $sourceSessionRecordPath))
{
    Write-Host("Ensure you have run the tests in Record mode. $sessionRecordPath does not exist`n")
}

Write-Host("Removing items from $targetSessionRecordsPattern");
Remove-Item -path $targetSessionRecordsPattern -Recurse -Force
Write-Host("Copying items from $sourceSessionRecordPath to $targetSessionRecordsPattern")
Copy-Item -Path $sourceSessionRecordPath -Destination $targetSessionRecordsPattern -Force -Recurse -Container
