package TravelCompanionScala {
package solrapi {


import org.junit.Test
import org.junit.Before
import org.junit.After
import xml.{NodeSeq}
import dispatch.{Http}
import model.SolrAPI
//Because of this class the whole test class was moved to Module web
class TestSolrDeleteAll {
  @Before
  def init() = {

  }

  @After
  def close() = {

  }


  @Test
  def delete_all() = {
    SolrAPI.deleteAll
  }


}
}
}