# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

$scriptUnderTest = Join-Path $PSScriptRoot 'Assert-ServiceRegressionResults.ps1'

function Assert-GateRejects {
    param([string] $ReportsDirectory, [string] $ConnectionMode = 'Direct')
    $rejected = $false
    try {
        & $scriptUnderTest -ReportsDirectory $ReportsDirectory -ConnectionMode $ConnectionMode
    } catch {
        $rejected = $true
    }
    if (-not $rejected) {
        throw 'The result gate accepted invalid signoff evidence.'
    }
}

Describe 'Service-regression result gate' {
    BeforeEach {
        $reportPath = Join-Path $TestDrive 'TEST-com.azure.cosmos.rx.VectorSearchQueryTest.xml'
        $report = @'
<testsuite>
  <properties>
    <property name="COSMOS.HTTP2_ENABLED" value="false"/>
    <property name="COSMOS.THINCLIENT_ENABLED" value="false"/>
    <property name="COSMOS.THINCLIENT_QUERY_PLAN_ENABLED" value="true"/>
  </properties>
  <testcase classname="com.azure.cosmos.rx.VectorSearchQueryTest" name="testVectorSearchAcrossEmbeddingTypes"/>
</testsuite>
'@
    }

    It 'accepts a passing Direct result' {
        $report | Set-Content $reportPath
        & $scriptUnderTest -ReportsDirectory $TestDrive -ConnectionMode Direct
    }

    It 'accepts a passing Compute result with normalized test names' {
        $report.Replace('HTTP2_ENABLED" value="false"', 'HTTP2_ENABLED" value="true"').
            Replace('name="testVectorSearchAcrossEmbeddingTypes"',
                'name="c.a.c.r.VectorSearchQueryTest.testVectorSearchAcrossEmbeddingTypes"') |
            Set-Content $reportPath
        & $scriptUnderTest -ReportsDirectory $TestDrive -ConnectionMode Compute
    }

    It 'accepts a passing ThinClient result' {
        $report.Replace('HTTP2_ENABLED" value="false"', 'HTTP2_ENABLED" value="true"').
            Replace('THINCLIENT_ENABLED" value="false"', 'THINCLIENT_ENABLED" value="true"') |
            Set-Content $reportPath
        & $scriptUnderTest -ReportsDirectory $TestDrive -ConnectionMode ThinClient
    }

    It 'rejects a missing report' {
        Assert-GateRejects -ReportsDirectory (Join-Path $TestDrive 'missing')
    }

    It 'rejects malformed XML' {
        '<testsuite>' | Set-Content $reportPath
        Assert-GateRejects -ReportsDirectory $TestDrive
    }

    It 'rejects zero executed tests' {
        '<testsuite/>' | Set-Content $reportPath
        Assert-GateRejects -ReportsDirectory $TestDrive
    }

    foreach ($outcome in @('failure', 'error', 'skipped')) {
        It "rejects a $outcome result" {
            $report.Replace('name="testVectorSearchAcrossEmbeddingTypes"/>',
                "name=`"testVectorSearchAcrossEmbeddingTypes`"><$outcome/></testcase>") | Set-Content $reportPath
            Assert-GateRejects -ReportsDirectory $TestDrive
        }
    }

    It 'rejects a passing unrelated test' {
        $report.Replace('testVectorSearchAcrossEmbeddingTypes', 'someOtherTest') | Set-Content $reportPath
        Assert-GateRejects -ReportsDirectory $TestDrive
    }

    It 'rejects the wrong connection mode' {
        $report | Set-Content $reportPath
        Assert-GateRejects -ReportsDirectory $TestDrive -ConnectionMode ThinClient
    }

    It 'rejects a disabled proxy query-plan route' {
        $report.Replace('THINCLIENT_QUERY_PLAN_ENABLED" value="true"',
            'THINCLIENT_QUERY_PLAN_ENABLED" value="false"') | Set-Content $reportPath
        Assert-GateRejects -ReportsDirectory $TestDrive
    }
}
