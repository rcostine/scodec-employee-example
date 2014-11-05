package com.zekelogix.scodec.codecs

import java.nio.charset.Charset
import java.util.Date
import java.util.Date

import scodec.Err
import scodec.bits.{ByteVector, BitVector, ByteOrdering}
import scodec.codecs._

import scalaz.\/
/**
 * Created by rjc on 11/4/14.
 */
private[codecs] final class OptionalCharCodec(set: Charset) extends scodec.Codec[Option[Char]] {
  private def description = s"Optional Char"

  override def encode(charOpt: Option[Char]) = {

    charOpt match {
      case(Some(c)) =>
        val enc = char(set).encode(c)
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
          val decodeChar = char(set).decode(nb)
          if (decodeChar.isLeft) {
            \/.left(decodeChar.toEither.left.get)
          }
          else {
            val decodeEither = decodeChar.toEither.right.get
            \/.right((decodeEither._1,Some(decodeEither._2)))
          }
        }
        else {
          \/.right((buffer.drop(1), None))
        }
    }

  override def toString = description
}
