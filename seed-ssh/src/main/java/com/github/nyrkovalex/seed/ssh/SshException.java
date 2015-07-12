package com.github.nyrkovalex.seed.ssh;

public class SshException extends Exception {

	SshException(String message) {
		super(message);
	}

	SshException(Throwable cause) {
		super(cause);
	}
}
