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
  contraindicated_by: Option[List[String]],
  interacts_with: Option[List[String]]) {

  def cypherNode:Map[String, Any] = Map("name" -> name, "ddbcode" -> ddbcode, "aka" -> aka)

  def cypherEdges: Map[String, List[Map[String, String]]] = {
    val relationships = Map(
      "category" -> category,
      "caused_by" -> caused_by,
      "drug_family" -> drug_family,
      "risk_factor_for" -> risk_factor_for,
      "associated_with" -> associated_with,
      "contraindicated_by" -> contraindicated_by,
      "interacts_with" -> interacts_with
    )
    val edges = relationships.filter(_._2.isDefined).flatMap(x => {
      x._2.get.map { y => Map("from" -> ddbcode, "to" -> y, "type" -> x._1) }
    }).toList

    edges.groupBy(_ ("type")).mapValues(_.map(_ - "type"))
  }
}