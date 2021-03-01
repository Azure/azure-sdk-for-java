We highly recommend you refer to the README.md files of the released versions. You can do this by:  
1 On GitHub, navigate to the main page of the repository.  
2 Click the "master" button, then click "Tags".   
3 In the "Find a tag" text box, input and enter the starter module name you want to search, such as azure-spring-boot-starter-<the-module-you-need>.  
4 Click one of the searched tags. (You will find that the address in your browser bar changes to that specific version. You can manually edit the address with other versions, then quickly switch to other versions.)  
5 Then check the README.md file there.  
  
However, if you want to try something the newest, you can git clone the master branch, then build and install beta version dependencies to your local maven repository by:  
In command line, enter under the top level folder azure-sdk-for-java, then execute the maven command:  
`mvn clean install -pl .\sdk\spring\azure-spring-boot-starter-active-directory\ -am`
