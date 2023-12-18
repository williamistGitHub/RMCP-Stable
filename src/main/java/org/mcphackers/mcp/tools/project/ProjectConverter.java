package org.mcphackers.mcp.tools.project;

import org.json.JSONObject;
import org.mcphackers.mcp.MCP;
import org.mcphackers.mcp.MCPPaths;
import org.mcphackers.mcp.tasks.Task;
import org.mcphackers.mcp.tasks.mode.TaskMode;
import org.mcphackers.mcp.tasks.mode.TaskParameter;
import org.mcphackers.mcp.tools.Util;

import java.awt.datatransfer.FlavorEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProjectConverter {

    public static boolean convert(MCP mcp) {
        ProjectVersion version = ProjectVersion.detect(mcp);
        mcp.log("Detected project version: " + version);

        // if we couldn't detect, ignore
        // idc its ur problem
        if (version == null)
            return false;

        // dont need to convert pre3 projects
        if (version == ProjectVersion.RMCP_V1_0_PRE3)
            return false;

        mcp.showMessage("Auto-Conversion Notice", "Your project is about to be auto-converted to a supported format. This may take a while!", Task.INFO);

        // auto-backup
        try {
            String backupFileName = "old_project_" + version.toString().toLowerCase() + ".zip";
            ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(mcp.getWorkingDir().resolve(backupFileName)));

            Files.walkFileTree(mcp.getWorkingDir(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // ignore zip file that we created
                    if (file.toString().contains(backupFileName))
                        return FileVisitResult.CONTINUE;

                    out.putNextEntry(new ZipEntry(file.toString()));
                    Files.copy(file, out);
                    out.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });

            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // do other things blah blah blah
        if (version == ProjectVersion.RMCP_V1_0)
            return convertV1(mcp);
        else if (version == ProjectVersion.RMCP_V1_0_PRE2)
            return convertPre2(mcp);
        else if (version == ProjectVersion.RMCP_V1_0_PRE1)
            return convertPre1(mcp);

        // there is no physical way this statement will ever be reached (because enums)
        // but the compiler forced me to do it anyway
        // *sigh*
        return false;
    }

    private static boolean convertV1(MCP mcp) {
        // parse
        Path versionJsonPath = MCPPaths.get(mcp, "conf" + File.separator + "version.json");
        String versionString;
        try {
            JSONObject versionJson = Util.parseJSONFile(versionJsonPath);
            versionString = versionJson.getString("id");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // delete old conf/lib/jars folders
        deleteDirectory(MCPPaths.get(mcp, "conf"));
        deleteDirectory(MCPPaths.get(mcp, "libraries"));
        deleteDirectory(MCPPaths.get(mcp, "jars"));

        // re-gen with version :)
        mcp.setParameter(TaskParameter.SETUP_VERSION, versionString);
        mcp.performTask(TaskMode.SETUP, Task.Side.ANY, false);

        // move source code & md5s & stuff around
        moveFile(mcp, "minecraft/src", "src/minecraft");
        moveFile(mcp, "minecraft_server/src", "src/minecraft_server");
        moveFile(mcp, "minecraft_merged/src", "src/minecraft_merged");

        // *thanks* lassebq
        // not like file names were meant to be informative or anything
        moveFile(mcp, "minecraft/source", "temp/minecraft/src");
        moveFile(mcp, "minecraft_server/source", "temp/minecraft_server/src");
        moveFile(mcp, "minecraft_merged/source", "temp/minecraft_merged/src");

        moveFile(mcp, "minecraft/bin", "bin/minecraft");
        moveFile(mcp, "minecraft_server/bin", "bin/minecraft_server");
        moveFile(mcp, "minecraft_merged/bin", "bin/minecraft_merged");

        moveFile(mcp, "minecraft/md5/original.md5", "temp/client/original.md5");
        moveFile(mcp, "minecraft_server/md5/original.md5", "temp/server/original.md5");
        moveFile(mcp, "minecraft_merged/md5/original.md5", "temp/merged/original.md5");

        moveFile(mcp, "minecraft/jars/deobfuscated.jar", "temp/client/deobf.jar");
        moveFile(mcp, "minecraft_server/jars/deobfuscated.jar", "temp/server/deobf.jar");
        moveFile(mcp, "minecraft_merged/jars/deobfuscated.jar", "temp/merged/deobf.jar");

        // delete remains
        deleteDirectory(MCPPaths.get(mcp, "minecraft"));
        deleteDirectory(MCPPaths.get(mcp, "minecraft_server"));
        deleteDirectory(MCPPaths.get(mcp, "minecraft_merged"));

        return true;
    }

    private static boolean convertPre2(MCP mcp) {
        replaceInFile(mcp, "workspace/Client/.idea/libraries/natives.xml", "lib/natives", "lib/client/natives");

        return true;
    }

    private static boolean convertPre1(MCP mcp) {
        replaceInFile(mcp, "workspace/.metadata/.plugins/org.eclipse.debug.core/.launches/Server.launch",
                "net/minecraft/server/MinecraftServer", "Start",
                "net.minecraft.server.MinecraftServer", "Start"
        );

        replaceInFile(mcp, "workspace/Server/.idea/workspace.xml",
                "net.minecraft.server.MinecraftServer", "Start");

        replaceInFile(mcp, "workspace/Client/.idea/libraries/natives.xml", "lib/natives", "lib/client/natives");

        moveFile(mcp, "temp/src/client", "temp/client/src");
        moveFile(mcp, "temp/src/server", "temp/server/src");
        deleteDirectory(MCPPaths.get(mcp, "temp" + File.separator + "src"));

        moveFile(mcp, "temp/client.md5", "temp/client/original.md5");
        moveFile(mcp, "temp/client_deobf.jar", "temp/client/deobf.jar");
        moveFile(mcp, "temp/client_deobf.tiny", "temp/client/deobf.tiny");
        moveFile(mcp, "temp/client_exc.jar", "temp/client/exc.jar");
        moveFile(mcp, "temp/client_src.zip", "temp/client/src.zip");

        moveFile(mcp, "temp/server.md5", "temp/server/original.md5");
        moveFile(mcp, "temp/server_deobf.jar", "temp/server/deobf.jar");
        moveFile(mcp, "temp/server_deobf.tiny", "temp/server/deobf.tiny");
        moveFile(mcp, "temp/server_exc.jar", "temp/server/exc.jar");
        moveFile(mcp, "temp/server_src.zip", "temp/server/src.zip");

        return true;
    }

    private static void replaceInFile(MCP mcp, String fileName, String... findAndReplace) {
        if (findAndReplace.length % 2 != 0) {
            throw new IllegalArgumentException("replaceInFile needs a multiple of 2 find and replace arguments");
        }
        final int findAndReplaces = findAndReplace.length / 2;

        Path filePath = MCPPaths.get(mcp, fileName.replace("/", File.separator));
        if (!Files.exists(filePath))
            return;

        try {
            Stream<String> lines = Files.lines(filePath);
            List<String> newLines = new ArrayList<>();

            lines.forEachOrdered(line -> {
                for (int i = 0; i < findAndReplaces; i+=2) {
                    line = line.replace(findAndReplace[i], findAndReplace[i+1]);
                }
                newLines.add(line);
            });

            lines.close();

            Files.write(filePath, newLines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void deleteDirectory(Path path) {
        if (!Files.exists(path))
            return;

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void moveFile(MCP mcp, String from, String to) {
        Path fromPath = MCPPaths.get(mcp, from.replace("/", File.separator));
        if (!Files.exists(fromPath))
            return;

        Path toPath = MCPPaths.get(mcp, to.replace("/", File.separator));

        try {
            // make sure new dirs exist
            if (!to.contains("."))
                Files.createDirectories(toPath);
            else
                Files.createDirectories(toPath.getParent());


            if (Files.isDirectory(fromPath)) {
                // if fromPath is a directory, we cant move atomically (sadly)
                Files.move(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
            } else
                Files.move(fromPath, toPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
