package service.health

import java.lang.management.ManagementFactory

import akka.http.scaladsl.server.Directives._

import scala.concurrent.duration._

trait HealthRoutes {

  def healthRoutes = (pathPrefix("healthcheck") & get) {
    complete("OK")
  }
}
