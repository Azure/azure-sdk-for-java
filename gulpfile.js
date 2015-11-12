var gulp = require('gulp');
var args = require('yargs').argv;
var colors = require('colors');
var exec = require('child_process').exec;

var mappings = {
    'azure-mgmt-compute': 'arm-compute/2015-06-15/swagger/compute.json',
    'azure-mgmt-storage': 'arm-storage/2015-06-15/swagger/storage.json',
    'azure-mgmt-resources': 'arm-storage/2014-04-01/swagger/resources.json'
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
                console.error('Invalid project name ' + project);
                process.exit(1);
            }
            codegen(project, cb);
        }
    });
});


var codegen = function(project, cb) {
    console.log('Generating ' + project + ' from spec file ' + specRoot + '/' + mappings[project]);
    exec(autoRestExe + ' -Modeler Swagger -CodeGenerator Azure.Java -Namespace com.microsoft.azure -Input ' + specRoot + '/' + mappings[project] + 
            ' -outputDirectory ' + project + '/src/main/java/com/microsoft/azure -Header MICROSOFT_MIT', function(err, stdout, stderr) {
        console.log(stdout);
        console.error(stderr);
    });
};
