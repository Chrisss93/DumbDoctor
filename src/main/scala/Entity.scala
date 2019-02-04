package DumbDoctor

case class Entity(
  name: String,
  ddbcode: String,
  aka: List[String],
  category: Option[List[String]],
  caused_by: Option[List[String]],
  drug_family: Option[List[String]],
  risk_factor_for: Option[List[String]],
  associated_with: Option[List[String]],
  allelic_with: Option[List[String]],
  vector_for: Option[List[String]],
  contraindicated_by: Option[List[String]],
  interacts_with: Option[List[String]],
  see_also: Option[List[String]]) {

  def cypherNode:Map[String, Any] = Map("name" -> name, "ddbcode" -> ddbcode, "aka" -> aka)

  def cypherEdges: Map[String, List[Map[String, String]]] = {
    val relationships = Map(
      "is_a" -> category,
      "caused_by" -> caused_by,
      "drug_family" -> drug_family,
      "risk_factor_for" -> risk_factor_for,
      "associated_with" -> associated_with,
      "allelic_with" -> allelic_with,
      "vector_for" -> vector_for,
      "contraindicated_by" -> contraindicated_by,
      "interacts_with" -> interacts_with,
      "relevant_to" -> see_also
    )
    val edges = relationships.filter(_._2.isDefined).flatMap(x => {
      x._2.get.map { y => Map("from" -> ddbcode, "to" -> y, "type" -> x._1) }
    }).toList

    edges.groupBy(_ ("type")).mapValues(_.map(_ - "type"))
  }
}