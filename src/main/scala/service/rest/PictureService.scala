package service.rest

import java.io.File
import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FileInfo
import JsonProtocol._
import service.persistance.{Schema, User, Pic}
import slick.jdbc.PostgresProfile.api._
import spray.json.DefaultJsonProtocol
import service.persistance.Schema._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class PictureService extends DefaultJsonProtocol with SprayJsonSupport {
  def tempDestination(fileInfo: FileInfo): File =
    new File("./temp/"+UUID.randomUUID().toString + ".jpg")


  val pictireRoutes =
    path("pictires" / IntNumber) { userId =>
      validate(Await.result(db.run(users.filter(_.id === userId).result.headOption), Duration.Inf).nonEmpty, "User is not exist") {
        storeUploadedFile("jpg", tempDestination ) { (metadata, file) =>

          val p = Pic(None, userId, file.getPath)

          val q1 = (pictures returning pictures.map(_.id)) += p
          val q2 = rating(userId)

          onComplete(db.run(q1 zip q2)) {
            case Success(r) => complete(PictureUploadRes(r._1, r._2))
            case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
          }
        }
      }
    }
}

case class PictureUploadRes(id:Int, rating:Double)