package io.github.capure.dynamo.config;

public final class JudgerConfig {
    public static final String JUDGER_TEST_RUN_PREFIX = "judger_test_run";
    public static final String JUDGER_TEST_COMPILE_PREFIX = "judger_test";
    public static final String COMPILER_LOG_PATH = "/tmp/judger/compiler.log";
    public static final String RUN_LOG_PATH = "/tmp/judger/run.log";
    public static final Integer COMPILER_USER_UID = 901;
    public static final Integer COMPILER_USER_GID = 901;
    public static final Integer RUN_USER_UID = 902;
    public static final Integer RUN_USER_GID = 902;
}
