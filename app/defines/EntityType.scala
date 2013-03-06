package defines

object EntityType extends Enumeration() {
  type Type = Value
  val DocumentaryUnit = Value("documentaryUnit")
  val Repository = Value("agent")
  val Actor = Value("authority")
  val ActorDescription = Value("authorityDescription")
  val SystemEvent = Value("systemEvent")
  val UserProfile = Value("userProfile")
  val Group = Value("group")
  val ContentType = Value("contentType")
  val DocumentaryUnitDescription = Value("documentDescription")
  val RepositoryDescription = Value("agentDescription")
  val AuthorityDescription = Value("authorityDescription")
  val DatePeriod = Value("datePeriod")
  val Address = Value("address")
  val PermissionGrant = Value("permissionGrant")
  val Permission = Value("permission")
  val Annotation = Value("annotation")
  val Concept = Value("cvocConcept")
  val ConceptDescription = Value("cvocConceptDescription")
  val Vocabulary = Value("cvocVocabulary")
}
