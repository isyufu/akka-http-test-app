enablePlugins(JavaAppPackaging)

organization := "io.forward"

name := "akka-http-test-app"

version := "1.0"

scalaVersion := "2.12.4"

scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "10.1.1"
  val scalaTestV  = "3.0.5"
  Seq(
    "com.typesafe"        % "config"                               % "1.3.3",
    "com.typesafe.akka"   %% "akka-http-core"                      % akkaV,
//    "com.typesafe.akka" %% "akka-stream"                         % akkaV,
    "com.typesafe.akka"   %% "akka-http-spray-json"                % akkaV,
    "com.typesafe.akka"   %%  "akka-http-testkit"                  % akkaV,
    "com.typesafe.slick"  %% "slick"                               % "3.2.3",
    "org.postgresql"      % "postgresql" % "42.2.2",

    "com.google.inject"   % "guice"                                % "4.1.0",
    "org.scalatest"       %% "scalatest"                           % scalaTestV  % "test",
    "com.github.javafaker" % "javafaker"                           % "0.15"      % "test"
  )
}
