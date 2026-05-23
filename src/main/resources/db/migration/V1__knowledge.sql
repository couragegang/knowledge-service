CREATE TABLE connector_catalog (
    connector_key   TEXT PRIMARY KEY,
    display_name    TEXT NOT NULL,
    ingest_mode     TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO connector_catalog (connector_key, display_name, ingest_mode) VALUES
    ('notion', 'Notion', 'mcp_tools'),
    ('upload', 'File upload', 'upload');

CREATE TABLE knowledge_sources (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id               UUID NOT NULL,
    workspace_id         UUID NOT NULL,
    connector_key        TEXT NOT NULL REFERENCES connector_catalog (connector_key),
    ingest_channel       TEXT NOT NULL DEFAULT 'mcp',
    mcp_installation_id  UUID,
    display_name         TEXT NOT NULL,
    status               TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'paused', 'error')),
    last_indexed_at      TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX knowledge_sources_ws_idx ON knowledge_sources (org_id, workspace_id);

CREATE TABLE knowledge_documents (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id          UUID NOT NULL REFERENCES knowledge_sources (id) ON DELETE CASCADE,
    connector_key      TEXT NOT NULL,
    external_id        TEXT NOT NULL,
    title              TEXT NOT NULL,
    body               TEXT NOT NULL DEFAULT '',
    source_updated_at  TIMESTAMPTZ,
    indexed_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (source_id, external_id)
);

CREATE INDEX knowledge_documents_search_idx ON knowledge_documents
    USING gin (to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(body, '')));
