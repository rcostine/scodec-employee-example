package com.zekelogix.scodec.codecs

import java.nio.charset.Charset

import com.zekelogix.scodec.CodecSuite

/**
 * Created by rjc on 11/4/14.
 *
 * Tests for the OptionalCharCodec
 */
class CharCodecTest extends CodecSuite{

  "date codec" should {
    "roundtrip" in {
      roundtripAll(
        optionalChar(Charset.forName("US-ASCII")), Seq(Some('A'),None)
      )
    }
  }
}
