import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import { Card, CardTitle } from '@/components/ui/Card';
import { clearToken } from '@/lib/authStorage';
import { getErrorMessage, topicsApi } from '@/services/api';
import { CreateLinkForm } from '../components/CreateLinkForm';
import { CreateTopicForm } from '../components/CreateTopicForm';
import { TopicCard } from '../components/TopicCard';

export function DashboardPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const topicsQuery = useQuery({
    queryKey: ['topics'],
    queryFn: () => topicsApi.listTopics(),
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
          <p className="mt-1 text-sm font-bold text-neutral-800">Topics, links, and sharing.</p>
        </div>
        <Button type="button" variant="ghost" onClick={logout}>
          Log out
        </Button>
      </header>

      <div className="grid gap-8">
        <Card>
          <CardTitle className="mb-4">New topic</CardTitle>
          <CreateTopicForm />
        </Card>

        <Card>
          <CardTitle className="mb-4">New short link</CardTitle>
          <CreateLinkForm topics={topicsQuery.data ?? []} />
        </Card>

        <section>
          <h2 className="mb-4 font-display text-2xl uppercase">Your topics</h2>
          {topicsQuery.isLoading && (
            <p className="font-bold text-neutral-700">Loading topics…</p>
          )}
          {topicsQuery.error && (
            <p className="border-4 border-black bg-[#fecaca] p-4 font-semibold">
              {getErrorMessage(topicsQuery.error)}
            </p>
          )}
          {topicsQuery.data && topicsQuery.data.length === 0 && (
            <Card>
              <p className="font-semibold text-neutral-800">
                No topics yet. Create one above to group your links.
              </p>
            </Card>
          )}
          {topicsQuery.data && topicsQuery.data.length > 0 && (
            <div className="space-y-6">
              {topicsQuery.data.map((topic) => (
                <TopicCard key={topic.publicId} topic={topic} />
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
