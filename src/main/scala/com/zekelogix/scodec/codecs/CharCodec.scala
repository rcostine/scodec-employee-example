package com.zekelogix.scodec.codecs

import scodec.bits.{ByteOrdering, BitVector}
import scodec.codecs
import scodec.Codec
import scodec.Err

import scalaz.\/

import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.{MalformedInputException, UnmappableCharacterException}

private[codecs] final class CharCodec(charset: Charset) extends Codec[Char] {

  val lengthBits = 63
  val ordering  = ByteOrdering.LittleEndian
  val lengthIsSigned = false

  override def encode(str: Char) = {
    val encoder = charset.newEncoder
    val string = str.toString
    val buffer = CharBuffer.wrap(string)
    try {
      val vect = BitVector(encoder.encode(buffer))
      val vectSize = vect.size
      \/.right(BitVector.fromLong(vectSize, lengthBits, ordering) ++ vect)
    } catch {
      case (_: MalformedInputException | _: UnmappableCharacterException) =>
        \/.left(Err(s"${charset.displayName} cannot encode character '${buffer.charAt(0)}'"))
    }
  }

  override def decode(buffer: BitVector) = {
    val decoder = charset.newDecoder
    try {
      val lengthVector = buffer.acquire(lengthBits)
      if (lengthVector.isRight) {
        val vectorSize = lengthVector.right.get.toLong(lengthIsSigned, ordering)
        val rest = buffer.drop(lengthBits)
        val payloadVector = rest.acquire(vectorSize)

        if (payloadVector.isRight) {
          val payload = payloadVector.right.get
          val charPayload = decoder.decode(payload.toByteBuffer).toString.charAt(0)
          \/.right((rest.drop(vectorSize), charPayload))
        }
        else {
          \/.left(Err(payloadVector.left.get))
        }
      }
      else {
        \/.left(Err(lengthVector.left.get))
      }
    } catch {
      case (_: MalformedInputException | _: UnmappableCharacterException) =>
        \/.left(Err(s"${charset.displayName} cannot decode Char from '0x${buffer.toByteVector.toHex}'"))
    }
  }

  override def toString = charset.displayName
}
