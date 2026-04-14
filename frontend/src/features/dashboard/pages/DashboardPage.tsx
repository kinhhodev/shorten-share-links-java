import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import { Card, CardTitle } from '@/components/ui/Card';
import { clearToken } from '@/lib/authStorage';
import { getErrorMessage, linksApi } from '@/services/api';
import { CreateLinkForm } from '../components/CreateLinkForm';

export function DashboardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const linksQuery = useQuery({
    queryKey: ['links', 'mine'],
    queryFn: () => linksApi.listMine(),
  });

  const deleteLink = useMutation({
    mutationFn: (linkPublicId: string) => linksApi.deleteLink(linkPublicId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['links', 'mine'] });
    },
  });

  function logout() {
    clearToken();
    void queryClient.clear();
    navigate('/login', { replace: true });
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-10">
      <header className="mb-10 flex flex-wrap items-center justify-between gap-4 border-b-4 border-black pb-6">
        <div>
          <h1 className="font-display text-3xl uppercase tracking-tight md:text-4xl">Dashboard</h1>
          <p className="mt-1 text-sm font-bold text-neutral-800">Your short links.</p>
        </div>
        <Button type="button" variant="ghost" onClick={logout}>
          Log out
        </Button>
      </header>

      <div className="grid gap-8">
        <Card>
          <CardTitle className="mb-4">New short link</CardTitle>
          <CreateLinkForm />
        </Card>

        <section>
          <h2 className="mb-4 font-display text-2xl uppercase">Your links</h2>
          {linksQuery.isLoading && <p className="font-bold text-neutral-700">Loading…</p>}
          {linksQuery.error && (
            <p className="border-4 border-black bg-[#fecaca] p-4 font-semibold">
              {getErrorMessage(linksQuery.error)}
            </p>
          )}
          {linksQuery.data && linksQuery.data.length === 0 && (
            <Card>
              <p className="font-semibold text-neutral-800">No links yet. Create one above.</p>
            </Card>
          )}
          {linksQuery.data && linksQuery.data.length > 0 && (
            <div className="space-y-4">
              {linksQuery.data.map((link) => (
                <Card key={link.publicId}>
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div className="min-w-0 flex-1">
                      <p className="font-mono text-sm font-bold text-neutral-600">
                        /r/{link.topic}/{link.slug}
                      </p>
                      <a
                        href={link.shortUrl}
                        className="break-all text-sm font-semibold text-black underline"
                        target="_blank"
                        rel="noreferrer"
                      >
                        {link.shortUrl}
                      </a>
                      <p className="mt-2 text-sm text-neutral-800">→ {link.originalUrl}</p>
                    </div>
                    <Button
                      type="button"
                      variant="danger"
                      disabled={deleteLink.isPending}
                      onClick={() => deleteLink.mutate(link.publicId)}
                    >
                      Delete
                    </Button>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
