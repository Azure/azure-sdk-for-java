setlocal
set specFile=%1
set namespace=%2
set generateFolder=%3

set source=-Source https://www.myget.org/F/autorest/api/v2

set repoRoot=%~dp0..
set autoRestExe=%repoRoot%\tools\autorest\AutoRest.exe

@echo on
%autoRestExe% -Modeler Swagger -CodeGenerator Azure.Java -Namespace %namespace% -Input %specFile% -outputDirectory %generateFolder% -Header MICROSOFT_MIT %~5
@echo off
endlocal
