package tech.jknair.processor

import java.io.OutputStream

internal operator fun OutputStream.plusAssign(content: String) {
    this.write((content).toByteArray())
}