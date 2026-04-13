import clsx from 'clsx';
import { forwardRef, type SelectHTMLAttributes } from 'react';

export const Select = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement>>(
  function Select({ className, children, ...props }, ref) {
    return (
      <select
        ref={ref}
        className={clsx(
          'w-full cursor-pointer border-4 border-black bg-white px-3 py-2 font-bold uppercase shadow-brutal-sm outline-none',
          'focus:ring-2 focus:ring-black focus:ring-offset-2',
          className
        )}
        {...props}
      >
        {children}
      </select>
    );
  }
);
