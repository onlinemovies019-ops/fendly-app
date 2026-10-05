const vscode = require("vscode");
const { spawn, spawnSync } = require("child_process");
const fs = require("fs");
const path = require("path");

const DEFAULT_JAVA_HOME = "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home";

function run(command, args, cwd, env, output) {
    return new Promise((resolve, reject) => {
        const process = spawn(command, args, { cwd, env, stdio: ["ignore", "pipe", "pipe"] });
        process.stdout.on("data", (data) => output.append(data.toString()));
        process.stderr.on("data", (data) => output.append(data.toString()));
        process.on("error", reject);
        process.on("close", (code) => {
            if (code === 0) resolve();
            else reject(new Error(`${path.basename(command)} exited with code ${code}`));
        });
    });
}

function resolveAdb() {
    const sdkRoot = process.env.ANDROID_SDK_ROOT || process.env.ANDROID_HOME;
    const candidates = [
        sdkRoot && path.join(sdkRoot, "platform-tools", "adb"),
        "/Users/ashok.bagmare/Library/Android/sdk/platform-tools/adb",
        "adb",
    ].filter(Boolean);
    return candidates.find((candidate) => candidate === "adb" || fs.existsSync(candidate));
}

function javaMajorVersion(javaHome) {
    const java = path.join(javaHome, "bin", "java");
    if (!fs.existsSync(java)) return null;
    const result = spawnSync(java, ["-version"], { encoding: "utf8" });
    const version = `${result.stdout || ""}\n${result.stderr || ""}`;
    const match = version.match(/version "(?:1\.)?(\d+)/);
    return result.status === 0 && match ? Number(match[1]) : null;
}

function resolveJavaHome(output) {
    const requestedJavaHome = process.env.FENDLY_JAVA_HOME;
    if (requestedJavaHome && javaMajorVersion(requestedJavaHome) === 17) {
        return requestedJavaHome;
    }
    if (requestedJavaHome) {
        output.appendLine(`Ignoring FENDLY_JAVA_HOME: Android builds require JDK 17 (found ${requestedJavaHome}).`);
    }
    if (javaMajorVersion(DEFAULT_JAVA_HOME) === 17) {
        return DEFAULT_JAVA_HOME;
    }
    throw new Error(`JDK 17 was not found at ${DEFAULT_JAVA_HOME}. Install JDK 17 or set FENDLY_JAVA_HOME to a JDK 17 installation.`);
}

function activate(context) {
    const output = vscode.window.createOutputChannel("Fendly Device Runner");
    context.subscriptions.push(output);
    context.subscriptions.push(
        vscode.commands.registerCommand("fendly.runOnDevice", async () => {
            const folder = vscode.workspace.workspaceFolders?.[0];
            if (!folder) {
                vscode.window.showErrorMessage("Open the Fendly project folder first.");
                return;
            }

            const root = folder.uri.fsPath;
            const gradle = path.join(root, "gradlew");
            if (!fs.existsSync(gradle)) {
                vscode.window.showErrorMessage("The open folder does not contain Fendly's Gradle wrapper.");
                return;
            }

            const adb = resolveAdb();
            if (!adb) {
                vscode.window.showErrorMessage("Android Debug Bridge (adb) was not found.");
                return;
            }

            output.clear();
            output.show(true);
            let javaHome;
            try {
                javaHome = resolveJavaHome(output);
            } catch (error) {
                output.appendLine(error.message);
                vscode.window.showErrorMessage(error.message);
                return;
            }
            const env = {
                ...process.env,
                JAVA_HOME: javaHome,
                PATH: `${path.join(javaHome, "bin")}:${path.dirname(adb)}:${process.env.PATH || ""}`,
            };

            output.appendLine(`Using JDK 17: ${javaHome}`);
            output.appendLine("Building and installing Fendly...");
            await vscode.window.withProgress(
                {
                    location: vscode.ProgressLocation.Notification,
                    title: "Building and installing Fendly",
                    cancellable: false,
                },
                async () => {
                    try {
                        await run(gradle, [":app:installDebug", "--no-configuration-cache"], root, env, output);
                        output.appendLine("Launching Fendly...");
                        await run(adb, ["shell", "am", "start", "-n", "com.example.fendly/.SplashActivity"], root, env, output);
                        vscode.window.showInformationMessage("Fendly is running on your connected device.");
                    } catch (error) {
                        output.appendLine(String(error));
                        vscode.window.showErrorMessage(`Could not run Fendly: ${error.message}`);
                    }
                },
            );
        }),
    );
}

function deactivate() {}

module.exports = { activate, deactivate, javaMajorVersion, resolveJavaHome };
