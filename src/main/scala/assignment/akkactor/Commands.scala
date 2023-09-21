package assignment.akkactor

import akka.actor.typed.ActorRef
import scala.concurrent.Promise

sealed trait Command
case class ProcessFileWithPromise(
  filePath: String,
  replyPromise: Promise[Processed]
) extends Command
case class CalculateAverages(replyTo: ActorRef[AveragesCalculated])
  extends Command
case class Processed(filePath: String) extends Command
case class AveragesCalculated() extends Command
case class UpdatedData(value: Map[String, Stats]) extends Command
case class GetState(replyTo: ActorRef[State]) extends Command
case class State(data: Map[String, Stats]) extends Command
