package com.github.systeminvecklare.genny.parse;

/*package-protected*/ class BaseDocumentPart implements IDocumentPart {
	private final IDocument document;
	private int firstLine;
	private int lastLine;
	
	public BaseDocumentPart(IDocument document, int firstLine, int lastLine) {
		this.document = document;
		this.firstLine = firstLine;
		this.lastLine = lastLine;
	}

	@Override
	public int getFirstLine() {
		return firstLine;
	}

	@Override
	public int getLastLine() {
		return lastLine;
	}
	
	@Override
	public IDocument getDocument() {
		return document;
	}
}
