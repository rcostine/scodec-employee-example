package com.zekelogix.scodec.codecs

import java.util.Date
import scodec.codecs._
import shapeless._

import com.zekelogix.scodec.CodecSuite

/**
 * Created by rjc on 11/4/14.
 */
class CharCodecTest extends CodecSuite{

  "date codec" should {
    "roundtrip" in {
      roundtripAll(
        optionalDate, Seq(Some(new Date (-5)), Some (new Date ()) )
      )
    }
  }
}
