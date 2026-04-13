import clsx from 'clsx';
import { forwardRef, type InputHTMLAttributes } from 'react';

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  function Input({ className, ...props }, ref) {
    return (
      <input
        ref={ref}
        className={clsx(
          'w-full border-4 border-black bg-[#fffef8] px-3 py-2 font-medium text-black shadow-brutal-sm outline-none',
          'placeholder:text-neutral-500 focus:ring-2 focus:ring-black focus:ring-offset-2',
          className
        )}
        {...props}
      />
    );
  }
);
