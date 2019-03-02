/*
 * Fowarder4j.
 * Copyright (C) 2015-2019 Fowarder4j Team.
 * https://github.com/lolocohen/forwarder4j
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.forwarder4j;

import java.io.*;
import java.net.*;
import java.util.Properties;

import org.slf4j.*;

/**
 * Loads and handles the configuration properties.
 * @author Laurent Cohen
 */
class Config extends Properties {
  /**
   * Logger for this class.
   */
  private static Logger log = LoggerFactory.getLogger(Forwarder.class);
  /**
   * System property indicating the config file path.
   */
  private static final String CONFIG_FILE_PROP = "forwarder4j.config";
  /**
   * Default path for the config file.
   */
  private static final String DEFAULT_CONFIG_FILE = "config/forwarder4j.properties";
  /**
   * Singleton instance of the configuration
   */
  private static Config instance = null;

  /**
   * Get the configuration properties.
   * @return a {@link Config} singleton instance.
   */
  public static synchronized Config getConfiguration() {
    if (instance == null) {
      instance = new Config();
      final String location = System.getProperty(CONFIG_FILE_PROP);
      try (BufferedReader reader = new BufferedReader(new FileReader((location != null) ? location : DEFAULT_CONFIG_FILE))) {
        instance.load(reader);
      } catch(Exception e) {
        log.debug(e.getMessage(), e);
      }
    }
    return instance;
  }

  /**
   * Get the string value of a property with a specified name.
   * @param key the name of the property to look for.
   * @return the value of the property as a string, or null if it is not found.
   */
  public String getString(final String key) {
    return getProperty(key, null);
  }

  /**
   * Get the string value of a property with a specified name.
   * @param key the name of the property to look for.
   * @param defValue a default value to return if the property is not found.
   * @return the value of the property as a string, or the default value if it is not found.
   */
  public String getString(final String key, final String defValue) {
    return getProperty(key, defValue);
  }

  /**
   * Set a property with the specified String value.
   * @param key the name of the property to set.
   * @param value the value to set on the property.
   */
  public void setString(final String key, final String value) {
    setProperty(key, value);
  }

  /**
   * Get the integer value of a property with a specified name.
   * @param key the name of the property to look for.
   * @return the value of the property as an int, or zero if it is not found.
   */
  public int getInt(final String key) {
    return getInt(key, 0);
  }

  /**
   * Get the integer value of a property with a specified name.
   * @param key the name of the property to look for.
   * @param defValue a default value to return if the property is not found.
   * @return the value of the property as an int, or the default value if it is not found.
   */
  public int getInt(final String key, final int defValue) {
    int intVal = defValue;
    String val = getProperty(key, null);
    if (val != null) {
      val = val.trim();
      try {
        intVal = Integer.valueOf(val);
      } catch(@SuppressWarnings("unused") NumberFormatException ignore) {
        try {
          intVal = Double.valueOf(val).intValue();
        } catch(@SuppressWarnings("unused") NumberFormatException ignore2) {
        }
      }
    }
    return intVal;
  }

  /**
   * Set a property with the specified int value.
   * @param key the name of the property to set.
   * @param value the value to set on the property.
   */
  public void setInt(final String key, final int value) {
    setProperty(key, Integer.toString(value));
  }

  /**
   * Get the long integer value of a property with a specified name.
   * @param key the name of the property to look for.
   * @return the value of the property as a long, or zero if it is not found.
   */
  public long getLong(final String key) {
    return getLong(key, 0L);
  }

  /**
   * Get the long integer value of a property with a specified name.
   * @param key the name of the property to look for.
   * @param defValue a default value to return if the property is not found.
   * @return the value of the property as a long, or the default value if it is not found.
   */
  public long getLong(final String key, final long defValue) {
    long longVal = defValue;
    String val = getProperty(key, null);
    if (val != null) {
      val = val.trim();
      try {
        longVal = Long.valueOf(val);
      } catch(@SuppressWarnings("unused") NumberFormatException ignore) {
        try {
          longVal = Double.valueOf(val).longValue();
        } catch(@SuppressWarnings("unused") NumberFormatException ignore2) {
        }
      }
    }
    return longVal;
  }

  /**
   * Set a property with the specified long value.
   * @param key the name of the property to set.
   * @param value the value to set on the property.
   */
  public void setLong(final String key, final long value) {
    setProperty(key, Long.toString(value));
  }

  /**
   * Get the single precision value of a property with a specified name.
   * @param key the name of the property to look for.
   * @return the value of the property as a float, or zero if it is not found.
   */
  public float getFloat(final String key) {
    return getFloat(key, 0f);
  }

  /**
   * Get the single precision value of a property with a specified name.
   * @param key the name of the property to look for.
   * @param defValue a default value to return if the property is not found.
   * @return the value of the property as a float, or the default value if it is not found.
   */
  public float getFloat(final String key, final float defValue) {
    float floatVal = defValue;
    String val = getProperty(key, null);
    if (val != null) {
      try {
        floatVal = Float.parseFloat(val.trim());
      } catch(@SuppressWarnings("unused") final NumberFormatException e) {
      }
    }
    return floatVal;
  }

  /**
   * Set a property with the specified float value.
   * @param key the name of the property to set.
   * @param value the value to set on the property.
   */
  public void setFloat(final String key, final float value) {
    setProperty(key, Float.toString(value));
  }

  /**
   * Get the double precision value of a property with a specified name.
   * If the key is not found a default value of 0d is returned.
   * @param key the name of the property to look for.
   * @return the value of the property as a double, or zero if it is not found.
   */
  public double getDouble(final String key) {
    return getDouble(key, 0d);
  }

  /**
   * Get the double precision value of a property with a specified name.
   * @param key the name of the property to look for.
   * @param defValue a default value to return if the property is not found.
   * @return the value of the property as a double, or the default value if it is not found.
   */
  public double getDouble(final String key, final double defValue) {
    double doubleVal = defValue;
    String val = getProperty(key, null);
    if (val != null) {
      try {
        doubleVal = Double.parseDouble(val.trim());
      } catch(@SuppressWarnings("unused") final NumberFormatException e) {
      }
    }
    return doubleVal;
  }

  /**
   * Set a property with the specified double value.
   * @param key the name of the property to set.
   * @param value the value to set on the property.
   */
  public void setDouble(final String key, final double value) {
    setProperty(key, Double.toString(value));
  }

  /**
   * Get the boolean value of a property with a specified name.
   * If the key is not found a default value of false is returned.
   * @param key the name of the property to look for.
   * @return the value of the property as a boolean, or <code>false</code> if it is not found.
   */
  public boolean getBoolean(final String key) {
    return getBoolean(key, false);
  }

  /**
   * Get the boolean value of a property with a specified name.
   * @param key the name of the property to look for.
   * @param defValue a default value to return if the property is not found.
   * @return the value of the property as a boolean, or the default value if it is not found.
   */
  public boolean getBoolean(final String key, final boolean defValue) {
    boolean booleanVal = defValue;
    String val = getProperty(key, null);
    if (val != null) booleanVal = Boolean.valueOf(val.trim()).booleanValue();
    return booleanVal;
  }

  /**
   * Set a property with the specified boolean value.
   * @param key the name of the property to set.
   * @param value the value to set on the property.
   */
  public void setBoolean(final String key, final boolean value) {
    setProperty(key, Boolean.toString(value));
  }

  /**
   * Get the char value of a property with a specified name.
   * If the key is not found a default value of ' ' is returned.
   * @param key the name of the property to look for.
   * @return the value of the property as a char, or the default value ' ' (space character) if it is not found.
   */
  public char getChar(final String key) {
    return getChar(key, ' ');
  }

  /**
   * Get the char value of a property with a specified name.
   * If the value has more than one character, the first one will be used.
   * @param key the name of the property to look for.
   * @param defValue a default value to return if the property is not found.
   * @return the value of the property as a char, or the default value if it is not found.
   */
  public char getChar(final String key, final char defValue) {
    char charVal = defValue;
    String val = getProperty(key, null);
    if ((val != null) && (val.length() > 0)) charVal = val.charAt(0);
    return charVal;
  }

  /**
   * Set a property with the specified char value.
   * @param key the name of the property to set.
   * @param value the value to set on the property.
   */
  public void setChar(final String key, final char value) {
    setProperty(key, Character.toString(value));
  }

  /**
   * Get the value of the specified property as a {@link File}.
   * @param key the name of the property to look up.
   * @return an abstract file path based on the value of the property, or null if the property is not defined.
   */
  public File getFile(final String key) {
    return getFile(key, null);
  }

  /**
   * Get the value of the specified property as a {@link File}.
   * @param key the name of the property to look up.
   * @param defValue the value to return if the property is not found.
   * @return an abstract file path based on the value of the property, or the default value if the property is not defined.
   */
  public File getFile(final String key, final File defValue) {
    String s = getProperty(key);
    return (s == null) || s.trim().isEmpty() ? defValue : new File(s);
  }

  /**
   * Set the value of the specified property as a {@link File}.
   * @param key the name of the property to look up.
   * @param value the file whose path to set as the property value.
   */
  public void setFile(final String key, final File value) {
    if (value != null) setProperty(key, value.getPath());
  }

  /**
   * Get the value of a property with the specified name as an {@link InetAddress}.
   * @param key the name of the property to retrieve.
   * @return the property as an {@link InetAddress} instance, or null if the property is not defined or the host doesn't exist.
   */
  public InetAddress getInetAddress(final String key) {
    return getInetAddress(key, null);
  }

  /**
   * Get the value of a property with the specified name as an {@link InetAddress}.
   * @param key the name of the property to retrieve.
   * @param def the default value to use if the property is not defined.
   * @return the property as an {@link InetAddress} instance, or the specified default value if the property is not defined.
   */
  public InetAddress getInetAddress(final String key, final InetAddress def) {
    String val = getString(key);
    if (val == null) return def;
    try {
      return InetAddress.getByName(val);
    } catch(@SuppressWarnings("unused") final UnknownHostException e) {
      return def;
    }
  }

  /**
   * Convert this set of properties into a string.
   * @return a representation of this object as a string.
   */
  public String asString() {
    StringBuilder sb = new StringBuilder();
    for (String key: stringPropertyNames()) sb.append(key).append(" = ").append(getProperty(key)).append('\n');
    return sb.toString();
  }

  /**
   * Extract the properties that pass the specified filter.
   * @param filter the filter to use, if <code>null</code> then all properties are retruned.
   * @return a new <code>TypedProperties</code> object containing only the properties matching the filter.
   */
  public Config filter(final Filter filter) {
    Config result = new Config();
    for (String key: stringPropertyNames()) {
      String value = getProperty(key);
      if ((filter == null) || filter.accepts(key, value)) result.put(key, value);
    }
    return result;
  }

  /**
   * A filter for <code>TypedProperties</code> objects.
   */
  public interface Filter {
    /**
     * Determine whether this filter accepts a property with the specirfied name and value.
     * @param name the name of the property.
     * @param value the value of the property.
     * @return <code>true</code> if the property is accepted, <code>false</code> otherwise.
     */
    boolean accepts(String name, String value);
  }
}
