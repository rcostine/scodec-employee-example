import sbtrelease._
import ReleaseStateTransformations._
import ReleasePlugin._
import ReleaseKeys._
import com.typesafe.tools.mima.core._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys._

organization := "com.zekelogix"

name := "scodec-employee-example"

scalaVersion := "2.10.4"

crossScalaVersions := Seq(scalaVersion.value, "2.11.0")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-optimise",
  "-Xcheckinit",
  "-Xlint",
  "-Xverify",
  "-Yclosure-elim",
  "-Yinline",
  "-Yno-adapted-args")

scalacOptions in (Compile, doc) ++= {
  val tagOrBranch = if (version.value endsWith "SNAPSHOT") "master" else ("v" + version.value)
  Seq(
    "-diagrams",
    "-groups",
    "-implicits",
    "-implicits-show-all",
    "-skip-packages", "scalaz",
    "-sourcepath", baseDirectory.value.getAbsolutePath
  )
}

autoAPIMappings := true

triggeredMessage := (_ => Watched.clearScreen)

resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"

libraryDependencies ++= Seq(
  "org.typelevel" %% "scodec-core" % "1.4.0-SNAPSHOT",
  "org.typelevel" %% "scodec-bits" % "1.0.4",
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "com.chuusai" % "shapeless" % "2.0.0" cross CrossVersion.fullMapped {
    case "2.10.4" => "2.10.4"
    case "2.11.0" => "2.11"
  },
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.3" % "test",
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.50" % "test"
)

osgiSettings

OsgiKeys.exportPackage := Seq("!scodec.bits,scodec.*;version=${Bundle-Version}")

OsgiKeys.importPackage := Seq(
  """scodec.bits.*;version="$<range;[==,=+);$<@>>"""",
  """scala.*;version="$<range;[==,=+);$<@>>"""",
  """scalaz.*;version="$<range;[==,=+);$<@>>"""",
  "*"
)

OsgiKeys.additionalHeaders := Map("-removeheaders" -> "Include-Resource,Private-Package")

pomIncludeRepository := { x => false }

pomExtra := (
  <url>http://github.com/rcostine/scodec-employee-example</url>
  <scm>
    <url>git@github.com:rcostine/scodec-employee-example.git</url>
    <connection>scm:git:git@github.com:rcostine/scodec-employee-example.git</connection>
  </scm>
  <developers>
    <developer>
      <id>rcostine</id>
      <name>Richard Costine</name>
      <url>http://github.com/rcostine</url>
    </developer>
  </developers>
)
