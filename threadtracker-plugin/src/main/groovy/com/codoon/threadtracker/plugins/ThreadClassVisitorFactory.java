package com.codoon.threadtracker.plugins;

import com.android.build.api.instrumentation.AsmClassVisitorFactory;
import com.android.build.api.instrumentation.ClassContext;
import com.android.build.api.instrumentation.ClassData;
import com.android.build.api.instrumentation.InstrumentationParameters;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;

abstract public class ThreadClassVisitorFactory implements AsmClassVisitorFactory<ThreadClassVisitorFactory.ParametersImpl> {

    @Override
    public @NotNull ClassVisitor createClassVisitor(@NotNull ClassContext classContext,
                                                    @NotNull ClassVisitor classVisitor) {
        return new ThreadTrackerClassVisitor(
                classVisitor,
                null

        );
    }

    @Override
    public boolean isInstrumentable(@NotNull ClassData classData) {
        String name = classData.getClassName();
        return (name.endsWith(".class") && !name.startsWith("R\\$")
                && !name.equals("R.class") && !name.startsWith("BR\\$")
                && !name.equals("BR.class") && !name.equals("BuildConfig.class"));
    }

    static interface ParametersImpl extends InstrumentationParameters {
    }
}
