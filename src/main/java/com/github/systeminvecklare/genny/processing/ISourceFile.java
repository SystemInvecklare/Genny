package com.github.systeminvecklare.genny.processing;

import java.util.List;

public interface ISourceFile {
	List<IGennyBlock> getBlocks();
	IGennyBlock getBlock(String name);
}
