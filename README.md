# CDI extension for Camel

[![Build Status][Travis badge]][Travis build] [![Coverage Status][Coveralls badge]][Coveralls build] [![Dependency Status][VersionEye badge]][VersionEye build] [![Maven Central][Maven Central badge]][Maven Central build]

[Travis badge]: https://travis-ci.org/astefanutti/camel-cdi.svg?branch=master
[Travis build]: https://travis-ci.org/astefanutti/camel-cdi
[Coveralls badge]: https://coveralls.io/repos/astefanutti/camel-cdi/badge.svg?branch=master
[Coveralls build]: https://coveralls.io/r/astefanutti/camel-cdi?branch=master
[VersionEye badge]: https://www.versioneye.com/user/projects/53fca400e09da310ea0006c4/badge.svg?style=flat
[VersionEye build]: https://www.versioneye.com/user/projects/53fca400e09da310ea0006c4
[Maven Central badge]: http://img.shields.io/maven-central/v/io.astefanutti.camel.cdi/camel-cdi.svg?style=flat
[Maven Central build]: http://repo1.maven.org/maven2/io/astefanutti/camel/cdi/camel-cdi/1.1.0/

[CDI][] portable extension for Apache [Camel][] compliant with [JSR 346: Contexts and Dependency Injection for Java<sup>TM</sup> EE 1.2][JSR 346 1.2].

[CDI]: http://www.cdi-spec.org/
[Camel]: http://camel.apache.org/
[JSR 299]: https://jcp.org/en/jsr/detail?id=299
[JSR 346]: https://jcp.org/en/jsr/detail?id=346
[JSR 346 1.1]: https://jcp.org/aboutJava/communityprocess/final/jsr346/index.html
[JSR 346 1.2]: https://jcp.org/aboutJava/communityprocess/mrel/jsr346/index.html
[CDI 1.1]: http://docs.jboss.org/cdi/spec/1.1/cdi-spec.html
[CDI 1.2]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html

## About

Since version `2.10` of Camel, the [Camel CDI][] component supports the integration of Camel in CDI enabled environments. However, some experiments and _battlefield_ tests prove it troublesome to use because of the following concerns:
+ It relies on older [CDI 1.0][JSR 299] version of the specification and makes incorrect usages of [container lifecycle events][] w.r.t. [Assignability of type variables, raw and parameterized types]. As a consequence, it does not work properly with newer implementation versions like Weld 2.x and containers like Wildfly 8.x as reported in [CAMEL-7760][] among other issues.
+ It relies on Apache [DeltaSpike][] and its `BeanManagerProvider` class to retrieve the `BeanManager` instance during the CDI container initialisation. That may not be suitable in complex container configurations, for example, in multiple CDI containers per JVM context, as reported in [CAMEL-6338][] and that causes [CAMEL-6095][] and [CAMEL-6937][].
+ It relies on _DeltaSpike_ and its [configuration mechanism][DeltaSpike Configuration Mechanism] to source configuration locations for the [Properties component][]. While this is suitable for most use cases, it relies on the `ServiceLoader` mechanism to support custom [configuration sources][ConfigSource] that may not be suitable in more complex container configurations and relates to [CAMEL-5986].
+ Besides, while _DeltaSpike_ is a valuable addition to the CDI ecosystem, Camel CDI having a direct dependency on it is questionable from a design standpoint as opposed to relying on standard Camel mechanism for producing the Camel Properties component and delegating, as a plugable strategy, the configuration sourcing concern and implementation choice to the application itself or eventually using the [Java EE Configuration JSR] when available.
+ It declares a `CamelContext` CDI bean that's automatically instantiated and started with a `@PostConstruct` lifecycle callback called before the CDI container is completely initialized. That prevents, among other side effects, proper advising of Camel routes as documented in [Camel AdviceWith][].
+ It uses the `@ContextName` annotation to bind routes to the `CamelContext` instance specified by name as an attempt to provide support for multiple Camel contexts per application. However, that is an incomplete feature from the CDI programming model standpoint as discussed in [CAMEL-5566][] and that causes [CAMEL-5742][].

The objective of this project is to alleviate all these concerns, provide additional features, and have that improved version of the Camel CDI component contributed back into the official codeline. In the meantime, you can get it from Maven Central with the following coordinates:

```xml
<dependency>
    <groupId>io.astefanutti.camel.cdi</groupId>
    <artifactId>camel-cdi</artifactId>
    <version>1.1.0</version>
</dependency>
```

[Camel CDI]: http://camel.apache.org/cdi.html
[DeltaSpike]: https://deltaspike.apache.org/
[DeltaSpike Configuration Mechanism]: https://deltaspike.apache.org/configuration.html
[ConfigSource]: https://deltaspike.apache.org/configuration.html#custom-config-sources
[Camel AdviceWith]: http://camel.apache.org/advicewith.html
[Properties component]: http://camel.apache.org/properties
[CAMEL-5566]: https://issues.apache.org/jira/browse/CAMEL-5566
[CAMEL-5742]: https://issues.apache.org/jira/browse/CAMEL-5742
[CAMEL-5986]: https://issues.apache.org/jira/browse/CAMEL-5986
[CAMEL-6095]: https://issues.apache.org/jira/browse/CAMEL-6095
[CAMEL-6336]: https://issues.apache.org/jira/browse/CAMEL-6336
[CAMEL-6338]: https://issues.apache.org/jira/browse/CAMEL-6338
[CAMEL-6937]: https://issues.apache.org/jira/browse/CAMEL-6937
[CAMEL-7760]: https://issues.apache.org/jira/browse/CAMEL-7760
[container lifecycle events]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#init_events
[Assignability of type variables, raw and parameterized types]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#observers_assignability
[Java EE Configuration JSR]: http://javaeeconfig.blogspot.ch/

## Contribution

### Bug Fixes

This project fixes the following issues currently opened in the Camel CDI backlog:
- [CAMEL-5742]: The `@ContextName` should only refer to a `CamelContext` and not create a new `CamelContext` on the fly
- [CAMEL-5986]: Property placeholders do not work for CDI injection
- [CAMEL-6336]: Camel CDI uses `postConstruct` to inject in CDI beans
- [CAMEL-6338]: Camel CDI shouldn't use DeltaSpike bean manager provider in the `CamelExtension`
- [CAMEL-6937]: `BeanManager` cannot be retrieved when Camel CDI is deployed in Karaf
- [CAMEL-7760]: WELD-001409: ambiguous dependencies for type `CdiCamelContext`

Besides bug fixes, this project completes the following improvements and features:
- [CAMEL-5408]: Extend CDI component with support for events
- [CAMEL-5553]: Support injection of endpoint and `@Produce` `@Consume` annotations

[CAMEL-5408]: https://issues.apache.org/jira/browse/CAMEL-5408
[CAMEL-5553]: https://issues.apache.org/jira/browse/CAMEL-5553

### Supported Containers

This version of Camel CDI is currently successfully tested with the following containers:

| Container        | Version       | Specification          | Arquillian Container Adapter           |
| ---------------- | ------------- | ---------------------- | -------------------------------------- |
| [Weld SE][]      | `2.2.10.SP1`  | [CDI 1.2][JSR 346 1.2] | `arquillian-weld-se-embedded-1.1`      |
| [Weld EE][]      | `2.2.10.SP1`  | [CDI 1.2][JSR 346 1.2] | `arquillian-weld-ee-embedded-1.1`      |
| [OpenWebBeans][] | `1.5.0`       | [CDI 1.2][JSR 346 1.2] | `owb-arquillian-standalone`            |
| [WildFly][]      | `8.2.0.Final` | [Java EE 7][]          | `wildfly-arquillian-container-managed` |

WildFly 8.1 requires to be patched with Weld 2.2+ as documented in [Weld 2.2 on WildFly][].

[Weld SE]: http://weld.cdi-spec.org/
[Weld EE]: http://weld.cdi-spec.org/
[WildFly]: http://www.wildfly.org/
[OpenWebBeans]: http://openwebbeans.apache.org/
[Java EE 7]: https://jcp.org/en/jsr/detail?id=342
[Weld 2.2 on WildFly]: http://weld.cdi-spec.org/news/2014/04/15/weld-220-final/

### New Features

##### CDI Event Camel Endpoint

The CDI event endpoint bridges the [CDI events][] facility with the Camel routes so that CDI events can be seamlessly observed / consumed (respectively produced / fired) from Camel consumers (respectively by Camel producers). This is the implementation fixing [CAMEL-5408][].

The `CdiEventEndpoint<T>` bean can be used to observe / consume CDI events whose _event type_ is `T`, for example:

```java
@Inject
CdiEventEndpoint<String> cdiEventEndpoint;

from(cdiEventEndpoint).log("CDI event received: ${body}");
```

This is equivalent to writing:

```java
@Inject
@Uri("direct:event")
ProducerTemplate producer;

void observeCdiEvents(@Observes String event) {
    producer.sendBody(event);
}

from("direct:event").log("CDI event received: ${body}");
```

Conversely, the `CdiEventEndpoint<T>` bean can be used to produce / fire CDI events whose _event type_ is `T`, for example:

```java
@Inject
CdiEventEndpoint<String> cdiEventEndpoint;

from("direct:event").to(cdiEventEndpoint).log("CDI event sent: ${body}");
```

This is equivalent to writing:

```java
@Inject
Event<String> event;

from("direct:event").process(new Processor() {
    @Override
    public void process(Exchange exchange) {
        event.fire(exchange.getBody(String.class));
    }
}).log("CDI event sent: ${body}");
```

Or using a Java 8 lambda expression:
```java
@Inject
Event<String> event;

from("direct:event")
    .process(exchange -> event.fire(exchange.getIn().getBody(String.class)))
    .log("CDI event sent: ${body}");
```

The type variable `T`, respectively the qualifiers, of a particular `CdiEventEndpoint<T>` injection point are automatically translated into the parameterized _event type_, respectively into the _event qualifiers_, e.g.:

```java
@Inject
@FooQualifier
CdiEventEndpoint<List<String>> cdiEventEndpoint;

from("direct:event").to(cdiEventEndpoint);

void observeCdiEvents(@Observes @FooQualifier List<String> event) {
    logger.info("CDI event: {}", event);
}
```

When multiple Camel contexts exist in the CDI container, the `@ContextName` qualifier can be used to qualify the `CdiEventEndpoint<T>` injection points, e.g.:

```java
@Inject
@ContextName("foo")
CdiEventEndpoint<List<String>> cdiEventEndpoint;
// Only observe / consume events having the @ContextName("foo") qualifier
from(cdiEventEndpoint).log("Camel context 'foo' > CDI event received: ${body}");
// Produce / fire events with the @ContextName("foo") qualifier
from("...").to(cdiEventEndpoint);

void observeCdiEvents(@Observes @ContextName("foo") List<String> event) {
    logger.info("Camel context 'foo' > CDI event: {}", event);
}
```

Note that the CDI event Camel endpoint dynamically adds an [observer method][] for each unique combination of _event type_ and _event qualifiers_ and solely relies on the container typesafe [observer resolution][], which leads to an implementation as efficient as possible.

Besides, as the impedance between the _typesafe_ nature of CDI and the _dynamic_ nature of the [Camel component] model is quite high, it is not possible to create an instance of the CDI event Camel endpoint via [URIs]. Indeed, the URI format for the CDI event component is:

```
cdi-event://PayloadType<T1,...,Tn>[?qualifiers=QualifierType1[,...[,QualifierTypeN]...]]
```

With the authority `PayloadType` (respectively the `QualifierType`) being the URI escaped fully qualified name of the payload (respectively qualifier) raw type followed by the type parameters section delimited by angle brackets for payload parameterized type. Which leads to _unfriendly_ URI, e.g.:

```
cdi-event://org.apache.camel.cdi.se.pojo.EventPayload%3Cjava.lang.Integer%3E?qualifiers=org.apache.camel.cdi.se.qualifier.FooQualifier%2Corg.apache.camel.cdi.se.qualifier.BarQualifier
```

But more fundamentally, that would prevent efficient binding between the endpoint instances and the observer methods as the CDI container doesn't have any ways of discovering the Camel context model during the deployment phase.

[CDI events]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#events
[observer method]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#observer_methods
[observer resolution]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#observer_resolution
[Camel component]: http://camel.apache.org/component.html
[URIs]: http://camel.apache.org/uris.html

##### Camel Events to CDI Events

Camel provides a series of [management events][] that can be subscribed to for listening to Camel context, service, route and exchange events. This version of Camel CDI seamlessly translates these Camel events into CDI events that can be observed using CDI [observer methods][], e.g.:

```java
void onContextStarting(@Observes CamelContextStartingEvent event) {
    // Called before the default Camel context is about to start
}

```

When multiple Camel contexts exist in the CDI container, the `@ContextName` qualifier can be used to refine the observer method resolution to a particular Camel context as specified in [observer resolution][], e.g.:

```java
void onRouteStarted(@Observes @ContextName("first") RouteStartedEvent event) {
    // Called after the route (event.getRoute()) for the
    // Camel context ("first") has started
}

```

Similarly, the `@Default` qualifier can be used to observe Camel events for the _default_ Camel context if multiples contexts exist, e.g.:

```java
void onExchangeCompleted(@Observes @Default ExchangeCompletedEvent event) {
    // Called after the exchange (event.getExchange()) processing has completed
}

```
In that example, if no qualifier is specified, the `@Any` qualifier is implicitly assumed, so that corresponding events for all the Camel contexts deployed get received.

Note that the support for Camel events translation into CDI events is only activated if observer methods listening for Camel events are detected in the deployment, and that per Camel context.

[management events]: http://camel.apache.org/maven/current/camel-core/apidocs/org/apache/camel/management/event/package-summary.html
[observer methods]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#observer_methods

##### Type Converter Beans

CDI beans annotated with the `@Converter` annotation are automatically registered in the corresponding Camel context, e.g.:

```java
@Converter
public class TypeConverter {

    @Converter
    public Output convert(Input input) {
        ...
    }
}
```
Note that CDI injection is supported within the type converters.

### Improvements

##### Multiple Camel Contexts

The official Camel CDI declares the `@ContextName` annotation that can be used to declare multiple `CamelContext` instances. However that annotation is not declared as a [CDI qualifier][] and does not fit nicely in the CDI programming model as discussed in [CAMEL-5566][]. This version of Camel CDI alleviates that concern so that the `@ContextName` annotation can be used as a proper CDI qualifier, e.g.:

```java
@ApplicationScoped
@ContextName("first")
class FirstCamelContext extends CdiCamelContext {

}
```

And then by declaring an [injected field][], e.g.:

```java
@Inject
@ContextName("first")
CamelContext firstCamelContext;
```

[CDI qualifier]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#qualifiers
[injected field]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#injected_fields

##### Configuration Properties

Instead of enforcing a specific configuration solution to setup the Camel [Properties component][], this version of Camel CDI relies on standard Camel and CDI mechanisms so that the configuration sourcing concern is seperated and can be delegated to the application, e.g.:

```java
@Produces
@ApplicationScoped
@Named("properties")
PropertiesComponent propertiesComponent() {
    Properties configuration = new Properties();
    configuration.put("property", "value");
    PropertiesComponent component = CdiPropertiesComponent(configuration);
    component.setLocation("classpath:placeholder.properties");
    return component;
}

```

##### Camel Context Customization

The `CdiCamelContext` class can be extended to declare a custom Camel context bean that uses the `@PostConstruct` and `@PreDestroy` lifecycle callbacks, e.g.:

```java
@ApplicationScoped
class CustomCamelContext extends CdiCamelContext {

    @PostConstruct
    void postConstruct() {
        // Set the Camel context name
        setName("custom");
        // Add properties location
        getComponent("properties", PropertiesComponent.class)
            .setLocation("classpath:placeholder.properties");
        // Bind the Camel context lifecycle to that of the bean
        start();
    }

    @PreDestroy
    void preDestroy() {
        stop();
    }
}
```

##### `@Uri` and `@Mock` Endpoint Qualifiers Unification

As commented by @jstrachan in [CAMEL-5553](https://issues.apache.org/jira/browse/CAMEL-5553?focusedCommentId=13445936&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-13445936), the `@Mock` qualifier has been introduced to work around _ambiguous resolution_ with the producer method for other kinds of endpoints. Yet it is redundant with the semantic provided by the `@Uri` qualifier.

The use of the [`@Typed`][] annotation to restrict the _bean types_ of the `MockEndpoint` bean alleviates that _ambiguous resolution_ so that it is possible to rely solely on the `@Uri` qualifier, e.g.:

```java
@Inject
@Uri("mock:foo")
MockEndpoint mockEndpoint;

@Inject
@Uri("direct:bar")
Endpoint directEndpoint;

@Inject
MockEndpoint outbound; // URI defaults to the member name, i.e. mock:outbound
```

[`@Typed`]: http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#restricting_bean_types

Note that the `@Mock` qualifier remains for backward compatibility reason though it shouldn't be used anymore as it will be removed in the next major version of Camel.

### Existing features

##### Camel Route Builders

```java
@ContextName("first")
class FirstCamelContextRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:inbound")
            .setHeader("context").constant("first")
            .to("mock:outbound");
    }
}
```

##### Camel CDI Beans

Camel CDI declares some producer method beans that can be used to inject Camel objects, e.g.:

```java
@Inject
@Uri("direct:inbound")
ProducerTemplate producerTemplate;

@Inject
@Uri("mock:outbound")
MockEndpoint mockEndpoint;
```

##### Camel Annotations Support

Camel comes with a set of [annotations][Camel annotations] that are supported by Camel CDI for both standard CDI injection and Camel [bean integration][], e.g.:

```java
@PropertyInject("property")
String property;

@Produce(uri = "mock:outbound")
ProducerTemplate producer;

@EndpointInject(uri = "direct:inbound")
Endpoint endpoint;

@EndpointInject(uri = "direct:inbound", context = "...")
Endpoint contextEndpoint;

@BeanInject
MyBean bean;

@Consume(uri = "seda:inbound")
void consume(@Body String body) {
    ...
}
```

[Camel annotations]: http://camel.apache.org/bean-integration.html#BeanIntegration-Annotations
[bean integration]: http://camel.apache.org/bean-integration.html

### Futures Ideas

##### Camel Beans Integration with Multiple Camel Contexts

The [bean integration][] support with multiple Camel contexts could be enhanced so that beans and components can be defined per Camel context.

Each time a bean is looked up by type, a bean of that type annotated with the `@ContextName` qualifier is first looked up. If such bean exists, a contextual reference of that bean is retrieved, else the lookup falls back to a bean with the `@Default` qualifier, e.g.:

```java
@ContextName("first")
class FirstCamelContextRoute extends RouteBuilder {

    @Override
    public void configure() {
        // Lookup CDI bean with qualifier @ContextName("first")
        // Then @Default if any
        from("...").bean(Bean.class);
    }
}
```

For Camel components that are looked up by name, that approach could not be used because bean names declared with the `@Named` qualifier must be unique as documented in [Ambiguous EL names][]. To still support the ability to define Camel beans and components per Camel context, the bean name could be prefixed with the context name for the lookup, e.g.:

```java
@ContextName("first")
class FirstCamelContextRoute extends RouteBuilder {

    @Override
    public void configure() {
        // Lookup CDI bean with qualifier @Named("first:beanName")
        // Then @Named("beanName") if any
        from("...").beanRef("beanName");
    }
}
```

However, namespaces are not supported for EL variables as opposed to EL functions which leads to invalid EL names that cannot be used in EL expressions. One opposite alternative would be to remove that possibility and solely rely on distinct bean names for each Camel context. Though that does not meet the need for internal Camel components such as the [Properties component][] which is looked up by the `properties` name.

[Ambiguous EL names]: [http://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#ambig_names]

## License

Copyright © 2014-2015, Antonin Stefanutti

Published under Apache Software License 2.0, see LICENSE
