/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.cdi.se;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.CdiCamelExtension;
import org.apache.camel.cdi.CdiPropertiesComponent;
import org.apache.camel.cdi.Uri;
import org.apache.camel.cdi.se.bean.CustomPropertiesCamelContext;
import org.apache.camel.cdi.se.bean.PropertyEndpointRoute;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.properties.PropertiesComponent;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

@RunWith(Arquillian.class)
public class PropertyEndpointTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class)
            // Camel CDI
            .addPackage(CdiCamelExtension.class.getPackage())
            //Custom Camel context
            .addClass(CustomPropertiesCamelContext.class)
            // Test class
            .addClass(PropertyEndpointRoute.class)
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Produces
    @ApplicationScoped
    @Named("properties")
    private static PropertiesComponent configuration() {
        Properties configuration = new Properties();
        configuration.put("from", "inbound");
        configuration.put("to", "mock:outbound");
        return new CdiPropertiesComponent(configuration);
    }

    @Inject
    @Uri("direct:{{from}}")
    private ProducerTemplate inbound;

    @Inject
    @Uri("mock:outbound")
    private MockEndpoint outbound;

    @Test
    @InSequence(1)
    public void startCamelContext(CamelContext context) throws Exception {
        context.start();
    }

    @Test
    @InSequence(2)
    public void sendMessageToInbound() throws InterruptedException {
        outbound.expectedMessageCount(1);
        outbound.expectedBodiesReceived("test");
        outbound.expectedHeaderReceived("header", "message from file");
        
        inbound.sendBody("test");

        assertIsSatisfied(2L, TimeUnit.SECONDS, outbound);
    }

    @Test
    @InSequence(3)
    public void stopCamelContext(CamelContext context) throws Exception {
        context.stop();
    }
}
