package com.zekelogix.scodec.codecs

import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date

import com.zekelogix.scodec.CodecSuite

import scalaz.\/

class EmployeeCodecTest extends CodecSuite {

  val testCharset = Charset.forName("US-ASCII")
  val dateFormat = new SimpleDateFormat("yyyy/MM/dd")

  "employee codec" should {
    "roundtrip" in {
      roundtripAll(
        employee(testCharset),
        Seq(
          Employee(
            firstName = "Richard",
            middleInitial = Some('J'),
            lastName = "Costine",
            dateOfBirth =  dateFormat.parse("1960/05/24"),
            startDate = new Date (System.currentTimeMillis()),
            terminationDate = None,
            rank = 1
          ),
          Employee(
            firstName = "Chuck",
            middleInitial = None,
            lastName = "Catenstein",
            dateOfBirth =  dateFormat.parse("1997/06/30"),
            startDate = new Date (System.currentTimeMillis()),
            terminationDate = None,
            rank = 1
          )
        )
      )
    }
  }
}
