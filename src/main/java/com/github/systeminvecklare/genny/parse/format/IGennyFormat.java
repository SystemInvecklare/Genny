package com.github.systeminvecklare.genny.parse.format;

public interface IGennyFormat {
	IMatch getPotentialGennySubstring(String text);
	String wrapGennySubstring(String substring);

	public interface IMatch {
		String getContent();
		String getIndent();
	}
	
	public static final class Match implements IMatch {
		private final String indent;
		private final String content;
		
		public Match(String indent, String content) {
			this.indent = indent;
			this.content = content;
		}

		@Override
		public String getContent() {
			return content;
		}

		@Override
		public String getIndent() {
			return indent;
		}
	}
}
