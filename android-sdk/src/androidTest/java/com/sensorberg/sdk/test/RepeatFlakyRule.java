package com.sensorberg.sdk.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class RepeatFlakyRule implements TestRule {

    private static class RepeatStatement extends Statement {

        private final int times;

        private final Statement statement;

        private final Description description;

        private RepeatStatement(int times, Statement statement, Description description) {
            this.times = times;
            this.statement = statement;
            this.description = description;
        }

        @Override
        public void evaluate() throws Throwable {
            Throwable caughtThrowable = null;

            // implement retry logic here
            for (int i = 0; i < times; i++) {
                try {
                    statement.evaluate();
                    return;
                } catch (Throwable t) {
                    caughtThrowable = t;
                    System.err.println(description.getDisplayName() + ": run " + (i + 1) + " failed: = " + t.getMessage());
                }
            }
            System.err.println(description.getDisplayName() + ": giving up after " + times + " failures");
            throw caughtThrowable;
        }
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        Statement result = statement;
        RepeatFlaky repeatFlaky = description.getAnnotation(RepeatFlaky.class);
        if (repeatFlaky != null) {
            int times = repeatFlaky.times();
            result = new RepeatStatement(times, statement, description);
        }
        return result;
    }
}
