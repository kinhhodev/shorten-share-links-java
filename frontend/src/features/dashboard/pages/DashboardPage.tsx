import { useEffect, useMemo, useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { UserMenuDropdown } from '@/components/ui/UserMenuDropdown';
import { clearToken, getAuthUser, subscribeAuthUser } from '@/lib/authStorage';
import { getErrorMessage, linksApi, topicsApi } from '@/services/api';
import type { Link } from '@/services/api';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MAX_EMAIL_LENGTH = 320;

function isValidRecipientEmail(email: string): boolean {
  if (email.length > MAX_EMAIL_LENGTH) {
    return false;
  }
  return EMAIL_PATTERN.test(email);
}

function toSafeExternalUrl(url: string): string {
  try {
    const parsed = new URL(url);
    if (parsed.protocol === 'http:' || parsed.protocol === 'https:') {
      return parsed.toString();
    }
  } catch {
    // Fall back to a safe inert target for malformed/untrusted URLs.
  }
  return '#';
}

export function DashboardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const initialUser = getAuthUser();
  const [openTopics, setOpenTopics] = useState<Record<string, boolean>>({});
  const [searchTerm, setSearchTerm] = useState('');
  const [showCopyNotice, setShowCopyNotice] = useState(false);
  const [userLabel, setUserLabel] = useState(() => initialUser?.displayName ?? initialUser?.email ?? 'Account');
  const [viewMode, setViewMode] = useState<'active' | 'trash'>('active');
  const [openTrashTopics, setOpenTrashTopics] = useState<Record<string, boolean>>({});
  const [trashLinksByTopic, setTrashLinksByTopic] = useState<
    Record<string, { loading: boolean; error: string | null; links: Link[] }>
  >({});
  const copyNoticeTimerRef = useRef<number | null>(null);

  const linksQuery = useQuery({
    queryKey: ['links', 'mine'],
    queryFn: () => linksApi.listMine(),
    enabled: viewMode === 'active',
  });

  const deletedTopicsQuery = useQuery({
    queryKey: ['topics', 'deleted'],
    queryFn: () => topicsApi.listTopicsByStatus('DELETED'),
    enabled: viewMode === 'trash',
  });

  const filteredLinks = useMemo(() => {
    const q = searchTerm.trim().toLowerCase();
    if (!q) return linksQuery.data ?? [];
    return (linksQuery.data ?? []).filter((link) => {
      const topic = link.topic.toLowerCase();
      const slug = link.slug.toLowerCase();
      return topic.includes(q) || slug.includes(q);
    });
  }, [linksQuery.data, searchTerm]);

  const groupedLinks = useMemo(() => {
    const groups: Record<string, typeof linksQuery.data> = {};
    for (const link of filteredLinks) {
      const existing = groups[link.topic] ?? [];
      groups[link.topic] = [...existing, link];
    }
    return Object.entries(groups).map(([topic, links]) => ({ topic, links: links ?? [] }));
  }, [filteredLinks]);

  const filteredDeletedTopics = useMemo(() => {
    const q = searchTerm.trim().toLowerCase();
    if (!deletedTopicsQuery.data) {
      return [];
    }
    if (!q) {
      return deletedTopicsQuery.data;
    }
    return deletedTopicsQuery.data.filter((t) => t.name.toLowerCase().includes(q));
  }, [deletedTopicsQuery.data, searchTerm]);

  const deleteLink = useMutation({
    mutationFn: (linkPublicId: string) => linksApi.deleteLink(linkPublicId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['links', 'mine'] });
    },
  });

  const deleteTopic = useMutation({
    mutationFn: (topicName: string) => topicsApi.deleteTopic(topicName),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['links', 'mine'] });
      void queryClient.invalidateQueries({ queryKey: ['topics', 'deleted'] });
      setTrashLinksByTopic({});
      setOpenTrashTopics({});
    },
  });

  const restoreTopic = useMutation({
    mutationFn: (topicName: string) => topicsApi.restoreTopic(topicName),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['links', 'mine'] });
      void queryClient.invalidateQueries({ queryKey: ['topics', 'deleted'] });
      setTrashLinksByTopic({});
      setOpenTrashTopics({});
    },
  });

  const shareTopic = useMutation({
    mutationFn: ({ topicName, recipientEmail }: { topicName: string; recipientEmail: string }) =>
      topicsApi.shareTopic(topicName, { recipientEmail }),
  });

  function logout() {
    clearToken();
    void queryClient.clear();
    navigate('/', { replace: true });
  }

  function toggleTopic(topic: string) {
    setOpenTopics((prev) => ({ ...prev, [topic]: !prev[topic] }));
  }

  function onDeleteTopic(topic: string) {
    const accepted = window.confirm(
      `Delete topic "${topic}"? All links in this topic will be soft-deleted and can be restored later.`
    );
    if (!accepted) {
      return;
    }
    deleteTopic.mutate(topic);
  }

  function onRestoreTopic(topic: string) {
    const accepted = window.confirm(
      `Restore topic "${topic}"? All links in this topic will become active again if no slug conflicts.`
    );
    if (!accepted) return;
    restoreTopic.mutate(topic);
  }

  function onShareTopic(topic: string) {
    const email = window.prompt(`Share topic "${topic}" to email:`);
    if (!email) {
      return;
    }
    const recipientEmail = email.trim().toLowerCase();
    if (!recipientEmail) {
      return;
    }
    if (!isValidRecipientEmail(recipientEmail)) {
      window.alert('Please enter a valid recipient email address.');
      return;
    }
    shareTopic.mutate(
      { topicName: topic, recipientEmail },
      {
        onSuccess: (result) => {
          window.alert(
            `Shared ${result.sharedLinksCount} links to ${result.recipientEmail} (topic: ${result.topic}).`
          );
        },
        onError: (error) => {
          window.alert(getErrorMessage(error));
        },
      }
    );
  }

  function toggleTrashTopic(topicName: string) {
    setOpenTrashTopics((prev) => {
      const nextOpen = !prev[topicName];
      const cached = trashLinksByTopic[topicName];
      const shouldFetch = nextOpen && (!cached || cached.error);
      if (shouldFetch) {
        setTrashLinksByTopic((current) => ({
          ...current,
          [topicName]: { loading: true, error: null, links: cached?.links ?? [] },
        }));
        void topicsApi
          .listTopicLinksByStatus(topicName, 'DELETED')
          .then((links) => {
            setTrashLinksByTopic((current) => ({
              ...current,
              [topicName]: { loading: false, error: null, links },
            }));
          })
          .catch((error) => {
            setTrashLinksByTopic((current) => ({
              ...current,
              [topicName]: { loading: false, error: getErrorMessage(error), links: [] },
            }));
          });
      }
      return { ...prev, [topicName]: nextOpen };
    });
  }

  async function copyShortLink(shortUrl: string) {
    await navigator.clipboard.writeText(shortUrl);
    setShowCopyNotice(true);
    if (copyNoticeTimerRef.current) {
      window.clearTimeout(copyNoticeTimerRef.current);
    }
    copyNoticeTimerRef.current = window.setTimeout(() => {
      setShowCopyNotice(false);
      copyNoticeTimerRef.current = null;
    }, 2200);
  }

  useEffect(() => {
    const unsubscribe = subscribeAuthUser(() => {
      const user = getAuthUser();
      setUserLabel(user?.displayName ?? user?.email ?? 'Account');
    });

    return () => {
      unsubscribe();
      if (copyNoticeTimerRef.current) {
        window.clearTimeout(copyNoticeTimerRef.current);
      }
    };
  }, []);

  return (
    <div className="mx-auto max-w-4xl px-4 py-10">
      {showCopyNotice && (
        <div className="fixed right-4 top-4 z-50 border-4 border-black bg-[#d1fae5] px-4 py-3 text-sm font-bold uppercase shadow-brutal-sm">
          Your Link is copied
        </div>
      )}
      <header className="mb-10 flex flex-wrap items-center justify-between gap-4 border-b-4 border-black pb-6">
        <div>
          <h1 className="font-display text-3xl uppercase tracking-tight md:text-4xl">Dashboard</h1>
        </div>
        <div className="flex items-center gap-3">
          <Button type="button" onClick={() => navigate('/')}>
            Create Short Link
          </Button>
          <UserMenuDropdown label={userLabel} onLogout={logout} />
        </div>
      </header>

      <div className="grid gap-8">
        <section>
          <div className="mb-4 flex items-center justify-between gap-4">
            <h2 className="font-display text-2xl uppercase">
              {viewMode === 'active' ? 'Your links' : 'Trash'}
            </h2>
            <Button
              type="button"
              variant="ghost"
              onClick={() => setViewMode((prev) => (prev === 'active' ? 'trash' : 'active'))}
            >
              {viewMode === 'active' ? 'Trash' : 'Back'}
            </Button>
          </div>
          <div className="relative mb-4">
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Search by topic or slug..."
              className="w-full border-4 border-black bg-[#fffef8] px-4 py-3 pr-12 text-sm font-semibold text-black outline-none placeholder:text-neutral-500"
            />
            <span
              className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-black"
              aria-hidden="true"
            >
              <svg viewBox="0 0 24 24" className="h-5 w-5 fill-current">
                <path d="M10 2a8 8 0 1 0 5.3 14l5.35 5.35 1.4-1.4-5.35-5.35A8 8 0 0 0 10 2Zm0 2a6 6 0 1 1 0 12 6 6 0 0 1 0-12Z" />
              </svg>
            </span>
          </div>
          {viewMode === 'active' ? (
            <div className="space-y-4">
              {linksQuery.isLoading && <p className="font-bold text-neutral-700">Loading…</p>}
              {linksQuery.error && (
                <p className="border-4 border-black bg-[#fecaca] p-4 font-semibold">
                  {getErrorMessage(linksQuery.error)}
                </p>
              )}
              {linksQuery.data && linksQuery.data.length === 0 && (
                <Card>
                  <p className="font-semibold text-neutral-800">
                    No links yet. Use Create Short Link to add your first one.
                  </p>
                </Card>
              )}
              {linksQuery.data && linksQuery.data.length > 0 && groupedLinks.length === 0 && (
                <Card>
                  <p className="font-semibold text-neutral-800">No matching links found.</p>
                </Card>
              )}
              {groupedLinks.length > 0 && (
                <div className="space-y-4">
              {groupedLinks.map((group) => (
                <Card key={group.topic}>
                  <div className="flex w-full items-center justify-between border-4 border-black bg-white px-4 py-3 text-left font-display text-xl uppercase">
                    <button type="button" className="text-left" onClick={() => toggleTopic(group.topic)}>
                      {group.topic}
                    </button>
                    <span className="flex items-center gap-3">
                      <button
                        type="button"
                        className="inline-flex h-8 items-center justify-center border-4 border-black bg-[#79BAEC] px-3 text-base text-black hover:bg-[#82CAFF] disabled:pointer-events-none disabled:opacity-50"
                        disabled={shareTopic.isPending}
                        onClick={() => onShareTopic(group.topic)}
                      >
                        Share
                      </button>
                      <button
                        type="button"
                        className="inline-flex h-8 items-center justify-center border-4 border-black bg-[#fda4af] px-3 text-base text-black hover:bg-[#fecdd3] disabled:pointer-events-none disabled:opacity-50"
                        onClick={() => onDeleteTopic(group.topic)}
                        disabled={deleteTopic.isPending}
                        aria-label={`Delete topic ${group.topic}`}
                        title="Delete topic"
                      >
                        Delete
                      </button>
                      <button type="button" className="text-base" onClick={() => toggleTopic(group.topic)} aria-label="Toggle topic">
                        {openTopics[group.topic] ? '▲' : '▼'}
                      </button>
                    </span>
                  </div>

                  {(searchTerm.trim() ? true : openTopics[group.topic]) && (
                    <div className="mt-4 space-y-3">
                      {group.links.map((link) => (
                        <Card key={link.publicId}>
                          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                            <div className="min-w-0 flex-1">
                              <a
                                href={toSafeExternalUrl(link.shortUrl)}
                                className="break-all text-sm font-semibold text-black underline"
                                target="_blank"
                                rel="noopener noreferrer"
                              >
                                {link.shortUrl}
                              </a>
                            </div>
                            <div className="flex items-center gap-2">
                              <Button
                                type="button"
                                variant="ghost"
                                className="!px-3"
                                onClick={() => void copyShortLink(link.shortUrl)}
                                aria-label="Copy short link"
                                title="Copy short link"
                              >
                                <svg viewBox="0 0 24 24" className="h-5 w-5 fill-current" aria-hidden="true">
                                  <path d="M16 1H6a2 2 0 0 0-2 2v12h2V3h10V1Zm3 4H10a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h9a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2Zm0 16H10V7h9v14Z" />
                                </svg>
                              </Button>
                              <Button
                                type="button"
                                variant="danger"
                                className="!px-3"
                                disabled={deleteLink.isPending}
                                onClick={() => deleteLink.mutate(link.publicId)}
                                aria-label="Delete short link"
                                title="Delete short link"
                              >
                                Delete
                              </Button>
                            </div>
                          </div>
                        </Card>
                      ))}
                    </div>
                  )}
                </Card>
              ))}
            </div>
              )}
            </div>
          ) : (
            <div className="space-y-4">
              {deletedTopicsQuery.isLoading && <p className="font-bold text-neutral-700">Loading…</p>}
              {deletedTopicsQuery.error && (
                <p className="border-4 border-black bg-[#fecaca] p-4 font-semibold">
                  {getErrorMessage(deletedTopicsQuery.error)}
                </p>
              )}
              {deletedTopicsQuery.data && deletedTopicsQuery.data.length === 0 && (
                <Card>
                  <p className="font-semibold text-neutral-800">No deleted topics.</p>
                </Card>
              )}
              {deletedTopicsQuery.data && deletedTopicsQuery.data.length > 0 && (
                <div className="space-y-3">
                  {filteredDeletedTopics.map((t) => (
                      <Card key={t.name}>
                        <div className="flex items-center justify-between gap-3 border-4 border-black bg-white px-4 py-3">
                          <button
                            type="button"
                            className="font-display text-xl"
                            onClick={() => toggleTrashTopic(t.name)}
                          >
                            {t.name}
                          </button>
                          <div className="flex items-center gap-3">
                            <Button
                              type="button"
                              variant="secondary"
                              disabled={restoreTopic.isPending}
                              onClick={() => onRestoreTopic(t.name)}
                            >
                              Restore
                            </Button>
                            <button
                              type="button"
                              className="text-base"
                              onClick={() => toggleTrashTopic(t.name)}
                              aria-label={`Toggle deleted links for ${t.name}`}
                            >
                              {openTrashTopics[t.name] ? '▲' : '▼'}
                            </button>
                          </div>
                        </div>
                        {openTrashTopics[t.name] && (
                          <div className="mt-4 space-y-3">
                            {trashLinksByTopic[t.name]?.loading && (
                              <p className="font-bold text-neutral-700">Loading links…</p>
                            )}
                            {trashLinksByTopic[t.name]?.error && (
                              <p className="border-4 border-black bg-[#fecaca] p-3 font-semibold">
                                {trashLinksByTopic[t.name]?.error}
                              </p>
                            )}
                            {!trashLinksByTopic[t.name]?.loading &&
                              !trashLinksByTopic[t.name]?.error &&
                              (trashLinksByTopic[t.name]?.links.length ?? 0) === 0 && (
                                <p className="font-semibold text-neutral-700">No deleted links in this topic.</p>
                              )}
                            {(trashLinksByTopic[t.name]?.links ?? []).map((link) => (
                              <Card key={link.publicId}>
                                <div className="space-y-1">
                                  <a
                                    href={toSafeExternalUrl(link.shortUrl)}
                                    className="break-all text-sm font-semibold text-black underline"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                  >
                                    {link.shortUrl}
                                  </a>
                                </div>
                              </Card>
                            ))}
                          </div>
                        )}
                      </Card>
                    ))}
                </div>
              )}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
