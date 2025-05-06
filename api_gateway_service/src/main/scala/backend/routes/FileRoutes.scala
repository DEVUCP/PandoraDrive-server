package backend.routes

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import com.comcast.ip4s.*
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.multipart.Multipart

import io.circe.Json
import io.circe.generic.auto._
import org.http4s.implicits._
import utils.config

object FileRoutes {

  val file_service_port = config.FILE_SERVICE_PORT

  def routeRequestImpl[T](req: Request[IO], uriString: String, method : Method)(implicit decoder: EntityDecoder[IO, T], encoder: EntityEncoder[IO, T]): cats.effect.IO[org.http4s.Response[cats.effect.IO]] = {
    EmberClientBuilder.default[IO].build.use { client =>
          Uri.fromString(uriString) match {
        case Right(uri) =>
          client.expect[T](req.withUri(uri).withMethod(method))
            .flatMap(Ok(_))
            .handleErrorWith {
              case e =>
                println(s"Error during request to $uriString: $e")
                InternalServerError("Internal Server Error")
            }
        
        case Left(parseFailure) =>
          println(s"Invalid URI '$uriString': ${parseFailure.details}")
          InternalServerError("Invalid URI")
      }
    }

    }
  
  def routeRequestJson(req: Request[IO], URI: String, method : Method): cats.effect.IO[org.http4s.Response[cats.effect.IO]] = {
    routeRequestImpl[Json](req, URI, method)
  }

  def routeRequestString(req: Request[IO], URI: String, method : Method): cats.effect.IO[org.http4s.Response[cats.effect.IO]] = {
    routeRequestImpl[String](req, URI, method)
  }

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => 
      Ok("List of files & their metadata")
    
    case req @ GET -> Root / "ping" =>
      routeRequestJson(req, s"http://localhost:$file_service_port/ping", Method.GET)
    
    case req @ POST -> Root / "upload" / "init" => 
      Ok("placeholder upload initialize")      

    case DELETE -> Root / "delete" =>
      Ok(s"placeholder file deleted")
  } 
}