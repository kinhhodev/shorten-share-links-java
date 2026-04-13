import clsx from 'clsx';
import { type ButtonHTMLAttributes, forwardRef } from 'react';

type Variant = 'primary' | 'secondary' | 'danger' | 'ghost';

const variants: Record<Variant, string> = {
  primary:
    'bg-[#FFE156] hover:bg-[#ffed90] border-black text-black',
  secondary:
    'bg-[#7dd3fc] hover:bg-[#bae6fd] border-black text-black',
  danger:
    'bg-[#fda4af] hover:bg-[#fecdd3] border-black text-black',
  ghost:
    'bg-white hover:bg-neutral-100 border-black text-black',
};

export const Button = forwardRef<
  HTMLButtonElement,
  ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant }
>(function Button({ className, variant = 'primary', type = 'button', ...props }, ref) {
  return (
    <button
      ref={ref}
      type={type}
      className={clsx(
        'inline-flex items-center justify-center border-4 border-black px-4 py-2 font-bold uppercase tracking-wide shadow-brutal-sm transition-transform',
        'hover:translate-x-0.5 hover:translate-y-0.5 hover:shadow-[2px_2px_0_0_#000]',
        'active:translate-x-1 active:translate-y-1 active:shadow-none',
        'disabled:pointer-events-none disabled:opacity-50',
        variants[variant],
        className
      )}
      {...props}
    />
  );
});
