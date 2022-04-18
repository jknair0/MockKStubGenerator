package tech.jknair.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * This is what that creates the Symbol Processor and passes the necessary arguments
 */
internal class StubFactoryProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return StubFactoryProcessor(environment.codeGenerator, environment.logger)
    }

}