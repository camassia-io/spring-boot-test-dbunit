# Spring Boot Test DBUnit

An Open Source Spring Boot DB Unit Integration, based on the popular but no longer maintained [Spring Test Db Unit](https://springtestdbunit.github.io/spring-test-dbunit)

This is a Kotlin project, but should be easily integrated with Java.

## Getting Started

For some demo examples see the spring-boot-test-dbunit-demo project

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

The following standalone example uses an In Memory H2 Database with Springs JdbcTemplate as the test subject.

#### Using `DatabaseSetup` / `DatabaseTeardown` annotations
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
        This example uses an in memory H2 Database.
        If your DataSource is already available via component scanning then this is not necessary.
        The Spring-boot-test-dbunit library requires a DataSource bean to be registered at a minimum.
        If you would prefer to use an alternative connection type than a DataSource, see the Customization options below.
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
        This example uses an in memory H2 Database.
        If your DataSource is already available via component scanning then this is not necessary.
        The Spring-boot-test-dbunit library requires a DataSource bean to be registered at a minimum.
        If you would prefer to use an alternative connection type than a DataSource, see the Customization options below.
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
- Modify the underlying DBUnit DatabaseTester (e.g. to use an alternative to a DataSource)

etc

## Other Notes

If you need to specify extra `TestExecutionListeners`, make sure you include `DependencyInjectionTestExecutionListener` & `DatabaseSetupAndTeardownTestExecutionListener`

## Contributing

All contributions are much appreciated. 

If you'd like to help out, please either:

- Raise a Github Issue so we can discuss what's missing/could be improved

or
  
- Fork the project and create a PR to the master branch
