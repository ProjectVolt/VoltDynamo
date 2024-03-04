package io.github.capure.dynamo.dto;

import io.github.capure.schema.AvroCompileError;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class CompileError {
    @NonNull
    private String message;
    @NonNull
    private Boolean fatal;

    public AvroCompileError toAvro() {
    return new AvroCompileError(message, fatal);
    }
}
