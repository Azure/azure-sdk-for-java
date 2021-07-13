setJAVA_HOME=c:\Program Files\AdoptOpenJDK\jdk-11.0.10.9-hotspot
set "versioningClientFileLocation=%~dp0..\versioning\version_client.txt"
set "bomPomFileLocation=%~dp0..\..\sdk\boms\azure-sdk-bom\pom.xml"
set "outputFileLocation=%~dp0..\..\sdk\boms\azure-sdk-bom\newpom.xml"
mvn clean install && mvn exec:java -Dexec.args="-inputFile=%versioningClientFileLocation% -outputFile=%outputFileLocation% -pomFile=%bomPomFileLocation%"
set JAVA_HOME=c:\Program Files\AdoptOpenJDK\jdk-8.0.282.8-hotspot
