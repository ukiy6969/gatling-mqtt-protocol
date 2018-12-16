package com.github.jeanadrien.gatling.mqtt.actions

import akka.actor.ActorRef
import akka.pattern.AskTimeoutException
import akka.util.Timeout
import com.github.jeanadrien.gatling.mqtt.client.MqttCommands
import com.github.jeanadrien.gatling.mqtt.client.MqttQoS.MqttQoS
import com.github.jeanadrien.gatling.mqtt.protocol.MqttComponents
import io.gatling.commons.stats.{KO, OK}
import io.gatling.core.CoreComponents
import io.gatling.core.action.Action
import io.gatling.core.session._

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

/**
  *
  */
class PublishAndWaitAction(
    requestName     : Expression[String],
    mqttComponents  : MqttComponents,
    coreComponents  : CoreComponents,
    topic           : Expression[String],
    payload         : Expression[Array[Byte]],
    payloadFeedback : Array[Byte] => Array[Byte] => Boolean,
    qos             : MqttQoS,
    retain          : Boolean,
    timeout         : FiniteDuration,
    val next        : Action
) extends MqttAction(mqttComponents, coreComponents) {

    import akka.pattern.ask
    import mqttComponents.system.dispatcher

    override val name = genName("mqttPublishAndWait")

    override def execute(session : Session) : Unit = recover(session)(for {
        _requestName <- requestName(session)
        connection <- session("engine").validate[ActorRef]
        connectionId <- session("connectionId").validate[String]
        resolvedTopic <- topic(session)
        resolvedPayload <- payload(session)
    } yield {
        implicit val messageTimeout = Timeout(timeout)

        val requestStartDate = clock.nowMillis

        logger.debug(s"${connectionId} : Execute ${requestName} Payload: ${resolvedPayload}")

        val payloadCheck = payloadFeedback(resolvedPayload)

        (connection ? MqttCommands.PublishAndWait(
            topic = resolvedTopic,
            payload = resolvedPayload,
            payloadFeedback = payloadCheck,
            qos = qos,
            retain = retain
        )).mapTo[MqttCommands] onComplete {
            case Success(MqttCommands.FeedbackReceived) =>
                val requestEndDate = clock.nowMillis

                statsEngine.logResponse(
                    session,
                    _requestName,
                    requestStartDate,
                    requestEndDate,
                    OK,
                    None,
                    None
                )

                next ! session
            case Failure(th) =>
                val requestEndDate = clock.nowMillis

                statsEngine.logResponse(
                    session,
                    _requestName,
                    requestStartDate,
                    requestEndDate,
                    KO,
                    None,
                    th match {
                        case t : AskTimeoutException =>
                            logger
                                .warn(s"${connectionId}: Wait for PUBLISH back from mqtt timed out on ${resolvedTopic}")
                            Some("Wait for PUBLISH timed out")
                        case t =>
                            logger
                                .warn(s"${connectionId}: Failed to receive PUBLISH back from mqtt on ${resolvedTopic}: ${t}")
                            Some(t.getMessage)
                    }
                )

                next ! session
        }
    })
}
