package models

import play.api.libs.concurrent.Promise
import play.api.libs.json.JsValue
import play.api.libs.ws.WS
import play.api.libs.concurrent.execution.defaultContext
import scala.concurrent.Future
import defines._
import models.base.AccessibleEntity
import models.base.NamedEntity
import models.base.DescribedEntity
import models.base.Description
import models.base.Formable

case class AgentRepr(val e: Entity) extends NamedEntity with AccessibleEntity with DescribedEntity with Formable[Agent] {
  override def descriptions: List[AgentDescriptionRepr] = e.relations(DescribedEntity.DESCRIBES_REL).map(AgentDescriptionRepr(_))
  val publicationStatus = e.property("publicationStatus").flatMap(enum(PublicationStatus).reads(_).asOpt)
  
  def to: Agent = new Agent(
    id = Some(e.id),
    identifier = identifier,
    name = name,
    publicationStatus = publicationStatus,
    descriptions = descriptions.map(_.to)
  )
}

case class AgentDescriptionRepr(val e: Entity) extends Description with Formable[AgentDescription] {
  def to: AgentDescription = new AgentDescription(
	id = Some(e.id),
	languageCode = languageCode,
	name = e.property("name").flatMap(_.asOpt[String]),
	otherFormsOfName = e.property("otherFormsOfName").flatMap(_.asOpt[List[String]]).getOrElse(List()),
	parallelFormsOfName = e.property("parallelFormsOfName").flatMap(_.asOpt[List[String]]).getOrElse(List()),	
	generalContext = e.property("generalContext").flatMap(_.asOpt[String])      
  )
}


object Agent {

  final val DESC_REL = "describes"
  final val ADDRESS_REL = "hasAddress"
}


case class Agent (
  val id: Option[Long],
  val identifier: String,
  val name: String,
  val publicationStatus: Option[PublicationStatus.Value] = None,
  @Annotations.Relation(Agent.DESC_REL)
  val descriptions: List[AgentDescription] = Nil  
) extends Persistable {
  val isA = EntityType.Agent
}

case class AgentDescription(
  val id: Option[Long],
  val languageCode: String,
  val name: Option[String] = None,
  val otherFormsOfName: List[String] = Nil,
  val parallelFormsOfName: List[String] = Nil,  
  val generalContext: Option[String] = None
) extends Persistable {
  val isA = EntityType.AgentDescription
}

