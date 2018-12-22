package org.alephium.serde

import akka.util.ByteString

trait Deserializer[T] { self =>
  def _deserialize(input: ByteString): Either[SerdeError, (T, ByteString)]

  def deserialize(input: ByteString): Either[SerdeError, T] =
    _deserialize(input).flatMap {
      case (output, rest) =>
        if (rest.isEmpty) Right(output)
        else Left(WrongFormatError.redundant(input.size - rest.size, input.size))
    }

  def validateGet[U](get: T => Option[U], error: T => String): Deserializer[U] =
    (input: ByteString) => {
      self._deserialize(input).flatMap {
        case (t, rest) =>
          get(t) match {
            case Some(u) => Right((u, rest))
            case None    => Left(new WrongFormatError(error(t)))
          }
      }
    }
}

object Deserializer { def apply[T](implicit T: Deserializer[T]): Deserializer[T] = T }