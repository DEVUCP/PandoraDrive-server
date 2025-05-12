package backend

import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import com.comcast.ip4s.*
import org.http4s.server.Router
import backend.core.ChatbotEngine
import backend.utils.TokenReader
import backend.utils.config
import io.circe.generic.auto._  // For automatic codec derivation
import io.circe.syntax._        // For .asJson extension
import org.http4s.circe._       // For http4s JSON support
import backend.models._

object server extends IOApp:

  private val tokenLoader: IO[ChatbotTokens] =
    IO.fromEither(TokenReader.GetAllTokens())
      .handleErrorWith(err =>
        IO.raiseError(new RuntimeException(s"Failed to load tokens: $err")))

  //simple HTTP service/app
  private def helloWorldService(engine: ChatbotEngine): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name!")
    case GET -> Root / "hello" =>
      Ok("Hello, World!")
    case GET -> Root / "ping" =>
      Ok("pong")
    case GET -> Root / "chat" / prompt =>
      engine.handleUserInput(prompt).flatMap { response =>
        Ok(response.asJson)
      }
  }

  var port = sys.env.get("CHATBOT_SERVICE_PORT") match {
    case Some(port) => Port.fromString(port).getOrElse(port"55550")
    case None => port"55550"
  }
  // server run func
  def run(args: List[String]): IO[ExitCode] = {
    val service_port = config.SERVICE_PORT

    val serverResources = for {
      engine <- Resource.eval(ChatbotEngine.create(tokenLoader))
      routes = helloWorldService(engine)
      httpApp = Router("/" -> routes).orNotFound
      server <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(service_port)
        .withHttpApp(httpApp)
        .build
    } yield server

    serverResources.use { _ =>
      IO.println(s"Server started on http://localhost:${service_port}") *>
      IO.never
    }.as(ExitCode.Success)
  }