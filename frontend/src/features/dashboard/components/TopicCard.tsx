import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { Label } from '@/components/ui/Label';
import { Select } from '@/components/ui/Select';
import type { Topic, TopicPermission } from '@/services/api/types';
import { getErrorMessage, linksApi, topicsApi } from '@/services/api';

type Props = {
  topic: Topic;
};

export function TopicCard({ topic }: Props) {
  const queryClient = useQueryClient();
  const [expanded, setExpanded] = useState(true);
  const [shareEmail, setShareEmail] = useState('');
  const [sharePermission, setSharePermission] = useState<TopicPermission>('VIEW');
  const [shareError, setShareError] = useState<string | null>(null);

  const linksQuery = useQuery({
    queryKey: ['links', topic.publicId],
    queryFn: () => linksApi.listByTopic(topic.publicId),
    enabled: expanded,
  });

  const sharesQuery = useQuery({
    queryKey: ['topic-shares', topic.publicId],
    queryFn: () => topicsApi.listShares(topic.publicId),
    enabled: expanded,
    retry: false,
  });

  const deleteLink = useMutation({
    mutationFn: (linkPublicId: string) => linksApi.deleteLink(linkPublicId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['links', topic.publicId] });
    },
  });

  const deleteTopic = useMutation({
    mutationFn: () => topicsApi.deleteTopic(topic.publicId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['topics'] });
    },
  });

  const shareMutation = useMutation({
    mutationFn: () =>
      topicsApi.shareTopic(topic.publicId, {
        userEmail: shareEmail.trim(),
        permission: sharePermission,
      }),
    onSuccess: () => {
      setShareEmail('');
      setShareError(null);
      void queryClient.invalidateQueries({ queryKey: ['topic-shares', topic.publicId] });
    },
    onError: (e) => setShareError(getErrorMessage(e)),
  });

  return (
    <Card className="space-y-4">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <h3 className="font-display text-lg uppercase">{topic.name}</h3>
            <Badge>{topic.slug}</Badge>
          </div>
          {topic.description && (
            <p className="mt-1 text-sm font-medium text-neutral-800">{topic.description}</p>
          )}
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            type="button"
            variant="ghost"
            className="!text-xs"
            onClick={() => setExpanded((v) => !v)}
          >
            {expanded ? 'Collapse' : 'Expand'}
          </Button>
          <Button
            type="button"
            variant="danger"
            className="!text-xs"
            disabled={deleteTopic.isPending}
            onClick={() => {
              if (
                window.confirm(
                  `Delete topic "${topic.name}" and all its links? This cannot be undone.`
                )
              ) {
                deleteTopic.mutate();
              }
            }}
          >
            Delete topic
          </Button>
        </div>
      </div>

      {expanded && (
        <>
          <div>
            <h4 className="mb-2 text-sm font-black uppercase">Links in this topic</h4>
            {linksQuery.isLoading && (
              <p className="text-sm font-semibold text-neutral-600">Loading links…</p>
            )}
            {linksQuery.error && (
              <p className="border-2 border-black bg-[#fecaca] px-3 py-2 text-sm">
                {getErrorMessage(linksQuery.error)}
              </p>
            )}
            {linksQuery.data && linksQuery.data.length === 0 && (
              <p className="text-sm font-semibold text-neutral-600">No links yet.</p>
            )}
            {linksQuery.data && linksQuery.data.length > 0 && (
              <ul className="space-y-2">
                {linksQuery.data.map((link) => (
                  <li
                    key={link.publicId}
                    className="flex flex-col gap-2 border-4 border-black bg-[#f5f0e6] p-3 sm:flex-row sm:items-center sm:justify-between"
                  >
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-bold text-black">
                        <a
                          href={link.shortUrl}
                          target="_blank"
                          rel="noreferrer"
                          className="underline decoration-2 underline-offset-2"
                        >
                          {link.shortUrl}
                        </a>
                      </p>
                      <p className="truncate text-xs font-medium text-neutral-700" title={link.originalUrl}>
                        → {link.originalUrl}
                      </p>
                    </div>
                    <Button
                      type="button"
                      variant="danger"
                      className="shrink-0 !text-xs"
                      disabled={deleteLink.isPending}
                      onClick={() => {
                        if (window.confirm('Delete this short link?')) {
                          deleteLink.mutate(link.publicId);
                        }
                      }}
                    >
                      Delete
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="border-t-4 border-black pt-4">
            <h4 className="mb-2 text-sm font-black uppercase">Share topic</h4>
            {sharesQuery.isError && (
              <p className="mb-2 text-sm font-semibold text-neutral-700">
                {getErrorMessage(sharesQuery.error)} (only the owner can list shares or invite.)
              </p>
            )}
            {sharesQuery.data && sharesQuery.data.length > 0 && (
              <ul className="mb-3 space-y-1 text-sm font-semibold">
                {sharesQuery.data.map((s) => (
                  <li key={s.shareId}>
                    {s.userEmail}{' '}
                    <span className="text-neutral-600">({s.permission})</span>
                  </li>
                ))}
              </ul>
            )}
            <form
              className="flex flex-col gap-2 sm:flex-row sm:flex-wrap sm:items-end"
              onSubmit={(e) => {
                e.preventDefault();
                setShareError(null);
                shareMutation.mutate();
              }}
            >
              <div className="min-w-[200px] flex-1">
                <Label htmlFor={`share-email-${topic.publicId}`}>User email</Label>
                <Input
                  id={`share-email-${topic.publicId}`}
                  type="email"
                  required
                  value={shareEmail}
                  onChange={(e) => setShareEmail(e.target.value)}
                  placeholder="collaborator@company.com"
                />
              </div>
              <div className="w-full sm:w-40">
                <Label htmlFor={`share-perm-${topic.publicId}`}>Permission</Label>
                <Select
                  id={`share-perm-${topic.publicId}`}
                  value={sharePermission}
                  onChange={(e) => setSharePermission(e.target.value as TopicPermission)}
                >
                  <option value="VIEW">View</option>
                  <option value="EDIT">Edit</option>
                </Select>
              </div>
              <Button type="submit" variant="secondary" disabled={shareMutation.isPending}>
                {shareMutation.isPending ? 'Sharing…' : 'Share'}
              </Button>
            </form>
            {shareError && (
              <p className="mt-2 border-2 border-black bg-[#fecaca] px-3 py-2 text-sm">{shareError}</p>
            )}
          </div>
        </>
      )}
    </Card>
  );
}
