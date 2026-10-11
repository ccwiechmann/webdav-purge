# webdav-purge

`webdav-purge` is a small command line tool written in Java that removes the
content of a directory on a WebDAV server. It was built for the common
"bulk delete on a remote share" scenario where mounting the WebDAV resource
locally (e.g. via `davfs2`) is too heavy weight or simply not possible.

## Features

- Purges the **direct** content of a single WebDAV directory (files and
  sub-directories, one level deep).
- Verifies after the purge that the directory is actually empty and reports
  the outcome clearly.
- A failed delete of one entry does **not** abort the rest of the purge; the
  remaining entries are still attempted and the final verification tells you
  whether everything worked out.
- Ships as a single self-contained `jar-with-dependencies` file, so it runs
  anywhere a Java 25 runtime is installed — no build system, no packaging
  steps on the target machine.

## Requirements

- A [Java 25](https://adoptium.net/) (or newer) runtime / JDK.
- A reachable WebDAV server that supports basic authentication.

## Usage

```
webdav-purge [-u URL] [-n USER] [-p PASSWORD] [-d DIRECTORY]
```

All options are required. For each option a short flag (`-u`) and a long flag
(`--url`) are provided:

| Flag                  | Long flag      | Meaning                                                       |
|-----------------------|----------------|---------------------------------------------------------------|
| `-u`                  | `--url`        | Base URL of the WebDAV server, e.g. `https://example.com/remote.php/dav/files/jdoe` |
| `-n`                  | `--username`   | User name for the WebDAV connection                           |
| `-p`                  | `--password`   | Password for the WebDAV connection                            |
| `-d`                  | `--directory`  | Directory (relative to the server base) whose content is purged |

### Example

```
java -jar target/webdav-purge-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
    -u https://example.com/remote.php/dav/files/jdoe \
    -n jdoe \
    -p secret \
    -d uploads/outbox
```

Output on success:

```
Delete file: https://example.com/remote.php/dav/files/jdoe/uploads/outbox/report.pdf
Delete file: https://example.com/remote.php/dav/files/jdoe/uploads/outbox/draft.docx
WebDAV directory 'uploads/outbox' successfully purged.
```

If the tool cannot verify that the directory is empty (e.g. some delete
failed) it prints an error on standard error and exits non-zero.

## Building

[![Maven](https://img.shields.io/badge/Maven-3.9+-orange)]()

With a Java 25 JDK on your `PATH` (set `JAVA_HOME` accordingly), a single
command builds, tests and verifies the project:

```
mvn clean verify
```

This:

1. compiles the sources (Java 25, `--release 25`),
2. runs the unit tests with JUnit 6 (Jupiter),
3. packages the jar and the `jar-with-dependencies` distribution jar,
4. runs the Checkstyle verification using the Sun ruleset.

Afterwards the runnable, self-contained jar is located at
`target/webdav-purge-0.0.1-SNAPSHOT-jar-with-dependencies.jar`.

> **Note:** Maven compiles in-process by default. If the `java` binary Maven
> runs with is a JRE (or a JVM without the `ct.sym` signature database), the
> build fails with `Releaseversion 25 nicht unterstützt`. In that case point
> `JAVA_HOME` at a **full JDK** installation before running Maven, e.g.
> `export JAVA_HOME=$(/usr/libexec/java_home -v 25)` on macOS.

## Project layout

```
webdav-purge/
├── pom.xml                          # Maven build: Java 25, deps, plugins
├── src/
│   ├── main/
│   │   ├── checkstyle/
│   │   │   └── suppressions.xml     # Checks suppressed for all sources
│   │   └── java/io/ccw/webdav/purge/
│   │       ├── WebDavPurge.java                     # CLI entry point (picocli)
│   │       ├── ParameterContainer.java              # Command line options
│   │       ├── WebDavPurgeException.java            # User facing error type
│   │       └── webdav/
│   │           └── WebDavConnection.java            # Sardine WebDAV wrapper
│   └── test/java/io/ccw/webdav/purge/
│       ├── ParameterContainerTest.java              # CLI parsing tests
│       └── webdav/
│           └── WebDavConnectionTest.java            # Purge logic tests (mocks)
├── AGENTS.md                        # Guidance for AI agents
└── LICENSE                          # Apache License 2.0
```

## Technology & dependency stack

The following versions are current as of October 2026 and are pinned in
`pom.xml`.

| Component                | GroupId / Artifact                       | Version | Role                                        |
|--------------------------|------------------------------------------|---------|---------------------------------------------|
| Java                     | —                                        | 25      | Language level (`maven.compiler.release`)   |
| picocli                  | `info.picocli:picocli`                   | 4.7.7   | Command line parsing (replaced JCommander)  |
| Sardine                  | `com.github.lookfirst:sardine`           | 5.13    | WebDAV client (PROPFIND/DELETE)             |
| JUnit Jupiter            | `org.junit.jupiter:junit-jupiter`        | 6.1.3   | Testing framework                           |
| JUnit Platform Launcher  | `org.junit.platform:junit-platform-launcher` | 6.1.3 | Required by Surefire for JUnit 5/6          |
| Mockito                  | `org.mockito:mockito-core`               | 5.24.0  | Mocks the Sardine session in unit tests     |
| maven-compiler-plugin    | `org.apache.maven.plugins`               | 3.16.0  | Compiles with `--release 25`                |
| maven-surefire-plugin    | `org.apache.maven.plugins`               | 3.5.6   | Runs the JUnit tests                        |
| maven-jar-plugin         | `org.apache.maven.plugins`               | 3.5.1   | Main jar with `Main-Class` manifest         |
| maven-assembly-plugin    | `org.apache.maven.plugins`               | 3.8.0   | `jar-with-dependencies` distribution jar    |
| maven-checkstyle-plugin  | `org.apache.maven.plugins`               | 3.6.0   | Runs Checkstyle during `verify`             |
| Checkstyle               | `com.puppycrawl.tools:checkstyle`        | 10.26.1 | Linting engine (Sun ruleset)                |

Notes:

- **Sardine 5.13** transitively provides Apache HttpClient 4.x, Commons
  Codec, the Jakarta XML Bind API and the JAXB runtime — no separate JAXB
  dependencies are needed.
- **JCommander** was replaced by **picocli** because JCommander is no longer
  actively maintained. The option names (`-u`, `-n`, `-p`, `-d`) were kept
  identical so existing scripts and aliases keep working.

## Code style & static analysis

Checkstyle is integrated into the Maven build and bound to the `verify`
phase. It uses the built-in **Sun coding conventions** ruleset
(`sun_checks.xml`) with the suppressions from
`src/main/checkstyle/suppressions.xml`:

```xml
<suppressions>
    <suppress checks="DesignForExtension" files=".*\.java"/>
    <suppress checks="JavadocPackage" files=".*\.java"/>
    <suppress checks="HiddenField" files=".*\.java"/>
    <suppress checks="FinalParameters" files=".*\.java"/>
    <suppress checks="JavadocVariable" files=".*\.java"/>
    <suppress checks="LineLength" files=".*\.java"/>
</suppressions>
```

The build fails as soon as Checkstyle reports a violation
(`failOnViolation=true`), so a green `mvn verify` is the definition of a
healthy build. All public classes and methods carry Javadoc in English.

## Testing

The unit tests are written with **JUnit 6** (Jupiter) and do **not** require
a running WebDAV server. The Sardine session is replaced by a Mockito mock
that is injected into the connection, which allows the tests to:

- verify that the directory itself is excluded from listings,
- verify that every listed entry is deleted with its fully qualified URL,
- verify that a failing delete does not stop the remaining deletes,
- verify the `isDirectoryEmpty` behaviour,
- verify that `null` arguments are rejected with a descriptive message,
- exercise the picocli command line parsing (happy path and missing
  required options).

Run them on their own with:

```
mvn test
```

## License

Distributed under the [Apache License, Version 2.0](LICENSE).
