package com.github.systeminvecklare.genny.processing;

import com.mattiasselin.linewriter.ILineSource;

public interface IGennyBlock {
	String getName();
	void inject(ILineSource lineSource);
}
