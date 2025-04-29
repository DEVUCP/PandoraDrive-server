package schema

import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._

def initialize_schemas(): IO[Unit] = {
  create_folder_metadata_table()
    .flatMap(_ => create_file_metadata_table())
    .flatMap(_ => create_chunk_metadata_table())
    .flatMap(_ => create_file_chunk_relationship_table())
    .flatMap(_ => IO.println("Schemas have been initialized successfully"))
    .handleErrorWith { err =>
      IO(println(s"Error initializing schemas: ${err.getMessage}")) *> IO
        .raiseError(err)
    }
}
