package org.mcphackers.mcp.tools.fernflower;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.java.decompiler.main.decompiler.BaseDecompiler;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.mcphackers.mcp.tasks.ProgressListener;

public class Decompiler implements IBytecodeProvider {
	public final DecompileLogger log;
	private final Path source;
	private final Path destination;
	private final Map<String, Object> mapOptions = new HashMap<>();
	private final ZipFileCache openZips = new ZipFileCache();

	public Decompiler(ProgressListener listener, Path source, Path out, Path javadocs, String ind, boolean override) {
		this.source = source;
		this.destination = out;
		this.log = new DecompileLogger(listener);
		mapOptions.put(IFernflowerPreferences.OVERRIDE_ANNOTATION, override ? "1" : "0");
		mapOptions.put(IFernflowerPreferences.NO_COMMENT_OUTPUT, "1");
		mapOptions.put(IFernflowerPreferences.REMOVE_BRIDGE, "0");
		mapOptions.put(IFernflowerPreferences.ASCII_STRING_CHARACTERS, "1");
		mapOptions.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
		mapOptions.put(IFernflowerPreferences.INDENT_STRING, ind);
	}

	public void decompile() throws IOException {
		BaseDecompiler decompiler = new BaseDecompiler(this, new SingleFileSaver(destination), mapOptions, log);
		decompiler.addSpace(source.toAbsolutePath().toFile(), true);
		decompiler.decompileContext();
	}

	@Override
	public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
		if (internalPath == null) {
			File file = new File(externalPath);
			return InterpreterUtil.getBytes(file);
		} else {
			final ZipFile archive = this.openZips.get(externalPath);
			final ZipEntry entry = archive.getEntry(internalPath);
			if (entry == null) {
				throw new IOException("Entry not found: " + internalPath);
			}
			return InterpreterUtil.getBytes(archive, entry);
		}
	}
}