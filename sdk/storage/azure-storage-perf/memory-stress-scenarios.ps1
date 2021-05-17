$JavaPath = Join-Path -Path $Env:JAVA_HOME -ChildPath "bin" | Join-Path -ChildPath "java"

Invoke-Expression "& '$JavaPath' -version"
