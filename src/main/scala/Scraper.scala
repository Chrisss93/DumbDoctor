package DumbDoctor

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors._

object Scraper {
  private val base_address: String = "http://www.diseasesdatabase.com"

  def scrape = 'a'.to('z').flatMap(scrapeIndex)

  def scrapeIndex(letter:Char): List[Entity] = {
    val doc: Document = JsoupBrowser().get(s"$base_address/disease_index_$letter.asp")
    doc >> elementList("#page_specific_content > a") map (findEntity)
  }

  def findEntity(el: Element): Entity = {
    val url = JsoupBrowser().get(s"$base_address/${el.attr("href")}")
    val nm = url >> elementList("#page_specific_content > p.squeezed") map(_.text)
    val properties = parseEntity(url)
    Thread.sleep(1000)
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
      contraindicated_by = properties.get("may be contraindicated with +"),
      interacts_with     = properties.get("may interact with +")
    )
  }

  def parseEntity(doc: Document) = {
    val relationships = doc >> elementList("#page_specific_content > ul > li > a")
    relationships.map(x => {
      val html = JsoupBrowser().get(s"$base_address/${x.attr("href")}")
      val tags = List(
        html >> elementList("#page_specific_content > dl > dd > ul > li > a"),
        html >> elementList("#page_specific_content > ul > li > strong > a")
      ).filter(_.nonEmpty).flatten
      Thread.sleep(1000)
      (x.text, tags.map(_.attr("href").replaceFirst("\\.htm$", "")))
    }).toMap
  }
}
