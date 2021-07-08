package com.github.systeminvecklare.genny.parse.format;

import java.util.regex.Pattern;

public class HtmlGennyFormat extends PatternMatchingGennyFormat {
	private static final Pattern GENNY_CONTAINER_PATTERN = Pattern.compile("(?<indent>\\s*)\\<!---\\s?(?<content>.+)\\s?--->\\s*");

	public HtmlGennyFormat() {
		super(GENNY_CONTAINER_PATTERN, "indent","content");
	}
	
	@Override
	public String wrapGennySubstring(String substring) {
		return "<!---"+substring+"--->";
	}
}
