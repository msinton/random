scalafixDependencies in ThisBuild += "com.nequissimus" %% "sort-imports" % "0.2.1"

lazy val scalaSettings = Seq(
  scalaVersion := "2.13.0",
  scalacOptions ++= Seq(
    "-Yrangepos", // required by SemanticDB compiler plugin
    "-Ywarn-unused" // required for RemoveUnused
  )
)

lazy val commonSettings = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  addCompilerPlugin(scalafixSemanticdb),
  bloopExportJarClassifiers in Global := Some(Set("sources"))
)

lazy val testSettings = Seq(
  logBuffered in Test := false,
  parallelExecution in Test := false,
  testOptions in Test += Tests.Argument("-oDF")
)

lazy val root = project
  .in(file("."))
  .settings(scalaSettings)
  .aggregate(first, second, thrid)

lazy val first = project
  .in(file("day1-10"))
  .settings(
    moduleName := "day1-10",
    name := moduleName.value,
    scalaSettings,
    commonSettings,
    testSettings
  )
  .withDependencies

lazy val second = project
  .in(file("day11-20"))
  .settings(
    moduleName := "day1-10",
    name := moduleName.value,
    scalaSettings,
    commonSettings,
    testSettings
  )
  .withDependencies

lazy val thrid = project
  .in(file("day21-25"))
  .settings(
    moduleName := "day1-10",
    name := moduleName.value,
    scalaSettings,
    commonSettings,
    testSettings
  )
  .withDependencies

def addCommandsAlias(name: String, values: List[String]) =
  addCommandAlias(name, values.mkString(";", ";", ""))

addCommandsAlias(
  "tidy",
  List(
    "scalafix RemoveUnused",
    "scalafix SortImports"
  )
)

addCommandsAlias(
  "validate",
  List(
    "+clean",
    "+test",
    "scalafmtCheck",
    "scalafmtSbtCheck"
  )
)
