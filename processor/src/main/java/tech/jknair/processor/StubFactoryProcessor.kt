package tech.jknair.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind.INTERFACE
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import tech.jknair.processor.annotations.StubFactory
import java.io.OutputStream

internal class MySymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedClasses = resolver.getSymbolsWithAnnotation(StubFactory::class.java.canonicalName)
            .filterIsInstance<KSClassDeclaration>()
        if (!annotatedClasses.iterator().hasNext()) {
            return emptyList()
        }
        // the class that declares the required Stub should be an interface
        if (annotatedClasses.any { it.classKind != INTERFACE }) {
            logger.error("Only interfaces should be annotated with ${StubFactory::class.java.canonicalName}")
            showNotInterfaceError(annotatedClasses)
            return emptyList()
        }
        for (annotatedClass in annotatedClasses) {
            // get declared interface package name
            val packageName = annotatedClass.packageName.asString()
            val file: OutputStream = codeGenerator.createNewFile(
                dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
                packageName = packageName,
                fileName = "${annotatedClass.simpleName.asString()}_TestStubs"
            )
            // using the same package name
            file += "package ${packageName}\n\n"
            annotatedClass.accept(StubFactoryAnnotationVisitor(file, logger), Unit)
            file.close()
        }
        return annotatedClasses.filterNot { it.validate() }.toList()
    }

    private fun showNotInterfaceError(annotatedClasses: Sequence<KSClassDeclaration>) {
        annotatedClasses.filter { it.classKind != INTERFACE }.forEach { annotatedClass ->
            logger.error("Not an interface", annotatedClass)
        }
    }

}