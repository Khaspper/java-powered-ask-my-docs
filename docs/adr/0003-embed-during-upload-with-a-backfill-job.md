# Embed during upload, and let a background job clean up the misses

We embed every chunk while the upload request is still running, so a document is
searchable the moment `POST /documents` returns. But we do not fail the whole
upload when Gemini errors on a few chunks — those are saved with a null
embedding and the request still returns 201. A `@Scheduled` job then finds
null-embedding chunks and fills them in.

## Considered Options

- **All-or-nothing during upload** — one network blip throws away a 40-page PDF,
  and the scheduled job has nothing left to do.
- **Job does all the embedding** — snappy uploads, but a document is not
  searchable for up to a minute and the upload response cannot say whether
  anything worked.

## Consequences

The job gives up on a chunk after 3 failed attempts, tracked in an `attempts`
column, so one permanently-bad chunk cannot burn the API quota forever.
