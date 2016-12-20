package controllers.portal.guides

import javax.inject._

import backend.rest.cypher.Cypher
import controllers.Components
import controllers.generic.SearchType
import controllers.portal.FacetConfig
import controllers.portal.base.{Generic, PortalController}
import models.{GuidePage, _}
import play.api.mvc.{Action, AnyContent}
import utils.search.SearchConstants


@Singleton
case class DocumentaryUnits @Inject()(
  components: Components,
  guides: GuideService,
  cypher: Cypher
) extends PortalController
  with Generic[DocumentaryUnit]
  with SearchType[DocumentaryUnit]
  with FacetConfig {

  def browse(path: String, id: String): Action[AnyContent] = GetItemAction(id).async { implicit request =>
    futureItemOr404 {
      guides.find(path, activeOnly = true).map { guide =>
        val filterKey = if (!hasActiveQuery(request)) SearchConstants.PARENT_ID
          else SearchConstants.ANCESTOR_IDS

        findType[DocumentaryUnit](
          filters = Map(filterKey -> request.item.id),
          facetBuilder = docSearchFacets
        ).map { result =>
          Ok(views.html.guides.documentaryUnit(
            guide,
            GuidePage.document(Some(request.item.toStringLang)),
            guides.findPages(guide),
            request.item,
            result,
            controllers.portal.guides.routes.DocumentaryUnits.browse(path, id),
            request.annotations,
            request.links,
            request.watched
          ))
        }
      }
    }
  }
}