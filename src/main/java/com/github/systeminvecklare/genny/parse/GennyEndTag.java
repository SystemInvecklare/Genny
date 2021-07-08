package com.github.systeminvecklare.genny.parse;

public class GennyEndTag extends BaseDocumentPart implements IGennyTag {
	public GennyEndTag(IDocument document, int lineNumber) {
		super(document, lineNumber, lineNumber);
	}
}
