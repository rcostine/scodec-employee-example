package com.zekelogix.scodec


import scalaz.{\/, -\/, \/-}
import \/.{ right, left }

import scodec.bits._
import scodec.Err
import scodec.codecs._
import scodec.Codec
import shapeless._
import shapeless.record._
import shapeless.union._
import shapeless.syntax.singleton._

class CodecTest extends CodecSuite {
  sealed trait Parent
  case class Foo(x: Int, y: Int, s: String) extends Parent
  case class Bar(x: Int) extends Parent

  "all codecs" should {

    "support flatZip" in {
      val codec = uint8 flatZip { n => fixedSizeBits(n, ascii) }
      roundtripAll(codec, Seq((0, ""), (8, "a"), (32, "test")))
    }

    "support complete combinator" in {
      val codec = scodec.codecs.bits(8)
      codec.decode(hex"00112233".toBitVector) shouldBe \/.right((hex"112233".toBitVector, hex"00".toBitVector))
      codec.complete.decode(hex"00112233".toBitVector) shouldBe \/.left(Err("24 bits remaining: 0x112233"))
      codec.complete.decode(BitVector.fill(2000)(false)) shouldBe \/.left(Err("more than 512 bits remaining"))
    }

    "support as method for converting to a new codec using implicit isomorphism," which {

      "works with HList codecs of 1 element" in {
        roundtripAll(uint8.hlist.as[Bar], Seq(Bar(0), Bar(1), Bar(255)))
      }

      "works with non-HList codecs" in {
        roundtripAll(uint8.as[Bar], Seq(Bar(0), Bar(1), Bar(255)))
      }

      "supports destructuring case classes in to HLists" in {
        import shapeless._
        uint8.hlist.as[Bar].as[Int :: HNil]
      }

      "supports destructuring singleton case classes in to values" in {
        import shapeless._
        uint8.hlist.as[Bar].as[Int]
      }
    }

    "support the unit combinator" in {
      import scalaz.std.AllInstances._
      val codec = uint8.unitM
      codec.encode(()) shouldBe \/.right(BitVector(0))
      codec.decode(BitVector(1)) shouldBe \/.right((BitVector.empty, ()))
      codec.decode(BitVector.empty) shouldBe 'left
      uint8.unit(255).encode(()) shouldBe \/.right(BitVector(0xff))
    }

    "support dropRight combinator" in {
      val codec = uint8 <~ uint8.unit(0)
      codec.encode(0xff) shouldBe \/.right(hex"ff00".bits)
    }
  }

  "literal values" should {
    "be usable as constant codecs" in {
      import scodec.codecs.literals._
      (1 ~> uint8).encode(2) shouldBe \/.right(hex"0102".bits)
      (1.toByte ~> uint8).encode(2) shouldBe \/.right(hex"0102".bits)
      (hex"11223344" ~> uint8).encode(2) shouldBe \/.right(hex"1122334402".bits)
      (hex"11223344".bits ~> uint8).encode(2) shouldBe \/.right(hex"1122334402".bits)
    }
  }

  "exmap" should {
    "support validating input and output" in {
      // accept 8 bit values no greater than 9
      val oneDigit: Codec[Int] = uint8.exmap[Int](
        v => if (v > 9) -\/(Err("badv")) else \/-(v),
        d => if (d > 9) -\/(Err("badd")) else \/-(d))

      oneDigit.encode(3) shouldBe \/-(BitVector(0x03))
      oneDigit.encode(10) shouldBe -\/(Err("badd"))
      oneDigit.encode(30000000) shouldBe -\/(Err("badd"))
      oneDigit.decode(BitVector(0x05)) shouldBe \/-((BitVector.empty, 5))
      oneDigit.decode(BitVector(0xff)) shouldBe -\/(Err("badv"))
      oneDigit.decode(BitVector.empty) shouldBe uint8.decode(BitVector.empty)
    }

    "result in a no-op when mapping \\/.right over both sides" which {
      val noop: Codec[Int] = uint8.exmap[Int](right, right)
      forAll { (n: Int) =>
        noop.encode(n) shouldBe uint8.encode(n)
      }
    }
  }

  def i2l(i: Int): Long = i.toLong
  def l2i(l: Long): Err \/ Int = if (l >= Int.MinValue && l <= Int.MaxValue) right(l.toShort) else left(Err("out of range"))

  "narrow" should {
    "support converting to a smaller type" in {
      val narrowed: Codec[Int] = uint32.narrow(l2i, i2l)
      forAll { (n: Int) => narrowed.encode(n) shouldBe uint32.encode(n) }
    }
  }

  "widen" should {
    "support converting to a larger type" in {
      val narrowed = int32.widen(i2l, l2i)
      forAll { (n: Long) =>
        if (n >= Int.MinValue && n <= Int.MaxValue)
          narrowed.encode(n) shouldBe int32.encode(n.toInt)
        else
          narrowed.encode(n) shouldBe left(Err("out of range"))
      }
    }
  }

  "encodeOnly" should {
    val char8: Codec[Char] = uint8.contramap[Char](_.toInt).encodeOnly
    "encode successfully" in {
      char8.encode('a') shouldBe \/-(BitVector(0x61))
    }
    "fail to decode" in {
      char8.decode(hex"61".bits) shouldBe -\/(Err("decoding not supported"))
    }
  }

  "decodeOnly" should {
    val char8: Codec[Char] = uint8.map[Char](_.asInstanceOf[Char]).decodeOnly
    "decode successfully" in {
      char8.decode(BitVector(0x61)) shouldBe \/-((BitVector.empty, 'a'))
    }
    "fail to encode" in {
      char8.encode('a') shouldBe -\/(Err("encoding not supported"))
    }
  }

  "automatic codec generation" should {
    "support automatic generation of HList codecs" in {
      implicit val (i, s) = (uint8, variableSizeBytes(uint16, utf8))
      Codec.product[Int :: Int :: String :: HNil].encodeValid(1 :: 2 :: "Hello" :: HNil) shouldBe hex"0102000548656c6c6f".bits
    }
    "support automatic generation of case class codecs" in {
      implicit val (i, s) = (uint8, variableSizeBytes(uint16, utf8))
      Codec.product[Foo].encodeValid(Foo(1, 2, "Hello")) shouldBe hex"0102000548656c6c6f".bits
    }
    "include field names in case class codecs" in {
      implicit val (i, s) = (uint8, variableSizeBytes(uint16, utf8))
      Codec.product[Foo].encode(Foo(1, 256, "Hello")) shouldBe left(Err("256 is greater than maximum value 255 for 8-bit unsigned integer").pushContext("y"))
    }
    "support automatic generation of coproduct codec builders" in {
      implicit val (u, s) = (constant(1), variableSizeBytes(uint16, utf8))
      type C = Unit :+: String :+: CNil
      val codec = Codec.coproduct[C].choice
      codec.encodeValid(Coproduct[C]("Hello")) shouldBe hex"000548656c6c6f".bits
      codec.encodeValid(Coproduct[C](())) shouldBe hex"01".bits
    }
    "support automatic generation of coproduct codec builders from union types" in {
      implicit val (i, s) = (uint8, variableSizeBytes(uint16, utf8))
      val uSchema = RecordType.like('i ->> 24 :: 's ->> "foo" :: HNil)
      type U = uSchema.Union
      val codec = Codec.coproduct[U].discriminatedByIndex(uint8)
      codec.encodeValid(Coproduct[U]('s ->> "Hello")) shouldBe hex"01000548656c6c6f".bits
      codec.encode(Coproduct[U]('i ->> 256)) shouldBe left(Err("256 is greater than maximum value 255 for 8-bit unsigned integer").pushContext("i"))
    }
    "support automatic generation of coproduct codec builders from sealed trait and subclasses" in {
      implicit val (i, s) = (uint8, variableSizeBytes(uint16, utf8))
      implicit val (f, b) = (Codec.product[Foo], Codec.product[Bar])
      val codec: Codec[Parent] = Codec.coproduct[Parent].discriminatedByIndex(uint8)
      codec.encodeValid(Foo(1, 2, "Hello")) shouldBe hex"010102000548656c6c6f".bits
      codec.encodeValid(Bar(1)) shouldBe hex"0001".bits
    }
  }
}
