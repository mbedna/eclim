/**
 * Copyright (C) 2005 - 2012  Eric Van Dewoestine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eclim.plugin.jdt.command.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import org.eclim.Eclim;

import org.eclim.plugin.jdt.Jdt;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for ImplCommand.
 *
 * @author Eric Van Dewoestine
 */
public class ImplCommandTest
{
  private static final String TEST_FILE =
    "src/org/eclim/test/impl/TestImpl.java";
  private static final String TEST_SUB_FILE =
    "src/org/eclim/test/impl/TestSubImpl.java";

  @Test
  @SuppressWarnings("unchecked")
  public void execute()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-o", "83", "-e", "utf-8"
      });

    assertEquals("org.eclim.test.impl.TestImpl", result.get("type"));

    List<Map<String,Object>> superTypes =
      (List<Map<String,Object>>)result.get("superTypes");
    assertEquals(4, superTypes.size());

    assertEquals("java.util", superTypes.get(1).get("packageName"));
    assertEquals("class HashMap<Integer,String>",
        superTypes.get(0).get("signature"));
    HashSet<String> methods = new HashSet<String>(
        (List<String>)superTypes.get(0).get("methods"));
    assertTrue(methods.contains("public void clear()"));
    assertTrue(methods.contains("public Set<Entry<Integer,String>> entrySet()"));
    assertTrue(methods.contains("public String put(Integer,String)"));

    assertEquals("java.util", superTypes.get(3).get("packageName"));
    assertEquals("interface Comparator<String>",
        superTypes.get(3).get("signature"));
    methods = new HashSet<String>((List<String>)superTypes.get(3).get("methods"));
    assertTrue(methods.contains("public abstract int compare(String,String)"));

    result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_FILE,
        "-t", "org.eclim.test.impl.TestImpl",
        "-s", "java.util.HashMap", "-m", "[\"put(Integer,String)\"]"
      });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public String put\\(Integer key, String value\\)")
        .matcher(contents).find());

    superTypes = (List<Map<String,Object>>)result.get("superTypes");
    assertEquals("java.util", superTypes.get(0).get("packageName"));
    assertEquals("class HashMap<Integer,String>", superTypes.get(0).get("signature"));
    methods = new HashSet<String>((List<String>)superTypes.get(0).get("methods"));
    assertFalse(methods.contains("public String put(Integer,String)"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void executeSub()
  {
    assertTrue("Java project doesn't exist.",
        Eclim.projectExists(Jdt.TEST_PROJECT));

    Map<String,Object> result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_SUB_FILE,
        "-o", "83", "-e", "utf-8"
      });

    assertEquals("org.eclim.test.impl.TestSubImpl", result.get("type"));

    List<Map<String,Object>> superTypes =
      (List<Map<String,Object>>)result.get("superTypes");

    assertEquals("org.eclim.test.impl", superTypes.get(0).get("packageName"));
    assertEquals("class TestImpl", superTypes.get(0).get("signature"));
    HashSet<String> methods = new HashSet<String>(
        (List<String>)superTypes.get(0).get("methods"));
    assertTrue(methods.contains("public String put(Integer,String)"));

    result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_SUB_FILE,
        "-t", "org.eclim.test.impl.TestSubImpl",
        "-s", "org.eclim.test.impl.TestImpl", "-m", "[\"put(Integer,String)\"]"
      });

    String contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_SUB_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public String put\\(Integer key, String value\\)")
        .matcher(contents).find());

    superTypes = (List<Map<String,Object>>)result.get("superTypes");
    assertEquals("java.util", superTypes.get(1).get("packageName"));
    assertEquals("class HashMap<Integer,String>", superTypes.get(0).get("signature"));
    methods = new HashSet<String>((List<String>)superTypes.get(0).get("methods"));
    assertFalse(methods.contains("public String put(Integer,String)"));

    assertEquals("java.util", superTypes.get(3).get("packageName"));
    assertEquals("interface Comparator<String>",
        superTypes.get(3).get("signature"));
    methods = new HashSet<String>((List<String>)superTypes.get(3).get("methods"));
    assertTrue(methods.contains("public abstract int compare(String,String)"));

    result = (Map<String,Object>)
      Eclim.execute(new String[]{
        "java_impl", "-p", Jdt.TEST_PROJECT,
        "-f", TEST_SUB_FILE,
        "-t", "org.eclim.test.impl.TestSubImpl",
        "-s", "java.util.Comparator",
      });

    contents = Eclim.fileToString(Jdt.TEST_PROJECT, TEST_SUB_FILE);
    assertTrue("Method not found or invalid.",
        Pattern.compile("public int compare\\(String o1, String o2\\)")
        .matcher(contents).find());

    superTypes = (List<Map<String,Object>>)result.get("superTypes");
    assertEquals(3, superTypes.size());
  }
}
