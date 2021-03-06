package is.hail.expr.types.encoded

import is.hail.annotations.Region
import is.hail.asm4s._
import is.hail.expr.ir.EmitMethodBuilder
import is.hail.expr.types.BaseType
import is.hail.expr.types.physical._
import is.hail.expr.types.virtual._
import is.hail.io.{InputBuffer, OutputBuffer}
import is.hail.utils._

case object EInt32Optional extends EInt32(false)
case object EInt32Required extends EInt32(true)

class EInt32(override val required: Boolean) extends EType {
  def _buildEncoder(pt: PType, mb: EmitMethodBuilder, v: Code[_], out: Code[OutputBuffer]): Code[Unit] = {
    out.writeInt(coerce[Int](v))
  }

  def _buildDecoder(
    pt: PType,
    mb: EmitMethodBuilder,
    region: Code[Region],
    in: Code[InputBuffer]
  ): Code[Int] = in.readInt()

  def _buildSkip(mb: EmitMethodBuilder, r: Code[Region], in: Code[InputBuffer]): Code[Unit] = in.skipInt()

  override def _compatible(pt: PType): Boolean = pt.isInstanceOf[PInt32]

  def _decodedPType(requestedType: Type): PType = requestedType match {
    case t: TCall => PCall(required)
    case _ => PInt32(required)
  }

  def _asIdent = "int32"
  def _toPretty = "EInt32"
}

object EInt32 {
  def apply(required: Boolean = false): EInt32 = if (required) EInt32Required else EInt32Optional
}
