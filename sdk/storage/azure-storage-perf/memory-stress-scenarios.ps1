$JavaPath = Join-Path -Path $Env:JAVA_HOME -ChildPath "bin" | Join-Path -ChildPath "java"
$TargetDirPath = Join-Path -Path $PSScriptRoot -ChildPath "target"
$PerfJarPath = Get-ChildItem -Path (Join-Path -Path $TargetDirPath -ChildPath "*") -Include azure-storage-perf-*-jar-with-dependencies.jar -File

Invoke-Expression "& '$JavaPath' -version"

Invoke-Expression "& '$JavaPath' -Xms500m -Xmx500m -jar '$PerfJarPath' uploadoutputstream --warmup 0 --duration 1 --size 1048576000 --sync"
Invoke-Expression "& '$JavaPath' -Xms500m -Xmx500m -jar '$PerfJarPath' uploadblob --warmup 0 --duration 1 --size 1048576000"
