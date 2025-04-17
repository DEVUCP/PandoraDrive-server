package backend.routes

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

object FileRoutes {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => 
      Ok("List of files & their metadata")
    
    case POST -> Root / "upload" => 
      Ok(s"Upload file")

    case DELETE -> Root / "delete"  =>
      Ok(s"file deleted")
  } 
}
