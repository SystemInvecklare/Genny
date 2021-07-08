package com.github.systeminvecklare.genny.exception;

public class GennyException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public GennyException() {
		super();
	}

	public GennyException(String message, Throwable cause) {
		super(message, cause);
	}

	public GennyException(String message) {
		super(message);
	}

	public GennyException(Throwable cause) {
		super(cause);
	}
}
