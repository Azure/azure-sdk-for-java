// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// Includes work from:
/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.exporter.implementation;

import io.opentelemetry.api.common.AttributeKey;

import java.util.List;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

// this is a copy of io.opentelemetry.semconv.ResourceAttributes
// because the module that contains that class is not stable,
// so don't want to take a dependency on it
@SuppressWarnings("unused")
public final class ResourceAttributes {
    /** The URL of the OpenTelemetry schema for these keys and values. */
    public static final String SCHEMA_URL = "https://opentelemetry.io/schemas/1.22.0";

    /**
     * Uniquely identifies the framework API revision offered by a version ({@code os.version}) of the
     * android operating system. More information can be found <a
     * href="https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels">here</a>.
     */
    public static final AttributeKey<String> ANDROID_OS_API_LEVEL = stringKey("android.os.api_level");

    /**
     * Array of brand name and version separated by a space
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This value is intended to be taken from the <a
     *       href="https://wicg.github.io/ua-client-hints/#interface">UA client hints API</a> ({@code
     *       navigator.userAgentData.brands}).
     * </ul>
     */
    public static final AttributeKey<List<String>> BROWSER_BRANDS = stringArrayKey("browser.brands");

    /**
     * Preferred language of the user using the browser
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This value is intended to be taken from the Navigator API {@code navigator.language}.
     * </ul>
     */
    public static final AttributeKey<String> BROWSER_LANGUAGE = stringKey("browser.language");

    /**
     * A boolean that is true if the browser is running on a mobile device
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This value is intended to be taken from the <a
     *       href="https://wicg.github.io/ua-client-hints/#interface">UA client hints API</a> ({@code
     *       navigator.userAgentData.mobile}). If unavailable, this attribute SHOULD be left unset.
     * </ul>
     */
    public static final AttributeKey<Boolean> BROWSER_MOBILE = booleanKey("browser.mobile");

    /**
     * The platform on which the browser is running
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This value is intended to be taken from the <a
     *       href="https://wicg.github.io/ua-client-hints/#interface">UA client hints API</a> ({@code
     *       navigator.userAgentData.platform}). If unavailable, the legacy {@code navigator.platform}
     *       API SHOULD NOT be used instead and this attribute SHOULD be left unset in order for the
     *       values to be consistent. The list of possible values is defined in the <a
     *       href="https://wicg.github.io/ua-client-hints/#sec-ch-ua-platform">W3C User-Agent Client
     *       Hints specification</a>. Note that some (but not all) of these values can overlap with
     *       values in the <a href="./os.md">{@code os.type} and {@code os.name} attributes</a>.
     *       However, for consistency, the values in the {@code browser.platform} attribute should
     *       capture the exact value that the user agent provides.
     * </ul>
     */
    public static final AttributeKey<String> BROWSER_PLATFORM = stringKey("browser.platform");

    /** The cloud account ID the resource is assigned to. */
    public static final AttributeKey<String> CLOUD_ACCOUNT_ID = stringKey("cloud.account.id");

    /**
     * Cloud regions often have multiple, isolated locations known as zones to increase availability.
     * Availability zone represents the zone where the resource is running.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Availability zones are called &quot;zones&quot; on Alibaba Cloud and Google Cloud.
     * </ul>
     */
    public static final AttributeKey<String> CLOUD_AVAILABILITY_ZONE =
        stringKey("cloud.availability_zone");

    /**
     * The cloud platform in use.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The prefix of the service SHOULD match the one specified in {@code cloud.provider}.
     * </ul>
     */
    public static final AttributeKey<String> CLOUD_PLATFORM = stringKey("cloud.platform");

    /** Name of the cloud provider. */
    public static final AttributeKey<String> CLOUD_PROVIDER = stringKey("cloud.provider");

    /**
     * The geographical region the resource is running.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Refer to your provider's docs to see the available regions, for example <a
     *       href="https://www.alibabacloud.com/help/doc-detail/40654.htm">Alibaba Cloud regions</a>,
     *       <a href="https://aws.amazon.com/about-aws/global-infrastructure/regions_az/">AWS
     *       regions</a>, <a
     *       href="https://azure.microsoft.com/en-us/global-infrastructure/geographies/">Azure
     *       regions</a>, <a href="https://cloud.google.com/about/locations">Google Cloud regions</a>,
     *       or <a href="https://www.tencentcloud.com/document/product/213/6091">Tencent Cloud
     *       regions</a>.
     * </ul>
     */
    public static final AttributeKey<String> CLOUD_REGION = stringKey("cloud.region");

    /**
     * Cloud provider-specific native identifier of the monitored cloud resource (e.g. an <a
     * href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a> on
     * AWS, a <a href="https://learn.microsoft.com/en-us/rest/api/resources/resources/get-by-id">fully
     * qualified resource ID</a> on Azure, a <a
     * href="https://cloud.google.com/apis/design/resource_names#full_resource_name">full resource
     * name</a> on GCP)
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>On some cloud providers, it may not be possible to determine the full ID at startup, so
     *       it may be necessary to set {@code cloud.resource_id} as a span attribute instead.
     *   <li>The exact value to use for {@code cloud.resource_id} depends on the cloud provider. The
     *       following well-known definitions MUST be used if you set this attribute and they apply:
     *   <li><strong>AWS Lambda:</strong> The function <a
     *       href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a>.
     *       Take care not to use the &quot;invoked ARN&quot; directly but replace any <a
     *       href="https://docs.aws.amazon.com/lambda/latest/dg/configuration-aliases.html">alias
     *       suffix</a> with the resolved function version, as the same runtime instance may be
     *       invokable with multiple different aliases.
     *   <li><strong>GCP:</strong> The <a
     *       href="https://cloud.google.com/iam/docs/full-resource-names">URI of the resource</a>
     *   <li><strong>Azure:</strong> The <a
     *       href="https://docs.microsoft.com/en-us/rest/api/resources/resources/get-by-id">Fully
     *       Qualified Resource ID</a> of the invoked function, <em>not</em> the function app, having
     *       the form {@code
     *       /subscriptions/<SUBSCIPTION_GUID>/resourceGroups/<RG>/providers/Microsoft.Web/sites/<FUNCAPP>/functions/<FUNC>}.
     *       This means that a span attribute MUST be used, as an Azure function app can host multiple
     *       functions that would usually share a TracerProvider.
     * </ul>
     */
    public static final AttributeKey<String> CLOUD_RESOURCE_ID = stringKey("cloud.resource_id");

    /**
     * The ARN of an <a
     * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/clusters.html">ECS
     * cluster</a>.
     */
    public static final AttributeKey<String> AWS_ECS_CLUSTER_ARN = stringKey("aws.ecs.cluster.arn");

    /**
     * The Amazon Resource Name (ARN) of an <a
     * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ECS_instances.html">ECS
     * container instance</a>.
     */
    public static final AttributeKey<String> AWS_ECS_CONTAINER_ARN =
        stringKey("aws.ecs.container.arn");

    /**
     * The <a
     * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/launch_types.html">launch
     * type</a> for an ECS task.
     */
    public static final AttributeKey<String> AWS_ECS_LAUNCHTYPE = stringKey("aws.ecs.launchtype");

    /**
     * The ARN of an <a
     * href="https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_definitions.html">ECS
     * task definition</a>.
     */
    public static final AttributeKey<String> AWS_ECS_TASK_ARN = stringKey("aws.ecs.task.arn");

    /** The task definition family this task definition is a member of. */
    public static final AttributeKey<String> AWS_ECS_TASK_FAMILY = stringKey("aws.ecs.task.family");

    /** The revision for this task definition. */
    public static final AttributeKey<String> AWS_ECS_TASK_REVISION =
        stringKey("aws.ecs.task.revision");

    /** The ARN of an EKS cluster. */
    public static final AttributeKey<String> AWS_EKS_CLUSTER_ARN = stringKey("aws.eks.cluster.arn");

    /**
     * The Amazon Resource Name(s) (ARN) of the AWS log group(s).
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>See the <a
     *       href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format">log
     *       group ARN format documentation</a>.
     * </ul>
     */
    public static final AttributeKey<List<String>> AWS_LOG_GROUP_ARNS =
        stringArrayKey("aws.log.group.arns");

    /**
     * The name(s) of the AWS log group(s) an application is writing to.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Multiple log groups must be supported for cases like multi-container applications, where
     *       a single application has sidecar containers, and each write to their own log group.
     * </ul>
     */
    public static final AttributeKey<List<String>> AWS_LOG_GROUP_NAMES =
        stringArrayKey("aws.log.group.names");

    /**
     * The ARN(s) of the AWS log stream(s).
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>See the <a
     *       href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/iam-access-control-overview-cwl.html#CWL_ARN_Format">log
     *       stream ARN format documentation</a>. One log group can contain several log streams, so
     *       these ARNs necessarily identify both a log group and a log stream.
     * </ul>
     */
    public static final AttributeKey<List<String>> AWS_LOG_STREAM_ARNS =
        stringArrayKey("aws.log.stream.arns");

    /** The name(s) of the AWS log stream(s) an application is writing to. */
    public static final AttributeKey<List<String>> AWS_LOG_STREAM_NAMES =
        stringArrayKey("aws.log.stream.names");

    /**
     * The name of the Cloud Run <a
     * href="https://cloud.google.com/run/docs/managing/job-executions">execution</a> being run for
     * the Job, as set by the <a
     * href="https://cloud.google.com/run/docs/container-contract#jobs-env-vars">{@code
     * CLOUD_RUN_EXECUTION}</a> environment variable.
     */
    public static final AttributeKey<String> GCP_CLOUD_RUN_JOB_EXECUTION =
        stringKey("gcp.cloud_run.job.execution");

    /**
     * The index for a task within an execution as provided by the <a
     * href="https://cloud.google.com/run/docs/container-contract#jobs-env-vars">{@code
     * CLOUD_RUN_TASK_INDEX}</a> environment variable.
     */
    public static final AttributeKey<Long> GCP_CLOUD_RUN_JOB_TASK_INDEX =
        longKey("gcp.cloud_run.job.task_index");

    /**
     * The hostname of a GCE instance. This is the full value of the default or <a
     * href="https://cloud.google.com/compute/docs/instances/custom-hostname-vm">custom hostname</a>.
     */
    public static final AttributeKey<String> GCP_GCE_INSTANCE_HOSTNAME =
        stringKey("gcp.gce.instance.hostname");

    /**
     * The instance name of a GCE instance. This is the value provided by {@code host.name}, the
     * visible name of the instance in the Cloud Console UI, and the prefix for the default hostname
     * of the instance as defined by the <a
     * href="https://cloud.google.com/compute/docs/internal-dns#instance-fully-qualified-domain-names">default
     * internal DNS name</a>.
     */
    public static final AttributeKey<String> GCP_GCE_INSTANCE_NAME =
        stringKey("gcp.gce.instance.name");

    /** Unique identifier for the application */
    public static final AttributeKey<String> HEROKU_APP_ID = stringKey("heroku.app.id");

    /** Commit hash for the current release */
    public static final AttributeKey<String> HEROKU_RELEASE_COMMIT =
        stringKey("heroku.release.commit");

    /** Time and date the release was created */
    public static final AttributeKey<String> HEROKU_RELEASE_CREATION_TIMESTAMP =
        stringKey("heroku.release.creation_timestamp");

    /**
     * The command used to run the container (i.e. the command name).
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>If using embedded credentials or sensitive data, it is recommended to remove them to
     *       prevent potential leakage.
     * </ul>
     */
    public static final AttributeKey<String> CONTAINER_COMMAND = stringKey("container.command");

    /**
     * All the command arguments (including the command/executable itself) run by the container. [2]
     */
    public static final AttributeKey<List<String>> CONTAINER_COMMAND_ARGS =
        stringArrayKey("container.command_args");

    /** The full command run by the container as a single string representing the full command. [2] */
    public static final AttributeKey<String> CONTAINER_COMMAND_LINE =
        stringKey("container.command_line");

    /**
     * Container ID. Usually a UUID, as for example used to <a
     * href="https://docs.docker.com/engine/reference/run/#container-identification">identify Docker
     * containers</a>. The UUID might be abbreviated.
     */
    public static final AttributeKey<String> CONTAINER_ID = stringKey("container.id");

    /**
     * Runtime specific image identifier. Usually a hash algorithm followed by a UUID.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Docker defines a sha256 of the image id; {@code container.image.id} corresponds to the
     *       {@code Image} field from the Docker container inspect <a
     *       href="https://docs.docker.com/engine/api/v1.43/#tag/Container/operation/ContainerInspect">API</a>
     *       endpoint. K8s defines a link to the container registry repository with digest {@code
     *       "imageID": "registry.azurecr.io
     *       /namespace/service/dockerfile@sha256:bdeabd40c3a8a492eaf9e8e44d0ebbb84bac7ee25ac0cf8a7159d25f62555625"}.
     *       The ID is assinged by the container runtime and can vary in different environments.
     *       Consider using {@code oci.manifest.digest} if it is important to identify the same image
     *       in different environments/runtimes.
     * </ul>
     */
    public static final AttributeKey<String> CONTAINER_IMAGE_ID = stringKey("container.image.id");

    /** Name of the image the container was built on. */
    public static final AttributeKey<String> CONTAINER_IMAGE_NAME = stringKey("container.image.name");

    /**
     * Repo digests of the container image as provided by the container runtime.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li><a
     *       href="https://docs.docker.com/engine/api/v1.43/#tag/Image/operation/ImageInspect">Docker</a>
     *       and <a
     *       href="https://github.com/kubernetes/cri-api/blob/c75ef5b473bbe2d0a4fc92f82235efd665ea8e9f/pkg/apis/runtime/v1/api.proto#L1237-L1238">CRI</a>
     *       report those under the {@code RepoDigests} field.
     * </ul>
     */
    public static final AttributeKey<List<String>> CONTAINER_IMAGE_REPO_DIGESTS =
        stringArrayKey("container.image.repo_digests");

    /**
     * Container image tags. An example can be found in <a
     * href="https://docs.docker.com/engine/api/v1.43/#tag/Image/operation/ImageInspect">Docker Image
     * Inspect</a>. Should be only the {@code <tag>} section of the full name for example from {@code
     * registry.example.com/my-org/my-image:<tag>}.
     */
    public static final AttributeKey<List<String>> CONTAINER_IMAGE_TAGS =
        stringArrayKey("container.image.tags");

    /** Container name used by container runtime. */
    public static final AttributeKey<String> CONTAINER_NAME = stringKey("container.name");

    /** The container runtime managing this container. */
    public static final AttributeKey<String> CONTAINER_RUNTIME = stringKey("container.runtime");

    /**
     * Name of the <a href="https://en.wikipedia.org/wiki/Deployment_environment">deployment
     * environment</a> (aka deployment tier).
     */
    public static final AttributeKey<String> DEPLOYMENT_ENVIRONMENT =
        stringKey("deployment.environment");

    /**
     * A unique identifier representing the device
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The device identifier MUST only be defined using the values outlined below. This value is
     *       not an advertising identifier and MUST NOT be used as such. On iOS (Swift or
     *       Objective-C), this value MUST be equal to the <a
     *       href="https://developer.apple.com/documentation/uikit/uidevice/1620059-identifierforvendor">vendor
     *       identifier</a>. On Android (Java or Kotlin), this value MUST be equal to the Firebase
     *       Installation ID or a globally unique UUID which is persisted across sessions in your
     *       application. More information can be found <a
     *       href="https://developer.android.com/training/articles/user-data-ids">here</a> on best
     *       practices and exact implementation details. Caution should be taken when storing personal
     *       data or anything which can identify a user. GDPR and data protection laws may apply,
     *       ensure you do your own due diligence.
     * </ul>
     */
    public static final AttributeKey<String> DEVICE_ID = stringKey("device.id");

    /**
     * The name of the device manufacturer
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The Android OS provides this field via <a
     *       href="https://developer.android.com/reference/android/os/Build#MANUFACTURER">Build</a>.
     *       iOS apps SHOULD hardcode the value {@code Apple}.
     * </ul>
     */
    public static final AttributeKey<String> DEVICE_MANUFACTURER = stringKey("device.manufacturer");

    /**
     * The model identifier for the device
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>It's recommended this value represents a machine readable version of the model identifier
     *       rather than the market or consumer-friendly name of the device.
     * </ul>
     */
    public static final AttributeKey<String> DEVICE_MODEL_IDENTIFIER =
        stringKey("device.model.identifier");

    /**
     * The marketing name for the device model
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>It's recommended this value represents a human readable version of the device model
     *       rather than a machine readable alternative.
     * </ul>
     */
    public static final AttributeKey<String> DEVICE_MODEL_NAME = stringKey("device.model.name");

    /**
     * The execution environment ID as a string, that will be potentially reused for other invocations
     * to the same function/function version.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li><strong>AWS Lambda:</strong> Use the (full) log stream name.
     * </ul>
     */
    public static final AttributeKey<String> FAAS_INSTANCE = stringKey("faas.instance");

    /**
     * The amount of memory available to the serverless function converted to Bytes.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>It's recommended to set this attribute since e.g. too little memory can easily stop a
     *       Java AWS Lambda function from working correctly. On AWS Lambda, the environment variable
     *       {@code AWS_LAMBDA_FUNCTION_MEMORY_SIZE} provides this information (which must be
     *       multiplied by 1,048,576).
     * </ul>
     */
    public static final AttributeKey<Long> FAAS_MAX_MEMORY = longKey("faas.max_memory");

    /**
     * The name of the single function that this runtime instance executes.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>This is the name of the function as configured/deployed on the FaaS platform and is
     *       usually different from the name of the callback function (which may be stored in the <a
     *       href="/docs/general/attributes.md#source-code-attributes">{@code code.namespace}/{@code
     *       code.function}</a> span attributes).
     *   <li>For some cloud providers, the above definition is ambiguous. The following definition of
     *       function name MUST be used for this attribute (and consequently the span name) for the
     *       listed cloud providers/products:
     *   <li><strong>Azure:</strong> The full name {@code <FUNCAPP>/<FUNC>}, i.e., function app name
     *       followed by a forward slash followed by the function name (this form can also be seen in
     *       the resource JSON for the function). This means that a span attribute MUST be used, as an
     *       Azure function app can host multiple functions that would usually share a TracerProvider
     *       (see also the {@code cloud.resource_id} attribute).
     * </ul>
     */
    public static final AttributeKey<String> FAAS_NAME = stringKey("faas.name");

    /**
     * The immutable version of the function being executed.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Depending on the cloud provider and platform, use:
     *   <li><strong>AWS Lambda:</strong> The <a
     *       href="https://docs.aws.amazon.com/lambda/latest/dg/configuration-versions.html">function
     *       version</a> (an integer represented as a decimal string).
     *   <li><strong>Google Cloud Run (Services):</strong> The <a
     *       href="https://cloud.google.com/run/docs/managing/revisions">revision</a> (i.e., the
     *       function name plus the revision suffix).
     *   <li><strong>Google Cloud Functions:</strong> The value of the <a
     *       href="https://cloud.google.com/functions/docs/env-var#runtime_environment_variables_set_automatically">{@code
     *       K_REVISION} environment variable</a>.
     *   <li><strong>Azure Functions:</strong> Not applicable. Do not set this attribute.
     * </ul>
     */
    public static final AttributeKey<String> FAAS_VERSION = stringKey("faas.version");

    /** The CPU architecture the host system is running on. */
    public static final AttributeKey<String> HOST_ARCH = stringKey("host.arch");

    /**
     * Unique host ID. For Cloud, this must be the instance_id assigned by the cloud provider. For
     * non-containerized systems, this should be the {@code machine-id}. See the table below for the
     * sources to use to determine the {@code machine-id} based on operating system.
     */
    public static final AttributeKey<String> HOST_ID = stringKey("host.id");

    /** VM image ID or host OS image ID. For Cloud, this value is from the provider. */
    public static final AttributeKey<String> HOST_IMAGE_ID = stringKey("host.image.id");

    /** Name of the VM image or OS install the host was instantiated from. */
    public static final AttributeKey<String> HOST_IMAGE_NAME = stringKey("host.image.name");

    /**
     * The version string of the VM image or host OS as defined in <a
     * href="README.md#version-attributes">Version Attributes</a>.
     */
    public static final AttributeKey<String> HOST_IMAGE_VERSION = stringKey("host.image.version");

    /**
     * Available IP addresses of the host, excluding loopback interfaces.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>IPv4 Addresses MUST be specified in dotted-quad notation. IPv6 addresses MUST be
     *       specified in the <a href="https://www.rfc-editor.org/rfc/rfc5952.html">RFC 5952</a>
     *       format.
     * </ul>
     */
    public static final AttributeKey<List<String>> HOST_IP = stringArrayKey("host.ip");

    /**
     * Name of the host. On Unix systems, it may contain what the hostname command returns, or the
     * fully qualified hostname, or another name specified by the user.
     */
    public static final AttributeKey<String> HOST_NAME = stringKey("host.name");

    /** Type of host. For Cloud, this must be the machine type. */
    public static final AttributeKey<String> HOST_TYPE = stringKey("host.type");

    /** The amount of level 2 memory cache available to the processor (in Bytes). */
    public static final AttributeKey<Long> HOST_CPU_CACHE_L2_SIZE = longKey("host.cpu.cache.l2.size");

    /** Numeric value specifying the family or generation of the CPU. */
    public static final AttributeKey<Long> HOST_CPU_FAMILY = longKey("host.cpu.family");

    /**
     * Model identifier. It provides more granular information about the CPU, distinguishing it from
     * other CPUs within the same family.
     */
    public static final AttributeKey<Long> HOST_CPU_MODEL_ID = longKey("host.cpu.model.id");

    /** Model designation of the processor. */
    public static final AttributeKey<String> HOST_CPU_MODEL_NAME = stringKey("host.cpu.model.name");

    /** Stepping or core revisions. */
    public static final AttributeKey<Long> HOST_CPU_STEPPING = longKey("host.cpu.stepping");

    /**
     * Processor manufacturer identifier. A maximum 12-character string.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li><a href="https://wiki.osdev.org/CPUID">CPUID</a> command returns the vendor ID string in
     *       EBX, EDX and ECX registers. Writing these to memory in this order results in a
     *       12-character string.
     * </ul>
     */
    public static final AttributeKey<String> HOST_CPU_VENDOR_ID = stringKey("host.cpu.vendor.id");

    /** The name of the cluster. */
    public static final AttributeKey<String> K8S_CLUSTER_NAME = stringKey("k8s.cluster.name");

    /**
     * A pseudo-ID for the cluster, set to the UID of the {@code kube-system} namespace.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>K8s does not have support for obtaining a cluster ID. If this is ever added, we will
     *       recommend collecting the {@code k8s.cluster.uid} through the official APIs. In the
     *       meantime, we are able to use the {@code uid} of the {@code kube-system} namespace as a
     *       proxy for cluster ID. Read on for the rationale.
     *   <li>Every object created in a K8s cluster is assigned a distinct UID. The {@code kube-system}
     *       namespace is used by Kubernetes itself and will exist for the lifetime of the cluster.
     *       Using the {@code uid} of the {@code kube-system} namespace is a reasonable proxy for the
     *       K8s ClusterID as it will only change if the cluster is rebuilt. Furthermore, Kubernetes
     *       UIDs are UUIDs as standardized by <a
     *       href="https://www.itu.int/ITU-T/studygroups/com17/oid.html">ISO/IEC 9834-8 and ITU-T
     *       X.667</a>. Which states:
     *   <li>If generated according to one of the mechanisms defined in Rec. ITU-T X.667 | ISO/IEC
     *       9834-8, a UUID is either guaranteed to be different from all other UUIDs generated before
     *       3603 A.D., or is extremely likely to be different (depending on the mechanism chosen).
     *   <li>Therefore, UIDs between clusters should be extremely unlikely to conflict.
     * </ul>
     */
    public static final AttributeKey<String> K8S_CLUSTER_UID = stringKey("k8s.cluster.uid");

    /** The name of the Node. */
    public static final AttributeKey<String> K8S_NODE_NAME = stringKey("k8s.node.name");

    /** The UID of the Node. */
    public static final AttributeKey<String> K8S_NODE_UID = stringKey("k8s.node.uid");

    /** The name of the namespace that the pod is running in. */
    public static final AttributeKey<String> K8S_NAMESPACE_NAME = stringKey("k8s.namespace.name");

    /** The name of the Pod. */
    public static final AttributeKey<String> K8S_POD_NAME = stringKey("k8s.pod.name");

    /** The UID of the Pod. */
    public static final AttributeKey<String> K8S_POD_UID = stringKey("k8s.pod.uid");

    /**
     * The name of the Container from Pod specification, must be unique within a Pod. Container
     * runtime usually uses different globally unique name ({@code container.name}).
     */
    public static final AttributeKey<String> K8S_CONTAINER_NAME = stringKey("k8s.container.name");

    /**
     * Number of times the container was restarted. This attribute can be used to identify a
     * particular container (running or stopped) within a container spec.
     */
    public static final AttributeKey<Long> K8S_CONTAINER_RESTART_COUNT =
        longKey("k8s.container.restart_count");

    /** The name of the ReplicaSet. */
    public static final AttributeKey<String> K8S_REPLICASET_NAME = stringKey("k8s.replicaset.name");

    /** The UID of the ReplicaSet. */
    public static final AttributeKey<String> K8S_REPLICASET_UID = stringKey("k8s.replicaset.uid");

    /** The name of the Deployment. */
    public static final AttributeKey<String> K8S_DEPLOYMENT_NAME = stringKey("k8s.deployment.name");

    /** The UID of the Deployment. */
    public static final AttributeKey<String> K8S_DEPLOYMENT_UID = stringKey("k8s.deployment.uid");

    /** The name of the StatefulSet. */
    public static final AttributeKey<String> K8S_STATEFULSET_NAME = stringKey("k8s.statefulset.name");

    /** The UID of the StatefulSet. */
    public static final AttributeKey<String> K8S_STATEFULSET_UID = stringKey("k8s.statefulset.uid");

    /** The name of the DaemonSet. */
    public static final AttributeKey<String> K8S_DAEMONSET_NAME = stringKey("k8s.daemonset.name");

    /** The UID of the DaemonSet. */
    public static final AttributeKey<String> K8S_DAEMONSET_UID = stringKey("k8s.daemonset.uid");

    /** The name of the Job. */
    public static final AttributeKey<String> K8S_JOB_NAME = stringKey("k8s.job.name");

    /** The UID of the Job. */
    public static final AttributeKey<String> K8S_JOB_UID = stringKey("k8s.job.uid");

    /** The name of the CronJob. */
    public static final AttributeKey<String> K8S_CRONJOB_NAME = stringKey("k8s.cronjob.name");

    /** The UID of the CronJob. */
    public static final AttributeKey<String> K8S_CRONJOB_UID = stringKey("k8s.cronjob.uid");

    /**
     * The digest of the OCI image manifest. For container images specifically is the digest by which
     * the container image is known.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Follows <a href="https://github.com/opencontainers/image-spec/blob/main/manifest.md">OCI
     *       Image Manifest Specification</a>, and specifically the <a
     *       href="https://github.com/opencontainers/image-spec/blob/main/descriptor.md#digests">Digest
     *       property</a>. An example can be found in <a
     *       href="https://docs.docker.com/registry/spec/manifest-v2-2/#example-image-manifest">Example
     *       Image Manifest</a>.
     * </ul>
     */
    public static final AttributeKey<String> OCI_MANIFEST_DIGEST = stringKey("oci.manifest.digest");

    /** Unique identifier for a particular build or compilation of the operating system. */
    public static final AttributeKey<String> OS_BUILD_ID = stringKey("os.build_id");

    /**
     * Human readable (not intended to be parsed) OS version information, like e.g. reported by {@code
     * ver} or {@code lsb_release -a} commands.
     */
    public static final AttributeKey<String> OS_DESCRIPTION = stringKey("os.description");

    /** Human readable operating system name. */
    public static final AttributeKey<String> OS_NAME = stringKey("os.name");

    /** The operating system type. */
    public static final AttributeKey<String> OS_TYPE = stringKey("os.type");

    /**
     * The version string of the operating system as defined in <a
     * href="/docs/resource/README.md#version-attributes">Version Attributes</a>.
     */
    public static final AttributeKey<String> OS_VERSION = stringKey("os.version");

    /**
     * The command used to launch the process (i.e. the command name). On Linux based systems, can be
     * set to the zeroth string in {@code proc/[pid]/cmdline}. On Windows, can be set to the first
     * parameter extracted from {@code GetCommandLineW}.
     */
    public static final AttributeKey<String> PROCESS_COMMAND = stringKey("process.command");

    /**
     * All the command arguments (including the command/executable itself) as received by the process.
     * On Linux-based systems (and some other Unixoid systems supporting procfs), can be set according
     * to the list of null-delimited strings extracted from {@code proc/[pid]/cmdline}. For libc-based
     * executables, this would be the full argv vector passed to {@code main}.
     */
    public static final AttributeKey<List<String>> PROCESS_COMMAND_ARGS =
        stringArrayKey("process.command_args");

    /**
     * The full command used to launch the process as a single string representing the full command.
     * On Windows, can be set to the result of {@code GetCommandLineW}. Do not set this if you have to
     * assemble it just for monitoring; use {@code process.command_args} instead.
     */
    public static final AttributeKey<String> PROCESS_COMMAND_LINE = stringKey("process.command_line");

    /**
     * The name of the process executable. On Linux based systems, can be set to the {@code Name} in
     * {@code proc/[pid]/status}. On Windows, can be set to the base name of {@code
     * GetProcessImageFileNameW}.
     */
    public static final AttributeKey<String> PROCESS_EXECUTABLE_NAME =
        stringKey("process.executable.name");

    /**
     * The full path to the process executable. On Linux based systems, can be set to the target of
     * {@code proc/[pid]/exe}. On Windows, can be set to the result of {@code
     * GetProcessImageFileNameW}.
     */
    public static final AttributeKey<String> PROCESS_EXECUTABLE_PATH =
        stringKey("process.executable.path");

    /** The username of the user that owns the process. */
    public static final AttributeKey<String> PROCESS_OWNER = stringKey("process.owner");

    /** Parent Process identifier (PID). */
    public static final AttributeKey<Long> PROCESS_PARENT_PID = longKey("process.parent_pid");

    /** Process identifier (PID). */
    public static final AttributeKey<Long> PROCESS_PID = longKey("process.pid");

    /**
     * An additional description about the runtime of the process, for example a specific vendor
     * customization of the runtime environment.
     */
    public static final AttributeKey<String> PROCESS_RUNTIME_DESCRIPTION =
        stringKey("process.runtime.description");

    /**
     * The name of the runtime of this process. For compiled native binaries, this SHOULD be the name
     * of the compiler.
     */
    public static final AttributeKey<String> PROCESS_RUNTIME_NAME = stringKey("process.runtime.name");

    /**
     * The version of the runtime of this process, as returned by the runtime without modification.
     */
    public static final AttributeKey<String> PROCESS_RUNTIME_VERSION =
        stringKey("process.runtime.version");

    /**
     * Logical name of the service.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>MUST be the same for all instances of horizontally scaled services. If the value was not
     *       specified, SDKs MUST fallback to {@code unknown_service:} concatenated with <a
     *       href="process.md#process">{@code process.executable.name}</a>, e.g. {@code
     *       unknown_service:bash}. If {@code process.executable.name} is not available, the value
     *       MUST be set to {@code unknown_service}.
     * </ul>
     */
    public static final AttributeKey<String> SERVICE_NAME = stringKey("service.name");

    /**
     * The version string of the service API or implementation. The format is not defined by these
     * conventions.
     */
    public static final AttributeKey<String> SERVICE_VERSION = stringKey("service.version");

    /**
     * The string ID of the service instance.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>MUST be unique for each instance of the same {@code service.namespace,service.name} pair
     *       (in other words {@code service.namespace,service.name,service.instance.id} triplet MUST
     *       be globally unique). The ID helps to distinguish instances of the same service that exist
     *       at the same time (e.g. instances of a horizontally scaled service). It is preferable for
     *       the ID to be persistent and stay the same for the lifetime of the service instance,
     *       however it is acceptable that the ID is ephemeral and changes during important lifetime
     *       events for the service (e.g. service restarts). If the service has no inherent unique ID
     *       that can be used as the value of this attribute it is recommended to generate a random
     *       Version 1 or Version 4 RFC 4122 UUID (services aiming for reproducible UUIDs may also use
     *       Version 5, see RFC 4122 for more recommendations).
     * </ul>
     */
    public static final AttributeKey<String> SERVICE_INSTANCE_ID = stringKey("service.instance.id");

    /**
     * A namespace for {@code service.name}.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>A string value having a meaning that helps to distinguish a group of services, for
     *       example the team name that owns a group of services. {@code service.name} is expected to
     *       be unique within the same namespace. If {@code service.namespace} is not specified in the
     *       Resource then {@code service.name} is expected to be unique for all services that have no
     *       explicit namespace defined (so the empty/unspecified namespace is simply one more valid
     *       namespace). Zero-length namespace string is assumed equal to unspecified namespace.
     * </ul>
     */
    public static final AttributeKey<String> SERVICE_NAMESPACE = stringKey("service.namespace");

    /** The language of the telemetry SDK. */
    public static final AttributeKey<String> TELEMETRY_SDK_LANGUAGE =
        stringKey("telemetry.sdk.language");

    /**
     * The name of the telemetry SDK as defined above.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The OpenTelemetry SDK MUST set the {@code telemetry.sdk.name} attribute to {@code
     *       opentelemetry}. If another SDK, like a fork or a vendor-provided implementation, is used,
     *       this SDK MUST set the {@code telemetry.sdk.name} attribute to the fully-qualified class
     *       or module name of this SDK's main entry point or another suitable identifier depending on
     *       the language. The identifier {@code opentelemetry} is reserved and MUST NOT be used in
     *       this case. All custom identifiers SHOULD be stable across different versions of an
     *       implementation.
     * </ul>
     */
    public static final AttributeKey<String> TELEMETRY_SDK_NAME = stringKey("telemetry.sdk.name");

    /** The version string of the telemetry SDK. */
    public static final AttributeKey<String> TELEMETRY_SDK_VERSION =
        stringKey("telemetry.sdk.version");

    /**
     * The name of the auto instrumentation agent or distribution, if used.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>Official auto instrumentation agents and distributions SHOULD set the {@code
     *       telemetry.distro.name} attribute to a string starting with {@code opentelemetry-}, e.g.
     *       {@code opentelemetry-java-instrumentation}.
     * </ul>
     */
    public static final AttributeKey<String> TELEMETRY_DISTRO_NAME =
        stringKey("telemetry.distro.name");

    /** The version string of the auto instrumentation agent or distribution, if used. */
    public static final AttributeKey<String> TELEMETRY_DISTRO_VERSION =
        stringKey("telemetry.distro.version");

    /** Additional description of the web engine (e.g. detailed version and edition information). */
    public static final AttributeKey<String> WEBENGINE_DESCRIPTION =
        stringKey("webengine.description");

    /** The name of the web engine. */
    public static final AttributeKey<String> WEBENGINE_NAME = stringKey("webengine.name");

    /** The version of the web engine. */
    public static final AttributeKey<String> WEBENGINE_VERSION = stringKey("webengine.version");

    /** The name of the instrumentation scope - ({@code InstrumentationScope.Name} in OTLP). */
    public static final AttributeKey<String> OTEL_SCOPE_NAME = stringKey("otel.scope.name");

    /** The version of the instrumentation scope - ({@code InstrumentationScope.Version} in OTLP). */
    public static final AttributeKey<String> OTEL_SCOPE_VERSION = stringKey("otel.scope.version");

    /**
     * Deprecated, use the {@code otel.scope.name} attribute.
     *
     * @deprecated Deprecated, use the `otel.scope.name` attribute.
     */
    @Deprecated
    public static final AttributeKey<String> OTEL_LIBRARY_NAME = stringKey("otel.library.name");

    /**
     * Deprecated, use the {@code otel.scope.version} attribute.
     *
     * @deprecated Deprecated, use the `otel.scope.version` attribute.
     */
    @Deprecated
    public static final AttributeKey<String> OTEL_LIBRARY_VERSION = stringKey("otel.library.version");

    // Enum definitions
    public static final class CloudPlatformValues {
        /** Alibaba Cloud Elastic Compute Service. */
        public static final String ALIBABA_CLOUD_ECS = "alibaba_cloud_ecs";

        /** Alibaba Cloud Function Compute. */
        public static final String ALIBABA_CLOUD_FC = "alibaba_cloud_fc";

        /** Red Hat OpenShift on Alibaba Cloud. */
        public static final String ALIBABA_CLOUD_OPENSHIFT = "alibaba_cloud_openshift";

        /** AWS Elastic Compute Cloud. */
        public static final String AWS_EC2 = "aws_ec2";

        /** AWS Elastic Container Service. */
        public static final String AWS_ECS = "aws_ecs";

        /** AWS Elastic Kubernetes Service. */
        public static final String AWS_EKS = "aws_eks";

        /** AWS Lambda. */
        public static final String AWS_LAMBDA = "aws_lambda";

        /** AWS Elastic Beanstalk. */
        public static final String AWS_ELASTIC_BEANSTALK = "aws_elastic_beanstalk";

        /** AWS App Runner. */
        public static final String AWS_APP_RUNNER = "aws_app_runner";

        /** Red Hat OpenShift on AWS (ROSA). */
        public static final String AWS_OPENSHIFT = "aws_openshift";

        /** Azure Virtual Machines. */
        public static final String AZURE_VM = "azure_vm";

        /** Azure Container Instances. */
        public static final String AZURE_CONTAINER_INSTANCES = "azure_container_instances";

        /** Azure Kubernetes Service. */
        public static final String AZURE_AKS = "azure_aks";

        /** Azure Functions. */
        public static final String AZURE_FUNCTIONS = "azure_functions";

        /** Azure App Service. */
        public static final String AZURE_APP_SERVICE = "azure_app_service";

        /** Azure Red Hat OpenShift. */
        public static final String AZURE_OPENSHIFT = "azure_openshift";

        /** Google Bare Metal Solution (BMS). */
        public static final String GCP_BARE_METAL_SOLUTION = "gcp_bare_metal_solution";

        /** Google Cloud Compute Engine (GCE). */
        public static final String GCP_COMPUTE_ENGINE = "gcp_compute_engine";

        /** Google Cloud Run. */
        public static final String GCP_CLOUD_RUN = "gcp_cloud_run";

        /** Google Cloud Kubernetes Engine (GKE). */
        public static final String GCP_KUBERNETES_ENGINE = "gcp_kubernetes_engine";

        /** Google Cloud Functions (GCF). */
        public static final String GCP_CLOUD_FUNCTIONS = "gcp_cloud_functions";

        /** Google Cloud App Engine (GAE). */
        public static final String GCP_APP_ENGINE = "gcp_app_engine";

        /** Red Hat OpenShift on Google Cloud. */
        public static final String GCP_OPENSHIFT = "gcp_openshift";

        /** Red Hat OpenShift on IBM Cloud. */
        public static final String IBM_CLOUD_OPENSHIFT = "ibm_cloud_openshift";

        /** Tencent Cloud Cloud Virtual Machine (CVM). */
        public static final String TENCENT_CLOUD_CVM = "tencent_cloud_cvm";

        /** Tencent Cloud Elastic Kubernetes Service (EKS). */
        public static final String TENCENT_CLOUD_EKS = "tencent_cloud_eks";

        /** Tencent Cloud Serverless Cloud Function (SCF). */
        public static final String TENCENT_CLOUD_SCF = "tencent_cloud_scf";

        private CloudPlatformValues() {}
    }

    public static final class CloudProviderValues {
        /** Alibaba Cloud. */
        public static final String ALIBABA_CLOUD = "alibaba_cloud";

        /** Amazon Web Services. */
        public static final String AWS = "aws";

        /** Microsoft Azure. */
        public static final String AZURE = "azure";

        /** Google Cloud Platform. */
        public static final String GCP = "gcp";

        /** Heroku Platform as a Service. */
        public static final String HEROKU = "heroku";

        /** IBM Cloud. */
        public static final String IBM_CLOUD = "ibm_cloud";

        /** Tencent Cloud. */
        public static final String TENCENT_CLOUD = "tencent_cloud";

        private CloudProviderValues() {}
    }

    public static final class AwsEcsLaunchtypeValues {
        /** ec2. */
        public static final String EC2 = "ec2";

        /** fargate. */
        public static final String FARGATE = "fargate";

        private AwsEcsLaunchtypeValues() {}
    }

    public static final class HostArchValues {
        /** AMD64. */
        public static final String AMD64 = "amd64";

        /** ARM32. */
        public static final String ARM32 = "arm32";

        /** ARM64. */
        public static final String ARM64 = "arm64";

        /** Itanium. */
        public static final String IA64 = "ia64";

        /** 32-bit PowerPC. */
        public static final String PPC32 = "ppc32";

        /** 64-bit PowerPC. */
        public static final String PPC64 = "ppc64";

        /** IBM z/Architecture. */
        public static final String S390X = "s390x";

        /** 32-bit x86. */
        public static final String X86 = "x86";

        private HostArchValues() {}
    }

    public static final class OsTypeValues {
        /** Microsoft Windows. */
        public static final String WINDOWS = "windows";

        /** Linux. */
        public static final String LINUX = "linux";

        /** Apple Darwin. */
        public static final String DARWIN = "darwin";

        /** FreeBSD. */
        public static final String FREEBSD = "freebsd";

        /** NetBSD. */
        public static final String NETBSD = "netbsd";

        /** OpenBSD. */
        public static final String OPENBSD = "openbsd";

        /** DragonFly BSD. */
        public static final String DRAGONFLYBSD = "dragonflybsd";

        /** HP-UX (Hewlett Packard Unix). */
        public static final String HPUX = "hpux";

        /** AIX (Advanced Interactive eXecutive). */
        public static final String AIX = "aix";

        /** SunOS, Oracle Solaris. */
        public static final String SOLARIS = "solaris";

        /** IBM z/OS. */
        public static final String Z_OS = "z_os";

        private OsTypeValues() {}
    }

    public static final class TelemetrySdkLanguageValues {
        /** cpp. */
        public static final String CPP = "cpp";

        /** dotnet. */
        public static final String DOTNET = "dotnet";

        /** erlang. */
        public static final String ERLANG = "erlang";

        /** go. */
        public static final String GO = "go";

        /** java. */
        public static final String JAVA = "java";

        /** nodejs. */
        public static final String NODEJS = "nodejs";

        /** php. */
        public static final String PHP = "php";

        /** python. */
        public static final String PYTHON = "python";

        /** ruby. */
        public static final String RUBY = "ruby";

        /** rust. */
        public static final String RUST = "rust";

        /** swift. */
        public static final String SWIFT = "swift";

        /** webjs. */
        public static final String WEBJS = "webjs";

        private TelemetrySdkLanguageValues() {}
    }

    /**
     * Red Hat OpenShift on Google Cloud.
     *
     * @deprecated This item has been removed as of 1.18.0 of the semantic conventions. Use {@link
     *     ResourceAttributes#GCP_OPENSHIFT} instead.
     */
    @Deprecated public static final String GCP_OPENSHIFT = "gcp_openshift";

    /**
     * Full user-agent string provided by the browser
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>The user-agent value SHOULD be provided only from browsers that do not have a mechanism
     *       to retrieve brands and platform individually from the User-Agent Client Hints API. To
     *       retrieve the value, the legacy {@code navigator.userAgent} API can be used.
     * </ul>
     *
     * @deprecated This item has been renamed in 1.19.0 version of the semantic conventions. Use
     *     {@link SemanticAttributes#USER_AGENT_ORIGINAL} instead.
     */
    @Deprecated
    public static final AttributeKey<String> BROWSER_USER_AGENT = stringKey("browser.user_agent");

    /**
     * The unique ID of the single function that this runtime instance executes.
     *
     * <p>Notes:
     *
     * <ul>
     *   <li>On some cloud providers, it may not be possible to determine the full ID at startup, so
     *       consider setting {@code faas.id} as a span attribute instead.
     *   <li>The exact value to use for {@code faas.id} depends on the cloud provider:
     *   <li><strong>AWS Lambda:</strong> The function <a
     *       href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">ARN</a>.
     *       Take care not to use the &quot;invoked ARN&quot; directly but replace any <a
     *       href="https://docs.aws.amazon.com/lambda/latest/dg/configuration-aliases.html">alias
     *       suffix</a> with the resolved function version, as the same runtime instance may be
     *       invokable with multiple different aliases.
     *   <li><strong>GCP:</strong> The <a
     *       href="https://cloud.google.com/iam/docs/full-resource-names">URI of the resource</a>
     *   <li><strong>Azure:</strong> The <a
     *       href="https://docs.microsoft.com/en-us/rest/api/resources/resources/get-by-id">Fully
     *       Qualified Resource ID</a> of the invoked function, <em>not</em> the function app, having
     *       the form {@code
     *       /subscriptions/<SUBSCIPTION_GUID>/resourceGroups/<RG>/providers/Microsoft.Web/sites/<FUNCAPP>/functions/<FUNC>}.
     *       This means that a span attribute MUST be used, as an Azure function app can host multiple
     *       functions that would usually share a TracerProvider.
     * </ul>
     *
     * @deprecated This item has been removed in 1.19.0 version of the semantic conventions. Use
     *     {@link ResourceAttributes#CLOUD_RESOURCE_ID} instead.
     */
    @Deprecated public static final AttributeKey<String> FAAS_ID = stringKey("faas.id");

    /**
     * The version string of the auto instrumentation agent, if used.
     *
     * @deprecated This item has been renamed in 1.22.0 of the semantic conventions. Use {@link
     *     ResourceAttributes#TELEMETRY_DISTRO_VERSION} instead.
     */
    @Deprecated
    public static final AttributeKey<String> TELEMETRY_AUTO_VERSION =
        stringKey("telemetry.auto.version");

    /**
     * Container image tag.
     *
     * @deprecated This item has been renamed in 1.22.0 of the semantic conventions. Use {@link
     *     ResourceAttributes#CONTAINER_IMAGE_TAGS} instead.
     */
    @Deprecated
    public static final AttributeKey<String> CONTAINER_IMAGE_TAG = stringKey("container.image.tag");

    private ResourceAttributes() {}
}
