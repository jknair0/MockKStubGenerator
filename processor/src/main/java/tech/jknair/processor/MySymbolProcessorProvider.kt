package tech.jknair.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * This is what that creates the Symbol Processor and passes the needed arguments
 */
class MySymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MySymbolProcessor(environment.codeGenerator, environment.logger)
    }

}