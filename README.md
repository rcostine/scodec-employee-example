scodec-employee-example
=======================

encode/decode an Employee case class using the scodec library from CCAD

After cloning, pull into your favorite IDE, and run the EmployeeCodecTest

That will do a roundtrip happy path. The case class will be encoded and decoded.

I created a number of other Codec classes, including my own String and Char
Codecs. They prepend the length of the String and Char (in bits) as a 63-bit
unsigned long. Simply using the string(charset) codec was causing the "decode"
to fail, since it was trying to decode the entire remaining buffer, not just the
payload.

The OptionalDateCodec and OptionalCharCodec handle encoding and decoding the
Option[Date] and Option[Char] in the case class. They prepend a high/low. If
there is a "high" on the decode, that means the Option[A] is Some(A), if low
that means the decoded Option[A] is None.
