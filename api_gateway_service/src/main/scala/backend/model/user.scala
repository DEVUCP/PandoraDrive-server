package model

import doobie._
import doobie.implicits._
import cats._
import cats.data._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import io.circe.Encoder
import db.transactor

case class User(
    userId: Int,
    username: String,
    password: String
)

object User {

  case class AuthUser(
      id: Int,
      username: String
  )

  import doobie.generic.auto._

  def add_user(username: String, pass: String): IO[Unit] =
    sql"""
        INSERT INTO users(user_id, username, password) VALUES(CAST((SELECT COALESCE(MAX(CAST(user_id AS INTEGER)), 0) + 1 FROM users) AS TEXT), ${username}, ${pass})
        """.update.run.void.transact(transactor).handleErrorWith { error =>
      IO.pure(None)
    }

  def get_user_by_username(username: String): IO[Option[User]] =
    sql"""
        select user_id, username, password from users where username = $username
        """.query[User].option.transact(transactor).handleErrorWith { error =>
      IO.pure(None)
    }

  def get_user_by_id(userId: Int): IO[Option[User]] =
    sql"""
        select user_id, username, password from users where user_id = $userId
        """.query[User].option.transact(transactor).handleErrorWith { error =>
      IO.pure(None)
    }

  def remove_user(userId: Int): IO[Unit] =
    sql"""
        delete from users where user_id = $userId
        """.update.run.void.transact(transactor).handleErrorWith { error =>
      IO.pure(None)
    }

  def get_all_users(): IO[List[User]] =
    sql"""
        select user_id, username, password from users
        """.query[User].to[List].transact(transactor).handleErrorWith { error =>
      IO.pure(List.empty[User])
    }

}

