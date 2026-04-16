--liquibase formatted sql

--changeset shortlink:007-add-topics-and-soft-delete
--comment: Reintroduce topics ownership table and soft-delete status for links
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*)::integer FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'links'

ALTER TABLE links
    ADD COLUMN IF NOT EXISTS status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';

UPDATE links
SET status = 'ACTIVE'
WHERE status IS NULL;

DROP INDEX IF EXISTS uq_links_topic_slug;
CREATE UNIQUE INDEX IF NOT EXISTS uq_links_topic_slug_active ON links (topic, slug)
    WHERE status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_links_created_topic_status ON links (created_by_id, topic, status);

CREATE TABLE IF NOT EXISTS topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    public_id UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (owner_id, name)
);

CREATE INDEX IF NOT EXISTS idx_topics_owner_status ON topics (owner_id, status);
CREATE INDEX IF NOT EXISTS idx_topics_name ON topics (name);

INSERT INTO topics (owner_id, name, status)
SELECT DISTINCT l.created_by_id, l.topic, 'ACTIVE'
FROM links l
WHERE l.created_by_id IS NOT NULL
  AND l.status = 'ACTIVE'
ON CONFLICT (owner_id, name) DO NOTHING;
