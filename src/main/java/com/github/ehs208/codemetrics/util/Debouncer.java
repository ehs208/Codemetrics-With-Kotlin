package com.github.ehs208.codemetrics.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.Disposable;
import com.intellij.util.Alarm;

public class Debouncer implements Disposable {
  private final Alarm alarm;

  public Debouncer(Disposable parent) {
    // Register alarm with proper parent to prevent memory leaks
    this.alarm = new Alarm(parent);
  }

  public void debounce(Runnable runnable) {
    alarm.cancelAllRequests();
    alarm.addRequest(() -> ApplicationManager.getApplication().runReadAction(runnable), 1000);
  }

  @Override
  public void dispose() {
    alarm.cancelAllRequests();
  }
}
