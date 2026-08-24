# Embeddings are 768 numbers

pgvector needs the size fixed in the column definition (`vector(768)`), so this
is expensive to change later — it means dropping and rebuilding the chunks
table. We picked the smallest size Gemini offers because storage and search
memory scale directly with it, and at this project's scale (a handful of
documents) the larger sizes buy accuracy nobody will be able to measure.

## Consequences

Changing the embedding model later is only cheap if the new model also produces
768 numbers. Otherwise: new column, re-embed everything.
