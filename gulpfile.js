var gulp = require('gulp');
var args = require('yargs').argv;
var colors = require('colors');
var exec = require('child_process').exec;
var fs = require('fs');

var mappings = {
    'compute': {
        'dir': 'azure-mgmt-compute',
        'source': 'arm-compute/compositeComputeClient.json',
        'package': 'com.microsoft.azure.management.compute',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'eventhub': {
        'dir': 'azure-mgmt-eventhub',
        'source': 'arm-eventhub/2015-08-01/swagger/EventHub.json',
        'package': 'com.microsoft.azure.management.eventhub',
        'args': '-FT 1'
    },
    'servicefabric': {
        'dir': 'azure-mgmt-servicefabric',
        'source': 'arm-servicefabric/2016-09-01/swagger/servicefabric.json',
        'package': 'com.microsoft.azure.management.servicefabric',
        'args': '-FT 1'
    },
    'notificationhubs': {
        'dir': 'azure-mgmt-notificationhubs',
        'source': 'arm-notificationhubs/2017-04-01/swagger/notificationhubs.json',
        'package': 'com.microsoft.azure.management.notificationhubs',
        'args': '-FT 1'
    },
    'analysisservices': {
        'dir': 'azure-mgmt-analysisservices',
        'source': 'arm-analysisservices/2016-05-16/swagger/analysisservices.json',
        'package': 'com.microsoft.azure.management.analysisservices',
        'args': '-FT 1'
    },
    'automation': {
        'dir': 'azure-mgmt-automation',
        'source': 'arm-automation/compositeAutomation.json',
        'package': 'com.microsoft.azure.management.authorization',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'billing': {
        'dir': 'azure-mgmt-billing',
        'source': 'arm-billing/2017-04-24-preview/swagger/billing.json',
        'package': 'com.microsoft.azure.management.billing',
        'args': '-FT 1'
    },
    'cognitiveservices': {
        'dir': 'azure-mgmt-cognitiveservices',
        'source': 'arm-cognitiveservices/2017-04-18/swagger/cognitiveservices.json',
        'package': 'com.microsoft.azure.management.cognitiveservices',
        'args': '-FT 1'
    },
    'consumption': {
        'dir': 'azure-mgmt-consumption',
        'source': 'arm-consumption/2017-04-24-preview/swagger/consumption.json',
        'package': 'com.microsoft.azure.management.consumption',
        'args': '-FT 1'
    },
    'customerinsights': {
        'dir': 'azure-mgmt-customerinsights',
        'source': 'arm-customer-insights/2017-04-26/swagger/customer-insights.json',
        'package': 'com.microsoft.azure.management.customerinsights',
        'args': '-FT 1'
    },
    'devtestlab': {
        'dir': 'azure-mgmt-devtestlab',
        'source': 'arm-devtestlabs/2016-05-15/swagger/DTL.json',
        'package': 'com.microsoft.azure.management.devtestlab',
        'args': '-FT 1'
    },
    'insights': {
        'dir': 'azure-mgmt-insights',
        'source': 'arm-insights/compositeInsightsManagementClient.json',
        'package': 'com.microsoft.azure.management.gallery',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'intune': {
        'dir': 'azure-mgmt-intune',
        'source': 'arm-intune/2015-01-14-preview/swagger/intune.json',
        'package': 'com.microsoft.azure.management.intune',
        'args': '-FT 1'
    },
    'iothub': {
        'dir': 'azure-mgmt-devices',
        'source': 'arm-iothub/2017-01-19/swagger/iothub.json',
        'package': 'com.microsoft.azure.management.devices',
        'args': '-FT 1'
    },
    'logic': {
        'dir': 'azure-mgmt-logic',
        'source': 'arm-logic/2016-06-01/swagger/logic.json',
        'package': 'com.microsoft.azure.management.logic',
        'args': '-FT 1'
    },
    'machinelearning': {
        'dir': 'azure-mgmt-machinelearning',
        'source': 'arm-machinelearning/2017-01-01/swagger/webservices.json',
        'package': 'com.microsoft.azure.management.machinelearning',
        'args': '-FT 1'
    },
    'media': {
        'dir': 'azure-mgmt-media',
        'source': 'arm-mediaservices/2015-10-01/swagger/media.json',
        'package': 'com.microsoft.azure.management.media',
        'args': '-FT 1'
    },
    'operationalinsights': {
        'dir': 'azure-mgmt-operationalinsights',
        'source': 'arm-operationalinsights/compositeOperationalInsights.json',
        'package': 'com.microsoft.azure.management.operationalinsights',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'powerbi': {
        'dir': 'azure-mgmt-powerbi',
        'source': 'arm-powerbiembedded/2016-01-29/swagger/powerbiembedded.json',
        'package': 'com.microsoft.azure.management.powerbi',
        'args': '-FT 1'
    },
    'recoveryservices': {
        'dir': 'azure-mgmt-recoveryservices',
        'source': 'arm-recoveryservices/compositeRecoveryServicesClient.json',
        'package': 'com.microsoft.azure.management.recoveryservices',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'recoveryservicesbackup': {
        'dir': 'azure-mgmt-recoveryservicesbackup',
        'source': 'arm-recoveryservicesbackup/2016-12-01/swagger/backupManagement.json',
        'package': 'com.microsoft.azure.management.recoveryservicesbackup',
        'args': '-FT 1'
    },
    'recoveryservicessiterecovery': {
        'dir': 'azure-mgmt-recoveryservicessiterecovery',
        'source': 'arm-recoveryservicessiterecovery/2016-08-10/swagger/service.json',
        'package': 'com.microsoft.azure.management.recoveryservicessiterecovery',
        'args': '-FT 1'
    },
    'relay': {
        'dir': 'azure-mgmt-relay',
        'source': 'arm-relay/2017-04-01/swagger/relay.json',
        'package': 'com.microsoft.azure.management.relay',
        'args': '-FT 1'
    },
    'servermanagement': {
        'dir': 'azure-mgmt-servermanagement',
        'source': 'arm-servermanagement/2016-07-01-preview/swagger/servermanagement.json',
        'package': 'com.microsoft.azure.management.servermanagement',
        'args': '-FT 1'
    },
    'storsimple8000series': {
        'dir': 'azure-mgmt-storsimple8000series',
        'source': 'arm-storsimple8000series/2017-06-01/swagger/storsimple.json',
        'package': 'com.microsoft.azure.management.storsimple8000series',
        'args': '-FT 1'
    },
    'streamanalytics': {
        'dir': 'azure-mgmt-streamanalytics',
        'source': 'arm-streamanalytics/compositeStreamAnalytics.json',
        'package': 'com.microsoft.azure.management.streamanalytics',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'graphrbac': {
        'dir': 'azure-mgmt-graph-rbac',
        'source': 'arm-graphrbac/1.6/swagger/graphrbac.json',
        'package': 'com.microsoft.azure.management.graphrbac',
        'args': '-FT 1'
    },
    'authorization': {
        'dir': 'azure-mgmt-graph-rbac',
        'source': 'arm-authorization/2015-07-01/swagger/authorization.json',
        'package': 'com.microsoft.azure.management.graphrbac',
        'args': '-FT 1'
    },
    'arm-keyvault': {
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
        'source': 'arm-resources/resources/2016-09-01/swagger/resources.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'subscriptions': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/subscriptions/2016-06-01/swagger/subscriptions.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'features': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/features/2015-12-01/swagger/features.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'policy': {
        'dir': 'azure-mgmt-resources',
        'source': 'arm-resources/policy/2016-04-01/swagger/policy.json',
        'package': 'com.microsoft.azure.management.resources'
    },
    'network': {
        'dir': 'azure-mgmt-network',
        'source': 'arm-network/compositeNetworkClient.json',
        'package': 'com.microsoft.azure.management.network',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'appservice': {
        'dir': 'azure-mgmt-appservice',
        'source': 'arm-web/compositeWebAppClient.json',
        'package': 'com.microsoft.azure.management.appservice',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'redis': {
        'dir': 'azure-mgmt-redis',
        'source': 'arm-redis/2016-04-01/swagger/redis.json',
        'package': 'com.microsoft.azure.management.redis',
        'args': '-FT 1'
    },
    'search': {
        'dir': 'azure-mgmt-search',
        'source': 'arm-search/2015-08-19/swagger/search.json',
        'package': 'com.microsoft.azure.management.search',
        'args': '-FT 1'
    },
    'trafficmanager': {
        'dir': 'azure-mgmt-trafficmanager',
        'source': 'arm-trafficmanager/2017-05-01/swagger/trafficmanager.json',
        'package': 'com.microsoft.azure.management.trafficmanager',
        'args': '-FT 1'
    },
    'datalake.store.account': {
        'dir': 'azure-mgmt-datalake-store',
        'source': 'arm-datalake-store/account/2016-11-01/swagger/account.json',
        'package': 'com.microsoft.azure.management.datalake.store',
        'fluent': false
    },
    'datalake.analytics.account': {
        'dir': 'azure-mgmt-datalake-analytics',
        'source': 'arm-datalake-analytics/account/2016-11-01/swagger/account.json',
        'package': 'com.microsoft.azure.management.datalake.analytics',
        'fluent': false
    },
    'datalake.analytics.job': {
        'dir': 'azure-mgmt-datalake-analytics',
        'source': 'arm-datalake-analytics/job/2016-11-01/swagger/job.json',
        'package': 'com.microsoft.azure.management.datalake.analytics',
        'fluent': false
    },
    'datalake.analytics.catalog': {
        'dir': 'azure-mgmt-datalake-analytics',
        'source': 'arm-datalake-analytics/catalog/2016-11-01/swagger/catalog.json',
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
        'source': 'arm-batch/2017-05-01/swagger/BatchManagement.json',
        'package': 'com.microsoft.azure.management.batch',
        'args': '-FT 1'
    },
    'sql': {
        'dir': 'azure-mgmt-sql',
        'source': 'arm-sql/compositeSql.json',
        'package': 'com.microsoft.azure.management.sql',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'cdn': {
        'dir': 'azure-mgmt-cdn',
        'source': 'arm-cdn/2016-10-02/swagger/cdn.json',
        'package': 'com.microsoft.azure.management.cdn',
        'args': '-FT 2'
    },
    'dns': {
        'dir': 'azure-mgmt-dns',
        'source': 'arm-dns/2016-04-01/swagger/dns.json',
        'package': 'com.microsoft.azure.management.dns',
        'args': '-FT 1'
    },
    'servicebus': {
        'dir': 'azure-mgmt-servicebus',
        'source': 'arm-servicebus/2015-08-01/swagger/servicebus.json',
        'package': 'com.microsoft.azure.management.servicebus',
        'args': '-FT 1'
    },
    'monitor': {
        'dir': 'azure-mgmt-monitor',
        'source': 'arm-monitor/compositeMonitorManagementClient.json',
        'package': 'com.microsoft.azure.management.monitor',
        'args': '-FT 1',
        'modeler': 'CompositeSwagger'
    },
    'monitor-dataplane': {
        'dir': 'azure-mgmt-monitor',
        'source': 'monitor/compositeMonitorClient.json',
        'package': 'com.microsoft.azure.management.monitor',
        'args': '-FT 1 -ServiceName Monitor',
        'modeler': 'CompositeSwagger'
    },
    'containerregistry': {
        'dir': 'azure-mgmt-containerregistry',
        'source': 'arm-containerregistry/2017-03-01/swagger/containerregistry.json',
        'package': 'com.microsoft.azure.management.containerregistry',
        'args': '-FT 1',
    },
    'scheduler': {
        'dir': 'azure-mgmt-scheduler',
        'source': 'arm-scheduler/2016-03-01/swagger/scheduler.json',
        'package': 'com.microsoft.azure.management.scheduler',
        'args': '-FT 1'
    },
    'documentdb': {
        'dir': 'azure-mgmt-documentdb',
        'source': 'arm-documentdb/2015-04-08/swagger/documentdb.json',
        'package': 'com.microsoft.azure.management.documentdb',
        'args': '-FT 1',
    }
};

gulp.task('default', function() {
    console.log("Usage: gulp codegen [--spec-root <swagger specs root>] [--projects <project names>] [--autorest <autorest info>] [--autorest-args <AutoRest arguments>]\n");
    console.log("--spec-root");
    console.log("\tRoot location of Swagger API specs, default value is \"https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master\"");
    console.log("--projects\n\tComma separated projects to regenerate, default is all. List of available project names:");
    Object.keys(mappings).forEach(function(i) {
        console.log('\t' + i.magenta);
    });
    console.log("--autorest\n\tThe version of AutoRest. E.g. 1.0.1-20170222-2300-nightly, or the location of AutoRest repo, E.g. E:\\repo\\autorest");
    console.log("--autorest-args\n\tPasses additional argument to AutoRest generator");
});

var specRoot = args['spec-root'] || "https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master";
var projects = args['projects'];
var autoRestVersion = 'latest'; // default
if (args['autorest'] !== undefined) {
    autoRestVersion = args['autorest'];
}
var autoRestArgs = args['autorest-args'];
var autoRestExe;

gulp.task('codegen', function(cb) {
    if (autoRestVersion.match(/[0-9]+\.[0-9]+\.[0-9]+.*/) ||
        autoRestVersion == 'latest') {
        autoRestExe = 'autorest ---version=' + autoRestVersion;
        handleInput(projects, cb);
    } else {
        autoRestExe = autoRestVersion + "/src/core/AutoRest/bin/Debug/netcoreapp1.0/AutoRest.dll";
        autoRestExe = "dotnet " + autoRestExe;
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
    if (!args['preserve']) {
        deleteFolderRecursive(outputDir);
    }
    console.log('Generating "' + project + '" from spec file ' + specRoot + '/' + mappings[project].source);
    var generator = 'Azure.Java.Fluent';
    if (mappings[project].fluent !== null && mappings[project].fluent === false) {
        generator = 'Azure.Java';
    }
    var modeler = 'Swagger'; // default
    if (mappings[project].modeler !== undefined) {
        modeler = mappings[project].modeler;
    }
    cmd = autoRestExe + ' -Modeler ' + modeler +
                        ' -CodeGenerator ' + generator +
                        ' -Namespace ' + mappings[project].package +
                        ' -Input ' + specRoot + '/' + mappings[project].source +
                        ' -outputDirectory ' + outputDir +
                        ' -Header MICROSOFT_MIT_NO_CODEGEN' +
                        ' -skipValidation true' +
                        ' -RegenerateManager true ' +
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
