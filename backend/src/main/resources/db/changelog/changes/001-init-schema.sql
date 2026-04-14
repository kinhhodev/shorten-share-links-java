--liquibase formatted sql

--changeset shortlink:1
--comment: Initial schema (users, topics, links, topic_shares)
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*)::integer FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'users'
--rollback DROP TABLE IF EXISTS topic_shares CASCADE;
--rollback DROP TABLE IF EXISTS links CASCADE;
--rollback DROP TABLE IF EXISTS topics CASCADE;
--rollback DROP TABLE IF EXISTS users CASCADE;
--rollback DROP EXTENSION IF EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    public_id UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(200),
    auth_provider VARCHAR(32) NOT NULL DEFAULT 'LOCAL',
    provider_subject VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_local_password CHECK (
        (auth_provider = 'LOCAL' AND password_hash IS NOT NULL)
        OR (auth_provider <> 'LOCAL')
    )
);

CREATE INDEX idx_users_provider_subject ON users (auth_provider, provider_subject)
    WHERE provider_subject IS NOT NULL;

CREATE TABLE topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    public_id UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    owner_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (owner_id, slug)
);

CREATE INDEX idx_topics_slug ON topics (slug);

CREATE TABLE links (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    public_id UUID NOT NULL UNIQUE DEFAULT uuid_generate_v4(),
    short_slug VARCHAR(64) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    topic_id UUID NOT NULL REFERENCES topics (id) ON DELETE CASCADE,
    created_by_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    click_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_links_topic ON links (topic_id);
CREATE INDEX idx_links_slug ON links (short_slug);

CREATE TABLE topic_shares (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic_id UUID NOT NULL REFERENCES topics (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    permission VARCHAR(32) NOT NULL DEFAULT 'VIEW',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (topic_id, user_id)
);

CREATE INDEX idx_topic_shares_user ON topic_shares (user_id);
