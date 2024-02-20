package io.github.capure.dynamo.judger;

public enum SeccompRule {
	SECCOMP_RULE_C_CPP,
	SECCOMP_RULE_C_CPP_FILE_IO,
	SECCOMP_RULE_GENERAL,
	SECCOMP_RULE_GOLANG,
	SECCOMP_RULE_NODE;

	public String toString() {
		switch (this.name()) {
			case "SECCOMP_RULE_C_CPP":
				return "c_cpp";
			case "SECCOMP_RULE_C_CPP_FILE_IO":
				return "c_cpp_file_io";
			case "SECCOMP_RULE_GENERAL":
				return "general";
			case "SECCOMP_RULE_GOLANG":
				return "golang";
			case "SECCOMP_RULE_NODE":
				return "node";
			default:
				return null;
		}
	}
}
