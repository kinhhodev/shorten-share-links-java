import clsx from 'clsx';
import { type HTMLAttributes } from 'react';

export function Badge({ className, ...props }: HTMLAttributes<HTMLSpanElement>) {
  return (
    <span
      className={clsx(
        'inline-block border-2 border-black bg-[#c4b5fd] px-2 py-0.5 text-xs font-bold uppercase',
        className
      )}
      {...props}
    />
  );
}
