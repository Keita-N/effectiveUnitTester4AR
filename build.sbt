import AssemblyKeys._

name := "effectiveUnitTester4AR"

organization := "com.example"

version := "0.0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.4" % "test",
  "commons-codec" % "commons-codec" % "1.9",
  "commons-collections" % "commons-collections" % "3.0",
  "commons-lang" % "commons-lang" % "2.3",
  "commons-logging" % "commons-logging" % "1.1.1",
  "dom4j" % "dom4j" % "1.6.1",
  "org.apache.poi" % "poi" % "3.9",
  "org.apache.poi" % "poi-ooxml" % "3.9",
  "org.apache.poi" % "poi-ooxml-schemas" % "3.9",
  "org.apache.xmlbeans" % "xmlbeans" % "2.4.0",
  "org.json4s" %% "json4s-native" % "3.2.8",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "org.specs2" %% "specs2" % "2.3.7" % "test",
  "com.massrelevance" %% "dropwizard-scala" % "0.6.2-1"
)

scalacOptions in Test ++= Seq("-Yrangepos")

// Read here for optional dependencies:
// http://etorreborre.github.io/specs2/guide/org.specs2.guide.Runners.html#Dependencies

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

initialCommands := "import com.example.dropwizard._"

unmanagedClasspath in Test += baseDirectory.value / "special-resources"

assemblySettings