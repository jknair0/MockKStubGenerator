package tech.jknair.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier.SUSPEND
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

    private lateinit var targetClassName: String

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        targetClassName = classDeclaration.simpleName.asString()
        val targetClassFullyQualifiedName = classDeclaration.qualifiedName!!.asString()
        file += "class Stub$targetClassName(private val mocked${targetClassName}: $targetClassFullyQualifiedName = io.mockk.mockk()) {\n"

        val functions = classDeclaration.getAllFunctions()
        for (function in functions) {
            function.accept(this, Unit)
        }
        file += "\n}"
        file += "\n\n"
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val functionName = function.simpleName.asString()
        if (functionName in IN_BUILT_FUNCTIONS) {
            return
        }
        // an empty line before function declaration
        file += "\n"
        file += "\tfun $functionName("
        val functionArguments = function.parameters
        val argsNames = mutableListOf<String>()
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

            argsNames.add(argName)
        }

        val returnValueAvailable: Boolean
        val returnType = function.returnType?.resolve()?.declaration?.qualifiedName?.asString()
        if (returnType != null && returnType != UNIT_TYPE) {
            file += "\n\t\t$RETURN_VALUE_ARG_NAME: $returnType"
            visitTypeArguments(function.returnType?.element?.typeArguments ?: emptyList())
            returnValueAvailable = true
        } else {
            returnValueAvailable = false
        }

        file += "\n\t) {\n"

        val argsStr = argsNames.joinToString()
        val isSuspendFunction = function.modifiers.contains(SUSPEND)
        val mockkFunction = if (isSuspendFunction) MOCKK_CO_EVERY else MOCKK_EVERY
        val returnValue = if (returnValueAvailable) RETURN_VALUE_ARG_NAME else UNIT_TYPE
        file += "\t\t$mockkFunction { mocked$targetClassName.${functionName}($argsStr) } returns $returnValue\n"

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
        private const val UNIT_TYPE = "kotlin.Unit"
        private val IN_BUILT_FUNCTIONS = setOf("equals", "hashCode", "toString", "<init>")

        private const val RETURN_VALUE_ARG_NAME = "stub_returnValue"

        private const val MOCKK_EVERY = "io.mockk.every"
        private const val MOCKK_CO_EVERY = "io.mockk.coEvery"
        private const val MOCKK_INIT_CALL = "io.mockk.mockk()"
    }

}