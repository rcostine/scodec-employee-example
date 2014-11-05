package com.zekelogix.scodec.codecs

import scalaz.\/
import scalaz.syntax.std.either._
import scalaz.syntax.std.option._
import scodec.Err

import java.nio.{ ByteBuffer, ByteOrder }
import java.util.Date

import scodec.bits.{ BitVector, ByteOrdering, ByteVector }

private[codecs] final class DateCodec(ordering: ByteOrdering) extends scodec.Codec[Date] {

  val signed = true
  val bits = 64
  val MaxValue = (1L << (bits - 1)) - 1
  val MinValue = -(1L << (bits - 1))

  private def description = s"java.util.Date (as unsigned long)"

  override def encode(dt: Date) = {
    val i = dt.getTime

    if (i > MaxValue) {
      \/.left(Err(s"$dt is greater than maximum value $MaxValue for $description"))
    } else if (i < MinValue) {
      \/.left(Err(s"$dt is less than minimum value $MinValue for $description"))
    } else {
      \/.right(BitVector.fromLong(i, bits, ordering))
    }
  }

  override def decode(buffer: BitVector) =
    buffer.acquire(bits) match {
      case Left(e) => \/.left(Err.insufficientBits(bits, buffer.size))
      case Right(b) => \/.right((buffer.drop(bits), new Date (b.toLong(signed, ordering))))
    }

  override def toString = description
}