package com.github.systeminvecklare.genny.parse;

public class GennyBlockStartTag extends BaseDocumentPart implements IGennyTag {
	private final String indentation;
	private final String name;

	public GennyBlockStartTag(IDocument document, int lineNumber, String indentation, String name) {
		super(document, lineNumber, lineNumber);
		this.indentation = indentation;
		this.name = name;
	}

	public String getIndentation() {
		return indentation;
	}

	public String getName() {
		return name;
	}
}
