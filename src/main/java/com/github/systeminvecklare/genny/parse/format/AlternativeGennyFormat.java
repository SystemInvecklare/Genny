package com.github.systeminvecklare.genny.parse.format;

import java.util.regex.Pattern;

public class AlternativeGennyFormat extends PatternMatchingGennyFormat {
	private static final Pattern GENNY_CONTAINER_PATTERN = Pattern.compile("(?<indent>\\s*)///(?<content>[^/].+)\\s*");

	public AlternativeGennyFormat() {
		super(GENNY_CONTAINER_PATTERN, "indent","content");
	}
	
	@Override
	public String wrapGennySubstring(String substring) {
		return "///"+substring;
	}
}
