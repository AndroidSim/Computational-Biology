# Computational Biology

## 1. 📝 Basic Project Info

- Project Name: Computational-Biology
- Short Description: A biomimetic simulation system (Java) for liver physiology that models drug disposition, metabolism, damage, and cell death at the tissue / cellular scale.
- Long Description / Motivation:
  - This repository implements a domain-specific simulation environment for hepatic (liver) systems biology. It models compartments, vascular/extra-cellular flows, enzyme activity and metabolic processes, transport of solutes, and cell-state transitions (stressed, necrotic, etc.). The codebase was developed to let researchers and engineers run batch experiments or an interactive GUI to explore how dosing, enzyme distributions, and other parameters produce organ-level and cellular outcomes. The motivation is to provide a configurable, parameter-driven platform for studying drug disposition and liver damage mechanisms using a biomimetic compartmental/spatial model.

---

## 2. ⚙️ Technical Details

- Tech stack / frameworks / notable libraries:
  - Primary language: Java (main codebase)
  - Other languages present in the repository (ancillary): R, C++, Shell, HTML, Python
  - Simulation & utility libraries referenced in the code:
    - MASON-style simulation utilities (references to `sim.*`, `sim.display.Console`, `sim.util.Double2D`)
    - ECJ parameter utilities (`ec.util.ParameterDatabase`, `ec.util.Parameter`)
    - Genson JSON library (`com.owlike.genson.Genson`)
    - SLF4J + Logback for logging (`org.slf4j.*`, `ch.qos.logback.*`)
  - Build tooling:
    - Apache Ant (NetBeans-generated Ant build files; `build.xml` imports `nbproject/build-impl.xml`)
  - Runtime logging configuration in `resources/logback.xml`.

- System requirements:
  - Java SE JDK (recommended Java 8+). The code uses typical Java SE APIs and simulation libraries that commonly target Java 8-era toolchains; Java 11+ should also work in most environments, but test with your chosen JDK.
  - Apache Ant (optional if you prefer NetBeans or manual javac compile)
  - Recommended RAM: depends on simulation scale; larger tissue models / long batch runs will require significantly more heap memory (tune `-Xmx` when running).
  - OS: Cross-platform (Linux/macOS/Windows) — any platform that runs the above Java toolchain.

- Dependencies / packages (major ones referenced in source):
  - com.owlike.genson (Genson JSON)
  - org.slf4j: slf4j-api (logging façade)
  - ch.qos.logback: logback-classic (logging backend)
  - ECJ utilities (ec.util.ParameterDatabase) — for parameter database reading
  - MASON simulation core (packages named `sim.*` used in GUI/Console)
  - Note: this repo includes an Ant/NetBeans build; third-party jar dependencies are typically placed under `lib/` for the classpath. Inspect `lib/` and add the required jars if they are missing.

---

## 3. 🚀 Setup & Installation

- Prerequisites
  - Java JDK (8+ recommended)
  - Apache Ant (if using the provided Ant build)
  - (Optional) NetBeans IDE (project files are NetBeans-generated)
  - Ensure any third-party jars (MASON, ECJ, Genson, SLF4J/logback) are available in `lib/` or on your classpath.

- Installation steps (quick)
  1. Clone the repository
     - git clone https://github.com/AndroidSim/Computational-Biology.git
     - cd Computational-Biology
  2. Ensure dependencies (place jars into `lib/` if not already present)
     - The project expects runtime jars for MASON (`sim.*`), ECJ (`ec.util.*`), Genson, SLF4J and Logback. If `lib/` already contains them, no action needed. Otherwise download the appropriate versions and put them in `lib/`.
  3. Build with Ant (NetBeans-compatible build script provided)
     - ant clean
     - ant
     - After a successful build NetBeans / Ant usually produces `dist/` with the project jar (e.g., `dist/isl.jar`). The exact jar name and `dist/` layout come from the NetBeans build-impl imported by `build.xml`.
  4. (Alternative) Open in NetBeans
     - Open the project in NetBeans (the `nbproject/` folder and `build.xml` are present). Use the IDE to build and run.

- Configuration
  - Configuration & parameter files:
    - The application loads parameter/property files from the classpath under `cfg/` (packaged in `resources/cfg/`).
    - Key files referenced in code:
      - `cfg/batch_control.properties` — main parameter database used by `Main` for batch/gui configuration
      - `cfg/SR.properties` — toggles SR mode (special run control) used at startup
    - Example: `src/isl/Main.java` attempts to read `"cfg/batch_control.properties"` and `"cfg/SR.properties"` from the classpath.
  - To customize runs:
    - Edit or add property files in `resources/cfg/` (or provide them on the run classpath).
    - Many model parameters are documented under `docs/parameters/` (e.g., `parameter-scripts.odt`, `parameters.gnumeric`).
  - No environment variables or API keys are required by the core simulation (no external web services detected).

---

## 4. 🧪 Usage

- How to run the project (headless / batch)
  - After building with Ant (and ensuring `lib/` has required jars), run from the repository root (example, adjust classpath for your platform):
    - Unix/macOS:
      - java -cp "dist/isl.jar:lib/*" isl.Main
    - Windows (PowerShell / CMD):
      - java -cp "dist\isl.jar;lib\*" isl.Main
  - Notes:
    - `Main` reads `cfg/batch_control.properties` and `cfg/SR.properties` as classpath resources (from `resources/cfg/` inside the jar or classpath). If you want to run a custom parameter set, either edit those files before packaging or add them to the runtime classpath.
    - `Main` will launch either:
      - A GUI console (if `gui` parameter is true and environment allows), or
      - Headless batch control: `isl.measurement.view.BatchControl.doLoop(...)` (default for batch runs).
    - There is an SR-mode toggle: SR mode can vary random seeds and write parameter files if `SR` is set; it is controlled by `cfg/SR.properties`.

- How to run with NetBeans
  - Open the project in NetBeans and run the `Main` class. The IDE handles classpath and resources automatically.

- Example inputs / outputs
  - Inputs:
    - Parameter/property files in `resources/cfg/` (e.g., batch_control.properties, SR.properties)
    - Additional input data under `resources/data/` (if used by particular model runs)
    - Parameter spreadsheets / docs under `docs/parameters/` can be used as references when creating property files.
  - Outputs:
    - Observations and measurement output are produced by classes in `src/isl/measurement/` (Observer, DataModelObs, GraphObs, Count* classes). Output locations and formats are controlled by those observer classes and the configured property set. Look at the Observer and measurement implementations to find exact output filenames/formatting.
    - Logging to console is configured by `resources/logback.xml` (ROOT level = INFO by default).

- Example (simple run)
  - Build:
    - ant
  - Run:
    - java -cp "dist/isl.jar:lib/*" isl.Main
  - Expected behavior:
    - If `gui=true` in the batch control properties and a GUI is usable, the program will open a MASON-based console. Otherwise it will run a headless batch loop and write measurements/outputs according to the configured observers.

- Screenshots / GIFs
  - There are no pre-included screenshots or demo assets in the repository. The project can optionally launch a GUI (MASON console) when `gui` is enabled in configuration.

- Demo / hosted links
  - None available / not detected in this repository.

---

## 5. Project layout (quick guide)

- src/
  - isl/Main.java — application entrypoint (loads property DB and starts either GUI or batch loop)
  - isl/io — parameter & I/O utilities (Parameters.java, DataScanner.java, Parameters loader)
  - isl/model — core biological model (ISL.java, AbstractISLModel.java, HepStruct, HepStructParams, ISLParams, MetabolicParams, LiverNode, LiverNodes, etc.)
  - isl/measurement — observers and measurement classes (Observer.java, various Count* classes, GraphObs, DataModelObs)
  - isl/measurement/view — GUI / BatchControl classes (BatchControl and GUIControl referenced by Main)
- resources/
  - cfg/ — parameter/property files used at runtime (batch_control.properties, SR.properties expected)
  - data/ — input data directory (used by specific simulations)
  - logback.xml — logging configuration
- docs/
  - parameters/ — parameter spreadsheets and scripts (e.g., `parameter-scripts.odt`, `parameters.gnumeric`)
  - notes — design/implementation notes describing model design choices, floating-point/tolerance remarks, and other developer notes

---

## 6. Important classes to read first (recommended)
- src/isl/Main.java — how the application starts and how it selects GUI vs batch mode.
- src/isl/model/ISL.java and src/isl/model/AbstractISLModel.java — the simulation engine and model base.
- src/isl/model/HepStruct.java and HepStructParams.java — hepatic structure and parameters for tissue modeling.
- src/isl/model/MetabolicParams.java — metabolic parameterization (enzyme behavior, metabolic rates).
- src/isl/io/Parameters.java — property parsing helpers and parameter types (map1d, map2d, tuple parsing).
- src/isl/measurement/Observer.java and measurement classes — how outputs / observables are computed and emitted.

---

## 7. Tests
- A `test/` directory exists, but there is no explicit test runner configuration discovered in the top-level build files. Tests (if any) may be integrated with NetBeans / Ant JUnit targets. Use NetBeans or inspect `nbproject/` / `build-impl.xml` for JUnit integration.

---

## 8. Contributing & Issues
- Issues: Open an issue on this repository (Issues are enabled).
- Contributing guidelines: Not provided in the repo. If you want to contribute:
  - Fork the repository
  - Add or update parameter files in `resources/cfg/` or add tests
  - Submit a pull request with a clear description of changes and a small runnable example when possible

---

## 9. License & Attribution
- No license file was detected in the repository root. If you intend to use or redistribute this code, please contact the repository owner or add an appropriate LICENSE file. Source files contain copyright attribution (Regents of the University of California, San Francisco, and later dates) in several places — review individual files for exact copyright notices.

---

## 10. Useful pointers & troubleshooting
- If Main fails to start with a parameter database error, check that the `cfg/*.properties` files are available on the runtime classpath (i.e., packaged into the jar or available in classpath directories).
- If you see NoClassDefFoundError or ClassNotFoundException for `sim.*`, `ec.util.*`, or `com.owlike.genson.*`, ensure the corresponding jars are placed into `lib/` and included on the classpath at runtime.
- Logging level and formatting can be edited in `resources/logback.xml`.
- For large simulations, increase JVM heap: e.g., `java -Xmx8g -cp "dist/isl.jar:lib/*" isl.Main`.

---

If you want, I can:
- Draft a runnable example properties file (a minimal `batch_control.properties`) you can drop into `resources/cfg/` to run a small headless job.
- Create a short script (Ant target / shell script) that builds and runs a basic simulation using bundled jars.
- Add a LICENSE file template (e.g., MIT, BSD, or another) if you want to open-source this with a specific license.
```

Below is a focused, step-by-step tutorial to build and run a simulation using this repository (AndroidSim/Computational-Biology). It includes concrete commands, minimal example parameter files you can drop into resources/cfg/, how to run GUI vs batch/headless, expected behavior, tips for memory and troubleshooting, and pointers for customizing experiments.

Quick assumptions (based on the codebase)
- Project entry point: isl.Main (src/isl/Main.java). It reads cfg/batch_control.properties and cfg/SR.properties from the classpath.
- The project is a Java/Ant (NetBeans-generated) project and expects several third-party jars (MASON, ECJ, Genson, SLF4J / Logback) on the classpath. The typical layout stores jars in lib/.
- GUI is MASON-based (sim.display.Console) and BatchControl handles headless runs.

1) Prerequisites
- Java JDK (8+ recommended). Install a JDK and ensure java/javac are on PATH.
- Apache Ant (optional; recommended because repository includes build.xml)
- (Optional) NetBeans IDE — project contains NetBeans metadata (nbproject/) so opening in NetBeans simplifies builds/runs.
- Download required third-party jars (place them in repo/lib/):
  - MASON (provides sim.* classes)
  - ECJ (provides ec.util.ParameterDatabase / Parameter)
  - Genson (com.owlike.genson)
  - SLF4J API + Logback (logback-classic + logback-core)
  - Any other jars referenced by code (you will see ClassNotFoundException messages if you miss something)

If the repo already includes necessary jars under lib/, no further downloads are needed. If lib/ is empty, download the jars and add them.

2) Clone & build
Run these commands from a terminal:

- Clone
  - git clone https://github.com/AndroidSim/Computational-Biology.git
  - cd Computational-Biology

- Build with Ant
  - ant clean
  - ant
  - After a successful build you should have compiled classes and typically a distributable in dist/ (NetBeans/Ant will usually create dist/isl.jar or similar). If Ant/NetBeans created a jar, it will be referenced as something like dist/isl.jar.

Notes:
- If you prefer NetBeans: open the project directory (contains nbproject/) and use the IDE Build/Run buttons — NetBeans will manage the classpath.

3) Minimal runtime configuration files
Main expects properties on the classpath under cfg/*. Create or edit these in resources/cfg/ and package them on the classpath / inside the jar (or keep them on the runtime classpath as loose files).

Example minimal batch_control.properties (headless/batch run)
```properties name=resources/cfg/batch_control.properties
# Minimal batch control properties
# 'gui' toggles GUI (true) or headless (false)
gui=false

# Add additional model parameters here. Keys/values are parsed by the project's parameter utilities.
# Example (domain-specific keys will be read by model-specific parameter loaders)
# runDuration = 1000
# outputPrefix = run1
```

Example SR.properties (controls SR mode used by Main)
```properties name=resources/cfg/SR.properties
# SR (special-run) toggle
SR=false
```

Place those files at resources/cfg/batch_control.properties and resources/cfg/SR.properties (create directories if needed). When you run the jar, these files must be on the runtime classpath (packaged in jar or present in classpath directory).

4) Run the simulation (headless / batch)
Use a command like this (adjust paths for Windows):

- Unix/macOS:
  - java -Xmx4g -cp "dist/isl.jar:lib/*" isl.Main
- Windows (CMD/powershell):
  - java -Xmx4g -cp "dist\isl.jar;lib\*" isl.Main

Notes:
- -Xmx4g sets the max heap to 4GB — increase for large simulations.
- The classpath must include the built jar (e.g., dist/isl.jar) and all dependency jars (lib/*).
- If GUI is false, the program will run in batch mode via isl.measurement.view.BatchControl.doLoop(...). BatchControl controls the batch experiment loop and observers that write outputs.
- To pass command-line args (for BatchControl or GUIControl), append them after the class name: isl.Main arg1 arg2...
  - The code calls BatchControl.canUseGUI(args) so some arguments may control display behavior, but exact arg semantics are implemented inside BatchControl (inspect src/isl/measurement/view/BatchControl.java to tailor arguments).

5) Run the simulation (GUI)
- Turn on GUI by setting gui=true in resources/cfg/batch_control.properties:
```properties name=resources/cfg/batch_control.properties
gui=true
```
- Ensure you are running on a machine with a display (X11 on Linux/macOS, or local desktop on Windows). Then run the same java command:
  - java -Xmx4g -cp "dist/isl.jar:lib/*" isl.Main
- If GUI is available and parameter says gui=true, Main will create isl.measurement.view.GUIControl and launch sim.display.Console.

6) Where inputs come from and how to edit experiments
- Parameter files: many parameterized aspects of the model are loaded via ec.util.ParameterDatabase. The project uses property-like keys (see src/isl/io/Parameters.java utilities). Use resources/cfg/*.properties to hold the parameters.
- docs/parameters/ contains parameter spreadsheets and scripts to guide parameter creation. Use those as templates to build domain-specific parameter property strings or scripts.
- resources/data/ is available for any specific model data; place input datasets there if your run expects them.

7) Where outputs go
- Measurement and output code lives in src/isl/measurement/ (Observer.java, DataModelObs.java, GraphObs.java, many Count* classes).
- Observers typically compute metrics and write measurement files. Output file names and locations are determined by those observer implementations and by properties in batch_control.properties or other model parameter files. Inspect Observer.java and the Count* classes to find exact filenames/format or change them to suit your environment.
- Logging to console is configured by resources/logback.xml (INFO level by default).

8) Example minimal run — end-to-end checklist
1. Ensure jars are in lib/ (MASON, ECJ, Genson, slf4j/logback).
2. Place the example properties above in resources/cfg/.
3. ant && java -Xmx4g -cp "dist/isl.jar:lib/*" isl.Main
4. Inspect console logs and look for Observer/measurement output files in the working directory or configured output path.

9) Common issues & troubleshooting
- NoClassDefFoundError / ClassNotFoundException for classes such as sim.display.Console, ec.util.ParameterDatabase, com.owlike.genson.*:
  - You are missing required jars. Put the appropriate jar(s) in lib/ and include lib/* in classpath.
- FileNotFound / parameter load errors:
  - Ensure resources/cfg/batch_control.properties and resources/cfg/SR.properties are on the runtime classpath. If you package into a jar, make sure resources are included, or provide resources/cfg/ on the classpath as a directory.
- GUI doesn't appear even with gui=true:
  - BatchControl.canUseGUI(args) may determine GUI usability (e.g., if running headless via SSH without X forwarding). Ensure DISPLAY is set and X forwarding is enabled (ssh -X) or run locally.
- Simulation runs but no output files:
  - Check Observer implementations (src/isl/measurement/Observer.java and DataModelObs.java) to see exactly where they write outputs and what properties control file paths. Adjust property values accordingly.
- Memory issues / long run times:
  - Increase JVM heap (-Xmx), or reduce model size/steps in property files. Large spatial/tissue models can require substantial RAM.

10) Customizing experiments & next steps
- Read these core classes to understand how to configure runs:
  - src/isl/Main.java — startup behavior, where it reads cfg files
  - src/isl/io/Parameters.java — helpers for parsing typed properties (map1d/map2d/tuples)
  - src/isl/model/* — model parameters and structures (HepStruct, HepStructParams, SS, SSParams)
  - src/isl/measurement/* — observers defining measurement outputs
  - src/isl/measurement/view/* — GUI and BatchControl interactions
- Use docs/parameters/parameter-scripts.odt and parameters.gnumeric as references for scientific parameter choices.
- To automate multiple runs, create shell scripts or Ant targets that:
  - copy or generate resources/cfg/batch_control.properties for each experimental condition
  - run java -Xmx... isl.Main
  - collect outputs into per-run directories

11) Example helper shell script (Linux/macOS)
Create a script run_sim.sh to run a headless simulation and store outputs in a timestamped folder:
```bash
#!/usr/bin/env bash
set -e
OUTDIR="results-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$OUTDIR"
# Ensure dist/isl.jar exists and lib/* contains required jars
java -Xmx6g -cp "dist/isl.jar:lib/*" isl.Main "$@" > "$OUTDIR/run.log" 2>&1
echo "Logs and output (if any) in $OUTDIR"
```
Make executable: chmod +x run_sim.sh, then run ./run_sim.sh





