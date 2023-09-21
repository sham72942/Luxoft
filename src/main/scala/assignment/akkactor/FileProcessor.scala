package assignment.akkactor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

class FileProcessor(aggregationActor: ActorRef[Command]) extends LuxoftActor {
  override def behavior(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case ProcessFileWithPromise(filePath, replyPromise) =>
          val sensorDataMap = SensorManager.getSensorDataMap(filePath)
          aggregationActor ! UpdatedData(sensorDataMap)
          replyPromise.success(Processed(filePath))
          Behaviors.same

        case _ =>
          context.log.info("Unexpected Error...")
          Behaviors.same
      }
    }
}

