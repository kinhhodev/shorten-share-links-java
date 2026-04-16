import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useEffect, useRef, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { getErrorMessage, linksApi, publicLinksApi } from '@/services/api';

const TOPIC_SEGMENT_PATTERN = /^[a-zA-Z0-9_-]{1,100}$/;
const SLUG_PATTERN = /^[a-zA-Z0-9_-]+$/;

type CreateLinkFormProps = {
  authenticated?: boolean;
};

export function CreateLinkForm({ authenticated = false }: CreateLinkFormProps) {
  const queryClient = useQueryClient();
  const [topic, setTopic] = useState('');
  const [slug, setSlug] = useState('');
  const [originalUrl, setOriginalUrl] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [createdUrl, setCreatedUrl] = useState<string | null>(null);
  const [showCopyNotice, setShowCopyNotice] = useState(false);
  const copyNoticeTimerRef = useRef<number | null>(null);

  const mutation = useMutation({
    mutationFn: async () => {
      const body = {
        originalUrl,
        slug: slug.trim(),
        ...(topic.trim() ? { topic: topic.trim() } : {}),
      };
      if (authenticated) {
        return linksApi.createLink(body);
      }
      return publicLinksApi.createGuestLink(body);
    },
    onSuccess: (data) => {
      setCreatedUrl(data.shortUrl);
      setError(null);
      setTopic('');
      setSlug('');
      setOriginalUrl('');
      if (authenticated) {
        void queryClient.invalidateQueries({ queryKey: ['links', 'mine'] });
      }
    },
    onError: (e) => {
      setError(getErrorMessage(e));
      setCreatedUrl(null);
    },
  });

  function validate(): boolean {
    const t = topic.trim();
    if (t && !TOPIC_SEGMENT_PATTERN.test(t)) {
      setError(
        'Topic must be 1–100 characters (letters, digits, _, -), or leave blank for default "_".',
      );
      return false;
    }
    const s = slug.trim();
    if (!s) {
      setError('Slug is required.');
      return false;
    }
    if (s.length < 3 || s.length > 60) {
      setError('Slug must be between 3 and 60 characters.');
      return false;
    }
    if (!SLUG_PATTERN.test(s)) {
      setError('Slug may only contain letters, digits, underscore, and hyphen.');
      return false;
    }
    try {
      const u = new URL(originalUrl.trim());
      if (u.protocol !== 'http:' && u.protocol !== 'https:') {
        setError('Destination URL must start with http:// or https://');
        return false;
      }
    } catch {
      setError('Enter a valid destination URL.');
      return false;
    }
    return true;
  }

  async function copyShortLink(url: string) {
    await navigator.clipboard.writeText(url);
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
    <div className="space-y-4">
      {showCopyNotice && (
        <div className="fixed right-4 top-4 z-50 border-4 border-black bg-[#d1fae5] px-4 py-3 text-sm font-bold uppercase shadow-brutal-sm">
          Your Link is copied
        </div>
      )}
      <form
        className="flex w-full flex-col gap-3"
        onSubmit={(e) => {
          e.preventDefault();
          setError(null);
          setCreatedUrl(null);
          if (!validate()) return;
          mutation.mutate();
        }}
      >
        <div className="w-full">
          <Label htmlFor="guest-topic">Topic (optional)</Label>
          <Input
            id="guest-topic"
            type="text"
            value={topic}
            onChange={(e) => setTopic(e.target.value)}
            placeholder="e.g. toeic (leave blank for _)"
            autoComplete="off"
          />
          <p className="mt-1 text-xs font-semibold text-neutral-600">
            Chu de - phan dau trong URL <span className="font-mono">/r/&lt;topic&gt;/&lt;slug&gt;</span>. De trong dung
            mac dinh <span className="font-mono">_</span>.
          </p>
        </div>
        <div className="w-full">
          <Label htmlFor="guest-short-slug">Slug</Label>
          <Input
            id="guest-short-slug"
            required
            minLength={3}
            maxLength={60}
            value={slug}
            onChange={(e) => setSlug(e.target.value)}
            placeholder="thanh-ngu"
            autoComplete="off"
          />
          <p className="mt-1 text-xs font-semibold text-neutral-600">
            Ten goi nho (3-60 ky tu). Neu trung trong cung topic, he thong co the gan{' '}
            <span className="font-mono">name-1</span>, <span className="font-mono">name-2</span>, ...
          </p>
        </div>
        <div className="w-full">
          <Label htmlFor="guest-original-url">Destination URL</Label>
          <Input
            id="guest-original-url"
            type="url"
            required
            value={originalUrl}
            onChange={(e) => setOriginalUrl(e.target.value)}
            placeholder="https://example.com/page"
          />
        </div>
        {error && (
          <p className="border-2 border-black bg-[#fecaca] px-3 py-2 text-sm font-semibold">{error}</p>
        )}
        <div className="w-full">
          <Button type="submit" className="w-full" disabled={mutation.isPending}>
            {mutation.isPending ? 'Saving…' : 'Create short link'}
          </Button>
        </div>
      </form>

      {createdUrl && (
        <div className="border-4 border-black bg-[#d1fae5] p-4">
          <p className="mb-2 text-sm font-bold uppercase text-neutral-800">Your short link</p>
          <p className="break-all font-mono text-sm font-semibold text-black">{createdUrl}</p>
          <Button
            type="button"
            variant="secondary"
            className="mt-3"
            onClick={() => void copyShortLink(createdUrl)}
          >
            Copy link
          </Button>

          {!authenticated && createdUrl && (
            <p className="mt-3 text-sm font-semibold text-red-700">
              Note: Your link will expire tomorrow.
            </p>
          )}
        </div>
      )}

      {!authenticated && (
        <p className="text-sm font-semibold text-neutral-700">
          Guest links expire after a limited time. Log in to manage topics and long-lived links on your dashboard.
        </p>
      )}
    </div>
  );
}
