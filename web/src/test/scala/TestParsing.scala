package TravelCompanionScala {
package solrapi {

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert._
import xml.{NodeSeq, Text}
import dispatch.{:/, Http}
import net.liftweb.util.Helpers._
import collection.mutable.Queue
import model.SolrSuggestResult

class TestParsing {
  @Before
  def init() = {

  }

  @After
  def close() = {

  }

  @Test
  def parse_stuff() = {

    val xml =
    <response>
      <lst name="responseHeader">
        <int name="status">0</int>
        <int name="QTime">0</int>
        <lst name="params">
          <str name="facet.mincout">1</str>
          <str name="facet">on</str>
          <str name="indent">on</str>
          <str name="q">*</str>
          <str name="facet.prefix">trav</str>
          <str name="facet.limit">10</str>
          <str name="facet.field">description</str>
          <str name="rows">0</str>
        </lst>
      </lst>
        <result name="response" numFound="100" start="0"/>
      <lst name="facet_counts">
          <lst name="facet_queries"/>
        <lst name="facet_fields">
          <lst name="description">
            <int name="travel">100</int>
            <int name="travel1">110</int>
          </lst>
        </lst>
          <lst name="facet_dates"/>
      </lst>
    </response>

    //Ist mutable...
    var resultsColl = new Queue[SolrSuggestResult]()

    val colLst = xml \\ "lst"
    val facet_results = colLst(2) \\ "int"
    val results = facet_results.foreach {
      (result) => {
        val suggestResult = SolrSuggestResult((result \\ "@name").text, toInt(result.text))
        resultsColl.enqueue(suggestResult)

      }
     println("Number of Results: " + resultsColl.size)
     resultsColl.toList

    }


  }
}
}
}