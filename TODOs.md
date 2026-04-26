# TODOs / Future Work

## Topic restore conflict rules

- Soft delete for topics and links is implemented via `TopicStatus.DELETED` and `LinkStatus.DELETED`.
- When restoring a topic, the system uses **strategy A** for slug conflicts:
  - For each deleted link in the topic, it checks whether an **ACTIVE** link with the same `(topic, slug)` already exists.
  - If any conflict is found, the restore operation is aborted and the backend returns:
    - HTTP status: `409 CONFLICT`
    - error code: `topic_restore_conflict`
    - message: `"Cannot restore topic because some slugs are already used by active links."`
- Frontend should surface this conflict clearly to the user (e.g., toast or error banner) and not attempt automatic slug renaming.

## Open ideas

- Add a dedicated "Restore" screen with details of conflicting slugs and suggestions.
- Provide per-link restore for deleted links inside a topic.

## Login by Google social
