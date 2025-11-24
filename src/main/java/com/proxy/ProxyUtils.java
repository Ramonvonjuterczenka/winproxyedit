package com.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ProxyUtils {

    public static String getSystemProxyServer() throws ProxyException {
        return readRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "ProxyServer");
    }

    public static String getSystemProxyExceptions() throws ProxyException {
        return readRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "ProxyOverride");
    }

    public static String getSystemAutoConfigUrl() throws ProxyException {
        return readRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "AutoConfigURL");
    }

    public static String getWinHttpProxy() throws ProxyException {
        try {
            Process process = new ProcessBuilder("netsh", "winhttp", "show", "proxy").start();
            String output = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(Collectors.joining("\n"));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String error = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().collect(Collectors.joining("\n"));
                throw new IOException("netsh command failed with exit code " + exitCode + ": " + error);
            }
            if (output.contains("Direct access")) {
                return "";
            }
            String[] lines = output.split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().startsWith("Proxy Server(s):")) {
                    return line.substring(line.indexOf(":") + 1).trim();
                }
            }
            return "Not found";
        } catch (IOException | InterruptedException e) {
            throw new ProxyException("Error reading WinHTTP proxy", e);
        }
    }

    public static String getGitProxy(String scope, String protocol) throws ProxyException {
        try {
            Process process = new ProcessBuilder("git", "config", "--" + scope, protocol + ".proxy").start();
            String output = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            process.waitFor();
            return output != null ? output : "";
        } catch (IOException | InterruptedException e) {
            throw new ProxyException("Error reading Git proxy for " + protocol, e);
        }
    }

    private static String readRegistry(String key, String value) throws ProxyException {
        try {
            Process process = new ProcessBuilder("reg", "query", key, "/v", value).start();
            String output = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().collect(Collectors.joining("\n"));
            process.waitFor();
            if (output.contains("ERROR")) {
                return "";
            }
            String[] lines = output.split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().startsWith(value)) {
                    String[] parts = line.trim().split("\\s+");
                    return parts.length > 2 ? parts[parts.length - 1] : "";
                }
            }
            return "";
        } catch (IOException | InterruptedException e) {
            throw new ProxyException("Error reading registry key: " + key, e);
        }
    }

    public static void setSystemProxy(String proxyServer, String exceptions) throws ProxyException {
        writeRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "ProxyEnable", "REG_DWORD", "1");
        writeRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "ProxyServer", "REG_SZ", proxyServer);
        writeRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "ProxyOverride", "REG_SZ", exceptions);
    }

    public static void setSystemAutoConfigUrl(String url) throws ProxyException {
        writeRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "AutoConfigURL", "REG_SZ", url);
    }

    public static void setWinHttpProxy(String proxyServer) throws ProxyException {
        try {
            Process process = new ProcessBuilder("netsh", "winhttp", "set", "proxy", proxyServer).start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String error = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().collect(Collectors.joining("\n"));
                throw new IOException("netsh command failed with exit code " + exitCode + ": " + error);
            }
        } catch (IOException | InterruptedException e) {
            throw new ProxyException("Error setting WinHTTP proxy", e);
        }
    }

    public static void setGitProxy(String scope, String protocol, String proxy) throws ProxyException {
        try {
            ProcessBuilder pb;
            if (proxy == null || proxy.trim().isEmpty()) {
                pb = new ProcessBuilder("git", "config", "--" + scope, "--unset", protocol + ".proxy");
            } else {
                pb = new ProcessBuilder("git", "config", "--" + scope, protocol + ".proxy", proxy);
            }
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String error = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().collect(Collectors.joining("\n"));
                if (!error.isEmpty()) {
                    throw new IOException("git config command failed: " + error);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new ProxyException("Error setting Git proxy for " + protocol, e);
        }
    }

    private static void writeRegistry(String key, String value, String type, String data) throws ProxyException {
        try {
            Process process = new ProcessBuilder("reg", "add", key, "/v", value, "/t", type, "/d", data, "/f").start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String error = new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().collect(Collectors.joining("\n"));
                throw new IOException("reg add command failed with exit code " + exitCode + ": " + error);
            }
        } catch (IOException | InterruptedException e) {
            throw new ProxyException("Error writing to registry key: " + key, e);
        }
    }
}
