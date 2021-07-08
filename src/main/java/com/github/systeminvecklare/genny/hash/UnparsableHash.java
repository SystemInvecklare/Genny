package com.github.systeminvecklare.genny.hash;

public abstract class UnparsableHash implements IHash {
	private UnparsableHash() {}
	
	@Override
	public String toString() {
		return "UNPARSABLE("+getDescription()+")";
	}
	
	protected abstract String getDescription();

	public static IHash exception(String stringHash, Exception e) {
		return new ExceptionWhileParsing(stringHash, e);
	}
	
	private static class ExceptionWhileParsing extends UnparsableHash {
		private final String stringHash;
		private final Exception e;

		public ExceptionWhileParsing(String stringHash, Exception e) {
			this.stringHash = stringHash;
			this.e = e;
		}
		
		@Override
		public int hashCode() {
			return stringHash.hashCode()*31 ^ e.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ExceptionWhileParsing) {
				ExceptionWhileParsing other = (ExceptionWhileParsing) obj;
				return this.stringHash.equals(other.stringHash) && this.e.equals(other.e);
			}
			return false;
		}
		
		@Override
		protected String getDescription() {
			return stringHash;
		}
	}
}
