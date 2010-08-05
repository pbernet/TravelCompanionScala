package TravelCompanionScala {
package solrapi {

import org.junit.Test
import org.junit.Before
import org.junit.After
import java.util.{Date, ArrayList}
import TravelCompanionScala.model._
import collection.mutable.{ListBuffer, ArrayBuffer}
import scala.collection.JavaConversions._
import dispatch.{Http, :/}
import xml.{NodeSeq, Text}
import javax.persistence.{EntityManager, Persistence, EntityManagerFactory}

/**
 * Test Class which adds TestData to the Database AND the SolrIndex
 * User: pber
 */

class TestTravelGenerator {
  val isStoredInSolr = true

  var emf: EntityManagerFactory = _
  val capitals = new ArrayBuffer[String]
  val adjectives = new ArrayBuffer[String]
  var nouns = new ArrayBuffer[String]

  //the geo service can be slow...
  capitals += "Abu Dhabi"
  capitals += "Abuja"
  capitals += "Accra"
  capitals += "Adamstown"
  capitals += "Addis Ababa"
  capitals += "Algiers"
  capitals += "Alofi"
  capitals += "Amman"
  capitals += "Amsterdam"
  capitals += "Andorra la Vella"
  capitals += "Ankara"
//  capitals += "Antananarivo"
//  capitals += "Apia"
//  capitals += "Ashgabat"
//  capitals += "Asmara"
//  capitals += "Astana"
//  capitals += "Asunción"
//  capitals += "Athens"
//  capitals += "Avarua"
//  capitals += "Baghdad"
//  capitals += "Baku"
//  capitals += "Bamako"
//  capitals += "Bandar Seri Begawan"
//  capitals += "Bangkok"
//  capitals += "Bangui"
//  capitals += "Banjul"
//  capitals += "Basseterre"
//  capitals += "Beijing"
//  capitals += "Beirut"
//  capitals += "Belfast"
//  capitals += "Belgrade"
//  capitals += "Belmopan"
//  capitals += "Berlin"
//  capitals += "Bern"
//  capitals += "Bishkek"
//  capitals += "Bissau"
//  capitals += "Bogotá"
//  capitals += "Brasília"
//  capitals += "Bratislava"
//  capitals += "Brazzaville"
//  capitals += "Bridgetown"
//  capitals += "Brussels"
//  capitals += "Bucharest"
//  capitals += "Budapest"
//  capitals += "Buenos Aires"
//  capitals += "Bujumbura"
//  capitals += "Cairo"
//  capitals += "Canberra"
//  capitals += "Caracas"
//  capitals += "Cardiff"
//  capitals += "Castries"
//  capitals += "Charlotte Amalie"
//  capitals += "Chisinau"
//  capitals += "Cockburn Town"
//  capitals += "Conakry"
//  capitals += "Copenhagen"
//  capitals += "Dakar"
//  capitals += "Damascus"
//  capitals += "Dhaka"
//  capitals += "Dili"
//  capitals += "Djibouti"
//  capitals += "Dodoma"
//  capitals += "Doha"
//  capitals += "Douglas"
//  capitals += "Dublin"
//  capitals += "Dushanbe"
//  capitals += "Edinburgh"
//  capitals += "Episkopi Cantonment"
//  capitals += "Flying Fish Cove"
//  capitals += "Freetown"
//  capitals += "Funafuti"
//  capitals += "Gaborone"
//  capitals += "George Town"
//  capitals += "Georgetown"
//  capitals += "Gibraltar"
//  capitals += "Grytviken"
//  capitals += "Guatemala City"
//  capitals += "Gustavia"
//  capitals += "Hagåtña"
//  capitals += "Hamilton"
//  capitals += "Hanoi"
//  capitals += "Harare"
//  capitals += "Hargeisa"
//  capitals += "Havana"
//  capitals += "Helsinki"
//  capitals += "Honiara"
//  capitals += "Islamabad"
//  capitals += "Jakarta"
//  capitals += "Jamestown"
//  capitals += "Jerusalem"
//  capitals += "Jerusalem"
//  capitals += "Kabul"
//  capitals += "Kampala"
//  capitals += "Kathmandu"
//  capitals += "Khartoum"
//  capitals += "Kiev"
//  capitals += "Kigali"
//  capitals += "Kingston"
//  capitals += "Kingston"
//  capitals += "Kingstown"
//  capitals += "Kinshasa"
//  capitals += "Kuala Lumpur"
//  capitals += "Kuwait City"
//  capitals += "La Paz"
//  capitals += "Laâyoune (El Aaiún)"
//  capitals += "Libreville"
//  capitals += "Lilongwe"
//  capitals += "Lima"
//  capitals += "Lisbon"
//  capitals += "Ljubljana"
//  capitals += "Lomé"
//  capitals += "London"
//  capitals += "Luanda"
//  capitals += "Lusaka"
//  capitals += "Luxembourg City"
//  capitals += "Madrid"
//  capitals += "Majuro"
//  capitals += "Malabo"
//  capitals += "Malé"
//  capitals += "Mamoudzou"
//  capitals += "Managua"
//  capitals += "Manila"
//  capitals += "Maputo"
//  capitals += "Marigot"
//  capitals += "Maseru"
//  capitals += "Mata-Utu"
//  capitals += "Mbabane"
//  capitals += "Mexico City"
//  capitals += "Minsk"
//  capitals += "Mogadishu"
//  capitals += "Monaco"
//  capitals += "Monrovia"
//  capitals += "Montevideo"
//  capitals += "Moroni"
//  capitals += "Moscow"
//  capitals += "Muscat"
//  capitals += "Nairobi"
//  capitals += "Nassau"
//  capitals += "Naypyidaw"
//  capitals += "N'Djamena"
//  capitals += "New Delhi"
//  capitals += "Ngerulmud"
//  capitals += "Niamey"
//  capitals += "Nicosia"
//  capitals += "Nicosia"
//  capitals += "Nouakchott"
//  capitals += "Nouméa"
//  capitals += "Nuku?alofa"
//  capitals += "Nuuk"
//  capitals += "Oranjestad"
//  capitals += "Oslo"
//  capitals += "Ottawa"
//  capitals += "Ouagadougou"
//  capitals += "Pago Pago"
//  capitals += "Palikir"
//  capitals += "Panama City"
//  capitals += "Papeete"
//  capitals += "Paramaribo"
//  capitals += "Paris"
//  capitals += "Phnom Penh"
//  capitals += "Plymouth [f]"
//  capitals += "Podgorica"
//  capitals += "Port Louis"
//  capitals += "Port Moresby"
//  capitals += "Port Vila"
//  capitals += "Port-au-Prince"
//  capitals += "Port of Spain"
//  capitals += "Porto-Novo"
//  capitals += "Prague"
//  capitals += "Praia"
//  capitals += "Pretoria"
//  capitals += "Pristina"
//  capitals += "Putrajaya"
//  capitals += "Pyongyang"
//  capitals += "Quito"
//  capitals += "Rabat"
//  capitals += "Reykjavík"
//  capitals += "Riga"
//  capitals += "Riyadh"
//  capitals += "Road Town"
//  capitals += "Rome"
//  capitals += "Roseau"
//  capitals += "Saipan"
//  capitals += "San José"
//  capitals += "San Juan"
//  capitals += "San Marino"
//  capitals += "San Salvador"
//  capitals += "Sanaá"
//  capitals += "Santiago"
//  capitals += "Santo Domingo"
//  capitals += "São Tomé"
//  capitals += "Sarajevo"
//  capitals += "Seoul"
//  capitals += "Singapore"
//  capitals += "Skopje"
//  capitals += "Sofia"
//  capitals += "South Tarawa"
//  capitals += "Sri Jayawardenepura [g]"
//  capitals += "St. George's"
//  capitals += "St. Helier"
//  capitals += "St. John's"
//  capitals += "St. Peter Port"
//  capitals += "St. Pierre"
//  capitals += "Stanley"
//  capitals += "Stockholm"
//  capitals += "Sucre"
//  capitals += "Suva"
//  capitals += "Taipei"
//  capitals += "Tallinn"
//  capitals += "Tashkent"
//  capitals += "Tbilisi"
//  capitals += "Tegucigalpa"
//  capitals += "Tehran"
//  capitals += "Thimphu"
//  capitals += "Tirana"
//  capitals += "Tiraspol"
//  capitals += "Tokyo"
//  capitals += "Tórshavn"
//  capitals += "Tripoli"
//  capitals += "Tskhinvali"
//  capitals += "Tunis"
//  capitals += "Ulaanbaatar"
//  capitals += "Vaduz"
//  capitals += "alletta"
//  capitals += "The Valley"
//  capitals += "Vatican City"
//  capitals += "Victoria"
//  capitals += "Vienna"
//  capitals += "Vientiane"
//  capitals += "Vilnius"
//  capitals += "Warsaw"
//  capitals += "Washington, D.C."
//  capitals += "Wellington"
//  capitals += "West Island"
//  capitals += "Willemstad"
//  capitals += "Windhoek"
//  capitals += "Yamoussoukro"
//  capitals += "Yaoundé"
//  capitals += "Yaren"
//  capitals += "Yerevan"
//  capitals += "Zagreb"

  adjectives += "awesome"
  adjectives += "aware"
  adjectives += "away"
  adjectives += "award"
  adjectives += "awash"
  adjectives += "award-winning"
  adjectives += "awarded"
  adjectives += "awestruck"
  adjectives += "awry"
  adjectives += "awkward"
  adjectives += "awry"

  nouns += "tracking"
  nouns += "tracing"
  nouns += "tramping"
  nouns += "transacting"
  nouns += "tromping"
  nouns += "transfer"
  nouns += "trip"
  nouns += "travel"
  nouns += "transformation"
  nouns += "transforming"
  nouns += "translation"

  @Before
  def initEMF() = {
    try {
      emf = Persistence.createEntityManagerFactory("jpaweb")
    } catch {
      case e: Exception => {
        def printAndDescend(ex: Throwable): Unit = {
          println(e.getMessage())
          if (ex.getCause() != null) {
            printAndDescend(ex.getCause())
          }
        }
        printAndDescend(e)
      }
    }
  }

  @After
  def closeEMF() = {
    if (emf != null) emf.close()
  }

  @Test
  def generate() = {

    var em = emf.createEntityManager()
    val tx = em.getTransaction()

    tx.begin()

    //All the must fields must have a value, otherwise the data is inconsistent on load
    //See JSR 303 Annotations in Entity Class Member
    val member = new Member
    member.name = "pmei"
    member.email = "pmei@lol.com"
    member.password = "pmei"
    em.persist(member)

    //we can do this because
    //- the object member is already attached to JPA (= has a PK)
    //- all the changes are written to the database at the commit() statement
    member.name = member.name + member.id
    em.merge(member)

    val locations = generateLocations(em)

    //Generate tours with 10 stages each
    for (i <- 0 until 100) {
      var tour = new Tour
      tour.name = "Name: Generated Travel " + i
      tour.description = "Description " + i + ": This " + adjectives(scala.util.Random.nextInt(adjectives.size)) + " tour is generated by a generator"
      tour.owner = member
      em.persist(tour)

      var stages = new ArrayList[Stage]()
      for (i <- 0 until 10) {
        var stage = new Stage
        var stageLocation = locations(scala.util.Random.nextInt(locations.size))
        stage.startdate = new Date;
        stage.description = "Description: travel direct to " + stageLocation.countryname + " - " + adjectives(scala.util.Random.nextInt(adjectives.size)) + " " + nouns(scala.util.Random.nextInt(nouns.size))
        stage.name = "Stage " + i + " in " + stageLocation.name + ", " + stageLocation.countrycode
        stage.tour = tour
        stage.destination = stageLocation
        em.persist(stage)

        stages.add(stage)
      }
      tour.stages = stages;
      em.merge(tour)

      if(isStoredInSolr) addTourToSolr(tour)

    }

    tx.commit()
    em.close()
  }


  private def generateLocations(em: EntityManager) = {
    //Reuse persistent Locations on DB, this avoids double entries on subsequent runs of the Generator
    val query = Model.createQuery[Location]("SELECT l from Location l")
    var locations = new ListBuffer[Location]

    if (query.getResultList().size > 0) {
      query.getResultList().foreach(locations += _)
      locations.toList
    } else {

      for (i <- 0 until capitals.size) {
        //sometimes there are many places with that name
        var tempLocation = GeoCoder.findLocationsByName(capitals(i)).head
        //sometimes the geo service behaves strange
        if (tempLocation != null) locations += tempLocation
      }
      locations.foreach(elem => em.persist(elem))
      println("New Generated Locations: " + locations.size)
      locations.toList
    }
  }


  private def addTourToSolr(aTour: Tour): Unit = {
    //use implicit conversion from Java Collection to Scala Collection, see scala.collection.JavaConversions._
    aTour.stages.toList.foreach(SolrAPI.addToSolr(_))
  }


}
}  
}