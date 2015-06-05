package controllers.admin

import auth.AccountManager
import controllers.base.AdminController
import play.api.cache.CacheApi
import play.api.i18n.MessagesApi
import play.api.libs.ws.WSClient
import play.api.mvc.Action
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Execution.Implicits._

import javax.inject._
import backend.Backend
import play.api.http.MimeTypes
import com.ning.http.client.{Response => NingResponse}
import defines.EntityType
import backend.rest.cypher.Cypher
import utils.MovedPageLookup
import views.MarkdownRenderer

case class ApiController @Inject()(
  implicit app: play.api.Application,
  cache: CacheApi,
  globalConfig: global.GlobalConfig,
  backend: Backend,
  accounts: AccountManager,
  pageRelocator: MovedPageLookup,
  messagesApi: MessagesApi,
  markdown: MarkdownRenderer,
  cypher: Cypher,
  ws: WSClient
) extends AdminController {

  def listItems(contentType: EntityType.Value) = Action.async { implicit request =>
    get(s"$contentType/list")(request)
  }

  def getItem(contentType: EntityType.Value, id: String) = Action.async { implicit request =>
    get(s"$contentType/$id")(request)
  }

  def getAny(id: String) = Action.async { implicit request =>
    get(s"entities?id=$id")(request)
  }

  def get(urlpart: String) = OptionalUserAction.async { implicit request =>
    val url = urlpart + (if(request.rawQueryString.trim.isEmpty) "" else "?" + request.rawQueryString)
    userBackend.query(url, request.headers).map { r =>
      val response: NingResponse = r.underlying[NingResponse]
      Status(r.status)
        .chunked(Enumerator.fromStream(response.getResponseBodyAsStream))
        .as(response.getContentType)
    }
  }

  def jsRoutes = Action { implicit request =>
    Ok(
      play.api.routing.JavaScriptReverseRouter("jsRoutes")()
    ).as(MimeTypes.JAVASCRIPT)
  }



  //
  // Test guff for the Sparql endpoint...
  //

  import play.api.data.Form
  import play.api.data.Forms._
  private val queryForm = Form(single("q" -> text))

  private val defaultCypher =
    """
      |START n = node:entities("__ISA__:userProfile")
      |RETURN n, n.name
      |LIMIT 100
    """.stripMargin

  def cypherForm = AdminAction { implicit request =>
    Ok(views.html.admin.queryForm(queryForm.fill(defaultCypher),
      controllers.admin.routes.ApiController.cypherQuery(), "Cypher"))
  }

  def cypherQuery = AdminAction.async { implicit request =>
    cypher.stream(queryForm.bindFromRequest.value.getOrElse(""), Map.empty).map { r =>
      val response: NingResponse = r.underlying[NingResponse]
      Status(r.status)
        .chunked(Enumerator.fromStream(response.getResponseBodyAsStream))
        .as(response.getContentType)
    }
  }

  private val defaultSparql =
    """
      |PREFIX edge:   <http://tinkerpop.com/pgm/edge/>
      |PREFIX vertex: <http://tinkerpop.com/pgm/vertex/>
      |PREFIX prop:   <http://tinkerpop.com/pgm/property/>
      |PREFIX pgm:    <http://tinkerpop.com/pgm/ontology#>
      |PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |
      |# Select all the userProfile nodes and their name properties...
      |SELECT ?n ?u WHERE {
      |    ?u a pgm:Vertex ;
      |       prop:__ISA__  "userProfile" ;
      |       prop:name     ?n .
      |}
      |
      |LIMIT 100
    """.stripMargin

  def sparql = AdminAction { implicit request =>
    Ok(views.html.admin.queryForm(queryForm.fill(defaultSparql),
        controllers.admin.routes.ApiController.sparqlQuery(), "SparQL"))
  }

  def sparqlQuery = AdminAction.async { implicit request =>
    userBackend.query("sparql", request.headers, request.queryString).map { r =>
      val response: NingResponse = r.underlying[NingResponse]
      Status(r.status)
        .chunked(Enumerator.fromStream(response.getResponseBodyAsStream))
        .as(response.getContentType)
    }
  }
}