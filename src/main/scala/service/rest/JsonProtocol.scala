package service.rest

import service.persistance.User
import spray.json.DefaultJsonProtocol

object JsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(User)
//  implicit val simpleSupplierFormat = jsonFormat2(SimpleSupplier)

  implicit val formatPictureUploadRes = jsonFormat2(PictureUploadRes)
}