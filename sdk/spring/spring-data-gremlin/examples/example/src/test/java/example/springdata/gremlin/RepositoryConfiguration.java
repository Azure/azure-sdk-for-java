/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.springdata.gremlin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.microsoft.spring.data.gremlin.common.GremlinConfig;
import com.microsoft.spring.data.gremlin.config.AbstractGremlinConfiguration;
import com.microsoft.spring.data.gremlin.repository.config.EnableGremlinRepositories;

import example.springdata.gremlin.config.GremlinProperties;


@Configuration
@EnableGremlinRepositories(basePackages = "example.springdata.gremlin.repository")
@EnableConfigurationProperties(GremlinProperties.class)
@PropertySource("classpath:application.yml")
public class RepositoryConfiguration extends AbstractGremlinConfiguration {

    @Autowired
    private GremlinProperties gremlinProps;

    @Override
    public GremlinConfig getGremlinConfig() {
    	return new GremlinConfig(
    	        gremlinProps.getEndpoint(),
                gremlinProps.getPort(),
                gremlinProps.getUsername(),
    			gremlinProps.getPassword(),
                gremlinProps.isSslEnabled(),
                gremlinProps.isTelemetryAllowed(),
                gremlinProps.getSerializer(),
                gremlinProps.getMaxContentLength()
        );
    }
}
