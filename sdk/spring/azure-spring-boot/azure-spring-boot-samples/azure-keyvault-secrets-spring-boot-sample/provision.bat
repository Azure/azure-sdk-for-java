@echo off

SET LOCATION=%1
SET RESOURCEGROUPNAME=kvdemorg
SET APPNAME=kvdemoapp1
SET VAULTNAME=%RESOURCEGROUPNAME%vault

:createResourceGroup
FOR /F "tokens=*" %%A in ('az group exists -n %RESOURCEGROUPNAME%') DO SET "existingRG=%%A"
IF "%existingRG%"=="false" (
    ECHO Create resource group %RESOURCEGROUPNAME%
    CALL az group create -n %RESOURCEGROUPNAME% -l %LOCATION%
) else (
    ECHO Resource group %RESOURCEGROUPNAME% exists already
)


:createServicePrinciple
FOR /F "tokens=*" %%A in ('az ad app list --display-name %APPNAME% --query [0].appId') DO SET "existingAPPID=%%A"
IF "%existingAPPID%"=="" (
    ECHO Create app %APPNAME%
    FOR /F "tokens=*" %%C in ('az ad app create --display-name %APPNAME% --identifier-uris http://test.com/test --homepage http://test.com/test --query appId') DO SET "APPID=%%C"

    ECHO Create service principle
    CALL az ad sp create --id %APPID%
    SET OBJECTID=%APPID%    
) else (
    SET OBJECTID=%existingAPPID%
)
ECHO clientId=%OBJECTID%
rem FOR /F "tokens=*" %%B in ('az ad sp reset-credentials --name %OBJECTID% --query password') DO SET "SPKEY=%%B"
ECHO clientKey=%SPKEY%

:createKeyVault
ECHO Create Key Vault %VAULTNAME%
SET createkvcmd="az keyvault create --name %VAULTNAME% --resource-group %RESOURCEGROUPNAME% --location %LOCATION% --enabled-for-deployment true --enabled-for-disk-encryption true --enabled-for-template-deployment true --sku standard --query properties.vaultUri"
FOR /F "tokens=*" %%A in ('%createkvcmd%') DO SET "KEYVAULTURI=%%A"
ECHO vaultUri=https://%VAULTNAME%.vault.azure.net

:setKeyVaultPolicy
ECHO Set keyvault policy
CALL az keyvault set-policy --name %VAULTNAME% --secret-permission set get list delete --object-id %OBJECTID% >> LOG.txt


:setSecret
ECHO Set secrets to key vault
CALL az keyvault secret set --vault-name %VAULTNAME% --name spring-datasource-url --value jdbc:mysql//localhost:3306/testdb >> LOG.txt
CALL az keyvault secret set --vault-name %VAULTNAME% --name mysecretproperty --value secretvalue >> LOG.txt

:eof
ECHO Preparation done!!
