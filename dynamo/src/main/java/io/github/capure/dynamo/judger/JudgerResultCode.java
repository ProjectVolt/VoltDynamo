package io.github.capure.dynamo.judger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JudgerResultCode {
	RESULT_SUCCESS(0),
	RESULT_WRONG_ANSWER(-1),
	RESULT_CPU_TIME_LIMIT_EXCEEDED(1),
	RESULT_REAL_TIME_LIMIT_EXCEEDED(2),
	RESULT_MEMORY_LIMIT_EXCEEDED(3),
	RESULT_RUNTIME_ERROR(4),
	RESULT_SYSTEM_ERROR(5);

	private final int value;

	@JsonCreator
	JudgerResultCode(int value) {
		this.value = value;
	}

	@JsonValue
	public int getValue() {
		return value;
	}
}
