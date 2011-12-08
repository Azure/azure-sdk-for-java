<h1>Windows Azure SDK for Java</h1>
<p>This SDK allows you to build Windows Azure applications in  Java that allow
you to take advantage of Azure scalable cloud computing resources: table and blob
storage, messaging through Service Bus.</p>

<p>The SDK components available in this respository are:</p>
<ul>
    <li>Client Libraries for Windows Azure Service Runtime, Storage Services and
    Service Bus</li>
    <li>Code Samples</li>
    <li>Basic Tests</li>
</ul>
<p>For documentation please see the <a href="http://www.windowsazure.com/en-us/develop/java/">
Windows Azure Java Developer Center</a></p>

<h1>Getting Started</h1>
<h2>Download</h2>
<h3>Option 1: Via Git</h3>
<p>To get the source code of the SDK via git just type:<br/>
<pre>git clone git://github.com/WindowsAzure/azure-sdk-for-java.git
cd ./azure-sdk-for-java</pre>

<h3>Option 2: Via Maven</h3>
<p>To get the binaries of this library as distributed by Microsoft, ready for use
within your project you can also have them installed by the Java package manager Maven.<br/>
<pre><dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-windowsazure-api</artifactId>
  <version>0.1.0</version>
</dependency></pre></p>

<h2>Usage</h2>
<p>To use this SDK to call Windows Azure services, you need to first create an
account.  To host your Java code in Windows Azure, you additionally need to download
the full Windows Azure SDK for Java – which includes packaging, emulation, and
deployment tools.</p>

<h2>Code Samples</h2>
<p>The following is a quick example on how to set up a Azure blob using the API
and uploading a file to it.<br/>

<pre>import com.microsoft.windowsazure.services.core.storage.*;
import com.microsoft.windowsazure.services.blob.client.*;

public class BlobSample {

    public static final String storageConnectionString = 
            "DefaultEndpointsProtocol=http;" + 
               "AccountName=your_account_name;" + 
               "AccountKey= your_account_name"; 

    public static void main(String[] args) 
    {
        try
        {
            CloudStorageAccount account;
            CloudBlobClient serviceClient;
            CloudBlobContainer container;
            CloudBlockBlob blob;
            
            account = CloudStorageAccount.parse(storageConnectionString);
            serviceClient = account.createCloudBlobClient();
            // Container name must be lower case.
            container = serviceClient.getContainerReference("blobsample");
            container.createIfNotExist();
            
            // Set anonymous access on the container.
            BlobContainerPermissions containerPermissions;
            containerPermissions = new BlobContainerPermissions();

            // Upload an image file.
            blob = container.getBlockBlobReference("image1.jpg");
            File fileReference = new File ("c:\\myimages\\image1.jpg");
blob.upload(new FileInputStream(fileReference), fileReference.length());
        }
        catch (FileNotFoundException fileNotFoundException)
        {
            System.out.print("FileNotFoundException encountered: ");
            System.out.println(fileNotFoundException.getMessage());
            System.exit(-1);
        }
        catch (StorageException storageException)
        {
            System.out.print("StorageException encountered: ");
            System.out.println(storageException.getMessage());
            System.exit(-1);
        }
        catch (Exception e)
        {
            System.out.print("Exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        
    }
}
</pre></p>

<h1>Minimum Requirements</h1>
<ul>
    <li>Java 1.6</li>
</ul>

<h1>Need Help?</h1>
<p>Be sure to check out the Windows Azure <a href="http://go.microsoft.com/fwlink/?LinkId=234489">
Developer Forums on Stack Overflow</a> if you have trouble with the provided code.</p>

<h1>Feedback</h1>
<p>For feedback related specificically to this SDK, please use the Issues
section of the repository.</p>
<p>For general suggestions about Windows Azure please use our
<a href="http://www.mygreatwindowsazureidea.com/forums/34192-windows-azure-feature-voting">UserVoice forum</a>.</p>

<h1>Learn More</h1>
<ul>
    <li><a href="http://www.windowsazure.com/en-us/develop/java/">Windows Azure Java
    Developer Center</a></li>
    <li><a href="http://dl.windowsazure.com/javadoc/">
    JavaDocs</a></li>
</ul>

