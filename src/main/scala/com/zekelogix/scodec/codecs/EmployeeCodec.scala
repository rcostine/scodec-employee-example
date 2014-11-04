package com.zekelogix.scodec.codecs

import scodec.bits.BitVector
import scodec.codecs
import scodec.Codec
import scodec.Err

import scalaz.\/

import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.{MalformedInputException, UnmappableCharacterException}

import scodec.bits.BitVector

private[codecs] final class EmployeeCodec(charset: Charset) extends Codec[Char] {

  override def encode(str: Char) = {
    val encoder = charset.newEncoder
    val string = str.toString
    val buffer = CharBuffer.wrap(string)
    try \/.right(BitVector(encoder.encode(buffer)))
    catch {
      case (_: MalformedInputException | _: UnmappableCharacterException) =>
        \/.left(Err(s"${charset.displayName} cannot encode character '${buffer.charAt(0)}'"))
    }
  }

  override def decode(buffer: BitVector) = {
    val decoder = charset.newDecoder
    try {
      \/.right((BitVector.empty, decoder.decode(buffer.toByteBuffer).toString.charAt(0)))
    } catch {
      case (_: MalformedInputException | _: UnmappableCharacterException) =>
        \/.left(Err(s"${charset.displayName} cannot decode Char from '0x${buffer.toByteVector.toHex}'"))
    }
  }

  override def toString = charset.displayName
}
