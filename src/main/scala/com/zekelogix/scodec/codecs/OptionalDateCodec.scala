package com.zekelogix.scodec.codecs

import java.util.Date

import scodec.Err
import scodec.bits.{ByteVector, BitVector, ByteOrdering}
import scodec.codecs._

import scalaz.\/

/**
 * Created by rjc on 11/4/14
 *
 * This Codec will encode and decode an Option[Date]
 *
 * Some(Date) is represented by a high bit and the Date, None by a low.
 */
private[codecs] final class OptionalDateCodec() extends scodec.Codec[Option[Date]] {
  private def description = s"Optional java.util.Date (as unsigned long)"

  override def encode(dtOpt: Option[Date]) = {

    dtOpt match {
      case(Some(dt)) =>
        val enc = date.encode(dt)
        val bvEither = enc.toEither
        if (bvEither.isRight) {
          \/.right(BitVector.high(1) ++ bvEither.right.get)
        }
        else {
          \/.left(bvEither.left.get)
        }
      case(None) => \/.right(BitVector.low(1))

    }

  }

  override def decode(buffer: BitVector) =
    buffer.acquire(1) match {
      case Left(e) => \/.left(Err.insufficientBits(1, 0))
      case Right(b) =>
        if (b.head) {
          val nb = buffer.drop(1)
          val decodeDate = date.decode(nb)
          if (decodeDate.isLeft) {
            \/.left(decodeDate.toEither.left.get)
          }
          else {
            val decodeEither = decodeDate.toEither.right.get
            \/.right((decodeEither._1,Some(decodeEither._2)))
          }
        }
        else {
          \/.right((buffer.drop(1), None))
        }
    }

  override def toString = description
}
