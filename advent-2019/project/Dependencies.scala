import sbt.Keys._
import sbt._

object Dependencies extends AutoPlugin {
  object autoImport {
    implicit final class DependenciesProject(val project: Project) extends AnyVal {
      def withDependencies: Project = project.settings(defaultDependencySettings)
    }
  }

  val defaultDependencySettings: Seq[Def.Setting[_]] = {

    val catsVersion = "2.0.0"

    val kittens = Seq(
      "org.typelevel" %% "cats-free" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsVersion,
      "org.typelevel" %% "kittens" % catsVersion
    )

    val refined = Seq(
      "eu.timepit" %% "refined",
      "eu.timepit" %% "refined-cats"
    ).map(_ % "0.9.10")

    val circe = Seq(
      "io.circe" %% "circe-generic"
    ).map(_ % "0.12.0-M4")

    val enumeratum = Seq(
      "com.beachape" %% "enumeratum" % "1.5.13",
      "com.beachape" %% "enumeratum-circe" % "1.5.21",
      "com.beachape" %% "enumeratum-cats" % "1.5.16"
    )

    val monocle = Seq(
      "com.github.julien-truffaut" %% "monocle-core",
      "com.github.julien-truffaut" %% "monocle-macro"
    ).map(_ % "2.0.0")

    val scalaTest = Seq(
      "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"
    )

    Seq(
      libraryDependencies ++= {
        kittens ++
          refined ++
          circe ++
          enumeratum ++
          monocle
      },
      libraryDependencies ++= {
        scalaTest
      }.map(_ % Test)
    )
  }
}
