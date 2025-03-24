package com.codoon.threadtracker.plugins

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
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

        val ace = project.extensions.getByType(AndroidComponentsExtension::class.java)
        ace.onVariants { variant ->
            val taskProvider = project.tasks.register(
                "${variant.name}ModifyClasses", ModifyClassesTask::class.java
            )

            variant.artifacts
                .forScope(ScopedArtifacts.Scope.ALL)
                .use(taskProvider)
                .toTransform(
                    ScopedArtifact.CLASSES,
                    ModifyClassesTask::allJars,
                    ModifyClassesTask::allDirectories,
                    ModifyClassesTask::output
                )
        }
    }
}
