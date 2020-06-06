package org.xusheng.ioliw

import cats.data.NonEmptyList
import cats.effect._
import cats.instances.list._
import cats.syntax.all._
import fetch._

object sample {
  type NodeName = String

  case class Node(name: NodeName)

  case class Dep(name: NodeName, deps: List[NodeName])

  def latency[F[_] : Concurrent](msg: String): F[Unit] =
    for {
      _ <- Sync[F].delay(println(s"--> [${Thread.currentThread.getId}] $msg"))
      _ <- Sync[F].delay(Thread.sleep(1000))
      _ <- Sync[F].delay(println(s"<-- [${Thread.currentThread.getId}] $msg"))
    } yield ()

  val nodeDatabase: Map[NodeName, Node] = Map(
    "A" -> Node("A"),
    "B" -> Node("B"),
    "C" -> Node("C"),
    "D" -> Node("D"),
    "E" -> Node("E"),
    "F" -> Node("F")
  )

  object Nodes extends Data[NodeName, Node] {
    def name = "Nodes"

    def source[F[_] : ConcurrentEffect]: DataSource[F, NodeName, Node] = new DataSource[F, NodeName, Node] {
      override def data = Nodes

      override def CF = ConcurrentEffect[F]

      override def fetch(id: NodeName): F[Option[Node]] =
        latency[F](s"One Node $id") >> CF.pure(nodeDatabase.get(id))

      // override def maxBatchSize: Option[Int] = Some(2)

      override def batchExecution: BatchExecution = InParallel

      override def batch(ids: NonEmptyList[NodeName]): F[Map[NodeName, Node]] =
        latency[F](s"Batch Nodes $ids") >> CF.pure(nodeDatabase.filterKeys(ids.toList.toSet))
    }
  }

  def getNode[F[_] : ConcurrentEffect](id: NodeName): Fetch[F, Node] =
    Fetch(id, Nodes.source)

  def getGraph[F[_] : ConcurrentEffect](id: NodeName, deps: Map[NodeName, List[NodeName]]): Fetch[F, Node] =
    for {
      _ <- deps.get(id).map {
        ids => ids.traverse(i => getGraph(i, deps))
      }.getOrElse(Fetch.pure[F, List[Node]](List.empty))
      n <- getNode(id)
    } yield n

  def main(args: Array[String]): Unit = {
    import java.util.concurrent._

    import scala.concurrent.ExecutionContext
    import scala.concurrent.duration._

    val executor = new ScheduledThreadPoolExecutor(4)
    val executionContext: ExecutionContext = ExecutionContext.fromExecutor(executor)

    implicit val timer: Timer[IO] = IO.timer(executionContext)
    implicit val cs: ContextShift[IO] = IO.contextShift(executionContext)


    val deps = Map(
      "A" -> List("B", "C"),
      "B" -> List("D", "E"),
      "C" -> List("E", "F"),
    )

    Fetch.run[IO](getGraph("A", deps)).unsafeRunTimed(10.seconds)

    executor.shutdown()
  }
}
