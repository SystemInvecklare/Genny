package com.github.systeminvecklare.genny.hash;

public interface IHashFactory {
	IHash calculateHash(IHashable hashable);
	IHash parseHash(String text);
}
