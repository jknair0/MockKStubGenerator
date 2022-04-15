package tech.jknair.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind.INTERFACE
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability.NULLABLE
import com.google.devtools.ksp.symbol.Variance
import com.google.devtools.ksp.symbol.Variance.CONTRAVARIANT
import com.google.devtools.ksp.symbol.Variance.COVARIANT
import com.google.devtools.ksp.symbol.Variance.INVARIANT
import com.google.devtools.ksp.symbol.Variance.STAR
import com.google.devtools.ksp.validate
import java.io.OutputStream

class MySymbolProcessor(
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
            annotatedClasses.filter { it.classKind != INTERFACE }.forEach { annotatedClass ->
                logger.error("Not an interface", annotatedClass)
            }
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

    private operator fun OutputStream.plusAssign(content: String) {
        this.write((content).toByteArray())
    }

    inner class StubFactoryAnnotationVisitor(
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

    inner class StubTargetVisitor(
        private val file: OutputStream,
        private val logger: KSPLogger
    ): KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            file += "class Stub${classDeclaration.simpleName.asString()} {\n"
            val functions = classDeclaration.getAllFunctions()
            for (function in functions) {
                function.accept(this, Unit)
            }
            file += "\n}"
            file += "\n"
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val functionName = function.simpleName.asString()
            if (functionName in OBJECT_FUNCTIONS) {
                return
            }
            // an empty line before function declaration
            file += "\n"
            file += "\tfun $functionName("
            val functionArguments = function.parameters
            for (argument in functionArguments) {
                val argName = argument.name!!.asString()
                val type: KSTypeReference = argument.type
                val resolvedArgumentType: KSType = type.resolve()
                val argType: String = resolvedArgumentType.declaration.qualifiedName!!.asString()
                val nullability = "?".takeIf { resolvedArgumentType.nullability == NULLABLE }.orEmpty()
                // argumentName: ObjectType
                // eg:
                // name: String,
                // age: Int,
                file += "\n\t\t$argName: $argType"
                // processing generic types
                visitTypeArguments(type.element?.typeArguments ?: emptyList())
                file += "${nullability},"
            }
            file += "\n\t) {"
            file += ""
            file += "\t}\n"
            // an empty line after function
        }

        private fun visitTypeArguments(typeArguments: List<KSTypeArgument>) {
            if (typeArguments.isNotEmpty()) {
                file += "<"
                for ((index, typeArgument) in typeArguments.withIndex()) {
                    typeArgument.accept(this, Unit)
                    if (index < typeArguments.size - 1) file += ", "
                }
                file += ">"
            }
        }

        override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) {
            when (val variance: Variance = typeArgument.variance) {
                STAR -> {
                    file += "*"
                    return
                }
                COVARIANT, CONTRAVARIANT -> {
                    file += variance.label // 'out' or 'in'
                    file += " "
                }
                INVARIANT -> {
                    // do nothing
                }
            }
            val resolvedType: KSType? = typeArgument.type?.resolve()
            file += resolvedType?.declaration?.qualifiedName?.asString() ?: run {
                logger.error("Invalid type argument", typeArgument)
                return
            }
            file += if (resolvedType?.nullability == NULLABLE) "?" else ""

            val genericArguments: List<KSTypeArgument> = typeArgument.type?.element?.typeArguments ?: emptyList()
            // a type inside a type. this traversal continues until all the generic types are resolved
            visitTypeArguments(genericArguments)
        }

    }

    companion object {
        private val OBJECT_FUNCTIONS = setOf("equals", "hashCode", "toString")
    }

}