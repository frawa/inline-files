addCommandAlias("lint", "headerCheckAll;fmtCheck;fixCheck")
addCommandAlias("lintFix", "headerCreateAll;fixFix;fmtFix")
addCommandAlias("fmtCheck", "all scalafmtCheck scalafmtSbtCheck")
addCommandAlias("fmtFix", "all scalafmt scalafmtSbt")
addCommandAlias("fixCheck", "scalafixAll --check")
addCommandAlias("fixFix", "scalafixAll")
addCommandAlias("testAll", "test;+ test")

lazy val scalaVersion3   = "3.4.3"
lazy val scalaVersion213 = "2.13.14"

import xerial.sbt.Sonatype._

publish / skip := true

lazy val sharedSettings = Seq(
  scalaVersion     := scalaVersion3,
  organization     := "io.github.frawa",
  organizationName := "Frank Wagner",
  description      := "A macro library to inline file contents.",
  homepage         := Some(url("https://github.com/frawa/inline-files")),
  sonatypeProjectHosting := Some(
    GitHubHosting("frawa", "inline-files", "agilecoderfrank@gmail.com")
  ),
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeRepository     := "https://s01.oss.sonatype.org/service/local",
  versionScheme          := Some("semver-spec"),
  crossScalaVersions     := Seq(scalaVersion3),
  developers := List(
    Developer(
      "frawa",
      "Frank Wagner",
      "agilecoderfrank@gmail.com",
      url("https://github.com/frawa")
    )
  )
)

lazy val sharedLintSettings = Seq(
  startYear := Some(2022),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  semanticdbEnabled := true
)

lazy val sharedScalacSettings = Seq(
  scalacOptions ++= {
    Seq(
      "-Xcheck-macros",
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xmigration",
      "-new-syntax",
      "-indent",
      "-Wunused:linted"
    )
  }
)

lazy val sharedTestSettings = Seq(
  libraryDependencies += "org.scalameta" %%% "munit" % "1.0.1" % Test,
  Test / testOptions += Tests.Argument("-q", "--summary=0")
)

lazy val rootFolder = file(".")
lazy val root = project
  .in(rootFolder)
  .settings(sharedSettings)
  .settings(
    name           := "inline-files-root",
    publish / skip := true
  )
  .aggregate(inlineFiles.jvm, inlineFiles.js)
  .aggregate(example.jvm, example.js)
  .settings(sharedLintSettings)

lazy val inlineFiles = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("inline-files"))
  .settings(sharedSettings)
  .settings(
    name := "inline-files"
  )
  .settings(sharedLintSettings)
  .settings(sharedScalacSettings)
  .settings(sharedTestSettings)
  .settings(scalacOptions ++= {
    Seq(
      s"-Xmacro-settings:MY_INLINE_HOME=${rootFolder.getAbsolutePath()}"
    )
  })
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion213
  )

lazy val example = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .withoutSuffixFor(JVMPlatform)
  .in(file("example"))
  .settings(sharedSettings)
  .settings(
    scalaVersion   := scalaVersion3,
    name           := "example",
    publish / skip := true
  )
  .settings(sharedLintSettings)
  .settings(sharedTestSettings)
  .settings(
    crossScalaVersions := Seq(scalaVersion3, scalaVersion213),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) => Seq("-Ytasty-reader", "-Wunused:imports")
        case _             => Seq("-Wunused:linted")
      }
    }
  )
  .dependsOn(inlineFiles)
