package com.github.jeanadrien.gatling.mqtt.actions

import com.github.jeanadrien.gatling.mqtt.protocol.MqttComponents
import io.gatling.commons.util.DefaultClock
import io.gatling.core.CoreComponents
import io.gatling.core.action.ExitableAction
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen

/**
  *
  */
abstract class MqttAction(
    mqttComponents : MqttComponents,
    coreComponents : CoreComponents
) extends ExitableAction with NameGen {

    val clock = new DefaultClock

    val statsEngine = coreComponents.statsEngine

}
