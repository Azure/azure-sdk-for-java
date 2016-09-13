var gulp = require('gulp');
var args = require('yargs').argv;
var colors = require('colors');
var exec = require('child_process').exec;
var fs = require('fs');

var mappings = {
    'compute': {
        'dir': 'azure-mgmt-compute',
        'source': 'arm-compute/2016-03-30/swagger/compute.json',
        'package': 'com.microsoft.azure.management.compute',
        'args': '-FT 1'
    },
    'graph': {
        'dir': 'azure-mgmt-graph-rbac',
        'source': 'arm-graphrbac/1.6/swagger/graphrbac.json',
        'package': 'com.microsoft.azure.management.graphrbac',
        'args': '-FT 1'
    },
    'keyvault': {
        'dir': 'azure-mgmt-keyvault',
        'source': 'arm-keyvault/2015-06-01/swagger/keyvault.json',
        'package': 'com.microsoft.azure.management.keyvault',
        'args': '-FT 1'
    },
    'storage': {
        'dir': 'azure-mgmt-storage',
        'source': 'arm-storage/2016-01-01/swagger/storage.json',
        'package': 'com.microsoft.azure.management.storage',
        'args': '-FT 2'
    },
    'resources': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/resources/2016-02-01/swagger/resources.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'subscriptions': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/subscriptions/2015-11-01/swagger/subscriptions.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'features': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/features/2015-12-01/swagger/features.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'network': {
        'dir': 'azure-mgmt-network',
        'source': 'arm-network/2016-06-01/swagger/network.json',
        'package': 'com.microsoft.azure.management.network',
        'args': '-FT 1'
    },
    'website': {
        'dir': 'azure-mgmt-website',
        'source': 'arm-web/2015-08-01/swagger/service.json',
        'package': 'com.microsoft.azure.management.website',
        'args': '-FT 1'
    },
    'graph.rbac': {
        'dir': 'azure-mgmt-graph-rbac',
        'source': 'arm-graphrbac/compositeGraphRbacManagementClient.json',
        'package': 'com.microsoft.azure.management.graph.rbac',
        'args': '-FT 1'
    },
    'redis': {
        'dir': 'azure-mgmt-redis',
        'source': 'arm-redis/2016-04-01/swagger/redis.json',
        'package': 'com.microsoft.azure.management.redis',
        'args': '-FT 1'
    },
    'search': {
        'dir': 'azure-mgmt-search',
        'source': 'arm-search/2015-02-28/swagger/search.json',
        'package': 'com.microsoft.azure.management.search',
        'args': '-FT 1'
    },
    'datalake.store.filesystem': {
        'dir': 'azure-mgmt-datalake-store',
        'source': 'arm-datalake-store/filesystem/2015-10-01-preview/swagger/filesystem.json',
        'package': 'com.microsoft.azure.management.datalake.store',
        'fluent': false
    },
    'datalake.store.account': {
        'dir': 'azure-mgmt-datalake-store',
        'source': 'arm-datalake-store/account/2015-10-01-preview/swagger/account.json',
        'package': 'com.microsoft.azure.management.datalake.store',
        'fluent': false
    },
    'datalake.analytics.account': {
        'dir': 'azure-mgmt-datalake-analytics',
        'source': 'arm-datalake-analytics/account/2015-10-01-preview/swagger/account.json',
        'package': 'com.microsoft.azure.management.datalake.analytics',
        'fluent': false
    },
    'datalake.analytics.job': {
        'dir': 'azure-mgmt-datalake-analytics',
        'source': 'arm-datalake-analytics/job/2016-03-20-preview/swagger/job.json',
        'package': 'com.microsoft.azure.management.datalake.analytics',
        'fluent': false
    },
    'datalake.analytics.catalog': {
        'dir': 'azure-mgmt-datalake-analytics',
        'source': 'arm-datalake-analytics/catalog/2015-10-01-preview/swagger/catalog.json',
        'package': 'com.microsoft.azure.management.datalake.analytics',
        'fluent': false
    },
    'batchService': {
        'dir': 'azure-batch',
        'source': 'batch/2016-07-01.3.1/swagger/BatchService.json',
        'package': 'com.microsoft.azure.batch.protocol',
        'fluent': false,
        'args': '-FT 1'
    },
    'keyvault': {
        'dir': 'azure-keyvault',
        'source': 'keyvault/2015-06-01/swagger/keyvault.json',
        'package': 'com.microsoft.azure.keyvault',
        'fluent': false,
        'args': '-FT 1'
    },
    'batch': {
        'dir': 'azure-mgmt-batch',
        'source': 'arm-batch/2015-12-01/swagger/BatchManagement.json',
        'package': 'com.microsoft.azure.management.batch',
        'args': '-FT 1'
    }
};

gulp.task('default', function() {
    console.log("Usage: gulp codegen [--spec-root <swagger specs root>] [--projects <project names>] [--autorest <autorest info>] [--modeler <modeler name>] [--autorest-args <AutoRest arguments>]\n");
    console.log("--spec-root");
    console.log("\tRoot location of Swagger API specs, default value is \"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master\"");
    console.log("--projects\n\tComma separated projects to regenerate, default is all. List of available project names:");
    Object.keys(mappings).forEach(function(i) {
        console.log('\t' + i.magenta);
    });
    console.log("--autorest\n\tThe version of AutoRest. E.g. 0.15.0, or the location of AutoRest repo, E.g. E:\\repo\\autorest");
    console.log("--modeler\n\tSpecifies which modeler to use. Default is 'Swagger'");
    console.log("--autorest-args\n\tPasses additional argument to AutoRest generator");
});

var isWindows = (process.platform.lastIndexOf('win') === 0);
var isLinux= (process.platform.lastIndexOf('linux') === 0);
var isMac = (process.platform.lastIndexOf('darwin') === 0);

var specRoot = args['spec-root'] || "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master";
var projects = args['projects'];
var autoRestVersion = '0.17.0-Nightly20160706'; // default
if (args['autorest'] !== undefined) {
    autoRestVersion = args['autorest'];
}
var modeler = 'Swagger'; // default
if (args['modeler'] !== undefined) {
	modeler = args['modeler'];
}
var autoRestArgs = args['autorest-args'];
var autoRestExe;

gulp.task('codegen', function(cb) {
    var nugetSource = 'https://www.myget.org/F/autorest/api/v2';
    if (autoRestVersion.match(/[0-9]+\.[0-9]+\.[0-9]+.*/)) {
        autoRestExe = 'packages\\autorest.' + autoRestVersion + '\\tools\\AutoRest.exe';
        exec('tools\\nuget.exe install AutoRest -Source ' + nugetSource + ' -Version ' + autoRestVersion + ' -o packages', function(err, stdout, stderr) {
            console.log(stdout);
            console.error(stderr);
            handleInput(projects, cb);
        });
    } else {
        autoRestExe = autoRestVersion + "/" + GetAutoRestFolder() + "AutoRest.exe";
        if (process.platform !== 'win32') {
            autoRestExe = "mono " + autoRestExe;
        }
        handleInput(projects, cb);
    }

});

var handleInput = function(projects, cb) {
    if (projects === undefined) {
        Object.keys(mappings).forEach(function(proj) {
            codegen(proj, cb);
        });
    } else {
        projects.split(",").forEach(function(proj) {
            proj = proj.replace(/\ /g, '');
            if (mappings[proj] === undefined) {
                console.error('Invalid project name "' + proj + '"!');
                process.exit(1);
            }
            codegen(proj, cb);
        });
    }
}

var codegen = function(project, cb) {
    var outputDir = mappings[project].dir + '/src/main/java/' + mappings[project].package.replace(/\./g, '/');
    deleteFolderRecursive(outputDir);
    console.log('Generating "' + project + '" from spec file ' + specRoot + '/' + mappings[project].source);
    var generator = 'Azure.Java.Fluent';
    if (mappings[project].fluent !== null && mappings[project].fluent === false) {
        generator = 'Azure.Java';
    }
    cmd = autoRestExe + ' -Modeler ' + modeler + 
                        ' -CodeGenerator ' + generator + 
                        ' -Namespace ' + mappings[project].package + 
                        ' -Input ' + specRoot + '/' + mappings[project].source + 
                        ' -outputDirectory ' + mappings[project].dir + '/src/main/java/' + mappings[project].package.replace(/\./g, '/') + 
                        ' -Header MICROSOFT_MIT_NO_CODEGEN' +
                        ' -' + autoRestArgs;
    if (mappings[project].args !== undefined) {
        cmd = cmd + ' ' + mappings[project].args;
    }
    console.log('Command: ' + cmd);
    exec(cmd, function(err, stdout, stderr) {
        console.log(stdout);
        console.error(stderr);
    });
};

var deleteFolderRecursive = function(path) {
    var header = "Code generated by Microsoft (R) AutoRest Code Generator";
    if(fs.existsSync(path)) {
        fs.readdirSync(path).forEach(function(file, index) {
            var curPath = path + "/" + file;
            if(fs.lstatSync(curPath).isDirectory()) { // recurse
                deleteFolderRecursive(curPath);
            } else { // delete file
                var content = fs.readFileSync(curPath).toString('utf8');
                if (content.indexOf(header) > -1) {
                    fs.unlinkSync(curPath);
                }
            }
        });
    }
};

function GetAutoRestFolder() {
  if (isWindows) {
    return "src/core/AutoRest/bin/Debug/net451/win7-x64/";
  }
  if( isMac ) {
	return "src/core/AutoRest/bin/Debug/net451/osx.10.11-x64/";
  } 
  if( isLinux ) { 
	return "src/core/AutoRest/bin/Debug/net451/ubuntu.14.04-x64/"
  }
   throw new Error("Unknown platform?");
}
