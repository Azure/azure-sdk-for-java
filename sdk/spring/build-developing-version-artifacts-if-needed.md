### Released version  
  
We **highly recommend** you refer to the `README.md` files of the released versions.  
  
Here are the steps to get `README.md` files of the released version:  
  
1. On GitHub, navigate to the main page of the repository.  
1. Click the `master` button, then click `Tags`.   
1. In the "Find a tag" text box, input and enter the starter module name you want to search, such as `azure-spring-boot-starter-xxx`.  
![Search for tag](images/search-for-tag.png "Search for a tag")  
1. Click one of the searched tags. (You will find that the address in your browser bar changes to that specific version. You can manually edit the address with other versions, then quickly switch to other versions.)  
![Tag main page](images/tag-main-page.png "Main page of a tag")  
1. Then check the README.md file there.  


### Developing version    

If you want to use the latest function in master branch, you can build the artifacts by yourself.

For example, if you want to build latest `azure-spring-boot-starter-active-directory`, you can execute commands:

```shell script
cd azure-sdk-for-java
mvn clean install -pl .\sdk\spring\azure-spring-boot-starter-active-directory\ -am
```

You can refer [Maven CLI Options] for more detail about `-pl` and `-am`.

[Maven CLI Options]: https://maven.apache.org/ref/3.1.0/maven-embedder/cli.html
