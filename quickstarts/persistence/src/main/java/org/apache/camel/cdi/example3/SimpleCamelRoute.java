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
package org.apache.camel.cdi.example3;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.cdi.Uri;
import org.apache.camel.cdi.example3.db.DataSourceDefinition;
import org.h2.jdbcx.JdbcDataSource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

@ContextName("simple")
public class SimpleCamelRoute extends RouteBuilder {

    @EndpointInject(uri="timer://start", context = "simple")
    private Endpoint timerEP;

    @Inject @Named("myDataSource") @DataSourceDefinition(className = "org.h2.jdbcx.JdbcDataSource",
            url = "jdbc:h2:/test",
            user = "sa",
            password = "")
    private DataSource myDataSource;

    @Inject
    @Uri("jdbc:myDataSource")
    @ContextName("simple")
    private Endpoint jdbcEP;

    @Inject
    @Uri("direct:continue")
    @ContextName("simple")
    private Endpoint directEP;

    @Inject
    private HelloBean helloBean;

    @Override
    public void configure() {
        from(timerEP).routeId("timerToDirect")
            .setHeader("context")
              .constant("simple")
            .setBody().constant("Camel CDI Example 3")
              .log("Message received : ${body} for the Context : ${header.context}")
            .setHeader("header.message")
              .simple("properties:header.message")
              .log("Message received : ${body} for the header : ${header.header.message}")
            .to(directEP);

        from(directEP).routeId("directToBean")
            .setBody().constant("CDI")
            .bean(helloBean, "sayHello")
            .log(">> Response : ${body}")
            .setBody().constant("select * from customer")
/*                .to("jdbc:myDataSource");*/
               .to(jdbcEP);
    }
}
