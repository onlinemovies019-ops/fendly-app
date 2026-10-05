const vscode = require("vscode");
const { spawn } = require("child_process");
const fs = require("fs");
const path = require("path");

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

            const javaHome = process.env.JAVA_HOME ||
                "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home";
            const env = {
                ...process.env,
                JAVA_HOME: javaHome,
                PATH: `${path.join(javaHome, "bin")}:${path.dirname(adb)}:${process.env.PATH || ""}`,
            };

            output.clear();
            output.show(true);
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

module.exports = { activate, deactivate };
