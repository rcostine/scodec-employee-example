package com.zekelogix.scodec.codecs
import java.nio.charset.Charset
import java.util.Date

import scodec.Codec
import scodec.codecs._

/**
 * Testing case class, just has Char(s)
 * @param ming
 * @param mang
 * @param dateOfBirth
 * @param terminationDate
 * @param rank
 */
case class TestWidget(
                     ming: Char,
                     mang: Char,
                     dateOfBirth: Date,
                     terminationDate: Option[Date],
                     rank: Int)
/**
 * Created by rjc on 11/4/14.
 */
object TestWidget {
    implicit def codec(set: Charset) : Codec[TestWidget] = {
      ("ming" | char(set)) ::
    ("mang" | char(set)) ::
    ("dateOfBirth" | date) ::
    ("terminationDate" | optionalDate) ::
    ("rank" | int8 )
    }.as[TestWidget]
}
