package com.example.utils

import com.example.ProjectProps
import net.neoforged.fml.ModList
import java.lang.annotation.ElementType

class LoadServiceImpl : LoadService {
    override fun loadAll(packageName: String) {
        val fileInfo = ModList.get().getModFileById(ProjectProps["modId"])
        val scanData = fileInfo.file.scanResult
        val classNamesKt = scanData.getAnnotatedBy(LoadKt::class.java, ElementType.TYPE).map { it.clazz.className }
        val classNamesJ = scanData.getAnnotatedBy(LoadJ::class.java, ElementType.TYPE).map { it.clazz.className }
        val allClassNames = classNamesKt.toList() + classNamesJ.toList()
        for (className in allClassNames) {
            try {
                LoadUtil.processClass(className)
            } catch (e: Exception) {
                throw RuntimeException("Failed to process class $className", e)
            }
        }

    }
}