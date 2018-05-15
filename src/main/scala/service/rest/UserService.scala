package service.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import service.persistance.{Schema, User}
import JsonProtocol._
import slick.jdbc.PostgresProfile.api._
import spray.json.DefaultJsonProtocol
import service.persistance.Schema._
import scala.util.{Failure, Success}

class UserService extends DefaultJsonProtocol with SprayJsonSupport {

  val errMSG = "must be: 18 <= age <= 150, 2 <= trim(name) <= 50, email is valid"
  //from play(stackoverflow)
  private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  def check(e: String): Boolean = e match{
    case null                                           => false
    case e if e.trim.isEmpty                            => false
    case e if emailRegex.findFirstMatchIn(e).isDefined  => true
    case _                                              => false
  }

  def userValidate(u:User) = u.age >= 18 && u.age <= 150 &&
    u.name.trim.length > 2 && u.name.trim.length <= 50 &&
    check(u.email)

  val userRoutes =
    path("users") {
      (get & parameters('limit.as[Int] ? 10, 'offset.as[Int] ? 0)){ (limit, offset) =>
        onComplete(db.run(users.drop(offset).take(limit).result)) {
          case Success(supplierOpt) => complete(supplierOpt)
          case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
        }
      } ~
      (post & withSizeLimit(1000) & entity(as[User])) { user =>
        validate(userValidate(user), errMSG){
          onComplete(db.run((users returning users.map(_.id)) += user)) {
            case Success(newId) => complete(newId.toString)
            case Failure(ex) => {
              if (ex.getMessage.contains("duplicate key value violates unique constraint \"USERS_EMAIL_key\"")) {
                val i = ex.getMessage.indexOf("Detail: ")
                complete(Conflict, ex.getMessage.substring(i + 8))
              } else
                complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
            }
          }
        }
      } ~
      (put & withSizeLimit(1000) & entity(as[User])) { user =>
        validate(userValidate(user), errMSG){
          validate(user.id.getOrElse(0) > 0, "user.id must be > 0") {
            onComplete(db.run(users.filter(_.id === user.id).update(user))) {
              case Success(id) => complete(id.toString)
              case Failure(ex) => {
                if (ex.getMessage.contains("duplicate key value violates unique constraint \"USERS_EMAIL_key\"")) {
                  val i = ex.getMessage.indexOf("Detail: ")
                  complete(Conflict, ex.getMessage.substring(i + 8))
                } else
                  complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
              }
            }
          }
        }
      }
    } ~
      ( get & path("users" / IntNumber)) { id =>
      onComplete(db.run(users.filter(_.id === id).result.headOption )) {
        case Success(opt) => opt match {
          case Some(u) => complete(u)
          case None => complete(NotFound)
        }
        case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
      }
    } ~
      ( delete & path("users" / IntNumber)) { id =>
      onComplete(db.run(users.filter(_.id === id).delete )) {
        case Success(c) => complete(c.toString)
        case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
      }
    }
}
