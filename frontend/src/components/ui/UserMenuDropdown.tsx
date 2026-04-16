import { useEffect, useRef, useState } from 'react';
import { Button } from '@/components/ui/Button';

type UserMenuDropdownProps = {
  label: string;
  onLogout: () => void;
};

export function UserMenuDropdown({ label, onLogout }: UserMenuDropdownProps) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (!containerRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    }

    function handleEscape(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setOpen(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  return (
    <div ref={containerRef} className="relative">
      <Button
        type="button"
        variant="ghost"
        className="min-w-[160px] justify-between gap-2 normal-case"
        onClick={() => setOpen((prev) => !prev)}
        aria-haspopup="menu"
        aria-expanded={open}
      >
        <span className="max-w-[140px] truncate">{label}</span>
        <span aria-hidden="true">{open ? '▲' : '▼'}</span>
      </Button>

      {open && (
        <div
          className="absolute right-0 top-[calc(100%+0.5rem)] z-20 min-w-full border-4 border-black bg-[#fffef8] p-2 shadow-brutal-sm"
          role="menu"
        >
          <button
            type="button"
            className="w-full border-4 border-black bg-white px-4 py-2 text-left font-bold uppercase transition-transform hover:translate-x-0.5 hover:translate-y-0.5 hover:bg-neutral-100"
            onClick={() => {
              setOpen(false);
              onLogout();
            }}
            role="menuitem"
          >
            Logout
          </button>
        </div>
      )}
    </div>
  );
}
