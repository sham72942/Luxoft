package assignment.akkactor

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ScalaTestWithActorTestKit}
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.io.File
import scala.concurrent.Promise

class FileProcessorSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach {

  val aggregationActor: ActorRef[Command] = testKit.spawn(Behaviors.empty)
  def createFileProcessorActor(): ActorRef[Command] = {
    val fileProcessorImpl = new FileProcessor(aggregationActor)
    testKit.spawn(fileProcessorImpl.behavior())
  }

  val testSystem = ActorTestKit()

  "FileProcessor" should {

    "reply with Processed when processing a file" in {

      val filePath =
        new File("src/test/resources/test.csv").getAbsoluteFile.getPath

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
