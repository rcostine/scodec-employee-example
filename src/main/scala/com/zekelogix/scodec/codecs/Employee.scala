package com.zekelogix.scodec.codecs

import java.nio.charset.Charset
import java.util.Date

import scodec.Codec
import scodec.codecs._

/**
 * Employee case class as supplied by the client
 * @param firstName
 * @param middleInitial
 * @param lastName
 * @param dateOfBirth
 * @param startDate
 * @param terminationDate
 * @param rank
 */
case class Employee(
  firstName: String,
  middleInitial: Option[Char],
  lastName: String,
  dateOfBirth: Date,
  startDate: Date,
  terminationDate: Option[Date],
  rank: Int)

/**
 * Employee codec setup
 * Created by rjc on 11/3/14.
 */
object Employee {

  implicit def codec(set: Charset): Codec[Employee] = {
      ("firstName" | varstring(set)) ::
      ("middleInitial" | optionalChar(set)) ::
      ("lastName" | varstring(set)) ::
      ("dateOfBirth" | date) ::
      ("startDate" | date) ::
      ("terminationDate" | optionalDate) ::
      ("rank" | int8 )
  }.as[Employee]
}
