// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package example.springdata.gremlin.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.tinkerpop.gremlin.driver.ser.Serializers;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("gremlin")
public class GremlinProperties {
    private String endpoint;

    private int port;

    private String username;

    private String password;

    private boolean sslEnabled;

    private boolean telemetryAllowed = true;

    private String serializer = Serializers.GRAPHSON.toString();

    private int maxContentLength;
}
