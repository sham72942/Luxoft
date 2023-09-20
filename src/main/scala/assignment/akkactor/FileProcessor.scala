package assignment.akkactor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import assignment.akkactor.SensorManager.getSensorDataMap

object FileProcessor {
  def apply(aggregationActor: ActorRef[Command]): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case ProcessFileWithPromise(filePath, replyPromise) =>
          val sensorDataMap = getSensorDataMap(filePath)
          aggregationActor ! UpdatedData(sensorDataMap)
          replyPromise.success(Processed(filePath))
          Behaviors.same

        case _ =>
          context.log.info("Unexpected Error...")
          Behaviors.same
      }
    }
}

