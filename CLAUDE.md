# CLAUDE.md — Arthritis-Saver-clicker-9000

## Project overview

**Arthritis-Saver-clicker-9000** is an app from [ai.hellsy.net](https://ai.hellsy.net) intended to reduce repetitive-click strain — an auto-clicker / click-automation tool that saves users (especially those with arthritis or RSI) from manual clicking fatigue.

This is a greenfield project. As of 2026-04-17 the repository contains only a `LICENSE` file (MIT) and this `CLAUDE.md`. No source code, tests, build tooling, or configuration have been committed yet. The target platform (desktop? browser? mobile?) and language/framework are still to be decided.

## Repository structure

```
Arthritis-Saver-clicker-9000---an-ai.hellsy.net-app/
├── CLAUDE.md   # This file — AI assistant guide
└── LICENSE     # MIT
```

Once scaffolding lands, expect a conventional layout along these lines:

```
Arthritis-Saver-clicker-9000---an-ai.hellsy.net-app/
├── src/        # Source code
├── tests/      # Test suite
├── assets/     # Icons, images, sounds
├── .github/    # CI workflows
├── CLAUDE.md
└── LICENSE
```

## Development guidelines

### General conventions

- Keep commits small and focused with clear, descriptive messages.
- Prefer editing existing files over creating new ones.
- Do not add speculative abstractions, unused utilities, or features beyond what is requested.
- Do not add comments, docstrings, or type annotations to code you did not change.
- Only add error handling at system boundaries (user input, OS/input APIs, external services) — trust internal code paths.

### Accessibility

This project exists to reduce physical strain. Keep accessibility front of mind:

- Every interaction should be keyboard-reachable; do not require precise or repeated clicks to configure or control the app.
- Provide visible, high-contrast feedback for state changes (running / paused / stopped).
- Prefer large hit targets and clear affordances; avoid hover-only controls.
- Expose global hotkeys to start/stop automation so users can avoid clicking the UI itself.

### Security & safety

- **Never commit secrets** — API keys, tokens, `.env` files, signing certificates, and credentials stay out of version control.
- Input-simulation tools can be abused. Consider platform-level risks (e.g. simulating clicks inside other applications) and require explicit user consent for any cross-app automation.
- Validate and sanitize all external input (config files, imported macros, IPC messages).
- Follow OWASP best practices where web surfaces exist; be mindful of injection risks when processing user-supplied automation scripts.

### Git workflow

- Default branch: `main`.
- Feature branches: use descriptive names (e.g., `feat/hotkey-config`, `fix/click-drift`).
- Always push with `git push -u origin <branch-name>`.
- Do not force-push to `main`.
- Do not amend published commits — create new commits instead.

### Testing

- Write tests for new functionality.
- Run the full test suite before pushing.
- Cover both the golden path and edge cases — for a clicker this includes: long-running sessions, hotkey conflicts, display sleep / focus changes, and stop/pause reliability.

### Dependencies

- Pin dependency versions for reproducible builds.
- Evaluate new dependencies carefully — prefer well-maintained, minimal libraries, especially for input-simulation primitives where a buggy library can freeze or confuse the host OS.

## Key concepts

### Click automation

The core feature of this project. At minimum it should support:

- **Configurable cadence** — interval between clicks, including jitter for non-robotic timing.
- **Start/stop controls** — both in-UI and via global hotkey.
- **Scope** — fixed screen coordinates, tracked target, or current cursor position.
- **Safety limits** — maximum runtime, maximum clicks, or a kill switch that always wins.

### Arthritis-friendly UX

The "Arthritis-Saver" framing is not branding — it is the core requirement. Design decisions should favor the user physically doing less:

- Defaults should be useful without configuration.
- Configuration should persist.
- Controls should be operable with minimal, imprecise input.

## Commands reference

> To be populated once the build/test/run tooling is established.

```bash
# Placeholder — update when tooling is chosen
# npm test        / pytest / cargo test
# npm run lint    / ruff check / cargo clippy
# npm start       / python -m src / cargo run
```

## Troubleshooting

- **Clicks not registering in target app**: Some OSes and applications (especially games with anti-cheat, or apps running as administrator) block synthetic input. Check permissions and whether the target window accepts simulated events.
- **Hotkeys not triggering**: Another app may own the hotkey globally, or the OS may require accessibility/input-monitoring permission for the process.
- **Drift / missed clicks over long runs**: Likely a scheduling or sleep-timer issue; verify the timing source and that the app isn't being throttled in the background.

## Notes for AI assistants

- Read files before modifying them.
- Do not create documentation files (README, etc.) unless explicitly asked.
- When the build/test toolchain is set up, run tests after making changes.
- The language/framework/platform is not yet chosen — ask before assuming one.
- Update this file as the project evolves: add build commands, architecture details, and conventions as they are established.
