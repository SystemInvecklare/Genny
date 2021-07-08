package com.github.systeminvecklare.genny.parse;

import java.util.List;

public class TextPart extends BaseDocumentPart implements IDocumentPart {
	private final List<String> lines;

	public TextPart(IDocument document, int firstLine, int lastLine, List<String> lines) {
		super(document, firstLine, lastLine);
		this.lines = lines;
	}
	
	public List<String> getLines() {
		return lines;
	}
}
