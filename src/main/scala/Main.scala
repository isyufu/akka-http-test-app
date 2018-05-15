import akka.actor.ActorSystem
import akka.event.Logging
import akka.event.Logging.InfoLevel
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
//import com.google.inject.Guice
import service.health._
import service.persistance._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await

object Main extends App with HealthRoutes {
  
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher 

  val settings = Settings(system)

  val logger = Logging(system, getClass)




//  val db = Database.forConfig("postgresDB")
//  try {
//    Await.result(db.run(DBIO.seq(
//      // create the schema
//      Schema.users.schema.create,
//
//      // insert two User instances
//      Schema.users += User(None, "xxx", "aaa@aaa.ru"),
//      Schema.users += User(None, "xxx", "aaa@aaa.ru"),
//
//      // print the users (select * from USERS)
//      users.result.map(println)
//    )), Duration.Inf)
//  } finally db.close

  /** Use Guice for Dependency Injection. Remove if not required */
//  private val injector = Guice.createInjector(UserServiceModule)
//  private val userService = injector.getInstance(classOf[UserService])
  
  val routes = logRequestResult("", InfoLevel)(/*userService.userRoutes ~*/ healthRoutes)

  Http().bindAndHandle(routes, settings.Http.interface, settings.Http.port) map { binding =>
    logger.info(s"Server started on port {}", binding.localAddress.getPort)
  } recoverWith { case _ => system.terminate() }
}
