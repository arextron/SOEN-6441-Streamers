
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
// Existing Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.18")
addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.2.0")