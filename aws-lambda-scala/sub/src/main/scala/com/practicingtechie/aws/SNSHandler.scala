package com.practicingtechie.aws


import collection.JavaConverters._
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.{Context => AWSContext}


class SNSHandler extends RequestHandler[SNSEvent, Object] {
  import argonaut._, Argonaut._

  override def handleRequest(ev: SNSEvent, ctx: AWSContext): Object = {
    ctx.getLogger.log(s"Invocation started/scala: ${java.time.Instant.now}")

    ev.getRecords.asScala.toList.headOption match {
      case Some(record) =>
        val sns = record.getSNS
        val attrs = sns.getMessageAttributes.asScala
        val contentTypeO = attrs.get("content-type").map(_.getValue)
        val messageO = Parse.decodeOption[UserChangeEvent](sns.getMessage)

        ctx.getLogger.log(s"attributes: $attrs")
        ctx.getLogger.log(s"content-type: $contentTypeO")
        ctx.getLogger.log(s"msg: ${messageO}")
        ctx.getLogger.log(s"Invocation completed: ${java.time.Instant.now}")
      case _ => ctx.getLogger.log("WARN: no records")
    }

    null
  }

}