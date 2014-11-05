package com.zekelogix.scodec.codecs

import java.nio.CharBuffer
import java.nio.charset.Charset

import scodec.Codec
import scodec.bits.{BitVector, ByteOrdering}
import scodec.Err

import scalaz.\/

import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.{MalformedInputException, UnmappableCharacterException}

private[codecs] final class StringCodec(charset: Charset) extends Codec[String] {

  val lengthBits = 63
  val ordering  = ByteOrdering.LittleEndian
  val lengthIsSigned = false

  override def encode(string: String) = {
    val encoder = charset.newEncoder
    val buffer = CharBuffer.wrap(string)
    try {
      val vect = BitVector(encoder.encode(buffer))
      val vectSize = vect.size

      \/.right(BitVector.fromLong(vectSize, lengthBits, ordering) ++ vect)
    } catch {
      case (_: MalformedInputException | _: UnmappableCharacterException) =>
        \/.left(Err(s"${charset.displayName} cannot encode string '$buffer'"))
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
          val stringPayload = decoder.decode(payload.toByteBuffer).toString
          \/.right((rest.drop(vectorSize), stringPayload))
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
        \/.left(Err(s"${charset.displayName} cannot decode String from '0x${buffer.toByteVector.toHex}'"))
    }
  }

  override def toString = charset.displayName
}
