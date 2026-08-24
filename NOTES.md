# Notes

My own notes, kept out of the code so they survive refactors.

## Maven

```
mvn [options] [<goal(s)>] [<phase(s)>]
```

Maven has 3 lifecycles: **clean**, **default**, **site**. A phase is a step in a
lifecycle.

**clean** — removes temporary directories and files.
- `pre-clean` — hook for before cleaning
- `clean` — the actual cleaning
- `post-clean` — hook for after cleaning

**default** — where the most useful goals live, and they run *in order*:
- `compile` — compiles code into bytecode
- `test` — runs unit tests
- `package` — creates a jar or war
- `verify` — runs checks and integration tests

Because it's in order, `./mvnw test` also runs `compile` first. Same logic for
`verify` — it runs everything before it.

**site** — generates documentation.

Handy:
- `./mvnw clean compile`
- `./mvnw spring-boot:run` — closest thing to `npm run dev`
