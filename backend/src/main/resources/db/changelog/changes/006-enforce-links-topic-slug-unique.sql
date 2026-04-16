--liquibase formatted sql

--changeset shortlink:006-enforce-links-topic-slug-unique
--comment: Keep /r/{topic}/{slug} globally unique by enforcing unique index on (topic, slug)
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*)::integer FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'links' AND column_name = 'topic'
--precondition-sql-check expectedResult:1 SELECT COUNT(*)::integer FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'links' AND column_name = 'slug'

-- Remove legacy unique constraints/indexes that enforce slug-only uniqueness.
ALTER TABLE links DROP CONSTRAINT IF EXISTS links_short_slug_key;
ALTER TABLE links DROP CONSTRAINT IF EXISTS links_slug_key;

-- Remove old composite index names if any left from previous migrations.
DROP INDEX IF EXISTS uq_links_topic_segment_short_slug;
DROP INDEX IF EXISTS uq_links_topic_segment_slug;

-- Enforce current global uniqueness contract for public redirects /r/{topic}/{slug}.
CREATE UNIQUE INDEX IF NOT EXISTS uq_links_topic_slug ON links (topic, slug);
