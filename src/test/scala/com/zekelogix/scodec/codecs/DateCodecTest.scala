package com.zekelogix.scodec.codecs

import java.util.Date
import scodec.codecs._
import shapeless._

import com.zekelogix.scodec.CodecSuite

/** Test for the OptionalDateCodec
 *
 * Created by rjc on 11/4/14.
 */
class DateCodecTest extends CodecSuite{

  "date codec" should {
    "roundtrip" in {
      roundtripAll(
        optionalDate, Seq(None, Some (new Date ()) )
      )
    }
  }
}
