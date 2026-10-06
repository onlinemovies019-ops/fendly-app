const vscode = require("vscode");
const { spawn, spawnSync } = require("child_process");
const fs = require("fs");
const path = require("path");

const DEFAULT_JAVA_HOME = "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home";

function run(command, args, cwd, env, output) {
    return new Promise((resolve, reject) => {
        const process = spawn(command, args, { cwd, env, stdio: ["ignore", "pipe", "pipe"] });
        let captured = "";
        process.stdout.on("data", (data) => {
            const text = data.toString();
            captured += text;
            output.append(text);
        });
        process.stderr.on("data", (data) => {
            const text = data.toString();
            captured += text;
            output.append(text);
        });
        process.on("error", reject);
        process.on("close", (code) => {
            if (code === 0) resolve(captured);
            else reject(new Error(`${path.basename(command)} exited with code ${code}${captured.trim() ? `: ${captured.trim()}` : ""}`));
        });
    });
}

function adbDevices(output) {
    return output
        .split(/\r?\n/)
        .slice(1)
        .map((line) => line.trim())
        .filter((line) => line && !line.startsWith("*"))
        .map((line) => {
            const [serial, state, ...details] = line.split(/\s+/);
            return { serial, state, details: details.join(" ") };
        });
}

function isRedmiDevice(device) {
    return /(?:product:violet|model:Redmi_Note_7_Pro)/.test(device.details);
}

function wait(milliseconds) {
    return new Promise((resolve) => setTimeout(resolve, milliseconds));
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
    let automaticReconnectRunning = false;
    context.subscriptions.push(
        vscode.commands.registerCommand("fendly.reconnectWifiAdb", async (automatic = false) => {
            const adb = resolveAdb();
            if (!adb) {
                if (!automatic) {
                    vscode.window.showErrorMessage("Android Debug Bridge (adb) was not found.");
                }
                return;
            }

            const root = vscode.workspace.workspaceFolders?.[0]?.uri.fsPath || process.cwd();
            const env = { ...process.env, PATH: `${path.dirname(adb)}:${process.env.PATH || ""}` };
            const savedEndpoint = context.globalState.get("fendly.wifiAdbEndpoint");
            output.clear();
            output.show(true);
            output.appendLine(automatic
                ? "Automatically reconnecting Redmi over Wi-Fi..."
                : "Checking Fendly device Wi-Fi connection...");

            try {
                const checkDevices = async () =>
                    adbDevices(await run(adb, ["devices", "-l"], root, env, output));
                let devices = await checkDevices();
                if (automatic) {
                    for (let attempt = 0; attempt < 12; attempt += 1) {
                        const hasRedmi = devices.some(isRedmiDevice);
                        const hasSavedWifiDevice = savedEndpoint && devices.some((device) =>
                            device.serial === savedEndpoint && device.state === "device"
                        );
                        if (hasRedmi || hasSavedWifiDevice) break;
                        output.appendLine("Waiting for Redmi ADB to become available...");
                        await wait(5000);
                        devices = await checkDevices();
                    }
                }
                const connectedWifi = devices.find((device) =>
                    device.state === "device"
                    && device.serial.includes(":")
                    && isRedmiDevice(device)
                );
                if (connectedWifi) {
                    await context.globalState.update("fendly.wifiAdbEndpoint", connectedWifi.serial);
                    vscode.window.showInformationMessage(`Redmi is connected over Wi-Fi at ${connectedWifi.serial}.`);
                    return;
                }

                const usbDevice = devices.find((device) =>
                    device.state === "device"
                    && !device.serial.includes(":")
                    && isRedmiDevice(device)
                );

                let endpoint = savedEndpoint;
                if (usbDevice) {
                    output.appendLine(`Preparing Wi-Fi ADB from USB device ${usbDevice.serial}...`);
                    let ipMatch = null;
                    const attempts = automatic ? 12 : 1;
                    for (let attempt = 0; attempt < attempts; attempt += 1) {
                        const ipOutput = await run(
                            adb,
                            ["-s", usbDevice.serial, "shell", "ip", "-4", "-o", "addr", "show", "wlan0"],
                            root,
                            env,
                            output,
                        );
                        ipMatch = ipOutput.match(/\binet\s+((?:\d{1,3}\.){3}\d{1,3})\/\d+/);
                        if (ipMatch && !ipMatch[1].startsWith("169.254.")) break;
                        if (attempt + 1 < attempts) {
                            output.appendLine("Waiting for Redmi Wi-Fi to become available...");
                            await wait(5000);
                        }
                    }
                    if (!ipMatch || ipMatch[1].startsWith("169.254.")) {
                        throw new Error("Redmi is not connected to Wi-Fi. Connect it and the Mac to the same network first.");
                    }
                    endpoint = `${ipMatch[1]}:5555`;
                    await run(adb, ["-s", usbDevice.serial, "tcpip", "5555"], root, env, output);
                    await new Promise((resolve) => setTimeout(resolve, 1200));
                }

                if (!endpoint) {
                    const message = "After a Redmi reboot, Android 10 disables Wi-Fi ADB. Connect it to this Mac with USB once, unlock it, and ensure both devices are on the same Wi-Fi.";
                    if (automatic) {
                        output.appendLine(message);
                        return;
                    }
                    throw new Error(message);
                }

                output.appendLine(`Connecting to ${endpoint}...`);
                let connected = false;
                const attempts = automatic ? 12 : 1;
                for (let attempt = 0; attempt < attempts; attempt += 1) {
                    await run(adb, ["connect", endpoint], root, env, output);
                    devices = await checkDevices();
                    connected = devices.some((device) =>
                        device.serial === endpoint && device.state === "device"
                    );
                    if (connected || attempt + 1 >= attempts) break;
                    output.appendLine("Waiting for Redmi Wi-Fi ADB to respond...");
                    await wait(5000);
                }
                if (!connected) {
                    throw new Error(
                        usbDevice
                            ? `Could not connect to ${endpoint}. Keep the Redmi awake and on the same Wi-Fi, then retry.`
                            : `Could not reach ${endpoint}. After a phone reboot, connect USB and click this button to re-enable Wi-Fi ADB.`
                    );
                }

                await context.globalState.update("fendly.wifiAdbEndpoint", endpoint);
                if (!automatic) {
                    vscode.window.showInformationMessage(`Redmi connected over Wi-Fi at ${endpoint}. You can unplug USB now.`);
                } else {
                    output.appendLine(`Redmi automatically connected over Wi-Fi at ${endpoint}.`);
                }
            } catch (error) {
                output.appendLine(String(error));
                if (!automatic) {
                    vscode.window.showErrorMessage(`Could not reconnect Redmi over Wi-Fi: ${error.message}`);
                }
            } finally {
                if (automatic) {
                    automaticReconnectRunning = false;
                }
            }
        }),
    );
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
            const wifiEndpoint = context.globalState.get("fendly.wifiAdbEndpoint");
            if (wifiEndpoint) {
                try {
                    const devices = adbDevices(await run(adb, ["devices", "-l"], root, env, output));
                    if (devices.some((device) => device.serial === wifiEndpoint && device.state === "device")) {
                        env.ANDROID_SERIAL = wifiEndpoint;
                        output.appendLine(`Using Wi-Fi device: ${wifiEndpoint}`);
                    }
                } catch (error) {
                    output.appendLine(String(error));
                    vscode.window.showErrorMessage(`Could not check connected Android devices: ${error.message}`);
                    return;
                }
            }

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
                        await run(
                            gradle,
                            [`-Dorg.gradle.java.home=${javaHome}`, ":app:installDebug", "--no-configuration-cache"],
                            root,
                            env,
                            output,
                        );
                        output.appendLine("Launching Fendly...");
                        const launchArgs = ["shell", "am", "start", "-n", "com.example.fendly/.SplashActivity"];
                        if (env.ANDROID_SERIAL) {
                            launchArgs.unshift(env.ANDROID_SERIAL);
                            launchArgs.unshift("-s");
                        }
                        await run(adb, launchArgs, root, env, output);
                        vscode.window.showInformationMessage("Fendly is running on your connected device.");
                    } catch (error) {
                        output.appendLine(String(error));
                        vscode.window.showErrorMessage(`Could not run Fendly: ${error.message}`);
                    }
                },
            );
        }),
    );
    const startupReconnect = setTimeout(() => {
        if (!automaticReconnectRunning) {
            automaticReconnectRunning = true;
            vscode.commands.executeCommand("fendly.reconnectWifiAdb", true);
        }
    }, 5000);
    context.subscriptions.push({ dispose: () => clearTimeout(startupReconnect) });
}

function deactivate() {}

module.exports = { activate, deactivate, javaMajorVersion, resolveJavaHome };
