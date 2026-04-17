--liquibase formatted sql

--changeset shortlink:009-add-topic-shares-history
--comment: Add history table for snapshot-based topic sharing
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*)::integer FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'topic_shares'

CREATE TABLE topic_shares (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    source_topic VARCHAR(100) NOT NULL,
    recipient_topic VARCHAR(100) NOT NULL,
    shared_links_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_topic_shares_owner_created_at ON topic_shares (owner_id, created_at DESC);
CREATE INDEX idx_topic_shares_recipient_created_at ON topic_shares (recipient_id, created_at DESC);
