/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.camel.cdi;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.impl.DefaultCamelContextNameStrategy;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import org.apache.camel.core.osgi.OsgiCamelContextHelper;
import org.apache.camel.core.osgi.OsgiComponentResolver;
import org.apache.camel.core.osgi.OsgiDataFormatResolver;
import org.apache.camel.core.osgi.OsgiFactoryFinderResolver;
import org.apache.camel.core.osgi.OsgiTypeConverter;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.FactoryFinder;
import org.apache.camel.spi.Injector;
import org.apache.camel.spi.Registry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

@Vetoed
public class CdiCamelContext extends DefaultCamelContext {

    @Inject
    private BeanManager manager;

    @Inject
    private CdiCamelExtension extension;
    
    @PostConstruct
    void postConstruct() {
        AnnotatedType<?> type = manager.createAnnotatedType(getClass());
        ContextName name = type.getAnnotation(ContextName.class);

        // Do not override the name if it's been already set (in the bean constructor for example)
        if (getNameStrategy() instanceof DefaultCamelContextNameStrategy) {
            setName(name != null ? name.value() : "camel-cdi");
        }

        Registry camelRegistry = null;
        Injector injector = new CdiCamelInjector(getInjector(), manager);

        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        if (bundle == null) {
            camelRegistry = new CdiCamelRegistry(manager);
        } else {
            BundleContext bundleContext = bundle.getBundleContext();
            if (bundleContext != null) {
                camelRegistry = OsgiCamelContextHelper.wrapRegistry(this, new CdiCamelRegistry(manager), bundleContext);
                setComponentResolver(new OsgiComponentResolver(bundleContext));
                FactoryFinder finder = new OsgiFactoryFinderResolver(bundleContext).resolveDefaultFactoryFinder(getClassResolver());
                setTypeConverter(new OsgiTypeConverter(bundleContext, injector, finder));
                setDataFormatResolver(new OsgiDataFormatResolver(bundleContext));
            }
        }

        // Add bean registry and Camel injector
        setRegistry(camelRegistry);
        setInjector(injector);

        // Add event notifier if at least one observer is present
        if (extension == null || extension.getContextInfo(name) == null) {
            Logger.getLogger(CdiCamelContext.class.getName()).log(Level.WARNING, "extension or extension.getContextInfo(name) is null, Assuming EventNotifierSupport");
            getManagementStrategy().addEventNotifier(new CdiEventNotifier(manager, name));
        } else {
            if (extension.getContextInfo(name).contains(ContextInfo.EventNotifierSupport)) {
                getManagementStrategy().addEventNotifier(new CdiEventNotifier(manager, name));
            }
        }
    }
}
