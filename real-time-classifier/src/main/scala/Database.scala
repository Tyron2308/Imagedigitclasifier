import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.dsl.{UUID, _}

import scala.concurrent.Future

object definition {
  val keySpaceClassifier     = "label-and-prediction"
}
case class Record(id: UUID, image: List[Double], prediction: String, label: String)

object DefaultConnector {
  val hosts = Seq("127.0.0.1")
  val connector = ContactPoints(hosts).keySpace(definition.keySpaceClassifier)
}

object CassandraContainerConnector {
  val host = Seq("172.31.13.70")
  val connector = ContactPoints(host).keySpace(definition.keySpaceClassifier)
}

class DmpDatabase(override val connector: CassandraConnection)
  extends Database[DmpDatabase](connector) {
  object users extends ConcreteLog with Connector
}

object DmpDatabase extends DmpDatabase(CassandraContainerConnector.connector)

abstract class Logs extends Table[Logs, Record] {
  object id extends UUIDColumn with PartitionKey
  object label extends StringColumn
  object prediction extends StringColumn
  object image extends ListColumn[Double]

  def getById(id: UUID): Future[Option[Record]] = {
    select.where(_.id eqs id).one()
  }
}

abstract class ConcreteLog extends Logs with RootConnector {
  def store(rec: Record): Future[ResultSet] = {
    insert
      .value(_.id, rec.id)
      .value(_.label, rec.label)
      .value(_.prediction, rec.prediction)
      .value(_.image, rec.image)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }

  def myselect(): Future[ListResult[Record]] = {
    select.all().fetchRecord()
  }
}
