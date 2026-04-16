--liquibase formatted sql

--changeset shortlink:008-rename-links-created-by-to-user-id
--comment: Rename links.created_by_id to links.user_id
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*)::integer FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'links' AND column_name = 'created_by_id'

ALTER TABLE links RENAME COLUMN created_by_id TO user_id;

ALTER INDEX IF EXISTS idx_links_created_topic_status RENAME TO idx_links_user_topic_status;
