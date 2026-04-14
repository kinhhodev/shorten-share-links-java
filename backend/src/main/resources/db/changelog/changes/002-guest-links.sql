--liquibase formatted sql

--changeset shortlink:2
--comment: Guest links — nullable topic/user, expiry, integrity check
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*)::integer FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'links' AND column_name = 'is_guest'
--rollback ALTER TABLE links DROP CONSTRAINT IF EXISTS chk_links_owner_or_guest;
--rollback DROP INDEX IF EXISTS idx_links_guest_expire;
--rollback ALTER TABLE links DROP COLUMN IF EXISTS expire_at;
--rollback ALTER TABLE links DROP COLUMN IF EXISTS is_guest;
--rollback ALTER TABLE links ALTER COLUMN created_by_id SET NOT NULL;
--rollback ALTER TABLE links ALTER COLUMN topic_id SET NOT NULL;

ALTER TABLE links ADD COLUMN is_guest BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE links ADD COLUMN expire_at TIMESTAMPTZ NULL;

ALTER TABLE links ALTER COLUMN topic_id DROP NOT NULL;
ALTER TABLE links ALTER COLUMN created_by_id DROP NOT NULL;

ALTER TABLE links ADD CONSTRAINT chk_links_owner_or_guest CHECK (
    (is_guest = FALSE AND topic_id IS NOT NULL AND created_by_id IS NOT NULL)
    OR (is_guest = TRUE AND created_by_id IS NULL AND expire_at IS NOT NULL)
);

CREATE INDEX idx_links_guest_expire ON links (expire_at) WHERE is_guest = TRUE;
