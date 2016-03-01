var gulp = require('gulp');
var args = require('yargs').argv;
var colors = require('colors');
var exec = require('child_process').exec;

var mappings = {
    'compute': {
        'dir': 'azure-mgmt-compute',
        'source': 'arm-compute/2015-06-15/swagger/compute.json',
        'package': 'com.microsoft.azure.management.compute',
        'args': '-FT 1'
    },
    'storage': {
        'dir': 'azure-mgmt-storage',
        'source': 'arm-storage/2015-06-15/swagger/storage.json',
        'package': 'com.microsoft.azure.management.storage',
        'args': '-FT 2'
    },
    'resources': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/resources/2015-11-01/swagger/resources.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'subscriptions': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/subscriptions/2015-11-01/swagger/subscriptions.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'authorization': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/authorization/2015-01-01/swagger/authorization.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'features': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/features/2015-12-01/swagger/features.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'network': {
        'dir': 'azure-mgmt-network',
        'source': 'arm-network/2015-06-15/swagger/network.json',
        'package': 'com.microsoft.azure.management.network',
        'args': '-FT 1'
    },
    'website': {
        'dir': 'azure-mgmt-website',
        'source': 'arm-web/2015-08-01/swagger/service.json',
        'package': 'com.microsoft.azure.management.website',
        'args': '-FT 1'
    }
};

gulp.task('default', function() {
    console.log("Usage: gulp codegen [--spec-root <swagger specs root>] [--project <project name>]\n");
    console.log("--spec-root");
    console.log("\tRoot location of Swagger API specs, default value is \"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master\"");
    console.log("--project\n\tProject to regenerate, default is all. List of available project names:");
    Object.keys(mappings).forEach(function(i) {
        console.log('\t' + i.magenta);
    });
});

var specRoot = args['spec-root'] || "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master";
var project = args['project'];
var autoRestVersion = '0.13.0-Nightly20151029';
var autoRestExe = 'packages\\autorest.' + autoRestVersion + '\\tools\\AutoRest.exe';
var nugetSource = 'https://www.myget.org/F/autorest/api/v2';

gulp.task('codegen', function(cb) {
    exec('tools\\nuget.exe install autorest -Source ' + nugetSource + ' -Version ' + autoRestVersion + ' -o packages', function(err, stdout, stderr) {
        console.log(stdout);
        console.error(stderr);
        if (project === undefined) {
            Object.keys(mappings).forEach(function(proj) {
                codegen(proj, cb);
            });
        } else {
            if (mappings[project] === undefined) {
                console.error('Invalid project name "' + project + '"!');
                process.exit(1);
            }
            codegen(project, cb);
        }
    });
});


var codegen = function(project, cb) {
    console.log('Generating "' + project + '" from spec file ' + specRoot + '/' + mappings[project].source);
    cmd = autoRestExe + ' -Modeler Swagger -CodeGenerator Azure.Java -Namespace ' + mappings[project].package + ' -Input ' + specRoot + '/' + mappings[project].source + 
            ' -outputDirectory ' + mappings[project].dir + '/src/main/java/' + mappings[project].package.replace(/\./g, '/') + ' -Header MICROSOFT_MIT';
    if (mappings[project].args !== undefined) {
        cmd = cmd + ' ' + args;
    }
    exec(cmd, function(err, stdout, stderr) {
        console.log(stdout);
        console.error(stderr);
    });
};
