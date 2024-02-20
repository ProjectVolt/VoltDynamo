package io.github.capure.dynamo.exception;

import java.util.Optional;

import io.github.capure.dynamo.judger.JudgerResult;

public class CompileErrorException extends RuntimeException {
	private JudgerResult result;
	private String compilerOutput;

	public CompileErrorException(String message) {
		super(message);
	}

	public CompileErrorException(String message, JudgerResult result) {
		super(message + result);
		this.result = result;
	}

	public CompileErrorException(String message, JudgerResult result, String compilerOutput) {
		super(message + result);
		this.result = result;
		this.compilerOutput = compilerOutput;
	}

	public Optional<JudgerResult> getResult() {
		return Optional.ofNullable(result);
	}

	public Optional<String> getCompilerOutput() {
		return Optional.ofNullable(compilerOutput);
	}
}
