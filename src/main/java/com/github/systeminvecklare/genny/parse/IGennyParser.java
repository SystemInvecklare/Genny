package com.github.systeminvecklare.genny.parse;

import java.util.Iterator;

import com.github.systeminvecklare.genny.hash.IHashFactory;
import com.github.systeminvecklare.genny.parse.format.IGennyFormat;

public interface IGennyParser {
	IDocument parse(Iterator<String> lines, IHashFactory hashFactory, IGennyFormat gennyFormat);
}
