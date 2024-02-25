package io.github.capure.dynamo.judger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Judger {
    private static final String JUDGER_PATH = "/usr/lib/judger/libjudger.so";

    public static JudgerResult run(JudgerOptions options) throws JudgerErrorException, JudgerCommunicationException {
        log.debug("Running judger with options: " + options.toString());
        String cmd = JUDGER_PATH + options.toString();
        log.debug("Running judger with command: " + cmd);
        try {
            Process process = Runtime.getRuntime().exec(cmd.split(" "));
            process.waitFor();

            BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            if (stdError.ready()) {
                log.debug("StdError not empty, throwing JudgerErrorException");
                throw new JudgerErrorException("Judger error: " + stdError.readLine());
            }

            StringBuffer stdOutputString = new StringBuffer();
            while (stdOutput.ready()) {
                stdOutputString.append(stdOutput.readLine());
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(stdOutputString.toString(), JudgerResult.class);
        } catch (JudgerErrorException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error running judger: " + e.getMessage());
            throw new JudgerCommunicationException("Judger error: " + e.getMessage());
        }
    }
}
