set "versioningClientFileLocation=%~dp0..\versioning\version_client.txt"
set "bomPomFileLocation=%~dp0..\..\sdk\boms\azure-sdk-bom\pom.xml"
set "outputFileLocation=%~dp0..\..\sdk\boms\azure-sdk-bom\newpom.xml"
mvn clean install && mvn exec:java -Dexec.args="-inputFile=%versioningClientFileLocation% -outputFile=%outputFileLocation% -pomFile=%bomPomFileLocation%"