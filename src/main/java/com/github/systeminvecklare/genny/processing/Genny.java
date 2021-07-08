package com.github.systeminvecklare.genny.processing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.systeminvecklare.genny.exception.GennyException;
import com.github.systeminvecklare.genny.hash.HashFactory;
import com.github.systeminvecklare.genny.hash.IHash;
import com.github.systeminvecklare.genny.hash.IHashComputation;
import com.github.systeminvecklare.genny.hash.IHashFactory;
import com.github.systeminvecklare.genny.hash.IHashable;
import com.github.systeminvecklare.genny.parse.GennyBlockStartTag;
import com.github.systeminvecklare.genny.parse.GennyEndTag;
import com.github.systeminvecklare.genny.parse.GennyForceTag;
import com.github.systeminvecklare.genny.parse.GennyHashTag;
import com.github.systeminvecklare.genny.parse.GennyParser;
import com.github.systeminvecklare.genny.parse.IDocument;
import com.github.systeminvecklare.genny.parse.IDocumentPart;
import com.github.systeminvecklare.genny.parse.IGennyParser;
import com.github.systeminvecklare.genny.parse.IGennyTag;
import com.github.systeminvecklare.genny.parse.TextPart;
import com.github.systeminvecklare.genny.parse.format.DefaultGennyFormat;
import com.github.systeminvecklare.genny.parse.format.IGennyFormat;
import com.github.systeminvecklare.genny.parse.locator.IFileLocator;
import com.mattiasselin.linewriter.ILineSource;
import com.mattiasselin.linewriter.ILineWriter;
import com.mattiasselin.linewriter.WriterLineWriter;

public class Genny implements IGenny {
	public static class Builder {
		private IGennyParser parser = new GennyParser();
		private Charset defaultCharset = StandardCharsets.UTF_8;
		private IHashFactory hashFactory = new HashFactory();
		private IGennyFormat gennyFormat = new DefaultGennyFormat();
		
		public Builder setParser(IGennyParser parser) {
			this.parser = parser;
			return this;
		}
		
		public IGennyParser getParser() {
			return parser;
		}
		
		public Builder setDefaultCharset(Charset defaultCharset) {
			this.defaultCharset = defaultCharset;
			return this;
		}
		
		public Charset getDefaultCharset() {
			return defaultCharset;
		}
		
		public Builder setHashFactory(IHashFactory hashFactory) {
			this.hashFactory = hashFactory;
			return this;
		}
		
		public IHashFactory getHashFactory() {
			return hashFactory;
		}
		
		public IGennyFormat getGennyFormat() {
			return gennyFormat;
		}
		
		public Builder setGennyFormat(IGennyFormat gennyFormat) {
			this.gennyFormat = gennyFormat;
			return this;
		}
		
		public IGenny build() {
			return new Genny(parser, defaultCharset, hashFactory, gennyFormat);
		}
	}
	private final IGennyParser parser;
	private final Charset defaultCharset;
	private final IHashFactory hashFactory;
	private final Map<String, IGennyFile> gennyFiles = new HashMap<>();
	private final IGennyFormat gennyFormat;
	
	private Genny(IGennyParser parser, Charset defaultCharset, IHashFactory hashFactory, IGennyFormat gennyFormat) {
		this.parser = parser;
		this.defaultCharset = defaultCharset;
		this.hashFactory = hashFactory;
		this.gennyFormat = gennyFormat;
	}

	@Override
	public void parseFiles(IFileLocator locator) throws IOException {
		Set<String> normalizedAbsolutePaths = new HashSet<>();
		locator.locateFiles(new IFileLocator.IFileSink() {
			@Override
			public void onFile(File file) {
				normalizedAbsolutePaths.add(file.toPath().toAbsolutePath().normalize().toString());
			}
		});
		
		for(String path : normalizedAbsolutePaths) {
			gennyFiles.remove(path);
			Charset charset = defaultCharset;
			List<String> lines = readLines(path, charset);
			IDocument document = parser.parse(lines.iterator(), hashFactory, gennyFormat);
			IGennyFile gennyFile;
			if(!containsGennyTags(document)) {
				gennyFile = new NonGennyFile(path);
			} else {
				gennyFile = parseDocument(path, document, charset);
			}
			gennyFiles.put(path, gennyFile);
		}
	}

	private static boolean containsGennyTags(IDocument document) {
		for(IDocumentPart part : document.getParts()) {
			if(part instanceof IGennyTag) {
				return true;
			}
		}
		return false;
	}

	private static List<String> readLines(String path, Charset charset) throws IOException {
		try(InputStream inputStream = new FileInputStream(path)) {
			InputStreamReader reader = new InputStreamReader(inputStream, charset);
			BufferedReader bufferedReader = new BufferedReader(reader);
			List<String> lines = new ArrayList<>();
			String line = null;
			while((line = bufferedReader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		}
	}
	
	private GennyFile parseDocument(String path, IDocument document, Charset charset) {
		GennyFile gennyFile = new GennyFile(path, charset);
		
		GennyBlock currentGennyBlock = null;
		for(IDocumentPart documentPart : document.getParts()) {
			if(documentPart instanceof TextPart) {
				if(currentGennyBlock == null) {
					gennyFile.addTextLines(((TextPart) documentPart).getLines());
				} else {
					currentGennyBlock.appendContent(((TextPart) documentPart).getLines());
				}
			} else if(documentPart instanceof GennyBlockStartTag) {
				GennyBlockStartTag startTag = (GennyBlockStartTag) documentPart;
				if(currentGennyBlock != null) {
					throw new GennyException("Expected GENNY_END for GENNY_BLOCK("+currentGennyBlock.startTag.getName()+") at line "+currentGennyBlock.startTag.getFirstLine()+" before new block "+startTag.getName()+" at "+startTag.getFirstLine()+" in "+path);
				} else {
					currentGennyBlock = new GennyBlock(startTag, hashFactory, gennyFile);
				}
			} else if(documentPart instanceof GennyHashTag) {
				if(currentGennyBlock == null) {
					throw new GennyException("Unexpected GENNY_HASH outside GENNY_BLOCK. GENNY_HASH is at line "+documentPart.getFirstLine()+" in "+path);
				} else {
					currentGennyBlock.savedHash = ((GennyHashTag) documentPart).getHash();
				}
			} else if(documentPart instanceof GennyEndTag) {
				if(currentGennyBlock == null) {
					throw new GennyException("Unexpected GENNY_END outside GENNY_BLOCK. GENNY_END is at line "+documentPart.getFirstLine()+" in "+path);
				} else {
					currentGennyBlock.validateContent();
					gennyFile.addGennyBlock(currentGennyBlock);
					currentGennyBlock = null;
				}
			} else if(documentPart instanceof GennyForceTag) {
				if(currentGennyBlock == null) {
					throw new GennyException("Unexpected GENNY_FORCE outside GENNY_BLOCK. GENNY_FORCE is at line "+documentPart.getFirstLine()+" in "+path);
				} else {
					currentGennyBlock.force = true;
				}
			} else {
				throw new IllegalStateException("Unknown "+IDocumentPart.class.getSimpleName()+" type: "+documentPart.getClass().getName());
			}
		}
		if(currentGennyBlock != null) {
			throw new GennyException("Missing GENNY_END for GENNY_BLOCK("+currentGennyBlock.startTag.getName()+") at line "+currentGennyBlock.startTag.getFirstLine()+" in "+path);
		}
		return gennyFile;
	}

	@Override
	public ISourceFile findFile(String name) {
		List<IGennyFile> matches = new ArrayList<>();
		for(Entry<String, IGennyFile> entry : gennyFiles.entrySet()) {
			if(name.equals(new File(entry.getKey()).getName())) {
				matches.add(entry.getValue());
			}
		}
		if(matches.isEmpty()) {
			throw new IllegalArgumentException("Could not find file "+name);
		} else if(matches.size() > 1) {
			StringBuilder builder = new StringBuilder();
			for(IGennyFile sourceFile : matches) {
				builder.append(sourceFile.getPath()+", ");
			}
			throw new IllegalArgumentException("Found multiple files with name "+name+": "+builder.toString());
		}
		return matches.get(0);
	}

	@Override
	public void saveChangedFiles() throws IOException {
		Set<GennyFile> changedFiles = new HashSet<>();
		for(IGennyFile file : gennyFiles.values()) {
			if(file instanceof GennyFile) {
				GennyFile gennyFile = (GennyFile) file;
				if(gennyFile.applyReplacements()) {
					changedFiles.add(gennyFile);
				}
			}
		}
		
		for(GennyFile file : changedFiles) {
			try(FileOutputStream fileOutputStream = new FileOutputStream(new File(file.path))) {
				file.save(fileOutputStream);
				fileOutputStream.flush();
			}
		}
	}

	public static IGenny newGenny() {
		return new Builder().build();
	}
	
	private interface IGennyFile extends ISourceFile {
		String getPath();
	}
	
	private static class NonGennyFile implements IGennyFile {
		private final String path;

		public NonGennyFile(String path) {
			this.path = path;
		}

		@Override
		public List<IGennyBlock> getBlocks() {
			return Collections.emptyList();
		}

		@Override
		public IGennyBlock getBlock(String name) {
			throw new IllegalArgumentException("No GENNY tags at all found in file "+path);
		}

		@Override
		public String getPath() {
			return path;
		}
	}
	
	private static class GennyFile implements IGennyFile {
		private final String path;
		private final Charset charset;
		private final List<IGennyFilePart> parts = new ArrayList<IGennyFilePart>();
		private final Map<String, GennyBlock> gennyBlocks = new LinkedHashMap<>();
		
		public GennyFile(String path, Charset charset) {
			this.path = path;
			this.charset = charset;
		}

		public void save(OutputStream outputStream) {
			WriterLineWriter writer = new WriterLineWriter(new OutputStreamWriter(outputStream, charset), true);
			for(IGennyFilePart part : parts) {
				part.writeTo(writer);
			}
		}

		public boolean applyReplacements() {
			boolean fileChanged = false;
			for(GennyBlock gennyBlock : gennyBlocks.values()) {
				boolean blockChanged = gennyBlock.applyReplacement();
				if(blockChanged) {
					fileChanged = true;
				}
			}
			return fileChanged;
		}

		public void addGennyBlock(GennyBlock gennyBlock) {
			parts.add(gennyBlock);
			if(gennyBlocks.put(gennyBlock.getName(), gennyBlock) != null) {
				throw new IllegalArgumentException("Duplicated GENNY_BLOCKs named "+gennyBlock.getName()+" in "+path);
			}
		}

		public void addTextLines(List<String> lines) {
			parts.add(new TextLines(lines));
		}

		@Override
		public List<IGennyBlock> getBlocks() {
			return new ArrayList<>(gennyBlocks.values());
		}

		@Override
		public IGennyBlock getBlock(String name) {
			GennyBlock gennyBlock = gennyBlocks.get(name);
			if(gennyBlock == null) {
				throw new IllegalArgumentException("No GENNY_BLOCK named "+name+" in file "+path);
			}
			return gennyBlock;
		}
		
		@Override
		public String getPath() {
			return path;
		}
	}
	
	private interface IGennyFilePart {
		void writeTo(ILineWriter lineWriter);
	}
	
	private static class TextLines implements IGennyFilePart {
		private final List<String> lines;

		public TextLines(List<String> lines) {
			this.lines = lines;
		}

		@Override
		public void writeTo(ILineWriter writer) {
			for(String line : lines) {
				writer.println(line);
			}
		}
	}
	
	private class GennyBlock implements IGennyBlock, IGennyFilePart {
		public final GennyBlockStartTag startTag;
		public IHash savedHash = null;
		private final String name;
		private final IHashFactory hashFactory;
		private String currentContent = "";
		private final GennyFile gennyFile;
		private ILineSource replacement = null;
		public boolean force = false;
		
		public GennyBlock(GennyBlockStartTag startTag, IHashFactory hashFactory, GennyFile gennyFile) {
			this.startTag = startTag;
			this.name = startTag.getName();
			this.hashFactory = hashFactory;
			this.gennyFile = gennyFile;
		}

		public void appendContent(List<String> lines) {
			StringBuilder builder = new StringBuilder();
			boolean first = "".equals(currentContent);
			for(String line : lines) {
				if(first) {
					first = false;
				} else {
					builder.append("\n");
				}
				builder.append(line);
			}
			currentContent += builder.toString();
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void inject(ILineSource lineSource) {
			replacement = lineSource;
		}
		
		public boolean applyReplacement() {
			if(replacement != null) {
				StringWriter stringWriter = new StringWriter();
				WriterLineWriter writerLineWriter = new WriterLineWriter(stringWriter, true);
				replacement.writeTo(new OffsetLineWriter(startTag.getIndentation(), writerLineWriter, true));
				String replacementText = stringWriter.getBuffer().toString();
				IHash replacementHash = calculateHash(replacementText);
				if(force || savedHash == null || (!savedHash.equals(replacementHash))) {
					// We should overwrite (if we are allowed to)
					validateOverwrite();
					currentContent = replacementText;
					savedHash = replacementHash;
					return true;
				}
			}
			return false;
		}

		private void validateOverwrite() {
			validateContent();
		}

		private void validateContent() {
			if(force) {
				return;
			}
			if(savedHash == null) {
				if(currentContent.trim().length() != 0) {
					throw new GennyException("GENNY_BLOCK("+startTag.getName()+") at line "+startTag.getFirstLine()+" is missing GENNY_HASH and has non-whitespace content. Remove non-whitespace content or add GENNY_FORCE tag to trigger regeneration. In file "+gennyFile.path);
				}
			} else {
				IHash currentHash = calculateCurrentHash();
				if(!savedHash.equals(calculateCurrentHash())) {
					throw new GennyException("Mismatch between GENNY_HASH ("+savedHash+") and calculated hash ("+currentHash+") for GENNY_BLOCK("+startTag.getName()+") at line "+startTag.getFirstLine()+" in file "+gennyFile.path);
				}
			}
		}

		public IHash calculateCurrentHash() {
			return calculateHash(currentContent);
		}
		
		private IHash calculateHash(final String content) {
			return hashFactory.calculateHash(new IHashable() {
				@Override
				public void supply(IHashComputation computation) {
					computation.update(content.getBytes(gennyFile.charset));
				}
			});
		}
		
		@Override
		public void writeTo(ILineWriter lineWriter) {
			String indentation = startTag.getIndentation();
			lineWriter.print(indentation).println(gennyFormat.wrapGennySubstring("GENNY_BLOCK("+name+")"));
			lineWriter.print(indentation).println(gennyFormat.wrapGennySubstring("GENNY_HASH: "+calculateCurrentHash()));
			lineWriter.println(currentContent);
			lineWriter.print(indentation).println(gennyFormat.wrapGennySubstring("GENNY_END"));
		}
	}
}
