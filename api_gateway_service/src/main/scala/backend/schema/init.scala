package schema

import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._

def initialize_schema(): IO[Unit] = {
  create_user_table()
    .handleErrorWith { err =>
      IO(println(s"Error initializing schema: ${err.getMessage}")) *> IO
        .raiseError(err)
    }
}

