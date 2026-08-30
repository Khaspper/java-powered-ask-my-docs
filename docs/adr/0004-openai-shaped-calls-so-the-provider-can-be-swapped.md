# Talk the OpenAI request shape, so the model provider can be swapped

Decided 2026-08-29 by Markus. The project started calling Gemini directly, with
Google's own URLs, its `x-goog-api-key` header and its nested request and reply
shapes hard-coded into `Embedder` and `Answerer`. That worked, but it meant the
provider was welded into the Java: trying a different model was a rewrite of two
classes, not a settings change.

Both classes now send the OpenAI request shape — `POST /embeddings` and
`POST /chat/completions`, key in an `Authorization: Bearer` header — because
nearly every provider and every local runner accepts it. The base URL, the key
and the two model names all come from `application.properties`, so switching
providers is three lines and no recompile of logic.

Today those lines point at Ollama on the laptop: `nomic-embed-text` for the
numbers, `llama3.2` for the writing.

## Considered Options

- **Stay on Gemini's own shape.** Fewer moving parts, but every request costs
  money, needs a key exported in the right terminal, and needs the internet —
  and the upcoming tests are supposed to run offline for free.
- **Add Spring AI or another SDK.** It would hide exactly the wiring this
  project exists to look at.

## Consequences

- The whole app now runs with no internet and no bill, which is what makes
  `D1`'s "the models are always faked in tests" easy to honour and makes a demo
  possible on a plane.
- Answers are noticeably weaker than Gemini's. Accepted: the point is the Java.
- Switching back is uncommenting three lines — the Gemini settings are still in
  `application.properties`.
- `nomic-embed-text` needs a prefix on the text (`search_document: ` when
  storing, `search_query: ` when searching), so `Embedder` exposes two methods
  instead of one. A different model may need no prefix, or a different one.
- Ollama must be running. When it is not, uploads still return 201 with
  `embedded: 0` and the retry job keeps failing quietly.
