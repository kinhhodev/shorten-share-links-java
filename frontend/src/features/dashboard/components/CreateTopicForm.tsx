import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { getErrorMessage, topicsApi } from '@/services/api';

export function CreateTopicForm() {
  const queryClient = useQueryClient();
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: () =>
      topicsApi.createTopic({
        name,
        slug,
        description: description.trim() || undefined,
      }),
    onSuccess: () => {
      setName('');
      setSlug('');
      setDescription('');
      setError(null);
      void queryClient.invalidateQueries({ queryKey: ['topics'] });
    },
    onError: (e) => setError(getErrorMessage(e)),
  });

  return (
    <form
      className="grid gap-3 md:grid-cols-2"
      onSubmit={(e) => {
        e.preventDefault();
        setError(null);
        mutation.mutate();
      }}
    >
      <div className="md:col-span-2">
        <Label htmlFor="topic-name">Topic name</Label>
        <Input
          id="topic-name"
          required
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Marketing"
        />
      </div>
      <div>
        <Label htmlFor="topic-slug">Slug</Label>
        <Input
          id="topic-slug"
          required
          value={slug}
          onChange={(e) => setSlug(e.target.value.toLowerCase())}
          placeholder="marketing"
        />
      </div>
      <div>
        <Label htmlFor="topic-desc">Description (optional)</Label>
        <Input
          id="topic-desc"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Campaign links"
        />
      </div>
      {error && (
        <p className="md:col-span-2 border-2 border-black bg-[#fecaca] px-3 py-2 text-sm font-semibold">
          {error}
        </p>
      )}
      <div className="md:col-span-2">
        <Button type="submit" variant="secondary" disabled={mutation.isPending}>
          {mutation.isPending ? 'Creating…' : '+ Create topic'}
        </Button>
      </div>
    </form>
  );
}
