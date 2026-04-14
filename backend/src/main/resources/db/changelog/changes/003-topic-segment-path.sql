--liquibase formatted sql

--changeset shortlink:003-topic-segment-path
--comment: URL path /r/{topic_segment}/{short_slug} — composite uniqueness, legacy rows use topic_segment = '_'
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*)::integer FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'links' AND column_name = 'topic_segment'

ALTER TABLE links ADD COLUMN topic_segment VARCHAR(100) NOT NULL DEFAULT '_';

ALTER TABLE links DROP CONSTRAINT IF EXISTS links_short_slug_key;

CREATE UNIQUE INDEX uq_links_topic_segment_short_slug ON links (topic_segment, short_slug);
