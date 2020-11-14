package com.niks.utils;

import java.util.Map;

public class EnvironmentVarUtils {

  public static String getEnvironmentVar(final String name) {
    final Map<String, String> environmentVariables = System.getenv();
    return environmentVariables.get(name);
  }
}
