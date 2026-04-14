--liquibase formatted sql

--changeset shortlink:005-links-topic-column
--comment: Rename links.topic_segment -> topic; unique index uq_links_topic_slug
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*)::integer FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'links' AND column_name = 'topic_segment'

ALTER TABLE links RENAME COLUMN topic_segment TO topic;

DROP INDEX IF EXISTS uq_links_topic_segment_slug;

CREATE UNIQUE INDEX uq_links_topic_slug ON links (topic, slug);
