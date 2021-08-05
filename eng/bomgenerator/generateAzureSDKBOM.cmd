set "versioningClientFileLocation=%~dp0..\versioning\version_client.txt"
set "bomPomFileLocation=%~dp0..\..\sdk\boms\azure-sdk-bom\pom.xml"
set "outputFileLocation=%~dp0..\..\sdk\boms\azure-sdk-bom\newpom.xml"
set "reportFileLocation=%~dp0report.html"
mvn clean install && mvn exec:java -Dexec.args="-inputFile=%versioningClientFileLocation% -outputFile=%outputFileLocation% -pomFile=%bomPomFileLocation% -reportFile=%reportFileLocation%" && copy %~dp0report.html azuresdkbomreport.html