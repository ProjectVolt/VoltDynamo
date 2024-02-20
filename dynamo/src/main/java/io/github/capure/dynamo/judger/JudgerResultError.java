package io.github.capure.dynamo.judger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JudgerResultError {
	ERROR_NONE(0),
	ERROR_INVALID_CONFIG(-1),
	ERROR_FORK_FAILED(-2),
	ERROR_PTHREAD_FAILED(-3),
	ERROR_WAIT_FAILED(-4),
	ERROR_ROOT_REQUIRED(-5),
	ERROR_LOAD_SECCOMP_FAILED(-6),
	ERROR_SETRLIMIT_FAILED(-7),
	ERROR_DUP2_FAILED(-8),
	ERROR_SETUID_FAILED(-9),
	ERROR_EXECVE_FAILED(-10),
	ERROR_SPJ_ERROR(-11);

	private final int value;

	@JsonCreator
	JudgerResultError(int value) {
		this.value = value;
	}

	@JsonValue
	public int getValue() {
		return value;
	}
}
