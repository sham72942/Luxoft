import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import assignment.akkactor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Success}

object AkkaMain extends App {

  val system: ActorSystem[Command] = ActorSystem(Behaviors.empty, "SensorDataProcessing")
  val aggregationActor = system.systemActorOf(AggregationActor(), "aggregationActor")
  val directoryPath = args.headOption.getOrElse("")

  processSensorDataFiles(directoryPath)

  def processSensorDataFiles(directoryPath: String): Unit = {
    val files = new java.io.File(directoryPath).listFiles.map(_.getPath).filter(_.endsWith(".csv")).toList

    val fileProcessorActors = files.map { filePath =>
      system.systemActorOf(FileProcessor(aggregationActor), s"fileProcessorActor_${filePath.hashCode}")
    }

    val processingFutures = files.zip(fileProcessorActors).map { case (file, actor) =>
      val replyPromise = Promise[Processed]()
      actor ! ProcessFileWithPromise(file, replyPromise)
      replyPromise.future
    }

    // Wait for all message processing to complete
    Future.sequence(processingFutures).onComplete {
      case Success(_) =>
        // All messages processed, calculate averages
        aggregationActor ! CalculateAverages(system.ignoreRef)
      case Failure(exception) =>
        println("Message processing failed")
    }

    // Await the termination of the ActorSystem
    Await.result(system.whenTerminated, Duration.Inf)
    ()
  }
}
