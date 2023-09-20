package assignment.akkactor

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ScalaTestWithActorTestKit}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Promise

class FileProcessorSpec
  extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach {

  val aggregationActor: ActorRef[Command] = testKit.spawn(Behaviors.empty)
  def createFileProcessorActor(): ActorRef[Command] =
    testKit.spawn(FileProcessor(aggregationActor))

  val testSystem = ActorTestKit()

  "FileProcessor" should {

    "reply with Processed when processing a file" in {
      val filePath = "C:\\Users\\shamo\\Documents\\Luxoft Sensor Assignment\\src\\test\\resources\\test.csv"
      val myActor = createFileProcessorActor()
      val replyPromise = Promise[Processed]()
      myActor ! ProcessFileWithPromise(filePath, replyPromise)

      val result = replyPromise.future.futureValue
      result shouldEqual Processed(filePath)
    }
  }

  override def afterAll(): Unit = {
    testSystem.shutdownTestKit()
    super.afterAll()
  }
}
