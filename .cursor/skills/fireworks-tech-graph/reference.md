# fireworks-tech-graph — reference

## Yes: install from GitHub in Cursor

Cursor skills are folders with `SKILL.md`. The official package is distributed from GitHub; **clone** is the right way to get the full skill assets into Cursor:

```bash
git clone https://github.com/yizhiyanhua-ai/fireworks-tech-graph.git ~/.cursor/skills/fireworks-tech-graph
```

Optional: replace or merge with the thin wrapper in this repo by checking out upstream files into the same directory (back up first if needed).

## npm package role

- [npm package](https://www.npmjs.com/package/@yizhiyanhua-ai/fireworks-tech-graph): public README, version history, discovery.
- **Do not** use the npm package name with Claude Code’s `skills add` (CLI expects `owner/repo` or a path).

## Update upstream clone

```bash
cd ~/.cursor/skills/fireworks-tech-graph && git pull
```

Claude Code update (not Cursor):

```bash
npx skills add yizhiyanhua-ai/fireworks-tech-graph --force -g -y
```

## Upstream layout (after clone)

- `SKILL.md` — full diagram rules
- `references/style-1-flat-icon.md` … `style-7-openai.md`, `icons.md`, `svg-layout-best-practices.md`
- `fixtures/*.json` — regression-style examples
- `templates/*.svg` — starters
- `scripts/generate-diagram.sh`, `validate-svg.sh`, etc.
