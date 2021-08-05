var path = require('path');
var gulp = require('gulp');
var args = require('yargs').argv;
var colors = require('colors');
var execa = require('execa');
var pAll = require('p-all');
var os = require('os');
var fs = require('fs');
var shell = require('gulp-shell');
var ghPages = require('gulp-gh-pages');
var argv = require('yargs').argv;
var gulpif = require('gulp-if');
var exec = require('child_process').exec;
const xmlparser = require('fast-xml-parser');

const mappings = require('./api-specs.json');
const defaultSpecRoot = "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master";

async function defaultInfo() {
    console.log("Usage: gulp codegen " +
        "[--spec-root <swagger specs root>] " +
        "[--projects <project names>] " +
        "[--autorest <autorest info>] " +
        "[--autorest-java <autorest.java info>] " +
        "[--debug] " +
        "[--parallel <number>] " +
        "[--autorest-args <AutoRest arguments>]\n");

    console.log("--spec-root");
    console.log(`\tRoot location of Swagger API specs, default value is "${defaultSpecRoot}"`);

    console.log("--projects");
    console.log("\tComma separated projects to regenerate, default is all. List of available project names:");
    Object.keys(mappings).forEach(function(i) {
        console.log('\t' + i.magenta);
    });

    console.log("--autorest");
    console.log("\tThe version of AutoRest. E.g. 2.0.9, or the location of AutoRest repo, e.g. E:\\repo\\autorest");

    console.log("--autorest-java");
    console.log("\tOption#1: Path to an autorest.java generator to pass as a --use argument to AutoRest.");
    console.log("\tOption#2: The version of AutoRest.Java. E.g. 2.0.9. You can also pass latest or preview.");
    console.log("\tUsually you'll only need to provide this and not a --autorest argument in order to work on Java code generation.");
    console.log("\tSee https://github.com/Azure/autorest/blob/master/.attic/developer/autorest-extension.md");

    console.log("--debug");
    console.log("\tFlag that allows you to attach a debugger to the autorest.java generator.");

    console.log("--parallel");
    console.log("\tSpecifies the maximum number of projects to generate in parallel.");
    console.log("\tDefaults to the number of logical CPUs on the system. (On this system, " + os.cpus().length + ")");

    console.log("--autorest-args");
    console.log("\tPasses additional argument to AutoRest generator");
}

const maxParallelism = parseInt(args['parallel'], 10) || os.cpus().length;
var specRoot = args['spec-root'] || defaultSpecRoot;
var sdkRoot = path.join(process.cwd(), '../..')
var projects = args['projects'];
var autoRestVersion = '2.0.4417'; // default
if (args['autorest'] !== undefined) {
    autoRestVersion = args['autorest'];
}
var autoRestJavaVersion = ''; // default
if (args['autorest-java'] !== undefined) {
    autoRestJavaVersion = args['autorest-java'];
}
var debug = args['debug'];
var autoRestArgs = args['autorest-args'] || '';
var autoRestExe;
const mgmtPomFilename = 'pom.mgmt.xml'

async function generate(cb) {
    if (autoRestVersion.match(/[0-9]+\.[0-9]+\.[0-9]+.*/) ||
        autoRestVersion == 'preview' || autoRestVersion == 'latest') {
            autoRestExe = 'autorest --version=' + autoRestVersion;
            await handleInput(projects, cb);
    } else {
        autoRestExe = "node " + path.join(autoRestVersion, "src/autorest-core/dist/app.js");
        await handleInput(projects, cb);
    }
}

async function handleInput(projects, cb) {
    console.info(`Generating up to ${maxParallelism} projects in parallel..`);
    if (projects === undefined) {
        const actions = Object.keys(mappings).map(proj => {
            return () => codegen(proj, cb);
        });
        pAll(actions, { concurrency: maxParallelism });
    } else {
        const actions = projects.split(",").map(proj => {
            return () => {
                proj = proj.replace(/\ /g, '');
                if (mappings[proj] === undefined) {
                    console.error('Invalid project name "' + proj + '"!');
                    process.exit(1);
                }
                return codegen(proj, cb);
            }
        });
        pAll(actions, { maxParallelism });
    }
}

async function codegen(project, cb) {
    if (!args['preserve']) {
        deleteMgmtFolders(project);
    }

    console.log('Generating "' + project + '" from spec file ' + specRoot + '/' + mappings[project].source);

    const generatorPath = autoRestJavaVersion == 'preview' || autoRestJavaVersion == 'latest' 
		|| autoRestJavaVersion.match(/^[0-9]+\.[0-9]+\.[0-9a-zA-Z]+$/)
        ? `--use=@microsoft.azure/autorest.java@` + autoRestJavaVersion +` `
        : (autoRestJavaVersion == '' ? '' : `--use=${path.resolve(args['autorest-java'])} `);

    const regenManager = args['regenerate-manager'] ? ' --regenerate-manager=true ' : '';

    const genInterface = args['generate-interface'] ? ' --generate-interface=true ' : '';

    var apiVersion;
    if (mappings[project].apiVersion !== undefined) {
        apiVersion = " --api-version='" + mappings[project].apiVersion + "'" + ' ';
    } else {
        apiVersion = '';
    }

    var fconfig;
    if (mappings[project].fconfig !== undefined) {
        fconfig = " --fconfig='" + JSON.stringify(mappings[project].fconfig) + "'" + ' ';
    } else {
        fconfig = '';
    }

    // path.join won't work if specRoot is a URL
    cmd = autoRestExe + ' ' + specRoot + "/" + mappings[project].source +
                        ' --java ' +
                        ' --azure-arm ' +
                        ' --azure-libraries-for-java-folder=' + sdkRoot + ' ' +
                        ` --license-header=MICROSOFT_MIT_NO_CODEGEN ` +
                        generatorPath +
                        regenManager +
                        genInterface +
                        apiVersion +
                        fconfig +
                        autoRestArgs;

    if (mappings[project].args !== undefined) {
        cmd += ' ' + mappings[project].args;
    }

    if (debug) {
        cmd += ' --java.debugger';
    }

    console.log('Command: ' + cmd);
    await execa(cmd, [], { shell: true, stdio: "inherit" });
    if (cmd.includes('--multiapi')) {
        changePom(project);
    }
};

var deleteMgmtFolders = function(project) {
    var modules = []

    project = project.split('/')[0]
    var projectRoot = path.join(sdkRoot, 'sdk', project);
    var projectPom = path.join(projectRoot, mgmtPomFilename);
    // find all modules from pom
    if(fs.existsSync(projectPom)) {
        var xml = fs.readFileSync(projectPom, {encoding: 'utf-8'});
        var mods = xmlparser.parse(xml).project.modules.module
        if (typeof mods === 'string') {
            modules.push(mods);
        } else {
            modules = mods;
        }
    }

    modules.forEach(function(mod, index) {
        moduleDir = path.join(projectRoot, mod);
        if(fs.lstatSync(moduleDir).isDirectory()) {
            deleteFolderRecursive(moduleDir);
        }
    });
}

function deleteFolderRecursive(folder) {
    var header = "Code generated by Microsoft (R) AutoRest Code Generator";
    if(fs.existsSync(folder)) {
        fs.readdirSync(folder).forEach(function(file, index) {
            var curPath = folder + "/" + file;
            if(fs.lstatSync(curPath).isDirectory() && path.dirname(file) != "test") { // recurse
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

function changePom(project) {
    var modules = []

    project = project.split('/')[0]
    var projectRoot = path.join(sdkRoot, 'sdk', project);
    var projectPom = path.join(projectRoot, mgmtPomFilename);

    // search modules
    fs.readdirSync(projectRoot).forEach(function(folder, index) {
        if (fs.lstatSync(path.join(projectRoot, folder)).isDirectory()) {
            if (folder.startsWith('mgmt-')) {
                modules.push(folder);
            }
        }
    })

    pomHeader = 
`<!-- Copyright (c) Microsoft Corporation. All rights reserved.
     Licensed under the MIT License. -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">`;
     pomVersion = '<version>1.0.0</version>  <!-- Need not change for every release-->'

    // add all modules to pom
    if(fs.existsSync(projectPom)) {
        var xmlContent = fs.readFileSync(projectPom, {encoding: 'utf-8'});
        var xml = xmlparser.parse(xmlContent);
        xml.project.modules.module = modules;
        xmlContent = new xmlparser.j2xParser({format: true, indentBy: "  "}).parse(xml);
        xmlContent = xmlContent.replace('<project>', pomHeader);
        xmlContent = xmlContent.replace('<version>1.0.0</version>', pomVersion);

        fs.writeFileSync(projectPom, xmlContent, {encoding: 'utf-8'})
    }
    
    // change all module pom.xml
    modules.forEach(function(mod, index) {
        var modulePom = path.join(projectRoot, mod, 'pom.xml');
        if (fs.existsSync(modulePom)) {
            var pomContent = fs.readFileSync(modulePom, {encoding: 'utf-8'});
            pomContent = pomContent.replace('<version>1.1.0</version>', '<version>1.3.2</version>');
            pomContent = pomContent.replace('<relativePath>../../../pom.management.xml</relativePath>', '<relativePath>../../parents/azure-arm-parent/pom.xml</relativePath>');
            fs.writeFileSync(modulePom, pomContent, {encoding: 'utf-8'});
        }
    });
}

async function prepareBuild() {
    return shell.task('mvn package javadoc:aggregate -DskipTests -q');
}

async function prepareStage() {
    return gulp.src('./target/site/apidocs/**/*').pipe(gulp.dest('./dist'));
}

async function preparePublish() {
    var options = {};
    if(process.env.GH_TOKEN){
        options.remoteUrl = 'https://' + process.env.GH_TOKEN + '@github.com/azure/azure-libraries-for-java.git'
    }
    return gulp.src('./dist/**/*').pipe(gulpif(!argv.dryrun, ghPages(options)));
}

const build = prepareBuild;
const stage = gulp.series(build, prepareStage);
const publish = gulp.series(stage, preparePublish);

//exports task
exports.default = defaultInfo;
exports.codegen = generate;
exports.build = build;
exports.stage = stage;
exports.publish = publish;
