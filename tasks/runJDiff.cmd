:: set global variables JDIFF, Branch location, etc
set JDIFF=[JDiff Path]
set OLDBRANCH=[Old branch path]
set NEWBRANCH=[New branch path]

:: set API name 
set OLDRUNTIMEAPI=microsoft-azure-runtime-api-old
set NEWRUNTIMEAPI=microsoft-azure-runtime-api-new
set OLDSERVICEBUSAPI=microsoft-azure-servicebus-api-old
set NEWSERVICEBUSAPI=microsoft-azure-servicebus-api-new
set OLDMEDIASERVICEAPI=microsoft-azure-media-api-old
set NEWMEDIASERVICEAPI=microsoft-azure-media-api-new
set OLDMANAGEMENTAPI=microsoft-azure-management-api-old
set NEWMANAGEMENTAPI=microsoft-azure-management-api-new
set OLDCOREAPI=microsoft-azure-core-api-old
set NEWCOREAPI=microsoft-azure-core-api-new
set NEWCOMPUTEMANAGEMENTAPI=microsoft-azure-management-compute-api-new
set NEWSQLMANAGEMENTAPI=microsoft-azure-management-sql-api-new
set NEWNETWORKMANAGEMENTAPI=microsoft-azure-management-network-api-new
set NEWSTORAGEMANAGEMENTAPI=microsoft-azure-management-storage-api-new
set NEWWEBSITEMANAGEMENTAPI=microsoft-azure-management-websites-api-new

:: set the paths 
set OLDBRANCHPATH=%OLDBRANCH%\microsoft-azure-api\src\main\java
set NEWRUNTIMEPATH=%NEWBRANCH%\serviceruntime\src\main\java
set NEWSERVICEBUSPATH=%NEWBRANCH%\servicebus\src\main\java
set NEWMEDIASERVICEPATH=%NEWBRANCH%\media\src\main\java
set NEWMANAGEMENTPATH=%NEWBRANCH%\management\src\main\java
set NEWCOREPATH=%NEWBRANCH%\core\src\main\java
set NEWCOMPUTEMANAGEMENTPATH=%NEWBRANCH%\management-compute\src\main\java
set NEWNETWORKMANAGEMENTPATH=%NEWBRANCH%\management-network\src\main\java
set NEWSQLMANAGEMENTPATH=%NEWBRANCH%\management-sql\src\main\java
set NEWSTORAGEMANAGEMENTPATH=%NEWBRANCH%\management-storage\src\main\java
set NEWWEBSITEMANAGEMENTPATH=%NEWBRANCH%\management-websites\src\main\java 

set OLDSERVICEBUSPATH=%OLDBRANCHPATH%

:: set the namespace 
set RUNTIMENAMESPACE=com.microsoft.windowsazure.serviceruntime
set OLDSERVICEBUSNAMESPACE=com.microsoft.windowsazure.services.serviceBus
set NEWSERVICEBUSNAMESPACE=com.microsoft.windowsazure.services.servicebus
set MEDIASERVICENAMESPACE=com.microsoft.windowsazure.services.media
set OLDMANAGEMENTNAMESPACE=com.microsoft.windowsazure.services.management
set NEWMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management
set OLDCORENAMESPACE=com.microsoft.windowsazure.services.core
set NEWCORENAMESPACE=com.microsoft.windowsazure.core
set NEWCOMPUTEMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.compute
set NEWNETWORKMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.network
set NEWSQLMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.sql
set NEWSTORAGEMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.storage
set NEWWEBSITEMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.websites

:: First, generate the javadoc for the old code. 
:: Service Bus
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDSERVICEBUSAPI% -sourcepath %OLDSERVICEBUSPATH% %OLDSERVICEBUSNAMESPACE%
:: Media Services
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDMEDIASERVICEAPI% -sourcepath %OLDBRANCHPATH% %MEDIASERVICENAMESPACE%
:: Service Runtime
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDRUNTIMEAPI% -sourcepath %OLDBRANCHPATH% %RUNTIMENAMESPACE%
:: Management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDMANAGEMENTAPI% -sourcepath %OLDBRANCHPATH% %OLDMANAGEMENTNAMESPACE%
:: Core
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDCOREAPI% -sourcepath %OLDBRANCHPATH% %OLDCORENAMESPACE%

:: Second, generate the java doc for the new code.
:: Service Bus
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWSERVICEBUSAPI% -sourcepath %NEWSERVICEBUSPATH% %NEWSERVICEBUSNAMESPACE% 
:: Media Services
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWMEDIASERVICEAPI% -sourcepath %NEWMEDIASERVICEPATH% %MEDIASERVICENAMESPACE% 
:: Service Runtime
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWRUNTIMEAPI% -sourcepath %NEWRUNTIMEPATH% %RUNTIMENAMESPACE% 
:: Management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWMANAGEMENTAPI% -sourcepath %NEWMANAGEMENTPATH% %NEWMANAGEMENTNAMESPACE%
:: Core
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWCOREAPI% -sourcepath %NEWCOREPATH% %NEWCORENAMESPACE%

:: Compute Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWCOMPUTEMANAGEMENTAPI% -sourcepath %NEWCOMPUTEMANAGEMENTPATH% %NEWCOMPUTEMANAGEMENTNAMESPACE%

:: Network Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWNETWORKMANAGEMENTAPI% -sourcepath %NEWNETWORKMANAGEMENTPATH% %NEWNETWORKMANAGEMENTNAMESPACE%

:: SQL Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWSQLMANAGEMENTAPI% -sourcepath %NEWSQLMANAGEMENTPATH% %NEWSQLMANAGEMENTNAMESPACE%

:: Storage Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWSTORAGEMANAGEMENTAPI% -sourcepath %NEWSTORAGEMANAGEMENTPATH% %NEWSTORAGEMANAGEMENTNAMESPACE%

:: Website Management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWWEBSITEMANAGEMENTAPI% -sourcepath %NEWWEBSITEMANAGEMENTPATH% %NEWWEBSITEMANAGEMENTNAMESPACE%


:: Third, create the sub directory 
:: Service Runtime
set RUNTIMEOUTPUT=jdiff\serviceruntime
mkdir %RUNTIMEOUTPUT%
:: Service Bus
set SERVICEBUSOUTPUT=jdiff\servicebus
mkdir %SERVICEBUSOUTPUT%
:: Media Services
set MEDIASERVICEOUTPUT=jdiff\mediaservice
mkdir %MEDIASERVICEOUTPUT%
:: Management 
set MANAGEMENTOUTPUT=jdiff\management
mkdir %MANAGEMENTOUTPUT%
:: Core
set COREOUTPUT=jdiff\core
mkdir %COREOUTPUT%

:: Fourth, perform the jdiff comparison
:: service bus
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %SERVICEBUSOUTPUT% -stats -oldapi %OLDSERVICEBUSAPI% -newapi %NEWSERVICEBUSAPI% d:\software\jdiff\Null.java 
:: runtime
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %RUNTIMEOUTPUT% -stats -oldapi %OLDRUNTIMEAPI% -newapi %NEWRUNTIMEAPI% d:\software\jdiff\Null.java 
:: media service
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %MEDIASERVICEOUTPUT% -stats -oldapi %OLDMEDIASERVICEAPI% -newapi %NEWMEDIASERVICEAPI% d:\software\jdiff\Null.java 
:: management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %MANAGEMENTOUTPUT% -stats -oldapi %OLDMANAGEMENTAPI% -newapi %NEWMANAGEMENTAPI% d:\software\jdiff\Null.java 
:: core
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %COREOUTPUT% -stats -oldapi %OLDCOREAPI% -newapi %NEWCOREAPI% d:\software\jdiff\Null.java 

