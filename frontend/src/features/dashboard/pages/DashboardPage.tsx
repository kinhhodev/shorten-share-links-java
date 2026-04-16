import { useEffect, useMemo, useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { clearToken } from '@/lib/authStorage';
import { getErrorMessage, linksApi } from '@/services/api';

export function DashboardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [openTopics, setOpenTopics] = useState<Record<string, boolean>>({});
  const [searchTerm, setSearchTerm] = useState('');
  const [showCopyNotice, setShowCopyNotice] = useState(false);
  const copyNoticeTimerRef = useRef<number | null>(null);

  const linksQuery = useQuery({
    queryKey: ['links', 'mine'],
    queryFn: () => linksApi.listMine(),
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

  const deleteLink = useMutation({
    mutationFn: (linkPublicId: string) => linksApi.deleteLink(linkPublicId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['links', 'mine'] });
    },
  });

  function logout() {
    clearToken();
    void queryClient.clear();
    navigate('/', { replace: true });
  }

  function toggleTopic(topic: string) {
    setOpenTopics((prev) => ({ ...prev, [topic]: !prev[topic] }));
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
    return () => {
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
          <Button type="button" variant="ghost" onClick={logout}>
            Log out
          </Button>
        </div>
      </header>

      <div className="grid gap-8">
        <section>
          <h2 className="mb-4 font-display text-2xl uppercase">Your links</h2>
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
          {linksQuery.isLoading && <p className="font-bold text-neutral-700">Loading…</p>}
          {linksQuery.error && (
            <p className="border-4 border-black bg-[#fecaca] p-4 font-semibold">
              {getErrorMessage(linksQuery.error)}
            </p>
          )}
          {linksQuery.data && linksQuery.data.length === 0 && (
            <Card>
              <p className="font-semibold text-neutral-800">No links yet. Use Create Short Link to add your first one.</p>
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
                  <button
                    type="button"
                    className="flex w-full items-center justify-between border-4 border-black bg-white px-4 py-3 text-left font-display text-xl uppercase"
                    onClick={() => toggleTopic(group.topic)}
                  >
                    <span>{group.topic}</span>
                    <span className="text-base">{openTopics[group.topic] ? '▲' : '▼'}</span>
                  </button>

                  {(searchTerm.trim() ? true : openTopics[group.topic]) && (
                    <div className="mt-4 space-y-3">
                      {group.links.map((link) => (
                        <Card key={link.publicId}>
                          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                            <div className="min-w-0 flex-1">
                              <a
                                href={link.shortUrl}
                                className="break-all text-sm font-semibold text-black underline"
                                target="_blank"
                                rel="noreferrer"
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
        </section>
      </div>
    </div>
  );
}
