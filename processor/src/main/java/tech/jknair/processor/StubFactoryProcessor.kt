package tech.jknair.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind.INTERFACE
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import tech.jknair.annotations.StubFactory

internal class StubFactoryProcessor(
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
        // read information from each annotated classes
        for (annotatedClass in annotatedClasses) {
            annotatedClass.accept(StubFactoryAnnotationVisitor(codeGenerator, logger), Unit)
        }
        return annotatedClasses.filterNot { it.validate() }.toList()
    }

    private fun showNotInterfaceError(annotatedClasses: Sequence<KSClassDeclaration>) {
        annotatedClasses.filter { it.classKind != INTERFACE }.forEach { annotatedClass ->
            logger.error("Not an interface", annotatedClass)
        }
    }

}