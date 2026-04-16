import clsx from 'clsx';
import { type ReactNode, useEffect } from 'react';
import { Button } from './Button';

type ModalProps = {
  open: boolean;
  title?: string;
  onClose: () => void;
  children: ReactNode;
  className?: string;
};

export function Modal({ open, title, onClose, children, className }: ModalProps) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby={title ? 'modal-title' : undefined}
    >
      <button
        type="button"
        className="absolute inset-0 bg-black/40"
        aria-label="Close modal"
        onClick={onClose}
      />
      <div
        className={clsx(
          'relative z-10 w-full max-w-lg border-4 border-black bg-[#fffef8] p-6 shadow-brutal',
          className
        )}
      >
        <div
          className={clsx(
            'flex items-start gap-4',
            title ? 'mb-4 justify-between' : 'mb-4 justify-end'
          )}
        >
          {title ? (
            <h2 id="modal-title" className="font-display text-xl uppercase">
              {title}
            </h2>
          ) : null}
          <Button type="button" variant="ghost" className="!px-2 !py-1 text-sm" onClick={onClose}>
            ✕
          </Button>
        </div>
        {children}
      </div>
    </div>
  );
}
