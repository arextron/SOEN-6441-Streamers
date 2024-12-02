ThisBuild / scalaVersion := "2.13.15"
ThisBuild / version := "1.0-SNAPSHOT"

javacOptions ++= Seq("-source", "11", "-target", "11")
javaOptions += "-Dhttp.port=9001"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """TubeLyticsv2""",
    libraryDependencies ++= Seq(
      guice,
      "com.typesafe.play" %% "play-cache" % "2.8.18",
      "com.typesafe.play" %% "play" % "2.8.18",
      "com.typesafe.play" %% "play-guice" % "2.8.18", // For dependency injection
      "com.typesafe.play" %% "play-ehcache" % "2.8.18", // Ehcache for caching
      "com.google.apis" % "google-api-services-youtube" % "v3-rev222-1.25.0", // YouTube API
      "org.mockito" % "mockito-core" % "4.5.1", // For testing
      "org.mockito" % "mockito-inline" % "4.5.1" % Test,
      "junit" % "junit" % "4.13.2" % Test, // JUnit for tests
      "org.mockito" %% "mockito-scala" % "1.16.55" % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.20",
      "com.typesafe.akka" %% "akka-testkit" % "2.6.20" % Test,
      "org.junit.jupiter" % "junit-jupiter" % "5.9.2" % Test // Removed trailing comma here
    ),
    jacocoExcludes := Seq(
      "views.*",
      "router.*",
      "controllers.routes*",
      "controllers.javascript.*",
      "controllers.ref.*",
      "Reverse*",
      "routes*",
      "controllers.ReverseHomeController",
      ".*\\.template\\.scala",
      // Exclude synthetic Scala templates
      ".\\$Lambda\\$.",
      // Exclude synthetic lambda classes
      "/$.class"
      // Exclude synthetic lambda classes
    )
  )
