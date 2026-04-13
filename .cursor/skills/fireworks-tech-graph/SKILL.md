---
name: fireworks-tech-graph
description: Generates production-quality SVG technical diagrams (architecture, flowchart, UML, ER, sequence) using seven visual styles and semantic shapes/arrows; exports SVG+PNG via rsvg-convert. Use when the user asks to draw, diagram, visualize, or export architecture/flow/RAG/agent/Mem0/UML charts, or mentions fireworks-tech-graph.
---

# fireworks-tech-graph (Cursor)

Upstream project: [yizhiyanhua-ai/fireworks-tech-graph](https://github.com/yizhiyanhua-ai/fireworks-tech-graph) (npm: `@yizhiyanhua-ai/fireworks-tech-graph` for docs/version only).

## Install the full skill (recommended)

Clone the repository so the agent can read the complete `SKILL.md`, `references/`, `fixtures/`, and `templates/`:

```bash
git clone https://github.com/yizhiyanhua-ai/fireworks-tech-graph.git ~/.cursor/skills/fireworks-tech-graph
```

Project-local alternative (shared with the repo):

```bash
git clone https://github.com/yizhiyanhua-ai/fireworks-tech-graph.git .cursor/skills/fireworks-tech-graph-upstream
```

After cloning, **read the upstream `SKILL.md` first**, then `references/style-*.md` and `references/icons.md` for the active style.

### PNG export dependency

```bash
# macOS
brew install librsvg
# Ubuntu/Debian
sudo apt install librsvg2-bin
rsvg-convert --version
```

Use **PNG** for diagrams (lossless); avoid JPG for line art.

## Claude Code vs Cursor

- **Claude Code**: `npx skills add yizhiyanhua-ai/fireworks-tech-graph` (GitHub-based installer; do **not** pass the npm package name to `skills add`).
- **Cursor**: use **git clone** into `~/.cursor/skills/fireworks-tech-graph` (or a path under `.cursor/skills/` in the project), as above.

## When this skill applies (triggers)

Chinese / English keywords such as: 画图, 架构图, 流程图, 可视化, generate diagram, draw diagram, architecture diagram, sequence diagram, RAG, Agent, Mem0, UML.

## Seven styles (pick one)

| # | Name | Notes |
|---|------|--------|
| 1 | Flat Icon | Default; docs, slides |
| 2 | Dark Terminal | Neon, monospace; README dark theme |
| 3 | Blueprint | Grid, cyan lines; engineering docs |
| 4 | Notion Clean | Minimal wiki |
| 5 | Glassmorphism | Frosted cards; decks / product |
| 6 | Claude Official | Cream `#f8f6f3`; Anthropic-aligned |
| 7 | OpenAI Official | White; OpenAI-aligned |

User may say “风格2”, “style 2”, “dark”, “glass”, etc.—map to the table.

## Diagram intent → style hints

- **UML class/component/package**: 1 or 4. **Sequence**: 2. **Activity/state**: 3.
- **RAG / Agentic Search**: 2 or 5. **Memory architecture**: 3. **Multi-Agent**: 5.
- **Internal wiki**: 4. **Blog**: 1. **Anthropic projects**: 6. **OpenAI projects**: 7.

## Semantic shapes (consistent vocabulary)

| Concept | Shape |
|---------|--------|
| User | Circle + body |
| LLM | Rounded rect, double border, ⚡ |
| Agent / orchestrator | Hexagon |
| Short-term memory | Dashed rounded rect |
| Long-term memory | Solid cylinder |
| Vector store | Cylinder with inner ring |
| Graph DB | Three-circle cluster |
| Tool | Rect with ⚙ |
| API / gateway | Hexagon (single border) |
| Queue / stream | Horizontal pipe |
| Document | Folded corner rect |
| Browser / UI | Rect with title bar |
| Decision | Diamond |
| External service | Dashed rect |

## Arrow semantics

- **Main data**: 2px solid.
- **Control / trigger**: 1.5px solid.
- **Memory read**: 1.5px solid (read path).
- **Memory write**: dashed pattern e.g. `5,3`.
- **Async / event**: dashed e.g. `4,2`.
- **Feedback loop**: curved emphasis.

## Working without the clone

If the upstream repo is **not** present, still follow triggers, style table, shapes, and arrows above; prefer generating **inline SVG** that respects those rules. When the repo is available, use upstream `templates/*.svg`, `fixtures/*.json`, and `scripts/` for validation and PNG export.

## Additional resources

- [reference.md](reference.md) — clone paths, npm vs GitHub, update commands.
