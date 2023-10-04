import akka.actor.typed.{ActorSystem, Props}
import akka.actor.typed.scaladsl.Behaviors
import assignment.akkactor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success}

object AkkaMain extends App {

  val system: ActorSystem[Command] =
    ActorSystem(Behaviors.empty, "SensorDataProcessing")
  val ag = new AggregationActor()
  val aggregationActor = system.systemActorOf(ag.behavior(), "aggregationActor")
  val directoryPath = args.headOption.getOrElse("")

  val fileProcessorTpd =
    Props.empty.withDispatcherFromConfig("fileProcessor-tpd")

  processSensorDataFiles(directoryPath, fileProcessorTpd)

  def processSensorDataFiles(directoryPath: String, dispatcher: Props): Unit = {
    val files = new java.io.File(directoryPath).listFiles
      .map(_.getPath)
      .filter(_.endsWith(".csv"))
      .toList

    val fileProcessorActors = files.map { filePath =>
      val fileProcessor = new FileProcessor(aggregationActor)
      system.systemActorOf(
        fileProcessor.behavior(),
        s"fileProcessorActor_${filePath.hashCode}",
        dispatcher)
    }

    val processingFutures =
      files.zip(fileProcessorActors).map { case (file, actor) =>
        val replyPromise = Promise[Processed]()
        actor ! ProcessFileWithPromise(file, replyPromise)
        replyPromise.future
      }

    // Wait for all message processing to complete
    Future.sequence(processingFutures).onComplete {
      case Success(_) =>
        aggregationActor ! CalculateAverages(system.ignoreRef)
        system.terminate()
      case Failure(exception) =>
        println(s"Message processing failed: ${exception}")
    }

    Await.result(system.whenTerminated, Duration.Inf)
    ()
  }
}
