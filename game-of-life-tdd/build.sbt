name := "game-of-life"

version := "0.1.SNAPSHOT"

scalaVersion := "2.12.8"

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe Scala compiler
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.0.0-M4",
  "org.scalactic" %% "scalactic" % "3.0.8",
  "org.scalatest" %% "scalatest" % "3.0.8",
  "org.scalacheck" %% "scalacheck" % "1.14.0"
)

Test / logBuffered := false
