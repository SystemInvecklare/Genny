package com.github.systeminvecklare.genny.processing;

import com.mattiasselin.linewriter.AbstractLineWriter;
import com.mattiasselin.linewriter.ILineWriter;

/*package-protected*/ class OffsetLineWriter extends AbstractLineWriter {
	private final String offset;
	private final ILineWriter wrapped;
	private String indent = "    ";
	private final boolean skipLastNewline;
	private boolean bufferedNewline = false;
	
	public OffsetLineWriter(String offset,  ILineWriter wrapped) {
		this(offset, wrapped, false);
	}
	
	public OffsetLineWriter(String offset, ILineWriter wrapped, boolean skipLastNewline) {
		this(offset, 0, wrapped, skipLastNewline);
	}

	private OffsetLineWriter(String offset, int indentAmount, ILineWriter wrapped, boolean skipLastNewline) {
		super(indentAmount);
		this.offset = offset;
		this.wrapped = wrapped;
		this.skipLastNewline = skipLastNewline;
	}

	@Override
	protected OffsetLineWriter newIndented(int indentAmount) {
		return new OffsetLineWriter(offset, indentAmount, wrapped, skipLastNewline);
	}

	@Override
	protected void doPrintIndent(int indentAmount) {
		writeBufferedNewlineIfAny();
		wrapped.print(offset);
		for(int i = 0; i < indentAmount; ++i) {
			wrapped.print(indent);
		}
	}

	@Override
	protected void doPrint(String text) {
		writeBufferedNewlineIfAny();
		wrapped.print(text);
	}

	@Override
	protected void doPrintln() {
		writeBufferedNewlineIfAny();
		if(skipLastNewline) {
			bufferedNewline = true;
		} else {
			wrapped.println();
		}
	}

	private void writeBufferedNewlineIfAny() {
		if(bufferedNewline) {
			bufferedNewline = false;
			wrapped.println();
		}
	}
}
