# Ask My Docs — spec

> Read `CLAUDE.md` before this file. It tells you how to talk to Markus.
> This file is the source of truth for **what** we are building.
> If a decision is not here, ask him, then add it here.

## Where it lives

| | |
|---|---|
| Folder | `~/Documents/Projects/java-powered-ask-my-docs` |
| Git branch | `main` |
| Maven group / artifact | `com.khaspper` / `askmydocs` |
| Java package | `com.khaspper.askmydocs` |
| Spring Boot | 3.5.3, pinned by hand in `pom.xml` |

## What it is

A backend service. You upload documents. You ask questions about them. It finds
the relevant pieces of your documents and has Gemini write an answer using only
those pieces — and it tells you which pieces it used.

There is no website until the very end. You test it with `curl`.

## Why it exists

Markus is learning **Java and Spring Boot**. The AI part is the excuse. When
there is a choice between a clever retrieval trick and simple code he can read,
**pick simple, every time.**

## Words used in this file

- **document** — one uploaded file, plus all its text.
- **chunk** — a small slice of one document's text (~1000 characters).
- **embedding** — a list of 768 numbers that stands for a chunk's meaning.
  Similar text gives similar numbers. That is how we find "close" chunks.
- **pgvector** — an add-on for Postgres that can store those numbers and find
  the closest ones quickly.

## Stack

| Thing | Choice | Why |
|---|---|---|
| Language | Java 21 | Spring Boot 3's home turf. Machine currently only has Java 25 — install `temurin@21`. |
| Framework | Spring Boot 3 | The thing being learned. |
| Build | Maven, via the `./mvnw` script in the repo | No global Maven install needed. |
| Database | Postgres + pgvector, in Docker | One `docker compose up`. |
| DB access | Spring Data JPA | Standard Spring way to talk to a database. |
| PDF reading | Apache PDFBox | Pulls words out of a PDF. |
| Gemini calls | Spring's `RestClient` | Plain HTTP. No AI SDK, nothing to hide the wiring. |
| Tests | JUnit 5 + Testcontainers | Testcontainers starts a real Postgres in Docker during tests. |
| Deploy | AWS CDK, written in Java | More Java reps. |

---

## Data

### `documents`

| column | type | notes |
|---|---|---|
| `id` | bigserial | |
| `filename` | text | as uploaded |
| `content_type` | text | txt / md / pdf |
| `sha256` | text, **unique** | fingerprint of the file's bytes; this is what makes duplicates a 409 |
| `text` | text | the **full** extracted text — kept so we can re-chop later without re-uploading |
| `uploaded_at` | timestamptz | |

### `chunks`

| column | type | notes |
|---|---|---|
| `id` | bigserial | |
| `document_id` | bigint | points at `documents.id`; deleting a document deletes its chunks |
| `chunk_index` | int | 0, 1, 2... order within the document |
| `text` | text | the slice |
| `embedding` | `vector(768)`, nullable | **null means "not embedded yet"** |
| `attempts` | int, default 0 | how many times embedding has failed |

**768 is baked into the column.** Changing it later means dropping and rebuilding
the table. See `docs/adr/0002`.

---

## Endpoints

### `POST /documents` — upload a file

Multipart form, field name `file`.

- Allowed: `.txt`, `.md`, `.pdf`. Anything else → **400**.
- Max size: **10 MB** → over that is **413**.
- Fingerprint the bytes (SHA-256). Already in the database → **409** naming the
  existing document id.
- Extract text (PDFBox for pdf, read as-is for txt/md).
- Chop into chunks and save them.
- Try to embed every chunk **during the request**. Any chunk that fails is saved
  with `embedding = null` — the request still succeeds.

```
201 Created
{ "id": 1, "filename": "notes.txt", "chunks": 150, "embedded": 147 }
```

### `GET /documents` — list what's uploaded

No document text — a big PDF would make the response unreadable.

```
200 OK
[ { "id": 1, "filename": "notes.txt",
    "uploadedAt": "2026-08-23T10:00:00Z", "chunkCount": 12 } ]
```

### `POST /search` — find chunks, no Gemini answer

```
POST /search   { "question": "how do I start the app?", "k": 5 }
```

`k` is optional, defaults to **5**. Embed the question, find the `k` closest
chunks with pgvector, return them with a score.

`score` is cosine similarity from 0 to 1, where 1 means identical.

```
200 OK
[ { "chunkId": 42, "documentId": 1, "filename": "notes.txt",
    "text": "Run ./mvnw spring-boot:run ...", "score": 0.83 } ]
```

### `POST /ask` — same search, then a Gemini answer

Same body as `/search`. Does the same retrieval, then sends the chunks plus the
question to Gemini.

**The instruction to Gemini must say: answer only from the text provided; if the
answer is not in it, say you don't know.** No falling back on Gemini's own
knowledge — otherwise you can never tell whether an answer came from the
documents or was invented.

```
200 OK
{ "answer": "Run ./mvnw spring-boot:run.",
  "sources": [ { "chunkId": 42, "filename": "notes.txt", "score": 0.83 } ] }
```

Nothing relevant found → still **200**, with an "I don't know" answer and
`"sources": []`.

---

## Chopping text into chunks

- **1000 characters** per chunk.
- **200 characters of overlap** — each chunk repeats the tail of the one before
  it, so a sentence cut in half still appears whole somewhere.
- Character counting only. No sentence detection, no paragraph splitting.

```
chunk 0: chars 0    - 1000
chunk 1: chars 800  - 1800
chunk 2: chars 1600 - 2600
```

## The background job

A `@Scheduled` method — a Java method Spring calls on a timer.

- Runs **every 60 seconds**.
- Picks up to **50** chunks `WHERE embedding IS NULL AND attempts < 3`.
- Tries to embed each one. Success saves the numbers; failure does
  `attempts = attempts + 1`.
- A chunk that fails 3 times is left alone forever. This is what stops a single
  bad chunk from calling Gemini every minute until the quota is gone.

## Tests

- **Unit** — the chopping logic is pure Java. Test it directly, no Spring, no
  database. Cases: 2500 chars → 3 chunks; empty text → 0 chunks; overlap really
  is 200.
- **Integration** — Testcontainers starts a real Postgres. Boot the app and hit
  the endpoints: upload `.txt` → 201, upload it again → 409, upload `.jpg` →
  400, `GET /documents` shows it.
- **Gemini is always faked in tests.** Tests must run offline and cost nothing.

## Deploying

- **One EC2 instance** (`t4g.micro`) running the app and Postgres side by side
  with docker compose. Same setup as the laptop.
- **No load balancer, no RDS.** A load balancer is ~$16/month and never free;
  RDS is a second paid thing. See `docs/adr/0001`.
- CDK stack written in **Java**, creating: VPC (no NAT gateway — those cost
  money), security group, the instance, and a user-data script that installs
  Docker and starts the app.
- Free-tier eligible only if the AWS account is under 12 months old. Otherwise a
  few dollars a month — delete the stack when done.

---

## Not doing

- Re-ranking, query rewriting, hybrid search — parked as v2.
- Users, auth, multi-tenancy.
- Streaming answers.
- A website, until every other node on `GRAPH.md` is green.

## Still open

- **Exact Gemini model names** for embedding and generation. Pin them by
  checking Google's current docs when node C3 is built — do not guess from
  memory.
- **`GEMINI_API_KEY`** comes from an environment variable. Never commit it.
