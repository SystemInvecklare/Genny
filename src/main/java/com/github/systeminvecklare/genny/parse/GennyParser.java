package com.github.systeminvecklare.genny.parse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.systeminvecklare.genny.hash.IHash;
import com.github.systeminvecklare.genny.hash.IHashFactory;
import com.github.systeminvecklare.genny.hash.UnparsableHash;
import com.github.systeminvecklare.genny.parse.format.IGennyFormat;

public class GennyParser implements IGennyParser {
	private static final Pattern GENNY_BLOCK_PATTERN = Pattern.compile("GENNY_BLOCK\\((?<name>.*)\\)\\s*");
	private static final Pattern GENNY_HASH_PATTERN = Pattern.compile("GENNY_HASH: (?<hash>.*)\\s*");
	private static final Pattern GENNY_END_PATTERN = Pattern.compile("GENNY_END\\s*");
	private static final Pattern GENNY_FORCE_PATTERN = Pattern.compile("GENNY_FORCE\\s*");

	@Override
	public IDocument parse(Iterator<String> lines, IHashFactory hashFactory, IGennyFormat gennyFormat) {
		return new Parsing(lines, hashFactory, gennyFormat).getResult();
	}

	private static class Document implements IDocument {
		private final List<IDocumentPart> parts = new ArrayList<IDocumentPart>();

		@Override
		public List<IDocumentPart> getParts() {
			return parts;
		}
	}
	
	private class Parsing {
		private final IHashFactory hashFactory;
		private final IGennyFormat gennyFormat;
		private final Document document = new Document();
		private IState state = new DefaultState();
		
		public Parsing(Iterator<String> lines, IHashFactory hashFactory, IGennyFormat gennyFormat) {
			this.hashFactory = hashFactory;
			this.gennyFormat = gennyFormat;
			int nextLineNumber = 1;
			while(lines.hasNext()) {
				state.feed(new Line(nextLineNumber++, lines.next()));
			}
			state.onEnd();
		}

		public IDocument getResult() {
			return document;
		}
		
		public void changeState(IState newState) {
			state.onEnd();
			state = newState;
		}
		
		public void addTextPart(int firstLine, int lastLine, List<String> lines) {
			document.parts.add(new TextPart(document, firstLine, lastLine, lines));
		}
		
		public void addGennyBlockStartTag(int lineNumber, String indentation, String name) {
			document.parts.add(new GennyBlockStartTag(document, lineNumber, indentation, name));
		}
		
		public void addGennyHashTag(int lineNumber, String stringHash) {
			IHash hash;
			try {
				hash = hashFactory.parseHash(stringHash);
			} catch(RuntimeException e) {
				hash = UnparsableHash.exception(stringHash, e);
				System.out.println("Warning: Failed to parse hash. "+e.getMessage());
				e.printStackTrace(System.out);
			}
			document.parts.add(new GennyHashTag(document, lineNumber, hash));
		}
		
		public void addGennyEndTag(int lineNumber) {
			document.parts.add(new GennyEndTag(document, lineNumber));
		}
		
		public void addGennyForceTag(int lineNumber) {
			document.parts.add(new GennyForceTag(document, lineNumber));
		}
		
		private class DefaultState implements IState {
			private int firstLine = -1;
			private int lastLine = -1;
			private final List<String> lines = new ArrayList<>();
			@Override
			public void feed(Line line) {
				IGennyFormat.IMatch match = gennyFormat.getPotentialGennySubstring(line.text);
				if(match != null) {
					String content = match.getContent();
					
					Matcher blockMatcher = GENNY_BLOCK_PATTERN.matcher(content);
					if(blockMatcher.matches()) {
						String name = blockMatcher.group("name");
						changeState(new DefaultState());
						addGennyBlockStartTag(line.number, match.getIndent(), name);
						return;
					}
					
					Matcher hashMatcher = GENNY_HASH_PATTERN.matcher(content);
					if(hashMatcher.matches()) {
						String hash = hashMatcher.group("hash");
						changeState(new DefaultState());
						addGennyHashTag(line.number, hash);
						return;
					}
					
					Matcher endMatcher = GENNY_END_PATTERN.matcher(content);
					if(endMatcher.matches()) {
						changeState(new DefaultState());
						addGennyEndTag(line.number);
						return;
					}
					
					Matcher forceMatcher = GENNY_FORCE_PATTERN.matcher(content);
					if(forceMatcher.matches()) {
						changeState(new DefaultState());
						addGennyForceTag(line.number);
						return;
					}
				}
				
				if(firstLine == -1) {
					firstLine = line.number;
				}
				lines.add(line.text);
				lastLine = Math.max(lastLine, line.number);
			}
			@Override
			public void onEnd() {
				if(!lines.isEmpty()) {
					addTextPart(firstLine, lastLine, lines);
				}
			}
		}
	}
	
	
	private interface IState {
		void feed(Line line);
		void onEnd();
	}
	
	private static class Line {
		public final int number;
		public final String text;
		
		public Line(int number, String text) {
			this.number = number;
			this.text = text;
		}
	}
}
