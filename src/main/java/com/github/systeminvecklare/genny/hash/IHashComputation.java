package com.github.systeminvecklare.genny.hash;

public interface IHashComputation {
	void update(byte[] data);
	void update(byte[] data, int offset, int length);
}
