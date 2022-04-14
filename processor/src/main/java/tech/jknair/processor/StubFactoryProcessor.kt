package tech.jknair.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
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
        // Get the KSClassDeclaration with StubFactory Annotations
        val symbols = resolver.getSymbolsWithAnnotation(StubFactory::class.java.canonicalName)
            .filterIsInstance<KSClassDeclaration>()

        // If no classes annotated
        if (!symbols.iterator().hasNext()) return emptyList()

        val file: OutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
            packageName = "tech.jknair.stubs",
            fileName = "GeneratedStubs"
        )

        for (symbol in symbols) {
            symbol.accept(StubFactoryAnnotationVisitor(file, logger), Unit)
        }

        file.close()
        return symbols.filterNot { it.validate() }.toList()
    }

    private operator fun OutputStream.plusAssign(content: String) {
        this.write((content + "\n").toByteArray())
    }

    inner class StubFactoryAnnotationVisitor(
        private val file: OutputStream,
        private val logger: KSPLogger
    ) : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            if (classDeclaration.classKind != ClassKind.INTERFACE) {
                logger.error("Only class should be annotated with @TargetAnnotation", classDeclaration)
                return
            }
            val packageName = classDeclaration.packageName

            file += "package ${packageName.asString()}\n"

            val stubFactoryAnnotation = classDeclaration.annotations.first {
                it.shortName.asString() == StubFactory::class.simpleName
            }

            val targetClassArgument: KSValueArgument = stubFactoryAnnotation.arguments.first { arg ->
                arg.name?.asString() == "targetClass"
            }

            val listOfClassTypeToCreateStub = targetClassArgument.value as ArrayList<*>

            for (classType in listOfClassTypeToCreateStub) {
                val targetClassDeclaration = (classType as KSType).declaration as KSClassDeclaration
                file += "class Stub${targetClassDeclaration.simpleName.asString()} {"
                declareStubMethods(file, targetClassDeclaration)
                file += "}"
                file += "\n"
                logger.info("completed for class ${targetClassDeclaration.simpleName.asString()}")
            }
        }

        private fun declareStubMethods(file: OutputStream, targetClassDeclaration: KSClassDeclaration) {
            val functions = targetClassDeclaration.getAllFunctions()
            for (ksFunctionDeclaration in functions) {
                declareMethod(ksFunctionDeclaration, file)
            }
        }

        private fun declareMethod(
            ksFunctionDeclaration: KSFunctionDeclaration,
            file: OutputStream
        ) {
            val functionName = ksFunctionDeclaration.simpleName.asString()
            if (functionName in OBJECT_FUNCTIONS) {
                return
            }
            // a line before function declaration
            file += ""
            val params = ksFunctionDeclaration.parameters.iterator()
            val parameterCode = StringBuilder().apply {
                for (param in params) {
                    val resolvedType = param.type.resolve()
                    val nullability = "?".takeIf { resolvedType.nullability == NULLABLE }.orEmpty()
                    append("\t\t")
                    val paramName = param.name!!.asString()
                    val paramType = resolvedType.declaration.qualifiedName?.asString()
                    append("""$paramName : $paramType${genericType(resolvedType)}${nullability}, """)
                    if (params.hasNext()) {
                        append('\n')
                    }
                }
            }

            file += "\tfun $functionName("
            file += "$parameterCode"
            file += "\t) {"
            file += ""
            file += "\t}"
        }

        private fun genericType(resolvedType: KSType): String {
            val typeParameters = resolvedType.declaration.typeParameters.ifEmpty {
                return ""
            }
            return typeParameters.joinToString(", ", prefix = "<", postfix = ">") { "*" }
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
        }

    }

    companion object {
        private val OBJECT_FUNCTIONS = setOf("equals", "hashCode", "toString")
    }

}