package com.github.systeminvecklare.genny.processing;

import java.io.IOException;

import com.github.systeminvecklare.genny.parse.locator.IFileLocator;

public interface IGenny {
	void parseFiles(IFileLocator locator) throws IOException;
	ISourceFile findFile(String name);
	void saveChangedFiles() throws IOException;
}
