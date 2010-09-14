package ru.circumflex.orm

import java.util.Date
import xml._
import java.sql.ResultSet

/*!# Field

The `Field` class holds atomic values of records. It wraps actual value
and provides methods for constructing column definitions for enclosing
tables. It also contains the `REFERENCES` method which is used to create
associations.
 */
class Field[T, R <: Record[_, R]](name: String, record: R , sqlType: String)
    extends ValueHolder[T, R](name, record, sqlType) {

  def read(rs: ResultSet, alias: String): Option[T] =
    typeConverter.read(rs, alias).asInstanceOf[Option[T]]

  def REFERENCES[P <: Record[T, P]](relation: Relation[T, P]): Association[T, R, P] =
    new Association(this, relation)

}

abstract class XmlSerializableField[T, R <: Record[_, R]](
    name: String, record: R, sqlType: String)
    extends Field[T, R](name, record, sqlType) with XmlSerializable[T] {
  def to(value: Option[T]): String =
    value.map(_.toString).getOrElse("")
}

class IntField[R <: Record[_, R]](name: String, record: R)
    extends XmlSerializableField[Int, R](name, record, dialect.integerType) {
  def from(str: String): Option[Int] =
    try Some(str.toInt) catch { case _ => None }
}

class LongField[R <: Record[_, R]](name: String, record: R)
    extends XmlSerializableField[Long, R](name, record, dialect.longType) {
  def from(str: String): Option[Long] =
    try Some(str.toLong) catch { case _ => None }
}

class NumericField[R <: Record[_, R]](
    name: String, record: R, val precision: Int = -1, val scale: Int = 0)
    extends XmlSerializableField[Double, R](
      name,
      record,
      dialect.numericType + (if (precision == -1) "" else "(" + precision + "," + scale + ")")) {
  def from(str: String): Option[Double] =
    try Some(str.toDouble) catch { case _ => None }
}

class TextField[R <: Record[_, R]](name: String, record: R, sqlType: String)
    extends XmlSerializableField[String, R](name, record, sqlType) {
  def this(name: String, record: R, length: Int = -1) =
    this(name, record, dialect.varcharType + (if (length == -1) "" else "(" + length + ")"))
  def from(str: String): Option[String] =
    if (str == "") None else Some(str)
}

class BooleanField[R <: Record[_, R]](name: String, record: R)
    extends XmlSerializableField[Boolean, R](name, record, dialect.booleanType) {
  def from(str: String): Option[Boolean] =
    try Some(str.toBoolean) catch { case _ => None }
}

class TimestampField[R <: Record[_, R]](name: String, record: R)
    extends XmlSerializableField[Date, R](name, record, dialect.timestampType) {
  def from(str: String): Option[Date] =
    try Some(new Date(java.sql.Timestamp.valueOf(str).getTime)) catch { case _ => None }
  override def to(value: Option[Date]): String =
    value.map(v => new java.sql.Timestamp(v.getTime).toString).getOrElse("")
}

class DateField[R <: Record[_, R]](name: String, record: R)
    extends XmlSerializableField[Date, R](name, record, dialect.dateType) {
  def from(str: String): Option[Date] =
    try Some(new Date(java.sql.Date.valueOf(str).getTime)) catch { case _ => None }
  override def to(value: Option[Date]): String =
    value.map(v => new java.sql.Date(v.getTime).toString).getOrElse("")
}

class TimeField[R <: Record[_, R]](name: String, record: R)
    extends XmlSerializableField[Date, R](name, record, dialect.timeType) {
  def from(str: String): Option[Date] =
    try Some(new Date(java.sql.Time.valueOf(str).getTime)) catch { case _ => None }
  override def to(value: Option[Date]): String =
    value.map(v => new java.sql.Time(v.getTime).toString).getOrElse("")
}

class XmlField[R <: Record[_, R]](name: String, record: R)
    extends XmlSerializableField[NodeSeq, R](name, record, dialect.xmlType) {
  def from(str: String): Option[NodeSeq] =
    try Some(XML.loadString(str)) catch { case _ => None }
  override def read(rs: ResultSet, alias: String) =
    from(rs.getString(alias))
}