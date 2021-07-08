package com.github.systeminvecklare.genny.parse.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PatternMatchingGennyFormat implements IGennyFormat {
	private final Pattern pattern;
	private final String indentGroupName;
	private final String contentGroupName;
	
	public PatternMatchingGennyFormat(Pattern pattern, String indentGroupName, String contentGroupName) {
		this.pattern = pattern;
		this.indentGroupName = indentGroupName;
		this.contentGroupName = contentGroupName;
	}

	@Override
	public IMatch getPotentialGennySubstring(String text) {
		Matcher matcher = pattern.matcher(text);
		if(matcher.matches()) {
			return new Match(matcher.group(indentGroupName), matcher.group(contentGroupName));
		} 
		return null;
	}
}
