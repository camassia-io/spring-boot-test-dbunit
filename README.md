# Spring Boot Test DBUnit

[![Build](https://github.com/camassia-io/spring-boot-test-dbunit/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/camassia-io/spring-boot-test-dbunit/actions/workflows/build.yml)

[![Publish](https://github.com/camassia-io/spring-boot-test-dbunit/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/camassia-io/spring-boot-test-dbunit/actions/workflows/release.yml)

An Open Source Spring Boot DB Unit Integration, based on the popular but no longer maintained [Spring Test Db Unit](https://springtestdbunit.github.io/spring-test-dbunit)

This library aims to provide a highly customisable, easy to use way of testing the Repository/Database layer of your Spring Boot services using real, or in memory databases.

This library is written in Kotlin, but can be easily integrated into a Java project.

## Getting Started

For some demo examples see the spring-boot-test-dbunit-demo project

### Register GH Packages as a Source Repository

#### Gradle

```
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/camassia-io/*")
        // Note specifying Credentials is a short term fix until https://github.community/t/download-from-github-package-registry-without-authentication/14407/93 is fixed by GitHub.
        credentials {
            username = "public"
            password = "\u0067hp_MbWuHdR1tjyN9Zk7v1PMIDh2rEm7tj0aBtZ0"
        }
    }
}
```

#### Maven

```
<repositories>
    <repository>
        <id>GitHubPackages</id>
        <name>GitHubPackages</name>
        <url>https://public:&#103;hp_MbWuHdR1tjyN9Zk7v1PMIDh2rEm7tj0aBtZ0@maven.pkg.github.com/camassia-io/*</url>
    </repository>
</repositories>
```

### Import the Dependency

#### Gradle 

`io.camassia:spring-boot-test-dbunit:{{latest version}}`

#### Maven

```xml
<dependency>
    <groupId>io.camassia</groupId>
    <artifactId>spring-boot-test-dbunit</artifactId>
    <version>{{latest version}}</version>
</dependency>
```

### Create a Spring Boot Test

The following standalone examples use an In Memory H2 Database with Springs JdbcTemplate as the test subject.

#### Using `DatabaseSetup` / `DatabaseTeardown` annotations

These by default will load XML files you specify using the default DataSetLoader.
The file types, & loader can be configured using Beans. See Customization below for more info.

```kotlin
@SpringBootTest(
  classes = [
      SomeTestClass.DemoTestConfiguration::class
  ]
)
@AutoConfigureDbUnit
class SomeTestClass @Autowired constructor(
    private val dbUnit: DatabaseTester
) {
    
    /*
    You would likely replace this with a repository class which you are testing
     */
    @Autowired
    private lateinit var jdbc: JdbcTemplate
    
    @BeforeEach
    fun beforeEach() {
        jdbc.execute("CREATE TABLE demo (id BIGINT NOT NULL, name VARCHAR(50) NOT NULL, CONSTRAINT demo_pk PRIMARY KEY (id))")
    }
    
    @AfterEach
    fun afterEach() {
        jdbc.execute("DROP TABLE demo")
    }

    @Test
    @DatabaseSetup("/Demo.xml")
    fun `repository should query successfully`() {
        val result = repository.selectAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(123)
        assertThat(result[0].name).isEqualTo("Test")
    }
    
    /*
    Configuration Class which sets up the minimum beans required to run this test.
    If using
     */
    @TestConfiguration
    class DemoTestConfiguration {

        /*
        This is the minimum connection config you need to provide to get Db Unit working
         */
        @Bean
        fun connectionSupplier(ds: DataSource) = DataSourceConnectionSupplier(ds)

        /*
        This example uses an in memory H2 Database.
        */
        @Bean
        fun dataSource(): DataSource = DataSourceBuilder
            .create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:dbunit")
            .username("sa")
            .password("sa")
            .build()

        /*
        Used in the Test example to query the DB. 
         */
        @Bean
        fun jdbc(ds: DataSource) = JdbcTemplate(ds)
    }
}
```

###### Useful Examples

See:
- DemoUsingAnnotations
- DemoUsingAnnotationsWithCustomDataSetLoader
- DemoUsingTemplatedAnnotations
- DemoUsingTemplatedAnnotationsAndDefaults

#### Templating

If using Templated files, e.g.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
    <demo id="[ID]" name="[NAME]"/>
</dataset>
```

You can override these templated values using `@TemplatedDatabaseSetup` / `@TemplatedDatabaseTeardown`. 

```kotlin
@TemplatedDatabaseSetup(
    File(
        "/TemplatedDemo.xml", 
        Override("[ID]", "123"), // Overrides value "[ID]" in TemplatedDemo.xml with 123
        Override("[NAME]", "[null]") // Overrides value "[ID]" in TemplatedDemo.xml with null
    )
)
```

Note that due to how DbUnits replacement dataset works, you have to reference overrides by the value, not the column name.
As a result, it's good practice to use brackets, or some other kind of indicator to make it obvious what columns should be overridden, the example above uses square brackets for this purpose.

You can also set up global default overrides, e.g. in case you only one to override 1 or 2 fields per test. 
You can achieve this by registering one or more beans of type `TableDefaults`
e.g.

```kotlin
@Bean
fun demoDefaults() = TableDefaults("demo", File.CellOverride("[NAME]", "Test"))
```

The example above overrides all dataset values for `[NAME]` with value `"Test"` unless a further override has been used in a `TemplatedDatabaseSetup` or `TemplatedDatabaseTeardown`, or via `DatabaseTester` directly.

###### Useful Examples

See:
- DemoUsingTemplatedAnnotations
- DemoUsingTemplatedAnnotationsAndDefaults
- DemoUsingTemplatedDatabaseTester
- DemoUsingTemplatedDatabaseTesterAndDefaults

#### Using `DatabaseTester` instead of annotations

```kotlin
@SpringBootTest(
  classes = [
      SomeTestClass.DemoTestConfiguration::class
  ]
)
@AutoConfigureDbUnit
class SomeTestClass @Autowired constructor(
    private val dbUnit: DatabaseTester
) {
    
    /*
    You would likely replace this with a repository class which you are testing
     */
    @Autowired
    private lateinit var jdbc: JdbcTemplate
    
    @BeforeEach
    fun beforeEach() {
        jdbc.execute("CREATE TABLE demo (id BIGINT NOT NULL, name VARCHAR(50) NOT NULL, CONSTRAINT demo_pk PRIMARY KEY (id))")
    }
    
    @AfterEach
    fun afterEach() {
        jdbc.execute("DROP TABLE demo")
    }

    @Test
    fun `repository should insert successfully`() {
        jdbc.update("INSERT INTO demo (id, name) VALUES (?, ?)")

        val demoTable = dbUnit.createTable("demo") // Standard DB Unit API
        assertThat(demoTable.rowCount).isOne
        assertThat(demoTable.getValue(0, "id")).isEqualTo(BigInteger.valueOf(123))
        assertThat(demoTable.getValue(0, "name")).isEqualTo("Test")
    }

    @Test
    fun `repository should query successfully`() {
        dbUnit.givenDataSet(DemoUsingDatabaseTester::class.java, "/Demo.xml")

        val result = repository.selectAll()
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(123)
        assertThat(result[0].name).isEqualTo("Test")
    }
    
    /*
    Configuration Class which sets up the minimum beans required to run this test.
    If using
     */
    @TestConfiguration
    class DemoTestConfiguration {

        /*
        This is the minimum connection config you need to provide to get Db Unit working
         */
        @Bean
        fun connectionSupplier(ds: DataSource) = DataSourceConnectionSupplier(ds)

        /*
        This example uses an in memory H2 Database.
        */
        @Bean
        fun dataSource(): DataSource = DataSourceBuilder
            .create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:dbunit")
            .username("sa")
            .password("sa")
            .build()

        /*
        Used in the Test example to query the DB. 
         */
        @Bean
        fun jdbc(ds: DataSource) = JdbcTemplate(ds)
    }
}
```

###### Useful Examples

See:
- DemoUsingDatabaseTester
- DemoUsingTemplatedDatabaseTester
- DemoUsingTemplatedDatabaseTesterAndDefaults

Notes:

- `@SpringBootTest`: Ensures your test is run with the Spring Runner
- `@AutoConfigureDbUnit`: Ensures the relevant Db Unit Configuration is available for Component Scanning.
- `DatabaseTester`: The DB Unit API, used for setting & retrieving the current database State

## Customization

Some sensible defaults have been set up as `@Bean`s within `SpringBootTestDbUnitConfiguration`.

You can register a bean of each type in your configuration classes to customize Db Unit to your preferences.

You can use this to:

- Add DatabaseConfig
- Change the DataSetLoader from the default XmlLocalResourceDataSetLoader
- Modify the Database Connection DBUnit uses

etc

Note there is a known bug which means all `Bean`s you create to customize the config need to be annotated with `@Primary`


###### Useful Examples

See:
- DemoUsingAnnotationsWithCustomDataSetLoader

## Other Notes

If you need to specify extra `TestExecutionListeners`, make sure you include `DependencyInjectionTestExecutionListener` & `DatabaseSetupAndTeardownTestExecutionListener`

## Contributing

All contributions are much appreciated. 

If you'd like to help out, please either:

- Raise a Github Issue so we can discuss what's missing/could be improved

or
  
- Fork the project and create a PR to the main branch
