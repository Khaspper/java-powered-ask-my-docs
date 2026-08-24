# Build graph

Root = the whole app. Leaves = the smallest thing you can build **and run** in
one sitting. Build bottom-up, left to right.

**A node is only a leaf if you can name the command that proves it works.**
If you can't, split it again.

Status: `[ ]` not started · `[~]` in progress · `[x]` done

```
Ask my docs
│
├── A. It runs
│   ├── [x] A1  empty Spring Boot app that starts
│   └── [x] A2  GET /ping -> "pong"
│
├── B. It stores files                (needs A1)
│   ├── [ ] B1  Postgres + pgvector running in Docker
│   ├── [ ] B2  documents table + Java class for it
│   ├── [ ] B3  POST /documents  - txt/md/pdf, 409 on duplicate
│   └── [ ] B4  GET /documents   - summary rows
│
├── C. It answers questions           (needs B1, B2)
│   ├── [ ] C1  chop text into chunks  (1000 chars, 200 overlap)
│   ├── [ ] C2  chunks table + Java class for it
│   ├── [ ] C3  ask Gemini to turn a chunk into 768 numbers
│   ├── [ ] C4  save the numbers next to the chunk (pgvector)
│   ├── [ ] C5  POST /search  - k closest chunks, default k=5
│   ├── [ ] C6  POST /ask     - grounded answer + sources
│   └── [ ] C7  background job - every 60s, fills in missing numbers
│
├── D. Proof it works                 (needs C)
│   ├── [ ] D1  unit tests (chopping) + integration tests (Testcontainers)
│   └── [ ] D2  deploy - CDK in Java, one EC2 box, docker compose
│
└── E. UI                             (DEAD LAST - only if time is left)
    └── [ ] E1  one page: upload a file, ask a question, see the answer
```

## Proof for each node

| node | the command that proves it |
|---|---|
| A1 | `./mvnw spring-boot:run` — it starts and doesn't crash |
| A2 | `curl localhost:8080/ping` → `pong` |
| B1 | `docker compose up -d` then `psql` and `SELECT 1` |
| B2 | app starts, table exists in Postgres |
| B3 | `curl -F 'file=@notes.txt' localhost:8080/documents` → 201 |
| B4 | `curl localhost:8080/documents` → your file is in the list |
| C1 | unit test: 2500 chars in, 3 chunks out |
| C2 | upload a file, `SELECT count(*) FROM chunks` is > 0 |
| C3 | logs show 768 numbers coming back from Gemini |
| C4 | `SELECT embedding FROM chunks LIMIT 1` is not null |
| C5 | `curl -d '{"question":"..."}' localhost:8080/search` → ranked chunks |
| C6 | `curl -d '{"question":"..."}' localhost:8080/ask` → answer + sources |
| C7 | null an embedding by hand, wait 60s, watch it come back |
| D1 | `./mvnw test` — all green |
| D2 | `cdk deploy`, then curl the public address |

## Why it's a graph and not a tree

`C` needs `B1` (Postgres), which lives in a different branch. An edge that
crosses branches is what makes this a graph rather than a tree.

## Rule

Never start a node whose dependencies aren't `[x]`.
