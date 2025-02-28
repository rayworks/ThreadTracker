package com.codoon.threadtracker.plugins;

import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.ApplicationAndroidComponentsExtension;
import com.android.build.gradle.AppPlugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import kotlin.Unit;

public class ThreadTrackerPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("hello ThreadTrackerPlugin");

        project.getRootProject().getSubprojects().forEach(
                subProject -> {
                    PluginUtils.addProjectName(subProject.getName());
                    PluginUtils.projectPathList.add(subProject.getProjectDir().toString());
                }
        );
        project.getPlugins().withType(AppPlugin.class).configureEach(
                appPlugin -> {

                    //val androidComponents =
                    ApplicationAndroidComponentsExtension androidComponents =
                            project.getExtensions().getByType(ApplicationAndroidComponentsExtension.class);

                    // Registers a callback to be called, when a new variant is configured
                    androidComponents.onVariants(
                            androidComponents.selector().all(),
                            variant -> {
                                variant.getInstrumentation().transformClassesWith(
                                        ThreadClassVisitorFactory.class,
                                        InstrumentationScope.PROJECT,
                                        exampleParams -> {
                                            return Unit.INSTANCE;
                                        }
                                );
                            }
                    );
                }
        );
    }
}
