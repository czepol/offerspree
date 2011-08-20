import sbt._

class LiftProject(info: ProjectInfo) extends DefaultWebProject(info) {
  val liftVersion = "2.3"
  
  /**
   * Application dependencies
   */
  val webkit    = "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default"
  val logback   = "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default"
  
  val servlet   = "javax.servlet" % "servlet-api" % "2.5" % "provided->default"
  val jetty6    = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test->default"  
  val junit     = "junit" % "junit" % "4.5" % "test->default"
  val specs     = "org.scala-tools.testing" %% "specs" % "1.6.6" % "test->default"
  
  val wizard    = "net.liftweb" %% "lift-wizard" % liftVersion % "compile"
  val mapper    = "net.liftweb" %% "lift-mapper" % liftVersion % "compile"
  val widgets   = "net.liftweb" %% "lift-widgets" % liftVersion % "compile" 
  val textile   = "net.liftweb" %% "lift-textile" % liftVersion % "compile"
  val postgresql= "postgresql" % "postgresql" % "9.0-801.jdbc4" % "compile"
  
  val databinder_net = "databinder.net repository" at "http://databinder.net/repo"
  val dispatch = "net.databinder" %% "dispatch-http" % "0.8.3"
  val dispatchTwitter = "net.databinder" %% "dispatch-twitter" % "0.8.3"
  
  //val thebuzzmedia_com = "The Buzz Media Maven Repository" at "http://maven.thebuzzmedia.com"
  //val scalr = "com.thebuzzmedia" %% "imgscalr-lib" % "3.1" % "compile"
  
  /**
   * Maven repositories
   */
  lazy val scalatoolsSnapshots = ScalaToolsSnapshots

  //override def temporaryWarPath = "build-jetty"
  
}
