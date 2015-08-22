SWITCH="\033["
NORMAL="${SWITCH}0m"
RED="${SWITCH}1;31m"
GREEN="${SWITCH}1;32m"
CYAN="${SWITCH}1;36m"
MUSIC="\x0e"
HEART="\x03"

# Arg 1: version, e.g. 0.9.0
if [ -z $1 ]; then 
	echo -e "$RED$MUSIC Please specify a version.$NORMAL"
	exit 1
fi
version=$1

# Arg 2: passphrase for GPG default secret key
if [ -z $2 ]; then
	echo -e "$RED$MUSIC Please provide a passphrase to your default GPG secret key.$NORMAL"
	exit 1
fi
passphrase=$2

# Arg 3: package name - all matched packages will be published
# E.g. azure-mgmt-sql will publish azure-mgmt-sql
#      azure-mgmt will publish all ARM packages
#      websites will publish ARM & ASM websites packages
#      azure if you want to publish all
if [ -z $3 ]; then
	echo -e "$RED$MUSIC Please provide a package name. E.g. 'azure' if you want to publish all.$NORMAL"
	exit 1
fi
packages=$(ls *$1.pom | grep $3 | sed "s/.$1.pom//g")
echo -e "$MUSIC Discovering packages...$CYAN"
for package in $packages
do
	echo -e "$HEART $package"
done
echo -e "$NORMAL$MUSIC Discovering packages done."

exit 0
for package in $packages
do
	if [ "$package" == "azure-parent" ]; then
		mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=azure-parent-0.8.0.pom -Dfile=azure-parent-0.8.0.pom
	elif [ "$package" == "azure-bom" ]; then
		mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=azure-bom-0.8.0.pom -Dfile=azure-bom-0.8.0.pom
	else
		echo -e "$GREEN$HEART Deploying $package-0.8.0.jar$NORMAL"
		mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$package-0.8.0.pom -Dfile=$package-0.8.0.jar -Dgpg.passphrase=$passphrase
		echo -e "$GREEN$HEART Deploying $package-0.8.0-javadoc.jar$NORMAL"
		mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$package-0.8.0.pom -Dfile=$package-0.8.0-javadoc.jar -Dclassifier=javadoc -Dgpg.passphrase=$passphrase
		echo -e "$GREEN$HEART Deploying $package-0.8.0-sources.jar$NORMAL"
		mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$package-0.8.0.pom -Dfile=$package-0.8.0-sources.jar -Dclassifier=sources -Dgpg.passphrase=$passphrase
	fi
done