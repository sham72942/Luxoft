package assignment.akkactor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.mutable

object AggregationActor {
  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      val finalData = mutable.Map.empty[String, Stats]

      Behaviors.receiveMessage {
        case UpdatedData(data) =>
          // Merge data from different files into finalData
          data.foreach {
            case (id, stats) =>
              val currentStats = finalData.getOrElse(id, Stats(0, 0.0, Int.MaxValue, Int.MinValue, 0))
              val newStats = Stats(
                currentStats.failedCount + stats.failedCount,
                currentStats.sum + stats.sum,
                Math.min(currentStats.min, stats.min),
                Math.max(currentStats.max, stats.max),
                currentStats.count + stats.count
              )
              finalData(id) = newStats
          }
          Behaviors.same

        case CalculateAverages(_) =>
          context.log.info("Sensors with highest avg humidity:")
          context.log.info("sensor-id,min,avg,max")
          finalData.foreach {
            case (id, stats) =>
              val average = stats.sum / stats.count
              context.log.info(s"$id,${stats.min},$average,${stats.max}")
          }
          Behaviors.same
      }
    }
}

