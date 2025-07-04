package com.example.utils

import com.example.utils.LoadUtil.processClass
import java.io.File
import java.net.URI
import java.net.URL
import java.util.*
import java.util.jar.JarFile

class LoadServiceImpl : LoadService {
    override fun loadAll(packageName: String) {
        val classLoader = Thread.currentThread().contextClassLoader
        val path = packageName.replace('.', '/')

        val resources: Enumeration<URL> = classLoader.getResources(path)
        while (resources.hasMoreElements()) {
            val resource = resources.nextElement()
            val urlStr = resource.toExternalForm()

            if (urlStr.startsWith("file:")) {
                // Handle directory based class loading
                val directory = File(URI(urlStr))
                scanDirectory(directory, packageName, path) { className ->
                    processClass(className)
                }
            } else if (urlStr.startsWith("jar:")) {
                // Handle JAR based class loading
                val jarPath = urlStr.substring(4, urlStr.indexOf("!")).replace("file:", "")
                JarFile(jarPath).use { jarFile ->
                    val entries = jarFile.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (entry.name.startsWith(path) && entry.name.endsWith(".class")) {
                            val className = entry.name
                                .replace("/", ".")
                                .substring(0, entry.name.length - 6) // remove .class
                            processClass(className)
                        }
                    }
                }
            } else {
                throw Exception("Unsupported resource type: $urlStr. Only file and jar resources are supported.")
            }
        }
    }

    private fun scanDirectory(dir: File, packageName: String, packagePath: String, processClass: (String) -> Unit) {
        if (!dir.exists()) return

        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                scanDirectory(file, "$packageName.${file.name}", "$packagePath/${file.name}", processClass)
            } else if (file.name.endsWith(".class")) {
                val className = packageName + "." + file.name.substring(0, file.name.length - 6)
                processClass(className)
            }
        }
    }
}