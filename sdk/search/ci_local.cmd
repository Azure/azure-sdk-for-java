@REM ************************************************************************************************
@REM ************************ DELETE THIS FILE BEFORE RETURNING TO MAIN REPO ************************
@REM ************************************************************************************************

SET SCOPE_DOWN=--projects com.azure.search:azure-search,com.azure:azure-client-sdk-parent --also-make

@echo ************************************
@echo Installing Code Quality Tools
call mvn -f eng/code-quality-reports/pom.xml install || exit /b



@echo ************************************
@echo Build
call mvn -f sdk/search/pom.service.xml -Dmaven.wagon.http.pool=false -Dmaven.test.skip=true -Dinclude-template -Dmaven.javadoc.skip=true package || exit /b



@echo ************************************
@echo Test
call mvn -f sdk/search/pom.service.xml -Dmaven.wagon.http.pool=false test || exit /b



@echo ************************************
@echo Install all client packages
call mvn -f pom.client.xml -Dmaven.wagon.http.pool=false -DskipTests -Dgpg.skip -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Dspotbugs.skip=true %SCOPE_DOWN% install || exit /b

@echo ************************************
@echo Analyze
call mvn -f pom.client.xml -DskipTests -Dgpg.skip spotbugs:check checkstyle:checkstyle-aggregate %SCOPE_DOWN% || exit /b
