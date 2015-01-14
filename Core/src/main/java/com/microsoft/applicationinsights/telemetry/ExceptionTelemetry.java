package com.microsoft.applicationinsights.telemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.microsoft.applicationinsights.internal.schemav2.ExceptionData;
import com.microsoft.applicationinsights.internal.schemav2.ExceptionDetails;
import com.microsoft.applicationinsights.internal.schemav2.StackFrame;
import com.microsoft.applicationinsights.internal.util.LocalStringsUtils;

import com.google.common.base.Strings;

/**
 * Telemetry used to track events.
 */
public final class ExceptionTelemetry extends BaseTelemetry<ExceptionData> {
    private final ExceptionData data;
    private Exception exception;

    private ExceptionTelemetry() {
        super();
        data = new ExceptionData();
        initialize(data.getProperties());
        setExceptionHandledAt(ExceptionHandledAt.Unhandled);
    }

    public ExceptionTelemetry(Exception exception) {
        this();
        setException(exception);

    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
        updateException(exception);
    }

    public ExceptionHandledAt getExceptionHandledAt() {
        return Enum.valueOf(ExceptionHandledAt.class, data.getHandledAt());
    }

    public void setExceptionHandledAt(ExceptionHandledAt value) {
        data.setHandledAt(value.toString());
    }

    public Map<String,Double> getMetrics() {
        return data.getMeasurements();
    }

    public List<ExceptionDetails> getExceptions() {
        return data.getExceptions();
    }

    @Override
    protected void additionalSanitize() {
        Sanitizer.sanitizeMeasurements(this.getMetrics());
    }

    @Override
    protected ExceptionData getData() {
        return data;
    }

    private void updateException(Exception exception) {
        ArrayList<ExceptionDetails> exceptions = new ArrayList<ExceptionDetails>();
        convertExceptionTree(exception, null, exceptions);

        data.setExceptions(exceptions);
    }

    private static void convertExceptionTree(Throwable exception, ExceptionDetails parentExceptionDetails, List<ExceptionDetails> exceptions) {
        if (exception == null) {
            exception = new Exception("");
        }

        ExceptionDetails exceptionDetails = createWithStackInfo(exception, parentExceptionDetails);
        exceptions.add(exceptionDetails);

        if (exception.getCause() != null) {
            convertExceptionTree(exception.getCause(), exceptionDetails, exceptions);
        }
    }

    private static ExceptionDetails createWithStackInfo(Throwable exception, ExceptionDetails parentExceptionDetails) {
        if (exception == null) {
            throw new IllegalArgumentException("exception cannot be null");
        }

        ExceptionDetails exceptionDetails = new ExceptionDetails();
        exceptionDetails.setId(LocalStringsUtils.generateRandomIntegerId());
        exceptionDetails.setTypeName(exception.getClass().getName());
        exceptionDetails.setMessage(exception.getMessage());

        if (parentExceptionDetails != null) {
            exceptionDetails.setOuterId(parentExceptionDetails.getId());
        }

        StackTraceElement[] trace = exception.getStackTrace();

        if (trace != null && trace.length > 0) {
            List<StackFrame> stack = exceptionDetails.getParsedStack();

            // We need to present the stack trace in reverse order.

            for (int idx = 0; idx < trace.length; idx++) {
                StackTraceElement elem = trace[idx];

                if (elem.isNativeMethod()) {
                    continue;
                }

                String className = elem.getClassName();

                StackFrame frame = new StackFrame();
                frame.setLevel(idx);
                frame.setFileName(elem.getFileName());
                frame.setLine(elem.getLineNumber());

                if (!Strings.isNullOrEmpty(className)) {
                    frame.setMethod(elem.getClassName() + "." + elem.getMethodName());
                }
                else {
                    frame.setMethod(elem.getMethodName());
                }

                stack.add(frame);
            }

            exceptionDetails.setHasFullStack(true); // TODO: sanitize and trim exception stack trace.
        }

        return exceptionDetails;
    }
}
