package service

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, _}
import service.health._
import service.persistance._
import slick.jdbc.PostgresProfile.api._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import service.rest.JsonProtocol._
import service.rest._
import Schema._
import akka.http.scaladsl.server.ValidationRejection
import com.github.javafaker.Faker

class Test1 extends WordSpecLike with Matchers with BeforeAndAfterAll with ScalatestRouteTest with SprayJsonSupport{


  val faker = new Faker()

  var db:Database = _
  val fakeUsers:List[User] = {
    (0 until 20).toList.map( x =>
      User(None, faker.name().username(), faker.internet().emailAddress(), faker.random().nextInt(18, 60))
    )
  }

  override def beforeAll(): Unit = {
    db = Database.forConfig("postgresDB")

    Await.result(db.run(DBIO.seq(
      users.schema.create,
      pictures.schema.create,
      users ++= fakeUsers.take(10)
    )), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(
      pictures.schema.drop,
      users.schema.drop
    )), Duration.Inf)
    db.close()
  }

  "Test UserServise" should {

    "return a list of users" in {
      val us =  new UserService()
      val ur = us.userRoutes

      Get("/users?offset=2&limit=8") ~> ur ~> check {
        println( response.toString())
        status shouldEqual StatusCodes.OK
        val l = responseAs[List[User]]
        l.length shouldBe 8
        l.head.email shouldBe fakeUsers(2).email
      }

      Get("/users/1") ~> ur ~> check {
        println( response.toString())
        status shouldEqual StatusCodes.OK
        responseAs[User].email shouldBe fakeUsers.head.email
      }

      Post("/users", fakeUsers(11)) ~> ur ~> check {
        println( response.toString())
        responseAs[String] shouldBe "11"
        status shouldEqual StatusCodes.OK
      }

      Put("/users", fakeUsers(12).copy(id = Some(8))) ~> ur ~> check {
        println( response.toString())
        status shouldEqual StatusCodes.OK
      }

      Get("/users/8") ~> ur ~> check {
        println( response.toString())
        status shouldEqual StatusCodes.OK
        responseAs[User].email shouldBe fakeUsers(12).email
      }

      Delete("/users/8")~> ur ~> check {
        println( response.toString())
        status shouldEqual StatusCodes.OK
      }

      Get("/users/8") ~> ur ~> check {
        println( response.toString())
        status shouldEqual StatusCodes.NotFound
      }

      //email conflict
      Post("/users", fakeUsers(0)) ~> ur ~> check {
        println( response.toString())
        status shouldEqual StatusCodes.Conflict
      }

      //email conflict
      Put("/users", fakeUsers(0).copy(id = Some(5))) ~> ur ~> check {
        println( response.toString())
        status shouldEqual StatusCodes.Conflict
      }

      //age < 18
      Post("/users", fakeUsers(13).copy(age = 5)) ~> ur ~> check {
        rejection shouldEqual ValidationRejection(us.errMSG, None)
      }
    }

    "pictures upload" in {
      val ps =  new PictureService()
      val pr = ps.pictireRoutes

      val multipartForm =
        Multipart.FormData(
          Multipart.FormData.BodyPart.Strict(
            "jpg",
            HttpEntity(ContentType(MediaTypes.`image/jpeg`), Array[Byte](12,12,12)),
            Map("filename" -> "temp.jpeg")))

      Post("/pictires/1", multipartForm) ~> pr ~> check {
        println(response.toString())
        status shouldEqual StatusCodes.OK
      }
    }
  }
}
