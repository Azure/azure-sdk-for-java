$JavaPath = Join-Path -Path $Env:JAVA_HOME -ChildPath "bin" | Join-Path -ChildPath "java"
$TargetDirPath = Join-Path -Path $PSScriptRoot -ChildPath "target"
$PerfJarPath = Get-ChildItem -Path (Join-Path -Path $TargetDirPath -ChildPath "*") -Include azure-storage-perf-*-jar-with-dependencies.jar -File

Invoke-Expression "& '$JavaPath' -version"

function Run-Scenario {
    param (
        [Parameter(Mandatory=$true, Position=0)]
        [string] $HeapSize,
        [Parameter(Mandatory=$true, Position=1)]
        [string] $Scenario,
        [Parameter(Mandatory=$false, Position=2)]
        [string] $ExtraFlags
    )
    Write-Host "Executing '$Scenario' with '$HeapSize' heap"
    Invoke-Expression "& '$JavaPath' -Xms$HeapSize -Xmx$HeapSize '$ExtraFlags' -jar '$PerfJarPath' $Scenario"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Scenario failed, exiting"
        exit 1
    }
}

# Blobs
$env:STORAGE_CONNECTION_STRING="UseDevelopmentStorage=true"
# Default transfer options
Run-Scenario "600m" "uploadoutputstream --warmup 0 --duration 1 --size 1048576000 --sync"
Run-Scenario "600m" "uploadblob --warmup 0 --duration 1 --size 1048576000"
Run-Scenario "400m" "uploadblob --warmup 0 --duration 1 --size 1048576000 --sync"
Run-Scenario "400m" "uploadblobnolength --warmup 0 --duration 1 --size 1048576000 --sync"
# Small transfer options
Run-Scenario "50m" "uploadoutputstream --warmup 0 --duration 1 --size 1048576000 --transfer-single-upload-size 4194304 --transfer-block-size 1048576 --sync"
Run-Scenario "50m" "uploadblobnolength --warmup 0 --duration 1 --size 1048576000 --transfer-single-upload-size 4194304 --transfer-block-size 1048576 --sync"
Run-Scenario "50m" "uploadblob --warmup 0 --duration 1 --size 1048576000 --transfer-single-upload-size 4194304 --transfer-block-size 1048576 --sync"
Run-Scenario "50m" "uploadblob --warmup 0 --duration 1 --size 1048576000 --transfer-single-upload-size 4194304 --transfer-block-size 1048576"
# Jumbo blobs
Run-Scenario "7g" "uploadblob --warmup 0 --duration 1 --size 8388608000 --transfer-block-size 2147483648 --transfer-concurrency 1" "-Dazure.core.perf.test.data.buffer.size=104857600"
Run-Scenario "7g" "uploadblob --warmup 0 --duration 1 --size 8388608000 --transfer-block-size 2147483648 --transfer-concurrency 1 --sync" "-Dazure.core.perf.test.data.buffer.size=104857600"
Run-Scenario "7g" "uploadblobnolength --warmup 0 --duration 1 --size 8388608000 --transfer-block-size 2147483648 --transfer-concurrency 1 --sync" "-Dazure.core.perf.test.data.buffer.size=104857600"
Run-Scenario "7g" "uploadoutputstream --warmup 0 --duration 1 --size 8388608000 --transfer-block-size 2147483648 --transfer-concurrency 1 --sync" "-Dazure.core.perf.test.data.buffer.size=104857600"

# DataLake
$env:STORAGE_CONNECTION_STRING=$env:STORAGE_DATA_LAKE_CONNECTION_STRING
# Default transfer options
Run-Scenario "200m" "uploadfiledatalake --warmup 0 --duration 1 --size 1048576000 --sync"
Run-Scenario "300m" "uploadfiledatalake --warmup 0 --duration 1 --size 1048576000"
# Small transfer options
Run-Scenario "50m" "uploadfiledatalake --warmup 0 --duration 1 --size 1048576000 --transfer-single-upload-size 4194304 --transfer-block-size 1048576 --sync"
Run-Scenario "50m" "uploadfiledatalake --warmup 0 --duration 1 --size 1048576000 --transfer-single-upload-size 4194304 --transfer-block-size 1048576"
# Jumbo blobs
Run-Scenario "7g" "uploadfiledatalake --warmup 0 --duration 1 --size 8388608000 --transfer-block-size 2147483648 --transfer-concurrency 1 --sync" "-Dazure.core.perf.test.data.buffer.size=104857600"
Run-Scenario "7g" "uploadfiledatalake --warmup 0 --duration 1 --size 8388608000 --transfer-block-size 2147483648 --transfer-concurrency 1" "-Dazure.core.perf.test.data.buffer.size=104857600"

# Shares
$env:STORAGE_CONNECTION_STRING=$env:PRIMARY_STORAGE_CONNECTION_STRING
Run-Scenario "100m" "uploadfileshare --warmup 0 --duration 1 --size 1048576000 --sync"
Run-Scenario "200m" "uploadfileshare --warmup 0 --duration 1 --size 1048576000"
