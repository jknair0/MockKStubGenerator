package tech.jknair.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability.NULLABLE
import com.google.devtools.ksp.symbol.Variance
import com.google.devtools.ksp.symbol.Variance.CONTRAVARIANT
import com.google.devtools.ksp.symbol.Variance.COVARIANT
import com.google.devtools.ksp.symbol.Variance.INVARIANT
import com.google.devtools.ksp.symbol.Variance.STAR
import java.io.OutputStream

internal class StubTargetVisitor(
    private val file: OutputStream,
    private val logger: KSPLogger
) : KSVisitorVoid() {

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
            // argumentName: ObjectType
            // eg:
            // name: String,
            // age: Int,
            file += "\n\t\t$argName: $argType"
            // processing generic types
            visitTypeArguments(type.element?.typeArguments ?: emptyList())
            // nullability
            file += "?".takeIf { resolvedArgumentType.nullability == NULLABLE }.orEmpty()

            file += ","
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

    companion object {
        private val OBJECT_FUNCTIONS = setOf("equals", "hashCode", "toString")
    }

}