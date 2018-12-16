package com.github.jeanadrien.gatling.mqtt

import com.github.jeanadrien.gatling.mqtt.Predef.MqttPayload
import com.github.jeanadrien.gatling.mqtt.actions.{ConnectActionBuilder, PublishActionBuilder, PublishAndWaitActionBuilder, SubscribeActionBuilder}
import io.gatling.core.session.Expression

case class Mqtt(requestName: Expression[String]) {

    def connect = ConnectActionBuilder(requestName)

    def subscribe(topic : Expression[String]) = SubscribeActionBuilder(requestName, topic)

    def publish[T <% MqttPayload](
        topic : Expression[String], payload : Expression[T]
    ) = PublishActionBuilder(requestName, topic, payload.map(_.toByteArray))

    def publishAndWait[T <% MqttPayload](
        topic : Expression[String], payload : Expression[T]
    ) = PublishAndWaitActionBuilder(requestName, topic, payload.map(_.toByteArray))
}
