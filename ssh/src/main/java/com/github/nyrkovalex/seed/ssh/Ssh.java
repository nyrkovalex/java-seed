package com.github.nyrkovalex.seed.ssh;

public final class Ssh {

  private Ssh() {
  }

  public static ScpCommand scpTo(String url) {
    return new ScpCommand(url);
  }

}
