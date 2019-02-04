package DumbDoctor

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors._
import com.typesafe.config.ConfigFactory
import scala.concurrent.{duration, Await}

object Scraper {
  val base_address: String = "http://www.diseasesdatabase.com"
  val browser = new JsoupBrowser(userAgent = "Mozilla/5.0 Firefox/64.0")
  val timer = breeze.stats.distributions.Poisson(2)

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load()
    val neo = NeoLoader(
      conf.getString("neo4j.host"),
      conf.getString("neo4j.user"),
      conf.getString("neo4j.pwd")
    )
    val entities = scrape()
    Await.ready(neo.insertNodes(entities), duration.Duration.Inf)
    Await.ready(neo.insertEdges(entities), duration.Duration.Inf)
  }

  def scrape(): Set[Entity] = {
    val all_links = ('a' to 'z').flatMap(letter => {
      val doc: Document = browser.get(s"$base_address/disease_index_$letter.asp")
      doc >> elementList("#page_specific_content > a")
    })
    all_links.toSet.map(findEntity)
  }

  def findEntity(el: Element): Entity = {
    println(s"Scraping: " + el.text)
    val url = browser.get(s"$base_address/${el.attr("href")}")
    val nm = url >> elementList("#page_specific_content > p.squeezed") map(_.text)
    val properties = parseEntity(url)
    Thread.sleep(timer.draw() * 1000)
    Entity(
      name               = el.text,
      ddbcode            = el.attr("href").replaceFirst("\\.htm$", ""),
      aka                = nm.filter(_ != el.text),
      category           = properties.get("belong(s) to the category of +"),
      caused_by          = properties.get("may be caused by or feature of +"),
//    leads_to           = properties.get("may cause or feature +"),
      drug_family        = properties.get("belongs to the drug family of +"),
      risk_factor_for    = properties.get("may be a risk factor for +"),
      associated_with    = properties.get("may be associated with +"),
      allelic_with       = properties.get("may be allelic with +"),
      vector_for         = properties.get("may be a vector for +"),
      contraindicated_by = properties.get("may be contraindicated with +"),
      interacts_with     = properties.get("may interact with +"),
      see_also           = properties.get("see also +")
    )
  }

  def parseEntity(doc: Document) = {
    val relationships = doc >> elementList("#page_specific_content > ul > li > a")
    relationships.map(x => {
      Thread.sleep(timer.draw() * 1000)
      val html = browser.get(s"$base_address/${x.attr("href")}")
      val tags = List(
        html >> elementList("#page_specific_content > dl > dd > ul > li > a"),
        html >> elementList("#page_specific_content > ul > li > strong > a")
      ).filter(_.nonEmpty).flatten

      (x.text, tags.map(_.attr("href").replaceFirst("\\.htm$", "")))
    }).toMap
  }
}
