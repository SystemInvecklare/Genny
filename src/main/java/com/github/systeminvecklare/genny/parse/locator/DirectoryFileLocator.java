package com.github.systeminvecklare.genny.parse.locator;

import java.io.File;

public class DirectoryFileLocator implements IFileLocator {
	private final File directory;

	public DirectoryFileLocator(File directory) {
		directory = directory.getAbsoluteFile();
		if(!directory.isDirectory()) {
			throw new IllegalArgumentException(directory.getAbsolutePath()+" is not a directory");
		}
		this.directory = directory; 
	}

	@Override
	public void locateFiles(IFileSink fileSink) {
		locateRecursively(directory, fileSink);
	}
	
	private static void locateRecursively(File directory, IFileSink fileSink) {
		for(File file : directory.listFiles()) {
			if(file.isDirectory()) {
				locateRecursively(file, fileSink);
			} else {
				fileSink.onFile(file);
			}
		}
	}

}
