package DumbDoctor

import org.neo4j.driver.v1.{GraphDatabase, AuthTokens}
import neotypes.implicits._
import neotypes.Driver
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class NeoLoader(private val host: String,
                     private val user: String,
                     private val pwd: String) {
  private val driver = GraphDatabase.driver(host, AuthTokens.basic(user, pwd)).asScala[Future]

  def insertNodes(entities: Set[Entity]): Future[Unit] = {
    println(entities.size + " nodes to batch insert")
    val nodes = entities.map(_.cypherNode)

    val cypher = c"UNWIND $nodes AS nodes CREATE(n:Thing) SET n = nodes"
    driver.readSession { session => cypher.query[Unit].execute(session) }
  }

  def insertEdges(entities: Set[Entity]): Future[Iterable[Unit]] = {

    val edges: Map[String, Set[Map[String, String]]] =
      entities.
      flatMap(_.cypherEdges).
      groupBy(_._1).
      mapValues(_.flatMap(_._2))

    println(edges.map(_._2.size).sum + " relationships to batch insert")
    Future.sequence {
      edges.map(x => {
        val cypher =
          c"UNWIND ${x._2} as edge MATCH(one:Thing),(two:Thing) " +
            c"WHERE one.ddbcode = edge.from AND two.ddbcode = edge.to " +
            c"CREATE (one)-[r:" + x._1 + c"]->(two)"
        driver.readSession { session => cypher.query[Unit].execute(session) }
      })
    }
  }
}
