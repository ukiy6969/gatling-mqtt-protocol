package com.github.jeanadrien.gatling.mqtt

import java.nio.charset.StandardCharsets

import com.github.jeanadrien.gatling.mqtt.actions._
import com.github.jeanadrien.gatling.mqtt.protocol.MqttProtocolBuilder
import io.gatling.core.Predef._
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session._

/**
  *
  */
object Predef {

    def mqtt(implicit configuration : GatlingConfiguration) = MqttProtocolBuilder(configuration)

    def mqtt(requestName: Expression[String]) = Mqtt(requestName)

    def connect = ConnectActionBuilder("connect")

    def subscribe(topic : Expression[String]) = SubscribeActionBuilder("subscribe", topic)

    def publish[T <% MqttPayload](
        topic : Expression[String], payload : Expression[T]
    ) = PublishActionBuilder("publish", topic, payload.map(_.toByteArray))

    def publishAndWait[T <% MqttPayload](
        topic : Expression[String], payload : Expression[T]
    ) = PublishAndWaitActionBuilder("publish and wait", topic, payload.map(_.toByteArray))

    def waitForMessages = WaitForMessagesActionBuilder

    def payload(in : Expression[String]) : Expression[Array[Byte]] =
        in.map(_.getBytes(StandardCharsets.UTF_8))

    trait MqttPayload {
        def toByteArray : Array[Byte]
    }

    implicit class StringMqttPayload(s : String) extends MqttPayload {
        override def toByteArray = s.getBytes
    }

    implicit def byteArrayPayload(b : Array[Byte]) : MqttPayload = new MqttPayload {
        override def toByteArray = b
    }
}
