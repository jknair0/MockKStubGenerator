plugins {
  kotlin("jvm")
}

dependencies {
  implementation(project(":annotation"))
  implementation("com.google.devtools.ksp:symbol-processing-api:1.6.20-1.0.5")
}