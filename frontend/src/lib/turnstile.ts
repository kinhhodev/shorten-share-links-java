const TURNSTILE_SCRIPT_ID = 'cloudflare-turnstile-script';
const TURNSTILE_SCRIPT_SRC = 'https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit';

type TurnstileWidgetId = string;

type TurnstileOptions = {
  sitekey: string;
  size: 'invisible';
  callback: (token: string) => void;
  'error-callback': () => void;
  'expired-callback': () => void;
};

type TurnstileApi = {
  render: (container: HTMLElement, options: TurnstileOptions) => TurnstileWidgetId;
  execute: (widgetId: TurnstileWidgetId) => void;
  remove: (widgetId: TurnstileWidgetId) => void;
};

declare global {
  interface Window {
    turnstile?: TurnstileApi;
  }
}

let loadPromise: Promise<void> | null = null;

export async function getInvisibleTurnstileToken(container: HTMLElement): Promise<string> {
  const siteKey = import.meta.env.TURNSTILE_SITE_KEY;
  if (!siteKey) {
    throw new Error('Turnstile site key is not configured.');
  }

  await loadTurnstileScript();

  if (!window.turnstile) {
    throw new Error('Turnstile failed to load.');
  }

  return new Promise((resolve, reject) => {
    let widgetId: TurnstileWidgetId | null = null;

    const settle = (fn: () => void) => {
      if (widgetId && window.turnstile) {
        window.turnstile.remove(widgetId);
      }
      fn();
    };

    widgetId = window.turnstile.render(container, {
      sitekey: siteKey,
      size: 'invisible',
      callback: (token) => settle(() => resolve(token)),
      'error-callback': () => settle(() => reject(new Error('Turnstile verification failed.'))),
      'expired-callback': () => settle(() => reject(new Error('Turnstile verification expired.'))),
    });

    window.turnstile.execute(widgetId);
  });
}

function loadTurnstileScript(): Promise<void> {
  if (window.turnstile) {
    return Promise.resolve();
  }
  if (loadPromise) {
    return loadPromise;
  }

  loadPromise = new Promise((resolve, reject) => {
    const existingScript = document.getElementById(TURNSTILE_SCRIPT_ID) as HTMLScriptElement | null;
    const script = existingScript ?? document.createElement('script');

    script.id = TURNSTILE_SCRIPT_ID;
    script.src = TURNSTILE_SCRIPT_SRC;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Turnstile failed to load.'));

    if (!existingScript) {
      document.head.appendChild(script);
    }
  });

  return loadPromise;
}
