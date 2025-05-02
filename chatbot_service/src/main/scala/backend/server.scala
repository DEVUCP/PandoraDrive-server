package backend

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import com.comcast.ip4s.*

object server extends IOApp:

  //simple HTTP service/app
  private val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
    case GET -> Root / "hello" =>
      Ok("Hello, World!")
    case GET -> Root / "ping" =>
      Ok("pong")
  }.orNotFound

  var port = sys.env.get("CHATBOT_SERVICE_PORT") match {
    case Some(port) => Port.fromString(port).getOrElse(port"55550")
    case None => port"55550"
  }
  // server run func
  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port)
      .withHttpApp(helloWorldService)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)