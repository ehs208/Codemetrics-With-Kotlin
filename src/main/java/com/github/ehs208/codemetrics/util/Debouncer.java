package com.github.ehs208.codemetrics.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Alarm;

public class Debouncer {
  private Alarm alarm = new Alarm();

  public void debounce(Runnable runnable) {
    alarm.cancelAllRequests();
    alarm.addRequest(() -> ApplicationManager.getApplication().runReadAction(runnable), 1000);
  }
}
