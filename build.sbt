ThisBuild / scalaVersion := "2.13.15"

ThisBuild / version := "1.0-SNAPSHOT"

javacOptions ++= Seq("-source", "11", "-target", "11")
lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """TubeLyticsv2""",
    libraryDependencies ++= Seq(
      guice,
        "com.typesafe.play" %% "play" % "2.8.18",
        "com.typesafe.play" %% "play-guice" % "2.8.18", // For dependency injection
        "com.google.apis" % "google-api-services-youtube" % "v3-rev222-1.25.0", // YouTube API
        "org.mockito" % "mockito-core" % "3.12.4", // For testing
        "junit" % "junit" % "4.13.2" % Test, // JUnit for tests
        "org.jacoco" % "org.jacoco.agent" % "0.8.7" % Test // JaCoCo for code coverage
        )
    )
