package com.zekelogix.scodec.codecs

import java.nio.charset.Charset
import java.text.SimpleDateFormat

import com.zekelogix.scodec.CodecSuite

/**
 * Created by rjc on 11/4/14.
 *
 * Test for the TestWidgetCodec
 */
class TestWidgetCodecTest extends CodecSuite{


  val dateFormat = new SimpleDateFormat("yyyy/MM/dd")
  val set = Charset.forName("US-ASCII")


  "date codec" should {
    "roundtrip" in {
      roundtripAll(
        testWidget(set),
          Seq (
            TestWidget(
              ming = 'R',
              mang = 'J',
              dateOfBirth = dateFormat.parse("1999/08/02"),
              terminationDate = None,
              rank = 1
            )
          )
        )
    }
  }
}
