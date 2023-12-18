package org.mcphackers.mcp.tools.project;

import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public enum ProjectVersion {

    RMCP_V1_0,
    RMCP_V1_0_PRE3,
    RMCP_V1_0_PRE2,
    RMCP_V1_0_PRE1;

    public static ProjectVersion detect(MCP mcp) {

        if (Files.exists(MCPPaths.get(mcp, "conf" + File.separator + "version.json")))
            return RMCP_V1_0;

        Path path = MCPPaths.get(mcp, "workspace" + File.separator + "Client" + File.separator +  ".idea" + File.separator + "libraries" + File.separator +  "natives.xml");
        if (Files.exists(path)) {
            try (Stream<String> lines = Files.lines(path)) {
                if (lines.anyMatch(line -> line.contains("client/")))
                    return RMCP_V1_0_PRE3;
            } catch (IOException ignored) {
                return null;
            }
        }

        path = MCPPaths.get(mcp, "workspace" + File.separator + "Server" + File.separator +  ".idea" + File.separator +  "workspace.xml");
        if (Files.exists(path)) {
            try (Stream<String> lines = Files.lines(path)) {
                System.out.println();
                if (lines.anyMatch(line -> line.contains("net.minecraft.server.MinecraftServer")))
                    return RMCP_V1_0_PRE1;
                return RMCP_V1_0_PRE2;
            } catch (IOException ignored) {
                return null;
            }
        }

        return null;
    }

}
