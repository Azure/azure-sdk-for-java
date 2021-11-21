set "bomPomFileLocation=%~dp0..\..\sdk\boms\azure-spring-boot-bom\pom.xml"
set "reportFileLocation=%~dp0report.html"
mvn clean install && mvn exec:java -Dexec.args="-mode=analyze -pomFile=%bomPomFileLocation% -reportFile=%reportFileLocation%" && copy %~dp0report.html azurespringbootbomreport.html