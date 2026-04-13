import clsx from 'clsx';
import { type HTMLAttributes } from 'react';

export function Card({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={clsx(
        'border-4 border-black bg-white p-5 shadow-brutal',
        className
      )}
      {...props}
    />
  );
}

export function CardTitle({ className, ...props }: HTMLAttributes<HTMLHeadingElement>) {
  return (
    <h2
      className={clsx('font-display text-xl uppercase tracking-tight text-black', className)}
      {...props}
    />
  );
}
