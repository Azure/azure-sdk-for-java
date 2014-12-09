:: set global variables JDIFF, Branch location, etc
set JDIFF=C:\sources\hydra\Tools\jdiff\build\jdiff-1.1.1\jdiff.jar;C:\sources\hydra\Tools\jdiff\build\jdiff-1.1.1\xerces.jar;C:\sources\hydra\Tools\jdiff\build\jdiff-1.1.1\commons-lang3-3.3.2.jar
set OLDBRANCH=C:\sources\old\azure-sdk-for-java
set NEWBRANCH=C:\sources\azure-sdk-for-java-pr

:: set API name
set OLDRUNTIMEAPI=azure-runtime-old
set NEWRUNTIMEAPI=azure-runtime
set OLDSERVICEBUSAPI=azure-servicebus-old
set NEWSERVICEBUSAPI=azure-servicebus
set OLDMEDIASERVICEAPI=azure-media-old
set NEWMEDIASERVICEAPI=azure-media
set OLDMANAGEMENTAPI=azure-management-old
set NEWMANAGEMENTAPI=azure-management
set OLDCOREAPI=azure-core-old
set NEWCOREAPI=azure-core
set OLDCOMPUTEMANAGEMENTAPI=azure-management-compute-old
set NEWCOMPUTEMANAGEMENTAPI=azure-management-compute
set OLDSQLMANAGEMENTAPI=azure-management-sql-old
set NEWSQLMANAGEMENTAPI=azure-management-sql
set OLDNETWORKMANAGEMENTAPI=azure-management-network-old
set NEWNETWORKMANAGEMENTAPI=azure-management-network
set OLDSTORAGEMANAGEMENTAPI=azure-management-storage-old
set NEWSTORAGEMANAGEMENTAPI=azure-management-storage
set OLDWEBSITEMANAGEMENTAPI=azure-management-websites-old
set NEWWEBSITEMANAGEMENTAPI=azure-management-websites

:: set the paths
set OLDCOREPATH=%OLDBRANCH%\core\src\main\java
set NEWCOREPATH=%NEWBRANCH%\core\src\main\java
set OLDRUNTIMEPATH=%OLDBRANCH%\serviceruntime\src\main\java
set NEWRUNTIMEPATH=%NEWBRANCH%\serviceruntime\src\main\java
set OLDSERVICEBUSPATH=%OLDBRANCH%\servicebus\src\main\java
set NEWSERVICEBUSPATH=%NEWBRANCH%\servicebus\src\main\java
set OLDMEDIASERVICEPATH=%OLDBRANCH%\media\src\main\java
set NEWMEDIASERVICEPATH=%NEWBRANCH%\media\src\main\java
set OLDMANAGEMENTPATH=%OLDBRANCH%\management\src\main\java
set NEWMANAGEMENTPATH=%NEWBRANCH%\management\src\main\java
set OLDCOMPUTEMANAGEMENTPATH=%OLDBRANCH%\management-compute\src\main\java
set NEWCOMPUTEMANAGEMENTPATH=%NEWBRANCH%\management-compute\src\main\java
set OLDNETWORKMANAGEMENTPATH=%OLDBRANCH%\management-network\src\main\java
set NEWNETWORKMANAGEMENTPATH=%NEWBRANCH%\management-network\src\main\java
set OLDSQLMANAGEMENTPATH=%OLDBRANCH%\management-sql\src\main\java
set NEWSQLMANAGEMENTPATH=%NEWBRANCH%\management-sql\src\main\java
set OLDSTORAGEMANAGEMENTPATH=%OLDBRANCH%\management-storage\src\main\java
set NEWSTORAGEMANAGEMENTPATH=%NEWBRANCH%\management-storage\src\main\java
set OLDWEBSITEMANAGEMENTPATH=%OLDBRANCH%\management-websites\src\main\java 
set NEWWEBSITEMANAGEMENTPATH=%NEWBRANCH%\management-websites\src\main\java

:: set the namespace
set RUNTIMENAMESPACE=com.microsoft.windowsazure.serviceruntime
set OLDSERVICEBUSNAMESPACE=com.microsoft.windowsazure.services.servicebus
set NEWSERVICEBUSNAMESPACE=com.microsoft.windowsazure.services.servicebus
set MEDIASERVICENAMESPACE=com.microsoft.windowsazure.services.media
set OLDMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management
set NEWMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management
set OLDCORENAMESPACE=com.microsoft.windowsazure.core
set NEWCORENAMESPACE=com.microsoft.windowsazure.core
set OLDCOMPUTEMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.compute
set NEWCOMPUTEMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.compute
set OLDNETWORKMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.network
set NEWNETWORKMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.network
set OLDSQLMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.sql
set NEWSQLMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.sql
set OLDSTORAGEMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.storage
set NEWSTORAGEMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.storage
set OLDWEBSITEMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.websites
set NEWWEBSITEMANAGEMENTNAMESPACE=com.microsoft.windowsazure.management.websites

:: First, generate the javadoc for the old code.
:: Service Bus
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDSERVICEBUSAPI% -sourcepath %OLDSERVICEBUSPATH% -subpackages %OLDSERVICEBUSNAMESPACE%
:: Media Services
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDMEDIASERVICEAPI% -sourcepath %OLDMEDIASERVICEPATH% -subpackages %MEDIASERVICENAMESPACE%
:: Service Runtime
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDRUNTIMEAPI% -sourcepath %OLDRUNTIMEPATH% -subpackages %RUNTIMENAMESPACE%
:: Core
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDCOREAPI% -sourcepath %OLDCOREPATH% -subpackages %OLDCORENAMESPACE%
:: Management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDMANAGEMENTAPI% -sourcepath %OLDMANAGEMENTPATH% -subpackages %OLDMANAGEMENTNAMESPACE%
:: Compute Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDCOMPUTEMANAGEMENTAPI% -sourcepath %OLDCOMPUTEMANAGEMENTPATH% -subpackages %OLDCOMPUTEMANAGEMENTNAMESPACE%
:: Network Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDNETWORKMANAGEMENTAPI% -sourcepath %OLDNETWORKMANAGEMENTPATH% -subpackages %OLDNETWORKMANAGEMENTNAMESPACE%
:: SQL Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDSQLMANAGEMENTAPI% -sourcepath %OLDSQLMANAGEMENTPATH% -subpackages %OLDSQLMANAGEMENTNAMESPACE%
:: Storage Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDSTORAGEMANAGEMENTAPI% -sourcepath %OLDSTORAGEMANAGEMENTPATH% -subpackages %OLDSTORAGEMANAGEMENTNAMESPACE%
:: Website Management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %OLDWEBSITEMANAGEMENTAPI% -sourcepath %OLDWEBSITEMANAGEMENTPATH% -subpackages %OLDWEBSITEMANAGEMENTNAMESPACE%

:: Second, generate the java doc for the new code.
:: Service Bus
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWSERVICEBUSAPI% -sourcepath %NEWSERVICEBUSPATH% -subpackages %NEWSERVICEBUSNAMESPACE% 
:: Media Services
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWMEDIASERVICEAPI% -sourcepath %NEWMEDIASERVICEPATH% -subpackages %MEDIASERVICENAMESPACE% 
:: Service Runtime
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWRUNTIMEAPI% -sourcepath %NEWRUNTIMEPATH% -subpackages %RUNTIMENAMESPACE% 
:: Core
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWCOREAPI% -sourcepath %NEWCOREPATH% -subpackages %NEWCORENAMESPACE%
:: Management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWMANAGEMENTAPI% -sourcepath %NEWMANAGEMENTPATH% -subpackages %NEWMANAGEMENTNAMESPACE%
:: Compute Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWCOMPUTEMANAGEMENTAPI% -sourcepath %NEWCOMPUTEMANAGEMENTPATH% -subpackages %NEWCOMPUTEMANAGEMENTNAMESPACE%
:: Network Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWNETWORKMANAGEMENTAPI% -sourcepath %NEWNETWORKMANAGEMENTPATH% -subpackages %NEWNETWORKMANAGEMENTNAMESPACE%
:: SQL Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWSQLMANAGEMENTAPI% -sourcepath %NEWSQLMANAGEMENTPATH% -subpackages %NEWSQLMANAGEMENTNAMESPACE%
:: Storage Management 
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWSTORAGEMANAGEMENTAPI% -sourcepath %NEWSTORAGEMANAGEMENTPATH% -subpackages %NEWSTORAGEMANAGEMENTNAMESPACE%
:: Website Management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -apiname %NEWWEBSITEMANAGEMENTAPI% -sourcepath %NEWWEBSITEMANAGEMENTPATH% -subpackages %NEWWEBSITEMANAGEMENTNAMESPACE%

:: Third, create the sub directory
:: Service Bus
set SERVICEBUSOUTPUT=jdiff\servicebus
mkdir %SERVICEBUSOUTPUT%
:: Media Services
set MEDIASERVICEOUTPUT=jdiff\mediaservice
mkdir %MEDIASERVICEOUTPUT%
:: Service Runtime
set RUNTIMEOUTPUT=jdiff\serviceruntime
mkdir %RUNTIMEOUTPUT%
:: Core
set COREOUTPUT=jdiff\core
mkdir %COREOUTPUT%
:: Management 
set MANAGEMENTOUTPUT=jdiff\management
mkdir %MANAGEMENTOUTPUT%
:: Compute Management
set COMPUTEMANAGEMENTOUTPUT=jdiff\compute
mkdir %COMPUTEMANAGEMENTOUTPUT%
:: Network Management
set NETWORKMANAGEMENTOUTPUT=jdiff\network
mkdir %NETWORKMANAGEMENTOUTPUT%
:: SQL Management
set SQLMANAGEMENTOUTPUT=jdiff\sql
mkdir %SQLMANAGEMENTOUTPUT%
:: Storage Management
set STORAGEMANAGEMENTOUTPUT=jdiff\storage
mkdir %STORAGEMANAGEMENTOUTPUT%
:: Website Management
set WEBSITEMANAGEMENTOUTPUT=jdiff\website
mkdir %WEBSITEMANAGEMENTOUTPUT%

:: Fourth, perform the jdiff comparison
:: service bus
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %SERVICEBUSOUTPUT% -stats -oldapi %OLDSERVICEBUSAPI% -newapi %NEWSERVICEBUSAPI% Null.java
:: media service
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %MEDIASERVICEOUTPUT% -stats -oldapi %OLDMEDIASERVICEAPI% -newapi %NEWMEDIASERVICEAPI% Null.java
:: runtime
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %RUNTIMEOUTPUT% -stats -oldapi %OLDRUNTIMEAPI% -newapi %NEWRUNTIMEAPI% Null.java
:: core
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %COREOUTPUT% -stats -oldapi %OLDCOREAPI% -newapi %NEWCOREAPI% Null.java
:: management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %MANAGEMENTOUTPUT% -stats -oldapi %OLDMANAGEMENTAPI% -newapi %NEWMANAGEMENTAPI% Null.java
:: compute management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %COMPUTEMANAGEMENTOUTPUT% -stats -oldapi %OLDCOMPUTEMANAGEMENTAPI% -newapi %NEWCOMPUTEMANAGEMENTAPI% Null.java
:: network management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %NETWORKMANAGEMENTOUTPUT% -stats -oldapi %OLDNETWORKMANAGEMENTAPI% -newapi %NEWNETWORKMANAGEMENTAPI% Null.java
:: SQL management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %SQLMANAGEMENTOUTPUT% -stats -oldapi %OLDSQLMANAGEMENTAPI% -newapi %NEWSQLMANAGEMENTAPI% Null.java
:: storage management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %STORAGEMANAGEMENTOUTPUT% -stats -oldapi %OLDSTORAGEMANAGEMENTAPI% -newapi %NEWSTORAGEMANAGEMENTAPI% Null.java
:: website management
javadoc -doclet jdiff.JDiff -docletpath %JDIFF% -d %WEBSITEMANAGEMENTOUTPUT% -stats -oldapi %OLDWEBSITEMANAGEMENTAPI% -newapi %NEWWEBSITEMANAGEMENTAPI% Null.java