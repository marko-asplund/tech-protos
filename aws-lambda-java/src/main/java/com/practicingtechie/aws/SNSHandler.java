package com.practicingtechie.aws;


import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;


public class SNSHandler implements RequestHandler<SNSEvent, Object> {

  public Object handleRequest(SNSEvent request, Context context) {
    context.getLogger().log("Invocation started/2: " + java.time.Instant.now());
    context.getLogger().log(request.getRecords().get(0).getSNS().getMessage());
    context.getLogger().log("Invocation completed: " + java.time.Instant.now());

    return null;
  }

}
