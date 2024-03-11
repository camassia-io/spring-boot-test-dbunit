# Spring Boot Test DBUnit

[![Build](https://github.com/camassia-io/spring-boot-test-dbunit/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/camassia-io/spring-boot-test-dbunit/actions/workflows/build.yml)

![Latest Version](https://img.shields.io/github/v/release/camassia-io/spring-boot-test-dbunit)


An Open Source Spring Boot DB Unit Integration, based on the popular but no longer maintained [Spring Test Db Unit](https://springtestdbunit.github.io/spring-test-dbunit)

This library aims to provide a highly customisable, easy to use way of testing the Repository/Database layer of your Spring Boot services using real, or in memory databases.

This library is written in Kotlin, but can be easily integrated into a Java project.

---

## Getting Started

For demo examples see the spring-boot-test-dbunit-demo project

### Import the Dependency

#### Gradle 

`implementation("io.camassia:spring-boot-test-dbunit:{{latest version}}")`

#### Maven

```xml
<dependency>
    <groupId>io.camassia</groupId>
    <artifactId>spring-boot-test-dbunit</artifactId>
    <version>{{latest version}}</version>
</dependency>
```

---

### Writing Tests

The following standalone examples use an In Memory H2 Database with Springs `JdbcTemplate` as the test subject. 
You would likely replace `JdbcTemplate` with your own Repository Class that you want to test.

- [Using DatabaseSetup & DatabaseTeardown Annotations](https://github.com/camassia-io/spring-boot-test-dbunit#using-databasesetup--databaseteardown-annotations)
  - [With Programmatic DataSets (recommended)](https://github.com/camassia-io/spring-boot-test-dbunit#with-programmatic-datasets)
  - [With DataSet Files](https://github.com/camassia-io/spring-boot-test-dbunit#with-file-based-datasets)
  - [With Templated DataSet Files](https://github.com/camassia-io/spring-boot-test-dbunit#with-templated-file-based-datasets)
- [Using DatabaseTester directly](https://github.com/camassia-io/spring-boot-test-dbunit#using-databasetester-instead-of-annotations)

#### Using `DatabaseSetup` / `DatabaseTeardown` annotations

##### With Programmatic DataSets

You can configure the entire DataSet using annotations if you like (rather than files).

This can be combined with TableDefaults to ensure you only have to specify the bare minimum of cells you want to change per test.

```kotlin
@DatabaseSetup(
    tables = [
        Table(
            "demo",
            Row(Cell("ID", "123"), Cell("NAME", "Test"))
        )
    ]
)
```

You can also set up Global Defaults so that you do not have to specify a value for each column of each table. See [Customization](https://github.com/camassia-io/spring-boot-test-dbunit#customization) for more info.

###### Useful Examples

See:
- [DemoUsingAnnotationsAndProgrammaticDataSet](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingAnnotationsAndProgrammaticDataSet.kt)

##### With File Based DataSets

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
- [DemoUsingAnnotations](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingAnnotations.kt)
- [DemoUsingAnnotationsAndStringDataSet](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingAnnotationsAndStringDataSet.kt)
- [DemoUsingAnnotationsWithCustomDataSetLoader](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingAnnotationsWithCustomDataSetLoader.kt)
- [DemoUsingTemplatedAnnotations](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingTemplatedAnnotations.kt)
- [DemoUsingTemplatedAnnotationsAndDefaults](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingTemplatedAnnotationsAndDefaults.kt)
- [DemoUsingAnnotationsAndProgrammaticDataSet](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingAnnotationsAndProgrammaticDataSet.kt)

##### With Templated File Based DataSets

If using Templated files, e.g.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dataset>
    <demo id="[ID]" name="[NAME]"/>
</dataset>
```

You can override these templated values using `@DatabaseSetup(files = [...])` / `@DatabaseTeardown(files= [...])`. 

```kotlin
@DatabaseSetup(
    files = [
        File(
            "/TemplatedDemo.xml", 
            Cell("[ID]", "123"), // Overrides value "[ID]" in TemplatedDemo.xml with 123
            Cell("[NAME]", "[null]") // Overrides value "[NAME]" in TemplatedDemo.xml with null
        )
    ]
)
```

Note that template replacements are done by value, not by column name. All template keys should be within square brackets.

You can also set up Global Defaults so that you do not have to specify a value for each column of each table. See [Setting Global Defaults](https://github.com/camassia-io/spring-boot-test-dbunit#global-defaults-for-each-table) for more info.

###### Useful Examples

See:
- [DemoUsingTemplatedAnnotations](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingTemplatedAnnotations.kt)
- [DemoUsingTemplatedAnnotationsAndDefaults](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingTemplatedAnnotationsAndDefaults.kt)
- [DemoUsingTemplatedDatabaseTester](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingTemplatedDatabaseTester.kt)
- [DemoUsingTemplatedDatabaseTesterAndDefaults](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingTemplatedDatabaseTesterAndDefaults.kt)

##### With String Based DataSets

These by default will load XML you specify using the default DataSetParser.
The file parser can be configured using Beans. See Customization below for more info.

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
    @DatabaseSetup(dataSet = """<demo id="123" name="test"/>""")
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
- [DemoUsingAnnotationsAndStringDataSet](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingAnnotationsAndStringDataSet.kt)

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

You can also set up Global Defaults so that you do not have to specify a value for each column of each table. See [Setting Global Defaults](https://github.com/camassia-io/spring-boot-test-dbunit#global-defaults-for-each-table) for more info.

###### Useful Examples

See:
- [DemoUsingDatabaseTester](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingDatabaseTester.kt)
- [DemoUsingTemplatedDatabaseTester](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingTemplatedDatabaseTester.kt)
- [DemoUsingTemplatedDatabaseTesterAndDefaults](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/spring-boot-test-dbunit-demo/src/test/kotlin/io/camassia/spring/dbunit/DemoUsingTemplatedDatabaseTesterAndDefaults.kt)

Notes:

- `@SpringBootTest`: Ensures your test is run with the Spring Runner
- `@AutoConfigureDbUnit`: Ensures the relevant Db Unit Configuration is available for Component Scanning.
- `DatabaseTester`: The DB Unit API, used for setting & retrieving the current database State

---

## Customization

Some sensible defaults have been set up as `@Bean`s within `SpringBootTestDbUnitConfiguration`.

You can register a bean of each type in your configuration classes to customize Db Unit to your preferences.

You can use this to:

- Add `DatabaseConfig`
- Change the `ResourceLoader` from the default file based loader
- Modify the Database Connection DBUnit uses (see `ConnectionSupplier` `afterCreation`)
- [Add Table Column Value Defaults](https://github.com/camassia-io/spring-boot-test-dbunit#global-defaults-for-each-table)
- Add/remove extensions that DbUnit uses to manage custom handling for mapping cell values. E.g if you wanted to do something like mapping `"[pi]"` to a numerical number for pi.

See `SpringBootTestDbUnitConfiguration` for existing defaults that are in use
  
Note there is a known bug which means all `Bean`s you create to customize the config need to be annotated with `@Primary`

##### Global Defaults for each Table

You can set up global default values for each column of each table, e.g. in case you only want to override 1 or 2 fields per test.
You can achieve this by registering one or more beans of type `TableDefaults`
e.g.

```kotlin
@Bean
fun demoDefaults() = TableDefaults("demo", Cell("[NAME]", "Test"))
```

The example above overrides all dataset values for `[NAME]` with value `"Test"` unless a further override has been used in a `DatabaseSetup` or `DatabaseTeardown`, or via `DatabaseTester` directly.


###### Useful Examples

See:
- DemoUsingAnnotationsWithCustomDataSetLoader

---

## Other Notes

If you need to specify extra `TestExecutionListeners`, make sure you include `DependencyInjectionTestExecutionListener` & `DatabaseSetupAndTeardownTestExecutionListener`

---

## Contributing

All contributions are much appreciated. 

If you'd like to help out, please either:

- Raise a Github Issue so we can discuss what's missing/could be improved

or
  
- Fork the project and create a PR to the main branch
