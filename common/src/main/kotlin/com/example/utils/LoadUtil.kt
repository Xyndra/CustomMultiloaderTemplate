package com.example.utils

import org.objectweb.asm.*
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class LoadKt

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class LoadJ

interface LoadService {
    fun loadAll(packageName: String)
}

object LoadUtil {
    fun loadAll(packageName: String) {
        val loader: ServiceLoader<LoadService> = ServiceLoader.load(LoadService::class.java)
        for (service in loader) {
            service.loadAll(packageName)
        }
    }

    private val loadedClasses = mutableSetOf<String>()
    fun processClass(className: String, classLoader: ClassLoader = Thread.currentThread().contextClassLoader) {
        if (loadedClasses.contains(className)) {
            return // Class already processed, this is probably a companion object
        }
        loadedClasses.add(className)
        if (hasAnnotation(className, "Lcom/example/utils/LoadKt;")) {
            val clazz = classLoader.loadClass(className)
            if (clazz.isAnnotationPresent(LoadJ::class.java)) {
                throw Exception("Class $clazz cannot be annotated with both LoadKt and LoadJ")
            }

            if (clazz.kotlin.isCompanion) {
                throw Exception(
                    "Class $clazz cannot be a companion object. " +
                            "Please keep the companion object and move the annotation to the class."
                )
            }

            val instance: Field? = try {
                clazz.getDeclaredField("INSTANCE")
            } catch (_: NoSuchFieldException) {
                try {
                    clazz.getDeclaredField("Companion")
                } catch (_: NoSuchFieldException) {
                    null
                }
            }

            if (instance == null) {
                throw Exception("Class $clazz annotated with LoadKt must have a field named INSTANCE or Companion")
            }

            if (!Modifier.isStatic(instance.modifiers)) {
                throw Exception("Field INSTANCE or Companion in class $clazz annotated with LoadKt must be static")
            }

            val loadMethod: Method? = try {
                instance.type.getDeclaredMethod("load")
            } catch (_: NoSuchMethodException) {
                null
            }

            checkMethod(loadMethod)

            loadMethod!!.invoke(instance.get(null))
        } else if (hasAnnotation(className, "Lcom/example/utils/LoadJ;")) {
            val clazz = classLoader.loadClass(className)
            val loadMethod: Method = try {
                clazz.getDeclaredMethod("load")
            } catch (_: NoSuchMethodException) {
                throw Exception("Class $clazz annotated with LoadJ must have a method named load")
            }

            checkMethod(loadMethod)

            if (!Modifier.isStatic(loadMethod.modifiers)) {
                throw Exception("Method ${loadMethod.name} in class ${loadMethod.declaringClass} must be static")
            }

            loadMethod.invoke(null)
        }
    }

    private fun checkMethod(method: Method?) {
        if (method == null) {
            throw Exception("Method must not be null")
        }

        if (method.parameterCount != 0) {
            throw Exception("Method ${method.name} in class ${method.declaringClass} must have no parameters")
        }

        if (method.returnType != Void.TYPE) {
            throw Exception("Method ${method.name} in class ${method.declaringClass} must return Void")
        }
    }

    @Throws(java.lang.Exception::class)
    fun hasAnnotation(className: String, annotationDescriptor: String): Boolean {
        val found = booleanArrayOf(false)
        val stream = this::class.java.classLoader.getResourceAsStream(className.replace('.', '/') + ".class")
        val reader = try { ClassReader(stream) } catch (_: Exception) {
            throw Exception("Class $className not found or cannot be read")
        }
        reader.accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                if (desc == annotationDescriptor) {
                    found[0] = true
                }
                return super.visitAnnotation(desc, visible)
            }
        }, 0)
        return found[0]
    }
}

