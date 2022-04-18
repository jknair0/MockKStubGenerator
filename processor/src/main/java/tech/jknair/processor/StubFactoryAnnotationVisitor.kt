package tech.jknair.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import tech.jknair.annotations.StubFactory

internal class StubFactoryAnnotationVisitor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val stubFactoryAnnotation = classDeclaration.annotations.first {
            it.shortName.asString() == StubFactory::class.simpleName
        }

        // accessing the StubFactory argument
        val targetClassArgument: KSValueArgument = stubFactoryAnnotation.arguments.first { arg ->
            arg.name?.asString() == "targetClass"
        }

        // accessing StubFactory argument value
        val targetClasses = targetClassArgument.value as ArrayList<*>

        // going through each class whose stubs will be generated
        for (targetClass in targetClasses) {
            val targetClassDeclaration = (targetClass as KSType).declaration as KSClassDeclaration
            val packageName = targetClassDeclaration.packageName.asString()
            codeGenerator.createNewFile(
                dependencies = Dependencies(false),
                packageName = packageName,
                fileName = "Stub${targetClassDeclaration.simpleName.asString()}"
            ).use { file ->
                file += "package ${packageName}\n\n"
                targetClassDeclaration.accept(StubTargetVisitor(file, logger), Unit)
            }
            logger.info("generated stub for ${targetClassDeclaration.simpleName.asString()}")
        }
    }

}