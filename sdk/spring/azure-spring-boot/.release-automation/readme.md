# How to do release automation

1. setup gpg key 
   Following [this instruction](http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven/ ) to setup gpg key and publish it. save your passphrase.
1. python version >=3.5.
1. install necessary python packages: requests, xmltodict, jenkinsapi
1. run maven install locally. **Important** when do build please use profile `release` which will contains ai PROD instrument key. Please refer [code here](https://github.com/Microsoft/azure-spring-boot/blob/yungez-ai/azure-spring-boot-starters/azure-spring-boot-starter/pom.xml#L51-L73) for detail. 
1. setup configuration in config.json 
    ```
   "targetFolder": "//path-to-your-local-to-copy-signed-jars",
    ...
   "passwords": {
    "jenkins": "your jenkins key to jenkins server http://azuresdkci.cloudapp.net/",
    "gpg": "your gpg key passphrase",
    "nexus": "your nexus key"
   }
    ```
  
1. run run.py.
1. verify your packages with staging repository id in output, specify repo id in pom.xml.
   ```xml
   	<repositories>
   		<repository>
   			<id>repo-id</id>
   			<name>repo-id</name>
   			<url>https://oss.sonatype.org/content/repositories/repo-id</url>
   		</repository>
   	</repositories>
   ```
   
1. after verification, login to https://oss.sonatype.org, release your repository.
  