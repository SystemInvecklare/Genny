package com.github.systeminvecklare.genny.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class HashFactory implements IHashFactory {
	@Override
	public IHash calculateHash(IHashable hashable) {
		try {
			SHA1Computation computation = new SHA1Computation();
			hashable.supply(computation);
			byte[] hashData = computation.getResult();
			
			return new Hash(byteArray2Hex(hashData));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String byteArray2Hex(final byte[] hash) {
	    try(Formatter formatter = new Formatter()) {
	    	for (byte b : hash) {
	    		formatter.format("%02x", b);
	    	}
	    	return formatter.toString();
	    }
	}


	@Override
	public IHash parseHash(String text) {
		HexBinaryAdapter adapter = new HexBinaryAdapter();
		byte[] bytes = adapter.unmarshal(text);
		return new Hash(byteArray2Hex(bytes)); //Why do we convert string->bytes->string? For safety!
	}
	
	private static class Hash implements IHash {
		private String hash;

		public Hash(String hash) {
			this.hash = hash;
		}
		
		@Override
		public int hashCode() {
			return hash.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Hash && ((Hash) obj).hash.equals(this.hash);
		}
		
		@Override
		public String toString() {
			return hash;
		}
	}

	private static class SHA1Computation implements IHashComputation {
		private final MessageDigest md;

		public SHA1Computation() throws NoSuchAlgorithmException {
			this.md = MessageDigest.getInstance("SHA-1");
		}

		@Override
		public void update(byte[] data) {
			md.update(data);
		}

		@Override
		public void update(byte[] data, int offset, int length) {
			md.update(data, offset, length);
		}
		
		public byte[] getResult() {
			return md.digest();
		}
	}
}
