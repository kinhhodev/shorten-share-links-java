import clsx from 'clsx';
import { type LabelHTMLAttributes } from 'react';

export function Label({ className, ...props }: LabelHTMLAttributes<HTMLLabelElement>) {
  return (
    <label
      className={clsx('mb-1 block text-sm font-bold uppercase tracking-wide text-black', className)}
      {...props}
    />
  );
}
