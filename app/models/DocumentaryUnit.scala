package models

import defines._
import defines.EnumUtils._
import models.base._

import models.base.{AttributeSet, Persistable, TemporalEntity}
import play.api.libs.json.{JsObject, Json, JsString, JsValue}
import models.json.{RestReadable, DocumentaryUnitFormat, ClientConvertable, RestConvertable}


object DocumentaryUnitF {

  object CopyrightStatus extends Enumeration {
    val Yes = Value("yes")
    val No = Value("no")
    val Unknown = Value("unknown")

    implicit val format = defines.EnumUtils.enumFormat(this)
  }

  object Scope extends Enumeration {
    val High = Value("high")
    val Medium = Value("medium")
    val Low = Value("low")

    implicit val format = defines.EnumUtils.enumFormat(this)
  }

  val PUBLICATION_STATUS = "publicationStatus"
  final val SCOPE = "scope"
  final val COPYRIGHT = "copyright"

  final val DESC_REL = "describes"
  final val ACCESS_REL = "access"
  final val HELD_REL = "heldBy"
  final val CHILD_REL = "childOf"

  //lazy implicit val restFormat = json.DocumentaryUnitFormat.restFormat

  implicit object Converter extends RestConvertable[DocumentaryUnitF] with ClientConvertable[DocumentaryUnitF] {
    lazy val restFormat = models.json.rest.documentaryUnitFormat
    lazy val clientFormat = models.json.client.documentaryUnitFormat
  }
}

case class DocumentaryUnitF(
  isA: EntityType.Value = EntityType.DocumentaryUnit,
  id: Option[String] = None,
  identifier: String,
  publicationStatus: Option[PublicationStatus.Value] = None,
  copyrightStatus: Option[DocumentaryUnitF.CopyrightStatus.Value] = Some(DocumentaryUnitF.CopyrightStatus.Unknown),
  scope: Option[DocumentaryUnitF.Scope.Value] = Some(DocumentaryUnitF.Scope.Low),

  @Annotations.Relation(DocumentaryUnitF.DESC_REL)
  descriptions: List[DocumentaryUnitDescriptionF] = Nil
) extends Model with Persistable with Described[DocumentaryUnitDescriptionF] {
  def withDescription(d: DocumentaryUnitDescriptionF): DocumentaryUnitF = copy(descriptions = descriptions ++ List(d))

  /**
   * Get a description with a given id.
   * @param did
   * @return
   */
  def description(did: String): Option[DocumentaryUnitDescriptionF] = descriptions.find(d => d.id.isDefined && d.id.get == did)

  /**
   * Replace an existing description with the same id as this one, or add
   * this one to the end of the list of descriptions.
   * @param d
   * @return
   */
  def replaceDescription(d: DocumentaryUnitDescriptionF): DocumentaryUnitF = d.id.map {
    did =>
    // If the description has an id, replace the existing one with that id
      val newDescriptions = descriptions.map {
        dm =>
          if (dm.id.isDefined && dm.id.get == did) d else dm
      }
      copy(descriptions = newDescriptions)
  } getOrElse {
    withDescription(d)
  }
}

object DocumentaryUnitMeta {
  implicit object Converter extends RestReadable[DocumentaryUnitMeta] with ClientConvertable[DocumentaryUnitMeta] {
    implicit val restReads = json.DocumentaryUnitFormat.metaReads
    implicit val clientFormat = json.client.documentaryUnitMetaFormat
  }
}

case class DocumentaryUnitMeta(
  model: DocumentaryUnitF,
  holder: Option[RepositoryMeta] = None,
  parent: Option[DocumentaryUnitMeta] = None,
  accessors: List[Accessor] = Nil,
  latestEvent: Option[SystemEventMeta] = None
) extends MetaModel[DocumentaryUnitF]
  with Hierarchical[DocumentaryUnitMeta]
  with Accessible
