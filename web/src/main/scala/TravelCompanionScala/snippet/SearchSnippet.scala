package TravelCompanionScala.snippet

import _root_.scala.xml.{NodeSeq, Text, Elem}
import _root_.net.liftweb.util.Helpers._
import TravelCompanionScala.model._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.{SessionVar, RequestVar, S, SHtml}
import TravelCompanionScala.widget.AutoSuggest
import net.liftweb.http.js._
import net.liftweb.common.Loggable

//Set up a requestVar to track the search Object
object tourSearchVar extends RequestVar[SolrDocStage](SolrDocStage(0,0,"",""))

//The SolrDocStage are representations of the SolrDocs
object toursSearchResultVar extends RequestVar[Seq[SolrDocStage]](Seq[SolrDocStage]())

//Needed for the view of Results
object IsQueryDone extends SessionVar(false)


class SearchSnippet extends Loggable {

  def inputCriteria(html: NodeSeq): NodeSeq = {

    def doSearch() = {
      //not used 
    }

    def onSubmitAutoSuggest(queryString: String) = {
      logger.info("SearchSnippet>>onSubmitAutoSuggest: " + "Params: queryString: " + queryString + " at %s".format(now))
      IsQueryDone(true)
      S.redirectTo("/tour/search", () => setRequestVars)

      //The RequestVar must be set here. This nested method has access to the variables
      def setRequestVars = {

        val queryStringTermOnly = queryString.split("-")(0)
        val results = SolrAPI.findTours(queryStringTermOnly, 0, 20);

        if (isError(results)) {
          S.error({
            S.?("Technical problem or problem with entry values")
          })
        } else {
          toursSearchResultVar(results)
        }

        //TODO Detect Errors, which are returned from the SolrAPI via Data / via Exception ?
        def isError(results: Seq[SolrDocStage]) = {
          false
        }

        logger.info("SearchSnippet>>onSubmitAutoSuggest: " + "Number of Results found: " + toursSearchResultVar.is.size + " at %s".format(now))
        var tempTour = SolrDocStage(0, 0, "", queryStringTermOnly)
        tourSearchVar(tempTour)
      }
    }

    def checkNumberOfSearchResults(queryString: String) = {

      val results = SolrAPI.findToursCount(queryString)
      var message: NodeSeq = <td>{results.getOrElse(0)}<img src="../images/cross.png" alt="None Found" title="None Found"/></td>;
      var inputclass: String = "inputerror"
      var link2AdminQuery:NodeSeq = <td></td>;

      if (results.getOrElse(0) > 0) {
        message = <td>{results.getOrElse(0)}<img src="../images/tick.png" alt="Some Found" title="Some Found"/></td>;
        inputclass = ""
        //apparently not nessecary: urlEncode()
        link2AdminQuery = <td><a href={"http://localhost:9090/solr/select/?q=" + queryString + "&version=2.2&start=0&rows=10&indent=on"}>{S.?("solrquery")}</a></td>;    
      }

      JsCmds.SetHtml("checkSearchResults", message) &
              JsRaw("$('#username').attr('class', '" + inputclass + "');").cmd &
              JsCmds.JsHideId("lift__noticesContainer__") & JsCmds.SetHtml("link2AdminQuery", link2AdminQuery)
 }

    bind("search", html,
      //Start with blank on each request
      "countResults" -%> ajaxLiveText("", checkNumberOfSearchResults),
      "link2Admin" -> SHtml.link("http://localhost:9090/solr/admin/", () => {}, Text("Solr Admin")),

      //Use selfmade AutoSuggest Component, because AutoComplete forces selection from DropDown
      //"autoSuggest" -> AutoComplete(tourSearchVar.is.description, (queryString, limit) => {SolrAPI.autoSuggest(queryString, limit).map(result => result.name + " - " + result.number)}, queryString => onSubmitAutoSuggest(queryString, currentTour)),
      //TODO The limit param is set to 10 internally - how can it be set from outside ?
      "autoSuggest" -> AutoSuggest(tourSearchVar.is.description, (queryString, limit) =>
        {SolrAPI.autoSuggest(queryString, limit).map(result => result.name + " - " + result.number)},
        queryString => onSubmitAutoSuggest(queryString), true),
       "search" -> SHtml.submit(S.?("search"), () => {doSearch()}))
  }


  def searchResults(html: NodeSeq): NodeSeq = {
    toursSearchResultVar.is.flatMap(stageDoc => bind("stageDoc", html,
      "name" -> SHtml.link("view", () => loadTour(stageDoc), Text(stageDoc.name)),
      "description" -> stageDoc.description,
      "edit" -> SHtml.link("edit", () => loadTour(stageDoc), Text(S.?("edit")))))
  }

  def loadTour(stageDoc: SolrDocStage){
    //if the default case happens, the search-index and the db are out of sync
    tourVar(Model.find(classOf[Tour], toLong(stageDoc.t_id)).getOrElse(new Tour()))
  }


   def showIfQueryDone(html: NodeSeq): NodeSeq = {
    showIf(html, IsQueryDone)
   }

  private def showIf(html: NodeSeq, cond: Boolean): NodeSeq = {
    if (cond)
      html
    else
      NodeSeq.Empty
  }

  //Because the method SHtml.ajaxText does the request by onBlur...
  //Workaround:
  //http://groups.google.com/group/liftweb/browse_thread/thread/4143c3e3d4b7eeae/9820366e3d8cbee5?lnk=raot
  def ajaxLiveText(value: String, func: String => JsCmd, attrs: (String, String)*): Elem = {
    S.fmapFunc(S.SFuncHolder(func)) {
      funcName =>
        (attrs.foldLeft(<input type="text" value={value}/>)(_ %
                _)) %
                ("onkeyup" -> SHtml.makeAjaxCall(JsRaw("'" + funcName +
                        "=' + encodeURIComponent(this.value)")))
    }
  }

}