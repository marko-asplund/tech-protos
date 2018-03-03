package com.practicingtechie.aws

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import collection.JavaConverters._
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent

class SNSHandler extends RequestHandler[SNSEvent, Object] {
  case class UserChangeEvent(userId: String)

  override def handleRequest(ev: SNSEvent, ctx: Context): Object = {
    ctx.getLogger().log(s"Invocation started/scala: ${java.time.Instant.now}")
    ctx.getLogger().log(ev.getRecords.get(0).getSNS().getMessage())
    ctx.getLogger().log(s"Invocation completed: ${java.time.Instant.now}")
    null
  }
}