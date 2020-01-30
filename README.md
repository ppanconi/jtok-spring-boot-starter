# spring-jtok

![JToK](jtok.png?raw=true "JToK")

Spring-jtok is Java Spring library to implement Event Driven Microservices. 

It propagates Spring Domain Events to remote services using Apache Kafka. 
Spring JPA Domain Entities can emit events using Spring Application Events 
support and @DomainEvents and @AfterDomainEventPublication annotations 
(@see also org.springframework.data.domain.AbstractAggregateRoot). 
JToK public these events to configured Apache Kafka topics.

## Getting Started
Spring-jtok can be used in spring boot jpa application.

Use spring-boot cli to create a new project:

`spring init --build=gradle --dependencies=data-jpa,webflux,postgresql,lombok,data-rest --groupId=it.plansoft depot` 

Add spring-jtok dependency to the new created project using jitpack virtual repository :

```
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
```

and add JToK jitpack dependency:

```
implementation 'com.github.ppanconi:spring-jtok:master-SNAPSHOT'
```

In main configuration enable JToK domain event publisher or/and
JTok external domain event subscriber:

```java
@SpringBootApplication
@EnableExternalDomainEventSubscriber
@EnableDomainEventPublisher
@EnableJpaRepositories
@EntityScan({"it.plansoft.depot"})
public class DepotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DepotApplication.class, args);
	}

}

``` 

You have also do add EnableJpaRepositories and EntityScan("\<\<application package\>\>"") annotations.



