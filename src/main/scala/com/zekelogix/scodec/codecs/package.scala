package com.zekelogix.scodec

import java.nio.charset.Charset
import java.util.Date

import scodec.Codec
import scodec.bits.ByteOrdering
import scodec.codecs.optional
import scodec.codecs.provide

/**
 * Easy access to our codecs via package defs. This is the correct way to
 * access them.
 * Created by rjc on 11/3/14.
 */
package object codecs {
  /**
   * Codec java.util.Date that is internally a long.
   * @group numbers
   */
  def date(): Codec[Date] = new DateCodec(ByteOrdering.LittleEndian)

  /**
   * Encode/Decode a single Char, prepends a bit length of the BitVector
   * @param set  charset used to encode and decode the Char
   * @return
   */
  def char(set: Charset): Codec[Char] = new CharCodec(set)

  /**
   * Encode and decode a String, prepends a bit length of the BitVector
   * @param set  charset used to encode and decode the String from the bytebuffer
   * @return
   */
  def varstring (set: Charset): Codec[String] = new StringCodec(set)

  /**
   * encode/decode an Option[Date]
   * Some(s) are represented with a prepended high bit and the Date Conversion
   * to a signed long.
   * None values are represented with a low
   * @return
   */
  def optionalDate (): Codec[Option[Date]] = new OptionalDateCodec()

  /**
   * encode/decode an Option{Char]
   * @param set  Charset to use for any character encoding and decodings
   *              from the byte buffer
   * @return
   */
  def optionalChar (set: Charset): Codec[Option[Char]] = new OptionalCharCodec(set)

  /**
   * encode and decode the Employee case class
   * @param set  Charset to use for any character encodings and decodings
   *            from the byte buffer
   * @return
   */
  def employee (set: Charset): Codec[Employee] = Employee.codec(set)

  /**
   * encode and decode the TestWidget case class.
   * @param set  Charset to use for any character encodings and decodings from
   *             the byte buffer
   * @return
   */
  def testWidget(set:Charset): Codec[TestWidget] = TestWidget.codec(set)
}
