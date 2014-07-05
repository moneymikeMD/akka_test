package controllers

import play.api._
import play.api.mvc._
import akka.actor.Props
import akka.pattern.ask
import com.test.actors.{Work, Ping, TestActor}
import play.api.Play.current
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {
  val akkaSys = play.api.libs.concurrent.Akka.system
  val testActor = akkaSys.actorOf(Props[TestActor], name = "test-actor-yesterday")
  implicit val timeout = Timeout(5 seconds)


  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def firstAsyncAction = Action.async { req =>
    val result = (testActor ? Ping).mapTo[String]
    result.map(Ok(_))
  }

  def workerAdd(userMsg: String) = Action.async { req =>
    val result = (testActor ? Work(userMsg)).mapTo[String]
    result.map(Ok(_))
  }

}