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
    ]
]

//////////////////////////////////////////////////////////////////////
//
// Implementation below here

// Check for required environment variables
def ensureEnvironment()
{
    def env = System.getenv()
    def notSet = []

    ['PRIVATE_FEED_USER_NAME', 'PRIVATE_FEED_PASSWORD', 'PRIVATE_FEED_URL'].each {
        if (!env.containsKey(it)) { notSet.add(it) }
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
    commands.execute()
}

// Execute nuget.exe with the given command line arguments
def nuget(String... args) { run('./nuget.exe', args) }

// Execute hydra.exe with the given command line arguments
def hydra(String hydraExePath, String... args)
{
    def commands = [exePrefix(), hydraExePath, args].flatten()
    commands.execute()
}

// Generate code for the given specInfo
def generate(hydraExePath, specInfo)
{
    def specDLL = findFileInPackagesDirectory(specInfo.specificationDllFileName)
    hydra(hydraExePath, '-f', 'java', '-s', 'namespace',
        '-c', specInfo.clientType,
        '-d', "../${specInfo.generatedCodeDestinationRootDirectoryName}/src/main/java/com",
        specDLL)
}

// Run nuget.exe to restore required nuget packages
def restorePackages()
{
    def env = System.getenv()
    try {
        nuget('sources', 'add',
            '-name', 'download',
            '-source', env['PRIVATE_FEED_URL'],
            '-configfile', './restore.config')
        nuget('sources', 'update',
            '-name', 'download',
            '-username', env['PRIVATE_FEED_USER_NAME'],
            '-password', env['PRIVATE_FEED_PASSWORD'],
            '-configfile', './restore.config')
        nuget('restore', 'packages.config',
            '-packagesdirectory', './packages',
            '-configfile', './restore.config')
    }
    finally {
        // Need to wait a bit, config file stays open while nuget.exe shuts down
        Thread.sleep(1000)
        new File('./restore.config').delete()
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
restorePackages()
def hydraPath = findFileInPackagesDirectory('hydra.exe')
hydraSpecs.each {
    System.out.println("generating code for ${it.specificationDllFileName}")
    generate hydraPath, it
}
