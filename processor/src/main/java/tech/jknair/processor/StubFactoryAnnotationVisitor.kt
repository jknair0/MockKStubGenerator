package tech.jknair.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import tech.jknair.processor.annotations.StubFactory
import java.io.OutputStream

internal class StubFactoryAnnotationVisitor(
    private val file: OutputStream,
    private val logger: KSPLogger
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val stubFactoryAnnotation = classDeclaration.annotations.first {
            it.shortName.asString() == StubFactory::class.simpleName
        }

        val targetClassArgument: KSValueArgument = stubFactoryAnnotation.arguments.first { arg ->
            arg.name?.asString() == "targetClass"
        }

        val targetClasses = targetClassArgument.value as ArrayList<*>

        for (targetClass in targetClasses) {
            val targetClassDeclaration = (targetClass as KSType).declaration as KSClassDeclaration
            targetClassDeclaration.accept(StubTargetVisitor(file, logger), Unit)
            logger.info("generated stub for ${targetClassDeclaration.simpleName.asString()}")
        }
    }

}