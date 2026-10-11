# AGENTS.md

Guidance for AI coding agents (and humans) working on `webdav-purge`.

## What this project is

`webdav-purge` is a tiny, single-purpose Java command line tool that purges
the direct content of one directory on a WebDAV server. It is **not** a
library and has no public API — changes should stay focused on that one
behaviour. Keep it simple: small CLI, one WebDAV wrapper class, tests.

## Hard requirements

- **Language level: Java 25** — `maven.compiler.release` in `pom.xml` must
  stay at `25`. Do not introduce syntax that only works on a newer preview
  release.
- **All user facing text, comments and Javadoc are in English.**
- **Every public class and every public/protected method needs Javadoc.**
- The tool must remain runnable as `java -jar
  target/webdav-purge-0.0.1-SNAPSHOT-jar-with-dependencies.jar`.

## Commands

| Task                    | Command                              |
|-------------------------|--------------------------------------|
| Full build + tests + checkstyle + packaging | `mvn clean verify` |
| Run unit tests only     | `mvn test`                           |
| Run Checkstyle only     | `mvn checkstyle:check`               |
| Check for dependency updates | Look up the latest stable version on [Maven Central](https://search.maven.org/) (e.g. `https://search.maven.org/solrsearch/select?q=g:%3CgroupId%3E+AND+a:%3CartifactId%3E&rows=1&wt=json`) or the artifact's GitHub releases page |

A green `mvn clean verify` (compile, tests, checkstyle, package) is the
definition of done for any change.

## Environment quirk (important)

The default `java` on the usual dev machine is a JRE without the `ct.sym`
file, so in-process compilation of `--release 25` fails with
`Releaseversion 25 nicht unterstützt`. If you see that error, re-run Maven
with a full JDK, e.g.:

```
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk mvn clean verify
```

This is an environment problem, not a project bug — do not "fix" it by
lowering `maven.compiler.release`.

## Architecture

```
io.ccw.webdav.purge
├── WebDavPurge          main(), picocli parsing, exit codes, error handling
├── ParameterContainer   picocli @Option fields (url, user, password, directory)
├── WebDavPurgeException user readable RuntimeException for purge failures
└── webdav.WebDavConnection  Sardine wrapper: list / delete / isEmpty
```

### Conventions

- **CLI options:** short + long name, `required = true`. Preserve the option
  names (`-u/--url`, `-n/--username`, `-p/--password`, `-d/--directory`) —
  they are part of the public interface of the tool.
- **WebDavConnection:** all network calls go through the `Sardine`
  session held in the private `connection` field (`getSardine()` is a
  read-only accessor tests use for verification). Keep the class free of
  static state so it stays mockable.
- **Delete semantics:** a single failing delete must print an error and
  continue with the remaining entries; the final `isDirectoryEmpty` check
  decides the exit status. Do not change this behaviour without updating the
  tests in `WebDavConnectionTest`.
- **Error handling:** unexpected failures surface as
  `WebDavPurgeException` with a short, actionable message; no stack traces
  to the user.
- **Exit codes:** `0` success, `1` purge/verification failure (message on
  stderr), `2` invalid command line usage (picocli prints message + usage on
  stderr). Keep `main` in `WebDavPurge` as the only place that calls
  `System.exit`.

## Testing

- Framework: **JUnit 6** (`org.junit.jupiter`, Jupiter). Do **not** add
  JUnit 4 (`junit:junit`) — it is gone on purpose.
- Tests must not touch the network: mock `com.github.sardine.Sardine`.
- Mockito note: never create a mock *inside* a `when(...).thenReturn(...)`
  chain (causes `UnfinishedStubbing`); build the stub list in a local
  variable first.
- When adding behaviour to `ParameterContainer` or `WebDavConnection`, add
  or extend the corresponding test in `src/test/java/...` in the same commit.

## Static analysis

- Checkstyle runs in the `verify` phase with the `sun_checks.xml` ruleset
  (engine version pinned in `pom.xml`).
- The suppressions that must stay (they express project policy, see
  `src/main/checkstyle/suppressions.xml`):
  `DesignForExtension`, `JavadocPackage`, `HiddenField`, `FinalParameters`,
  `JavadocVariable`, `LineLength` — all for `.*\.java`.
- `failOnViolation=true`: do not disable it; fix the violation instead.
- Common Sun rules to respect when writing code: `NeedBraces`,
  `LeftCurly`/`RightCurly` (same-line braces), `FinalLocalVariable` is
  **not** in the ruleset but the code base uses `final` locals — keep the
  style consistent, `UnusedImports`, `AvoidStarImport`, `MagicNumber`
  (avoid magic numbers in main code, constants are fine),
  `HideUtilityClassConstructor` (utility classes like `WebDavPurge` need a
  private constructor), `JavadocMethod` (document `@throws`),
  `DeclarationOrder` (fields before methods), `Indentation` (4 spaces).

## Dependencies

- Pin exact versions as Maven properties in `pom.xml`.
- When upgrading a dependency, verify the latest stable version on
  [Maven Central](https://search.maven.org/) (or the project's releases page)
  and re-run `mvn clean verify`.
- Current stack (verified October 2026): picocli 4.7.7, Sardine 5.13,
  JUnit Jupiter 6.1.3, Mockito 5.24.0, Checkstyle 10.26.1.
- Sardine brings its own JAXB / HttpClient dependencies via its POM — do not
  add them manually.
- JCommander is **not** allowed anymore (replaced by picocli, Oct 2026).

## Git workflow

- Commit small, focused steps with a short English subject line.
- Before committing: `git status`, `git diff`; stage only intended files;
  never commit secrets or `target/`.
- Keep `README.md` and this file in sync whenever build steps, dependency
  versions, options or conventions change.
- Push / open PRs only when explicitly asked.

## Definition of done

1. `mvn clean verify` passes (tests + checkstyle + packaging).
2. Javadoc present on all newly added public types and methods (English).
3. New/changed behaviour covered by a JUnit test.
4. `README.md` / `AGENTS.md` updated if user visible behaviour changed.
5. No new dependencies without a version pinned in `pom.xml`.
