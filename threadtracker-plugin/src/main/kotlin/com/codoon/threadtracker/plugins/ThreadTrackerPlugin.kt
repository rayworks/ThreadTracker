package com.codoon.threadtracker.plugins

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.function.Consumer

open class ThreadTrackerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        println("hello ThreadTrackerPlugin")

        project.rootProject.subprojects.forEach(
            Consumer { subProject: Project ->
                PluginUtils.addProjectName(subProject.name)
                PluginUtils.projectPathList.add(subProject.projectDir.toString())
                PluginUtils.dealAptFile()
            }
        )

//        project.afterEvaluate {
//            project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
//                .onVariants { variant ->
//                    val variantName = variant.name.capitalized()
//                    val javaCompileTask = project.tasks.named(
//                        "compile${variantName}JavaWithJavac",
//                        JavaCompile::class.java
//                    )
//
//                    val taskProvider = project.tasks.register(
//                        "transform${variantName}ModifyClasses",
//                        ModifyClassesTask::class.java
//                    ) /*{
//                        it.allDirectories = javaCompileTask.get().destinationDirectory.files()
//                        it.allJars = javaCompileTask.get().destinationDirectory.files()
//                        it.output = project.layout.buildDirectory.dir("transformed/$variantName")
//                            .get().asFile
//                    }*/
//
//                    variant.artifacts
//                        .forScope(ScopedArtifacts.Scope.ALL)
//                        .use(taskProvider)
//                        .toTransform(
//                            ScopedArtifact.CLASSES,
//                            ModifyClassesTask::allJars,
//                            ModifyClassesTask::allDirectories,
//                            ModifyClassesTask::output
//                        )
//
//
//
//                    /*val asmTask = project.tasks.register(
//                        "transform${variantName}WithAsm",
//                        AsmTransformTask::class.java
//                    ) {
//                        it.inputClasses = javaCompileTask.get().destinationDirectory.files()
//                        it.outputDir = project.layout.buildDirectory.dir("transformed/$variantName")
//                            .get().asFile
//                    }
//
////                    variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
////                        .use(asmTask)
////                        .toTransform(ScopedArtifact.CLASSES,
////                            AsmTransformTask::getInputClasses,
////
////                        }*/
//
//                }
//        }

        //        project.getDependencies().registerTransform(MyTransform.class, spec -> {
//            System.out.println(">>> dependency registered");
//            spec.getFrom().attribute(ARTIFACT_TYPE_ATTRIBUTE, "class");
//            spec.getTo().attribute(ARTIFACT_TYPE_ATTRIBUTE, "class");
//        });



        val ace = project.extensions
            .getByType(ApplicationAndroidComponentsExtension::class.java)

        ace.onVariants { variant ->
            val taskProvider = project.tasks.register(
                variant.name + "ModifyClasses", ModifyClassesTask::class.java
            )

            // Register modify classes task
            variant.artifacts
                .forScope(ScopedArtifacts.Scope.ALL)
                .use(taskProvider)
                .toTransform(
                    ScopedArtifact.CLASSES,
                    ModifyClassesTask::allJars,
                    ModifyClassesTask::allDirectories,
                    ModifyClassesTask::output
                )

//            variant.instrumentation.transformClassesWith(
//                ThreadClassVisitorFactory::class.java,
//                InstrumentationScope.ALL,
//            ) {
//            }
//            variant.instrumentation.setAsmFramesComputationMode(
//                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
//            )
        }
    }
}
