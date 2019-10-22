@REM ************************************************************************************************
@REM ************************ DELETE THIS FILE BEFORE RETURNING TO MAIN REPO ************************
@REM ************************************************************************************************

SET SCOPE_DOWN=--projects com.azure.search:azure-search,com.azure:azure-client-sdk-parent --also-make
SET DefaultOptions=--fail-at-end --settings eng/settings.xml

@echo ===========================================================================================================================
@echo Build
@echo ===========================================================================================================================
call mvn -f sdk/search/pom.service.xml -DskipTests -Dmaven.javadoc.skip=true -Dinclude-template package || exit /b
@echo on


@echo ===========================================================================================================================
@echo Test
@echo ===========================================================================================================================
call mvn -f sdk/search/pom.service.xml -Dmaven.wagon.http.pool=false test || exit /b
@echo on


@echo ===========================================================================================================================
@echo Installing Code Quality Tools
@echo ===========================================================================================================================
call mvn -f eng/code-quality-reports/pom.xml -Dmaven.wagon.http.pool=false install || exit /b
@echo on

@echo ===========================================================================================================================
@echo Install azure-sdk-parent
@echo ===========================================================================================================================
call mvn -f parent/pom.xml -DskipTests -Dgpg.skip install || exit /b
@echo on

@echo ===========================================================================================================================
@echo Install all client packages
@echo ===========================================================================================================================
call mvn -f pom.client.xml -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true %SCOPE_DOWN% install || exit /b
@echo on

@echo ===========================================================================================================================
@echo Analyze
@echo ===========================================================================================================================
rem call mvn -f pom.client.xml -DskipTests -Dgpg.skip checkstyle:checkstyle-aggregate %SCOPE_DOWN% || exit /b
call mvn -f pom.client.xml -DskipTests -Dgpg.skip spotbugs:check checkstyle:check %SCOPE_DOWN% || exit /b
@echo on
