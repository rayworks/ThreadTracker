package com.codoon.threadtracker.plugins

import com.codoon.threadtracker.plugins.PluginUtils.checkClassFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.FileInputStream

import org.objectweb.asm.ClassReader.EXPAND_FRAMES
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

abstract class ModifyClassesTask : DefaultTask() {
    // This property will be set to all Jar files available in scope
    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    // Gradle will set this property with all class directories that available in scope
    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    // Task will put all classes from directories and jars after optional modification into single jar
    @get:OutputFile
    abstract val output: RegularFileProperty

    @Internal
    val jarPaths = mutableSetOf<String>()

    @TaskAction
    fun taskAction() {
        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(output.get().asFile)
            )
        )

        // copy classes from jar files without modification
        allJars.get().forEach { file ->
            println("handling Jar " + file.asFile.absolutePath)
//            if (!file.asFile.isJarFile()) {
//                return@forEach
//            }
            val jarFile = JarFile(file.asFile)
            var jarName = jarFile.name
            if (jarName.endsWith(".jar"))
                jarName = jarName.substring(0, jarName.length - 4)

            //val jarName = jarFile.name
            val enumeration = jarFile.entries()
            val tmpFile =
                File(file.asFile.parentFile.absolutePath + File.separator + "classes_temp.jar")
            if (tmpFile.exists()) { // temp jar
                tmpFile.delete()
            }
            val jarOutputStream = JarOutputStream(FileOutputStream(tmpFile))

            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val entryName = jarEntry.name
                val zipEntry = ZipEntry(entryName)
                val inputStream = jarFile.getInputStream(jarEntry)

                println("----------- jarClass <' + $entryName + '> -----------")
                if (checkClassFile(entryName, true)) {
                    jarOutputStream.putNextEntry(zipEntry)
                    val classReader = ClassReader(inputStream)
                    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    val cv = ThreadTrackerClassVisitor(classWriter, jarName)
                    classReader.accept(cv, EXPAND_FRAMES)
                    val code = classWriter.toByteArray()
                    jarOutputStream.write(code)
                } else {
                    jarOutputStream.putNextEntry(zipEntry)
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()

            // override the old Jar
            println(">>> copy jar from ${tmpFile.absolutePath} to ${file.asFile.absolutePath}")
            FileUtils.copyFile(tmpFile, file.asFile)
            tmpFile.delete()
        }

        allDirectories.get().forEach { directory ->
            println("handling dir" + directory.asFile.absolutePath)

            directory.asFile.walk().forEach { file ->
                if (file.isClassFile()) {
                    val name = file.name
                    if (checkClassFile(name, false)) {
                        println("----------- class <' + $name + '> -----------")
                        val classReader = ClassReader(FileInputStream(file))
                        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                        val cv = ThreadTrackerClassVisitor(classWriter, null)
                        classReader.accept(cv, EXPAND_FRAMES)

                        val code = classWriter.toByteArray()
                        val fos = FileOutputStream(
                            file.parentFile.absolutePath + File.separator + name
                        )
                        fos.write(code)
                        fos.close()

                        // Writing changed class to output jar
                        val relativePath = directory.asFile.toURI().relativize(file.toURI()).getPath()
                        jarOutput.writeEntity(relativePath.replace(File.separatorChar, '/'), code)
                    } else {
                        // just copy it to output without modification
                        val relativePath = directory.asFile.toURI().relativize(file.toURI()).getPath()
                        println(
                            "Adding from directory ${relativePath.replace(File.separatorChar, '/')}"
                        )
                        jarOutput.writeEntity(
                            relativePath.replace(File.separatorChar, '/'),
                            file.inputStream()
                        )
                    }
                }
            }
        }
        jarOutput.close()
    }

    // writeEntity methods check if the file has name that already exists in output jar
    private fun JarOutputStream.writeEntity(name: String, inputStream: InputStream) {
        // check for duplication name first
        if (jarPaths.contains(name)) {
            printDuplicatedMessage(name)
        } else {
            putNextEntry(JarEntry(name))
            inputStream.copyTo(this)
            closeEntry()
            jarPaths.add(name)
        }
    }

    private fun JarOutputStream.writeEntity(relativePath: String, byteArray: ByteArray) {
        // check for duplication name first
        if (jarPaths.contains(relativePath)) {
            printDuplicatedMessage(relativePath)
        } else {
            putNextEntry(JarEntry(relativePath))
            write(byteArray)
            closeEntry()
            jarPaths.add(relativePath)
        }
    }

    private fun printDuplicatedMessage(name: String) =
        println("Cannot add ${name}, because output Jar already has file with the same name.")
}