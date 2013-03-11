package models

/**
 * ISAAR Field definitions
 */
case object Isaar {

  val FIELD_PREFIX = "isaar"

  val IDENTIFIER = "identifier"
  val NAME = "name"
  val PUBLICATION_STATUS = "publicationStatus"

  val LANG_CODE = "languageCode"

  // Field set
  val IDENTITY_AREA = "identityArea"
  val AUTHORIZED_FORM_OF_NAME = "name"
  val OTHER_FORMS_OF_NAME = "otherFormsOfName"
  val PARALLEL_FORMS_OF_NAME = "parallelFormsOfName"
  val ENTITY_TYPE = "typeOfEntity"

  // Description area
  val DESCRIPTION_AREA = "descriptionArea"
  val DATES_OF_EXISTENCE = "datesOfExistence"
  val HISTORY = "history"
  val PLACES = "places"
  val LEGAL_STATUS = "legalStatus"
  val FUNCTIONS = "functions"
  val MANDATES = "mandates"
  val INTERNAL_STRUCTURE = "internalStructure"
  val GENERAL_CONTEXT = "generalContext"

  // Control
  val CONTROL_AREA = "controlArea"
  val DESCRIPTION_IDENTIFIER = "descriptionIdentifier"
  val INSTITUTION_IDENTIFIER = "institutionIdentifier"
  val RULES_CONVENTIONS = "rulesAndConventions"
  val STATUS = "status"
  val LEVEL_OF_DETAIL = "levelOfDetail"
  val DATES_CVD = "datesCVD"
  val LANGUAGES_USED = "languages"
  val SCRIPTS_USED = "scripts"
  val SOURCES = "sources"
  val MAINTENANCE_NOTES = "maintenanceNotes"
}

