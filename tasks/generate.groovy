/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

//
// List of spec DLLs and generation detail
// Add to this list to add new specs
//
def hydraSpecs = [
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.Compute.Specification.dll",
        clientType: "Microsoft.WindowsAzure.Management.Compute.ComputeManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-compute"
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.Specification.dll",
        clientType: 'Microsoft.WindowsAzure.Management.ManagementClient',
        generatedCodeDestinationRootDirectoryName: 'management'
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.Network.Specification.dll",
        clientType: "Microsoft.WindowsAzure.Management.Network.NetworkManagementClient",
        generatedCodeDestinationRootDirectoryName: 'management-network'
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.ServiceBus.Specification.dll",
        clientType: 'Microsoft.WindowsAzure.Management.ServiceBus.ServiceBusManagementClient',
        generatedCodeDestinationRootDirectoryName: 'management-serviceBus'
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.Sql.Specification.dll",
        clientType: "Microsoft.WindowsAzure.Management.Sql.SqlManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-sql"
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.Storage.Specification.dll",
        clientType: "Microsoft.WindowsAzure.Management.Storage.StorageManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-storage"
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.WebSites.Specification.dll",
        clientType: "Microsoft.WindowsAzure.Management.WebSites.WebSiteManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-websites"
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.Scheduler.Specification.dll",
        clientType: "Microsoft.WindowsAzure.Management.Scheduler.SchedulerManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-scheduler"
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.Scheduler.Specification.dll",
        clientType: "Microsoft.WindowsAzure.Scheduler.SchedulerClient",
        generatedCodeDestinationRootDirectoryName: "management-scheduler"
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.CloudServices.Specification.dll",
        clientType: "Microsoft.WindowsAzure.Management.Scheduler.CloudServiceManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-scheduler"
    ],
    [
        specificationDllFileName: "Microsoft.WindowsAzure.Management.MediaServices.Specification.dll",
        clientType: "Microsoft.WindowsAzure.Management.MediaServices.MediaServicesManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-media"
    ],
    [
        specificationDllFileName: "Microsoft.Azure.Management.Resources.Specification.dll",
        clientType: "Microsoft.Azure.Management.Resources.ResourceManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-resources"
    ],
    [
        specificationDllFileName: "Microsoft.Azure.Management.Sql.Specification.dll",
        clientType: "Microsoft.Azure.Management.Sql.SqlManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-sql2"
    ],
    [
        specificationDllFileName: "Microsoft.Azure.Management.WebSites.Specification.dll",
        clientType: "Microsoft.Azure.Management.WebSites.WebSiteManagementClient",
        generatedCodeDestinationRootDirectoryName: "management-websites2"
    ]
]

//////////////////////////////////////////////////////////////////////
//
// Implementation below here

// Check for required environment variables
def ensureEnvironment()
{
    def env = System.getenv()
    def notSetCount = 0;

    // Specify either feed location or feed url
    ['PRIVATE_FEED_LOCATION', 'PRIVATE_FEED_URL'].each {
        if (!env.containsKey(it)) { notSetCount++ }
    }
    if (notSetCount == 2) {
        throw new Exception("Please either set environment variable PRIVATE_FEED_LOCATION or PRIVATE_FEED_URL")
    }

    def notSet = []
    // Specify user name and password if using feed url
    if (env.containsKey('PRIVATE_FEED_URL')) {
        ['PRIVATE_FEED_USER_NAME', 'PRIVATE_FEED_PASSWORD'].each {
            if (!env.containsKey(it)) { notSet.add(it) }
        }
    }
    if (notSet) {
        throw new Exception("Required environment variables not set: ${notSet}")
    }
}

// Download a file from a url
def download(address)
{
  def file = new FileOutputStream(address.tokenize("/")[-1])
  def out = new BufferedOutputStream(file)
  out << new URL(address).openStream()
  out.close()
}

// Calculates if a CLR exe should run directly or via mono
def exePrefix() {
    def isWindows = System.properties['os.name'].toLowerCase().contains('windows')
    if (isWindows) {
        return []
    }
    ["mono", '--runtime=v4.0.30319']
}

// Run an executable with the given command line arguments
def run(String exePath, String... args)
{
    def commands = [exePrefix(), exePath, args].flatten()
    commands.execute().waitFor()
}

// Execute nuget.exe with the given command line arguments
def nuget(String... args) { run('nuget.exe', args) }

// Execute hydra.exe with the given command line arguments
def hydra(String hydraExePath, String... args)
{
    def commands = [exePrefix(), hydraExePath, args].flatten()
    return commands.execute()
}

// Generate code for the given specInfo
def generate(hydraExePath, specInfo)
{
    def specDLL = findFileInPackagesDirectory(specInfo.specificationDllFileName)
    return hydra(hydraExePath, '-f', 'JavaEE.Azure', '-s', 'namespace',
        '-c', specInfo.clientType,
        '-d', "../${specInfo.generatedCodeDestinationRootDirectoryName}/src/main/java/com",
        specDLL)
}

// Create restore.config for local feed management
def createConfig()
{
    File existing = new File("./restore.config")
    if (existing.exists()) {
        existing.delete()
    }
    File configFile = new File("./restore.config")
    configFile << "<configurations></configurations>"
}

// Run nuget.exe to restore required nuget packages
def restorePackages()
{
    def folder = new File("packages")
    if (!folder.exists()) {
        folder.mkdirs()
    }
    def env = System.getenv()
    try {
        if (env.containsKey('PRIVATE_FEED_LOCATION')) {
            nuget('sources', 'add',
                '-name', 'primaryFeed',
                '-source', env['PRIVATE_FEED_LOCATION'],
                '-configfile', './restore.config')
            nuget('restore', 'packages.config',
                '-packagesdirectory', './packages',
                '-configfile', './restore.config')
        }
        if (env.containsKey('PRIVATE_FEED_URL')) {
            nuget('sources', 'add',
                '-name', 'secondaryFeed',
                '-source', env['PRIVATE_FEED_URL'],
                '-configfile', './restore.config')
            nuget('sources', 'update',
                '-name', 'secondaryFeed',
                '-username', env['PRIVATE_FEED_USER_NAME'],
                '-password', env['PRIVATE_FEED_PASSWORD'],
                '-configfile', './restore.config')
            nuget('restore', 'packages.config',
                '-packagesdirectory', './packages',
                '-configfile', './restore.config')
        }
    }
    finally {
        // Need to wait a bit, config file stays open while nuget.exe shuts down
//        Thread.sleep(1000)
 //       new File('./restore.config').delete()
    }
}

// Find a file with the given file name (case insensitive match) somewhere under packages
def findFileInPackagesDirectory(pattern)
{
    def path
    new File('packages').traverse([nameFilter: ~"(?i)${pattern}\$"], { path = it.toString() })
    path

}

//
// Main logic here
//
ensureEnvironment()
download("http://www.nuget.org/nuget.exe")
createConfig()
restorePackages()
def hydraPath = findFileInPackagesDirectory('hydra.exe')

def processes = [];
hydraSpecs.each {
    System.out.println("generating code for ${it.specificationDllFileName}")
    processes.push(generate(hydraPath, it))
}
// Wait for all generations to finish
processes.each() {
    it.consumeProcessErrorStream(System.err)
    it.waitFor()
}
System.out.println("Finished generating")
