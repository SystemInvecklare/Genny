package com.github.systeminvecklare.genny.parse;

import com.github.systeminvecklare.genny.hash.IHash;

public class GennyHashTag extends BaseDocumentPart implements IGennyTag {
	private final IHash hash;

	public GennyHashTag(IDocument document, int lineNumber, IHash hash) {
		super(document, lineNumber, lineNumber);
		this.hash = hash;
	}

	public IHash getHash() {
		return hash;
	}
}
