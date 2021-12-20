package org.screamingsandals.nms.mapper.utils;

import lombok.Data;

@Data
public class ErrorsLogger {
    private int errors;
    private boolean silent;

    public void log(String error) {
        errors++;
        if (!silent) {
            System.out.println(error);
        }
    }

    public void reset() {
        errors = 0;
        silent = false;
    }

    public void printWarn() {
        if (errors > 0) {
            System.out.println(errors + " symbols (fields, methods) not found, but they are not excluded.");
        }
    }
}
