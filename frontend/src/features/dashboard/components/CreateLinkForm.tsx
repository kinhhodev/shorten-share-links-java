import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select } from '@/components/ui/Select';
import type { Topic } from '@/services/api/types';
import { getErrorMessage, linksApi } from '@/services/api';

type Props = {
  topics: Topic[];
};

export function CreateLinkForm({ topics }: Props) {
  const queryClient = useQueryClient();
  const [topicPublicId, setTopicPublicId] = useState('');
  const [shortSlug, setShortSlug] = useState('');
  const [originalUrl, setOriginalUrl] = useState('');
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: () =>
      linksApi.createLink({
        topicPublicId,
        shortSlug,
        originalUrl,
      }),
    onSuccess: () => {
      setShortSlug('');
      setOriginalUrl('');
      setError(null);
      void queryClient.invalidateQueries({ queryKey: ['links', topicPublicId] });
      void queryClient.invalidateQueries({ queryKey: ['topics'] });
    },
    onError: (e) => setError(getErrorMessage(e)),
  });

  if (topics.length === 0) {
    return (
      <p className="text-sm font-bold text-neutral-700">
        Create a topic first, then you can add short links.
      </p>
    );
  }

  return (
    <form
      className="grid gap-3 md:grid-cols-2"
      onSubmit={(e) => {
        e.preventDefault();
        if (!topicPublicId) {
          setError('Pick a topic');
          return;
        }
        setError(null);
        mutation.mutate();
      }}
    >
      <div className="md:col-span-2">
        <Label htmlFor="link-topic">Topic</Label>
        <Select
          id="link-topic"
          required
          value={topicPublicId}
          onChange={(e) => setTopicPublicId(e.target.value)}
        >
          <option value="">Select topic…</option>
          {topics.map((t) => (
            <option key={t.publicId} value={t.publicId}>
              {t.name} ({t.slug})
            </option>
          ))}
        </Select>
      </div>
      <div>
        <Label htmlFor="short-slug">Short slug</Label>
        <Input
          id="short-slug"
          required
          value={shortSlug}
          onChange={(e) => setShortSlug(e.target.value)}
          placeholder="spring-sale"
        />
      </div>
      <div>
        <Label htmlFor="original-url">Destination URL</Label>
        <Input
          id="original-url"
          type="url"
          required
          value={originalUrl}
          onChange={(e) => setOriginalUrl(e.target.value)}
          placeholder="https://example.com/page"
        />
      </div>
      {error && (
        <p className="md:col-span-2 border-2 border-black bg-[#fecaca] px-3 py-2 text-sm font-semibold">
          {error}
        </p>
      )}
      <div className="md:col-span-2">
        <Button type="submit" disabled={mutation.isPending}>
          {mutation.isPending ? 'Saving…' : '+ Create short link'}
        </Button>
      </div>
    </form>
  );
}
