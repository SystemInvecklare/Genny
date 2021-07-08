package com.github.systeminvecklare.genny.parse.locator;

import java.io.File;

public interface IFileLocator {
	void locateFiles(IFileSink fileSink);
	
	interface IFileSink {
		void onFile(File file);
	}
}
