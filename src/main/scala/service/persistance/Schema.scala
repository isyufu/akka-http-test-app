package service.persistance

import slick.jdbc.PostgresProfile.api._
import spray.json.DefaultJsonProtocol.jsonFormat4

object Schema {
  lazy val db = Database.forConfig("postgresDB")

  val users = TableQuery[Users]
  val pictures = TableQuery[Pictures]

  def rating(userId:Int) = (pictures.filter(_.userId === userId).length.asColumnOf[Double] / pictures.length.asColumnOf[Double]).result
}

case class  User(id:Option[Int] = None,  name:String, email:String,  age:Int)
case class  Pic(id:Option[Int] = None,  userId:Int, location:String)

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def email = column[String]("EMAIL", O.Unique)
  def age = column[Int]("AGE")

  def * = (id.?, name, email, age) <> (User.tupled, User.unapply)
}

class Pictures(tag: Tag) extends Table[Pic](tag, "PICTURES") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def userId = column[Int]("USER_ID")
  def location = column[String]("LOCATION")

  def user = foreignKey("USER_ID", userId, Schema.users)(_.id)


  def * = (id.?, userId, location) <> (Pic.tupled, Pic.unapply)
}





