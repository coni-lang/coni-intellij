<p align="center">
  <img src="https://coni-lang.org/icon.png" width="128" alt="Coni Logo">
</p>

<h1 align="center">Coni for VS Code</h1>

<p align="center">
  <strong>The official VS Code extension for the Coni Programming Language.</strong><br>
  <em>Experience fast, native, AI-powered development directly in your editor.</em>
</p>

---

## ⚡️ Lightning Fast Setup

Getting started with Coni has never been easier. The extension seamlessly downloads the latest optimized `coni` binary specifically built for your operating system (macOS, Linux, Windows) with just a single click.

1. Open the Command Palette (`Cmd+Shift+P`)
2. Run **`Coni: Download/Update Binary`**
3. You're ready to code!

## 🎮 Coni Web Playground

Want to explore data science, sports analytics, or visual web applications without setting up a project? You can launch the interactive **Coni Web Playground** locally from right within the editor.

1. Open the Command Palette (`Cmd+Shift+P`)
2. Run **`Coni: Playbook`**
3. Your default browser will instantly open a live, hot-reloading development environment complete with interactive tutorials and examples.

## 📝 Code Sample

A quick look at Coni's clean, functional syntax:

```clojure
;; Fetch data and plot a sparkline
(require "libs/http/src/http.coni" :as http)
(require "libs/plot/src/plot.coni" :as plot)

(def data (http/fetch "https://api.example.com/telemetry"))
(println "Server Load:")
(println (plot/sparkline [1 2 5 4 8 9 5 2 1]))
```


## ✨ Features

- **Rich Syntax Highlighting:** Beautiful, comprehensive semantic coloring for keywords, built-ins, variables, and literals.
- **Intelligent Linting:** Real-time syntax checking that catches errors the moment you save or open a `.coni` file.
- **Interactive REPL:** Start, connect, and disconnect from a live Coni REPL session directly from your IDE.
- **Inline Evaluation:** Highlight any block of Coni code and hit `Cmd+Enter` to instantly evaluate it without leaving your editor.
- **WASM Builds:** Effortlessly compile your Coni projects into high-performance WebAssembly modules (`Coni: Build WASM`).
- **AI Integration:** Stuck on a problem? Highlight code and use `Cmd+Shift+Enter` to **Ask AI** for explanations, refactoring, or suggestions.

## 🚀 Available Commands

Access these tools anytime via the Command Palette:

| Command | Description |
|---|---|
| `Coni: Download/Update Binary` | Automatically fetches the latest native `coni` executable. |
| `Coni: Run Script` | Executes the currently active `.coni` file. |
| `Coni: Build` / `Build WASM` | Compiles your project natively or targets WebAssembly. |
| `Coni: Playbook` | Launches the interactive Coni Web Playground. |
| `Coni: Evaluate Selection` | Runs the highlighted code snippet instantly. |
| `Coni: Ask AI` | Sends the current context to the AI assistant for help. |

## ⚙️ Configuration

Customize the extension behavior in your `settings.json`:

- `coni.executablePath`: Override the default downloaded binary by pointing to a specific local executable.
- `coni.gpuBackend`: Switch the MLX / ROCm backend (`default`, `cpu`, `cuda`, `rocm`) for machine learning tasks.
- `coni.binaryDownloadUrl`: Provide a custom enterprise server URL for binary distribution.

## 🌐 Links & Resources

- **Official Website:** [coni-lang.org](https://coni-lang.org)
- **WASM App Gallery:** [coni-lang.org/wasm-apps/](https://coni-lang.org/wasm-apps/)

<p align="center">
  Built with ❤️ for the Coni ecosystem.<br>
  Copyright hellonico ©2026
</p>
