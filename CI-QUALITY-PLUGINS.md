# CI/CD Quality Plugins Configuration

This document provides optional plugin configurations that can be added to `pom.xml` to enhance code quality checks in the CI/CD pipeline.

## Current Status

The CI pipeline has been updated to gracefully handle missing quality plugins. All quality checks are optional and will not fail the build if not configured.

## Optional Quality Plugins

### 1. Spotless (Code Formatting)

Add to `<plugins>` section in `pom.xml`:

```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.43.0</version>
    <configuration>
        <java>
            <googleJavaFormat>
                <version>1.17.0</version>
                <style>GOOGLE</style>
            </googleJavaFormat>
            <removeUnusedImports />
            <trimTrailingWhitespace />
            <endWithNewline />
        </java>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 2. JaCoCo (Code Coverage)

Add to `<plugins>` section in `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>INSTRUCTION</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 3. OWASP Dependency Check (Security)

Add to `<plugins>` section in `pom.xml`:

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.7</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <skipSystemScope>true</skipSystemScope>
        <skipTestScope>true</skipTestScope>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 4. Checkstyle (Code Style)

Add to `<plugins>` section in `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
        <violationSeverity>warning</violationSeverity>
        <failOnViolation>true</failOnViolation>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
    </configuration>
    <executions>
        <execution>
            <id>validate</id>
            <phase>validate</phase>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 5. PMD (Static Analysis)

Add to `<plugins>` section in `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.21.2</version>
    <configuration>
        <rulesets>
            <ruleset>/category/java/bestpractices.xml</ruleset>
            <ruleset>/category/java/codestyle.xml</ruleset>
            <ruleset>/category/java/design.xml</ruleset>
            <ruleset>/category/java/errorprone.xml</ruleset>
            <ruleset>/category/java/performance.xml</ruleset>
        </rulesets>
        <failOnViolation>true</failOnViolation>
        <printFailingErrors>true</printFailingErrors>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Usage

1. **To enable any plugin**: Copy the relevant configuration to your `pom.xml`
2. **To format code**: Run `./mvnw spotless:apply` (if Spotless is configured)
3. **To check coverage**: Run `./mvnw jacoco:report` (if JaCoCo is configured)
4. **To run security scan**: Run `./mvnw dependency-check:check` (if OWASP is configured)

## CI/CD Integration

The CI pipeline automatically detects which plugins are configured and runs the appropriate checks. No changes to the CI configuration are needed when adding these plugins.

## Recommendations

For a Quarkus project like DurvalCRM:
1. **Start with JaCoCo** for code coverage monitoring
2. **Add Spotless** for consistent code formatting
3. **Consider OWASP** for security scanning (especially for production deployments)
4. **Add PMD or Checkstyle** for additional code quality checks if needed

## Notes

- All plugins are optional and the CI will continue to work without them
- Plugins can be added incrementally as needed
- Some plugins may require additional configuration files (like `checkstyle.xml`)
- Version numbers should be kept up to date for security and feature improvements