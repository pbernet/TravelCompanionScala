package TravelCompanionScala.model

import xml.{Text, NodeSeq}
import dispatch.{Http, :/}
import net.liftweb.actor.LiftActor
import collection.mutable.Queue
import net.liftweb.util.Helpers._
import java.net.ConnectException
import net.liftweb.common.{Loggable, Logger}

//TODO This API is a Singleton - try to apply LiftActors inside for async Access to Solr
object SolrBackgroundThread extends LiftActor {
  def messageHandler = {
    case DoFunc(func) => func()
  }
}
case class DoFunc(f: () => Unit)

//A temp container for AutoSuggest results
case class SolrSuggestResult(name: String, number: Int)

//This case classes correspond to the field type in the solr index
case class SolrDocStage(id: Int, t_id: Int, name: String, description: String)


object SolrAPI extends Loggable {

  def findToursCount(queryString: String): Option[Int] = {

    val trimmedString = queryString.trim

    if (!trimmedString.isEmpty && trimmedString.size > 1) {

      //the wildcard after q ist used so that the number of docs can be calculated
      //additional param rows=0, so the response size is short
      //TODO Test this with special search syntax: AND, OR, meta:, "fixed expression"
      val response = SolrAPI.selectRequest(Map("q" -> (trimmedString + "*"), "start" -> "0", "rows" -> "0"))

      //parse the response for the size
      val numFound = (response \\ "result" \ "@numFound").text
      Some(toInt(numFound))
    } else {
      None
    }
  }


  def findTours(queryString: String, start: Int, rows: Int): Seq[SolrDocStage] = {

    val trimmedString = queryString.trim

    if (!trimmedString.isEmpty && trimmedString.size > 1) {
      logger.info("SolrAPI>>findTours Params: queryString =" + trimmedString + " start =" + start + " rows =" + rows + " at %s".format(now))

      var results = new Queue[SolrDocStage]()
      val responseOut = SolrAPI.selectRequest(Map("q" -> (trimmedString), "start" -> start.toString, "rows" -> rows.toString))

      //Not yet used here - see method findToursCount
      //val numFound = (responseOut \\ "result" \ "@numFound").text

      responseOut \\ "result" \\ "doc" foreach {
        (doc) => {
          //returns all str elements...
          val strElems = doc \\ "str"
          val strValues = strElems.map(_.text)
          val theMap = Map.empty[String, String] ++ strElems.map {each => {(each.attribute("name")).get.text -> each.text}}
          //Create special case class instance display data
          val stageDoc = SolrDocStage(toInt(theMap("id")), toInt(theMap("s_t_id")), theMap("s_name"), theMap("s_description"))
          results.enqueue(stageDoc)
        }
      }
      logger.info("SolrAPI>>findTours Number of returned results: " + results.size + " at %s".format(now))
      results.toList

    } else {
      List()
    }
  }


  def autoSuggest(queryString: String, limit: Int): Seq[SolrSuggestResult] = {

    val trimmedString = queryString.trim

    if (!trimmedString.isEmpty && trimmedString.size > 1) {
      logger.info("SolrAPI>>autoSuggest: " + "Params: trimmedQueryString: " + trimmedString + " limit: " + limit + " at %s".format(now))
      val splittedQuery = splitQuery(trimmedString)

      //Works with facets and searches in field  "text", which is of type "textgen" (= no stemming)
      //If the facet.field param is passed n times: Only terms which ALL the fields are returned (eg "facet.field" -> "s_description", "facet.field" -> "s_name")
      //see http://wiki.apache.org/solr/SimpleFacetParameters#facet.field
      //TODO Another way of facetting: use param facet.query n times (See Solr book 152)
      val responseOut = SolrAPI.selectRequest(Map("q" -> splittedQuery._1, "facet.prefix" -> splittedQuery._2, "indent" -> "on", "facet" -> "on", "rows" -> "0", "facet.field" -> "text", "facet.limit" -> limit.toString, "facet.mincout" -> "1"))

      var resultsColl = new Queue[SolrSuggestResult]()
      val colLst = responseOut \\ "lst"
      val facet_results = colLst(2) \\ "int"
      facet_results.foreach {
        (result) => {
          var suggestResultValue = ""
          if (isFirstWord(splittedQuery)) {
            suggestResultValue = (result \\ "@name").text
          } else {
            suggestResultValue = splittedQuery._1 + " " + (result \\ "@name").text
          }
          resultsColl.enqueue(SolrSuggestResult(suggestResultValue, toInt(result.text)))
        }
      }
      logger.info("SolrAPI>>autoSuggest: " + "Number of Results: " + resultsColl.size + " at %s".format(now))
      resultsColl.toList
    } else {
      List()
    }
  }


  def addToSolr (aStage: Stage) = {
    //Denormalize Tour<->Stage<->Location objects. In Solr this doc has the type SolrDocStage
    val xml = <add>
      <doc>
        <field name="type">{"SolrDocStage"}</field>
        <field name="id">{aStage.id}</field>
        <field name="s_name">{aStage.name}</field>
        <field name="s_description">{aStage.description}</field>
        <field name="s_startdate">{aStage.startdate}</field>

        <field name="s_t_id">{aStage.tour.id}</field>
        <field name="s_t_name">{aStage.tour.name}</field>
        <field name="s_t_description">{aStage.tour.description}</field>

        <field name="s_l_name">{aStage.destination.name}</field>
        <field name="s_l_lng">{aStage.destination.lng}</field>
        <field name="s_l_lat">{aStage.destination.lat}</field>
        <field name="s_l_countrycode">{aStage.destination.countrycode}</field>
        <field name="s_l_adminname">{aStage.destination.adminname}</field>
        <field name="s_l_countryname">{aStage.destination.countryname}</field>
        <field name="s_l_admincode">{aStage.destination.admincode}</field>
        <field name="s_l_geonameid">{aStage.destination.geonameid}</field>
        <field name="s_l_population">{aStage.destination.population}</field>
      </doc>
    </add>

    val responseOut = updateRequest(xml)
    logger.info("SolrAPI>>addToSolr: " + "Added Stage with id: " + aStage.id +  responseOut + " at %s".format(now))
    val responseCommit = updateRequest(<commit/>)
  }

  def deleteDoc(id: Long){
    //http://lucene.apache.org/solr/tutorial.html#Deleting+Data
    val xml = <delete>
      <query><id>{id.toString}</id></query>
    </delete>
    val responseOut = SolrAPI.updateRequest(xml)
    logger.info("SolrAPI>>addToSolr: " + "Deleted Doc with ID: " + responseOut + " at %s".format(now))
    SolrAPI.updateRequest(<optimize/>)
  }


  def deleteAll() {
    // http://wiki.apache.org/solr/FAQ#How_can_I_rebuild_my_index_from_scratch_if_I_change_my_schema.3F
    val xml = <delete>
      <query>*:*</query>
    </delete>

    val responseOut = SolrAPI.updateRequest(xml)
    logger.info("SolrAPI>>deleteAll: " + "Deleted All: " + responseOut + " at %s".format(now))
    SolrAPI.updateRequest(<optimize/>)
  }

  def updateRequest(xml: NodeSeq): NodeSeq = {

    val http = new Http
    var response: NodeSeq = Text("")

    /* Where you are posting to http://localhost:8983/solr/update */
    val req = :/("localhost", 9090) / "solr" / "update"

    http(req << xml.toString <> {response = _})
    //returns the parsed xml response as NodeSeq
    response
  }

  def selectRequest(paramMap: Map[String, String]): NodeSeq = {

    val http = new Http
    var response: NodeSeq = Text("")
    val req = :/("localhost", 9090) / "solr" / "select"

    //Append query string
    val rquery = req <<? paramMap

    try {
      http(rquery <> {response = _})
      //returns the parsed xml response as NodeSeq
      response
    } catch {
      case cone: ConnectException => logger.error("SolrAPI>>selectRequest: " + "Connect Problem to localhost:9090 - Contact System Admin!" + " at %s".format(now)); Text("technical error")
      case e => logger.info("SolrAPI>>selectRequest: " + "Solr has a Syntax Problem with your entry - Try again!" + " at %s".format(now)); e.printStackTrace(); Text("user error")
    }
  }

  //TODO Does not work anymore, response is not filled. Lift-Actor Problem in Snapshot ?
  //http://groups.google.com/group/liftweb/browse_thread/thread/7efcd81aa30aab57/dbedf5db37d9a214?lnk=gst&q=solr#dbedf5db37d9a214
  def selectRequestAsync(paramMap: Map[String, String]): NodeSeq = {

    val http = new Http
    var response: NodeSeq = Text("")
    val req = :/("localhost", 9090) / "solr" / "select"

    //Append query string
    val rquery = req <<? paramMap

    //Only difference to method selectRequest
    SolrBackgroundThread ! DoFunc(() => {http(rquery <> {response = _})})
    //returns the parsed xml response as NodeSeq
    response
  }

  private def splitQuery(trimmedString: String) = {

    //take the query string apart and fill the query params
    val queryStringArray = trimmedString.split(" ")
    val facetPrefix = queryStringArray.last
    var query: String = ""

    if (queryStringArray.size > 1) {
      val allButLast = queryStringArray.reverse.tail.reverse
      query = allButLast.toList.map(_ + " ").mkString.trim
    } else {
      query = "*"
    }
    (query, facetPrefix)
  }

  private def isFirstWord(splittedQuery: (String, String)) = splittedQuery._1 == "*"


}