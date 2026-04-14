import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { getErrorMessage, linksApi } from '@/services/api';

export function CreateLinkForm() {
  const queryClient = useQueryClient();
  const [topic, setTopic] = useState('');
  const [slug, setSlug] = useState('');
  const [originalUrl, setOriginalUrl] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [createdShortUrl, setCreatedShortUrl] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: () =>
      linksApi.createLink({
        ...(topic.trim() ? { topic: topic.trim() } : {}),
        slug,
        originalUrl,
      }),
    onSuccess: (data) => {
      setCreatedShortUrl(data.shortUrl);
      setSlug('');
      setOriginalUrl('');
      setError(null);
      void queryClient.invalidateQueries({ queryKey: ['links', 'mine'] });
    },
    onError: (e) => setError(getErrorMessage(e)),
  });

  return (
    <form
      className="flex w-full flex-col gap-3"
      onSubmit={(e) => {
        e.preventDefault();
        setError(null);
        setCreatedShortUrl(null);
        mutation.mutate();
      }}
    >
      <div className="w-full">
        <Label htmlFor="link-topic">Topic (optional)</Label>
        <Input
          id="link-topic"
          value={topic}
          onChange={(e) => setTopic(e.target.value)}
          placeholder="e.g. toeic — leave blank for _"
          autoComplete="off"
        />
        <p className="mt-1 text-xs font-semibold text-neutral-600">
          Chủ đề — segment đầu trong URL <span className="font-mono">/r/&lt;topic&gt;/&lt;slug&gt;</span>. Để trống =
          <span className="font-mono"> _</span>.
        </p>
      </div>
      <div className="w-full">
        <Label htmlFor="link-slug">Slug</Label>
        <Input
          id="link-slug"
          required
          minLength={3}
          maxLength={60}
          value={slug}
          onChange={(e) => setSlug(e.target.value)}
          placeholder="spring-sale"
        />
        <p className="mt-1 text-xs font-semibold text-neutral-600">
          3–60 ký tự. Trùng trong cùng topic → <span className="font-mono">name-1</span>, <span className="font-mono">name-2</span>, …
        </p>
      </div>
      <div className="w-full">
        <Label htmlFor="link-original-url">Destination URL</Label>
        <Input
          id="link-original-url"
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
      {createdShortUrl && (
        <div className="border-4 border-black bg-[#d1fae5] p-4">
          <p className="mb-2 text-sm font-bold uppercase text-neutral-800">Short URL</p>
          <p className="break-all font-mono text-sm font-semibold text-black">{createdShortUrl}</p>
          <Button
            type="button"
            variant="secondary"
            className="mt-3"
            onClick={() => void navigator.clipboard.writeText(createdShortUrl)}
          >
            Copy link
          </Button>
        </div>
      )}
      <div>
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Saving…' : 'Create short link'}
        </Button>
      </div>
    </form>
  );
}
