package com.codoon.threadtracker.plugins;

import com.android.build.api.instrumentation.AsmClassVisitorFactory;
import com.android.build.api.instrumentation.ClassContext;
import com.android.build.api.instrumentation.ClassData;
import com.android.build.api.instrumentation.InstrumentationParameters;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;

abstract public class ThreadClassVisitorFactory
        implements AsmClassVisitorFactory<InstrumentationParameters.None> {

    @Override
    public @NotNull ClassVisitor createClassVisitor(@NotNull ClassContext classContext,
                                                    @NotNull ClassVisitor classVisitor) {
        System.out.println("Processing class : " + classContext.getCurrentClassData().getClassName());
        return new ThreadTrackerClassVisitor(classVisitor, classContext.getCurrentClassData().getClassName());
    }

    @Override
    public boolean isInstrumentable(@NotNull ClassData classData) {
        String name = classData.getClassName();
        System.out.println(">>> instrument : " + name);

        return (name.endsWith(".class") && !name.startsWith("R\\$")
                && !name.equals("R.class") && !name.startsWith("BR\\$")
                && !name.equals("BR.class") && !name.equals("BuildConfig.class"));
    }
}
