--liquibase formatted sql

--changeset shortlink:004-remove-topics-rename-slug
--comment: Drop topic_shares and topics; links use topic_segment string only; rename short_slug -> slug
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*)::integer FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'links' AND column_name = 'slug'

DROP TABLE IF EXISTS topic_shares CASCADE;

UPDATE links l
SET topic_segment = lower(trim(t.slug))
FROM topics t
WHERE l.topic_id IS NOT NULL AND l.topic_id = t.id;

ALTER TABLE links DROP CONSTRAINT IF EXISTS links_topic_id_fkey;

ALTER TABLE links DROP COLUMN IF EXISTS topic_id;

DROP TABLE IF EXISTS topics CASCADE;

ALTER TABLE links RENAME COLUMN short_slug TO slug;

DROP INDEX IF EXISTS uq_links_topic_segment_short_slug;

CREATE UNIQUE INDEX uq_links_topic_segment_slug ON links (topic_segment, slug);

DROP INDEX IF EXISTS idx_links_slug;

CREATE INDEX idx_links_slug ON links (slug);
