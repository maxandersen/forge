/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.forge.test.grammar.java.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.seam.forge.parser.java.Annotation;
import org.jboss.seam.forge.parser.java.AnnotationTarget;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AnnotationTest
{
   private AnnotationTarget<?> target;

   protected AnnotationTarget<?> getTarget()
   {
      return target;
   }

   protected void setTarget(final AnnotationTarget<?> target)
   {
      this.target = target;
   }

   @Before
   public void reset()
   {
      resetTests();
   }

   public abstract void resetTests();

   @Test
   public void testParseAnnotation() throws Exception
   {
      List<Annotation> annotations = target.getAnnotations();
      assertEquals(4, annotations.size());
      assertEquals("deprecation", annotations.get(1).getStringValue());
      assertEquals("deprecation", annotations.get(1).getStringValue("value"));
      assertEquals("value", annotations.get(1).getValues().get(0).getName());
      assertEquals("deprecation", annotations.get(1).getValues().get(0).getStringValue());

      assertEquals("unchecked", annotations.get(2).getStringValue("value"));
      assertEquals("unchecked", annotations.get(2).getStringValue());
      assertEquals("value", annotations.get(2).getValues().get(0).getName());
      assertEquals("unchecked", annotations.get(2).getValues().get(0).getStringValue());
   }

   @Test
   public void testAddAnnotation() throws Exception
   {
      int size = target.getAnnotations().size();
      Annotation annotation = target.addAnnotation().setName("RequestScoped");
      List<Annotation> annotations = target.getAnnotations();
      assertEquals(size + 1, annotations.size());
      assertEquals("RequestScoped", annotation.getName());
   }

   @Test
   public void testAddAnnotationByClass() throws Exception
   {
      int size = target.getAnnotations().size();
      Annotation annotation = target.addAnnotation(Test.class);
      List<Annotation> annotations = target.getAnnotations();
      assertEquals(size + 1, annotations.size());
      assertEquals(Test.class.getSimpleName(), annotation.getName());
      assertTrue(target.toString().contains("@" + Test.class.getSimpleName()));
   }

   @Test
   public void testAddAnnotationByName() throws Exception
   {
      int size = target.getAnnotations().size();
      Annotation annotation = target.addAnnotation("RequestScoped");
      List<Annotation> annotations = target.getAnnotations();
      assertEquals(size + 1, annotations.size());
      assertEquals("RequestScoped", annotation.getName());
      assertTrue(target.toString().contains("@RequestScoped"));
   }

   @Test
   public void testCanAddAnnotationDuplicate() throws Exception
   {
      int size = target.getAnnotations().size();
      Annotation anno1 = target.addAnnotation(Test.class);
      Annotation anno2 = target.addAnnotation(Test.class);
      List<Annotation> annotations = target.getAnnotations();
      assertEquals(size + 2, annotations.size());
      assertEquals(Test.class.getSimpleName(), anno1.getName());
      assertEquals(Test.class.getSimpleName(), anno2.getName());
      String pattern = "@" + Test.class.getSimpleName() + " " + "@" + Test.class.getSimpleName();
      assertTrue(target.toString().contains(pattern));
   }

   @Test(expected = IllegalArgumentException.class)
   public void testCannotAddAnnotationWithIllegalName() throws Exception
   {
      target.addAnnotation("sdf*(&#$%");
   }

   @Test
   public void testAddEnumValue() throws Exception
   {
      target.addAnnotation(Test.class).setEnumValue(MockEnum.FOO);

      List<Annotation> annotations = target.getAnnotations();

      Annotation annotation = annotations.get(annotations.size() - 1);
      MockEnum enumValue = annotation.getEnumValue(MockEnum.class);
      assertEquals(MockEnum.FOO, enumValue);
   }

   @Test
   public void testAddEnumNameValue() throws Exception
   {
      target.addAnnotation(Test.class).setEnumValue("name", MockEnum.BAR);

      List<Annotation> annotations = target.getAnnotations();

      Annotation annotation = annotations.get(annotations.size() - 1);
      MockEnum enumValue = annotation.getEnumValue(MockEnum.class, "name");
      assertEquals(MockEnum.BAR, enumValue);
   }

   @Test
   public void testAddLiteralValue() throws Exception
   {
      int size = target.getAnnotations().size();

      target.addAnnotation(Test.class).setLiteralValue("435");

      List<Annotation> annotations = target.getAnnotations();
      assertEquals(size + 1, annotations.size());

      Annotation annotation = annotations.get(annotations.size() - 1);
      assertEquals(Test.class.getSimpleName(), annotation.getName());
      assertEquals("435", annotation.getLiteralValue());
   }

   @Test
   public void testAddObjectValue() throws Exception
   {
      int size = target.getAnnotations().size();

      target.addAnnotation(Test.class).setLiteralValue("expected", "RuntimeException.class")
               .setLiteralValue("foo", "bar");

      List<Annotation> annotations = target.getAnnotations();
      assertEquals(size + 1, annotations.size());

      Annotation annotation = annotations.get(annotations.size() - 1);
      assertEquals(Test.class.getSimpleName(), annotation.getName());
      assertEquals(null, annotation.getLiteralValue());
      assertEquals("RuntimeException.class", annotation.getLiteralValue("expected"));
      assertEquals("bar", annotation.getLiteralValue("foo"));
   }

   @Test
   public void testAddValueConvertsToNormalAnnotation() throws Exception
   {
      target.addAnnotation(Test.class).setLiteralValue("RuntimeException.class");
      Annotation annotation = target.getAnnotations().get(target.getAnnotations().size() - 1);

      assertEquals("RuntimeException.class", annotation.getLiteralValue());
      assertTrue(annotation.isSingleValue());

      annotation.setLiteralValue("foo", "bar");
      assertFalse(annotation.isSingleValue());
      assertTrue(annotation.isNormal());

      assertEquals("RuntimeException.class", annotation.getLiteralValue());
      assertEquals("RuntimeException.class", annotation.getLiteralValue("value"));
      assertEquals("bar", annotation.getLiteralValue("foo"));
   }

   @Test
   public void testAnnotationBeginsAsMarker() throws Exception
   {
      Annotation anno = target.addAnnotation(Test.class);
      assertTrue(anno.isMarker());
      assertFalse(anno.isSingleValue());
      assertFalse(anno.isNormal());

      anno.setLiteralValue("\"Foo!\"");
      assertFalse(anno.isMarker());
      assertTrue(anno.isSingleValue());
      assertFalse(anno.isNormal());

      anno.setStringValue("bar", "Foo!");
      assertFalse(anno.isMarker());
      assertFalse(anno.isSingleValue());
      assertTrue(anno.isNormal());

      assertEquals("\"Foo!\"", anno.getLiteralValue("bar"));
      assertEquals("Foo!", anno.getStringValue("bar"));

      anno.removeAllValues();
      assertTrue(anno.isMarker());
      assertFalse(anno.isSingleValue());
      assertFalse(anno.isNormal());
   }

   @Test
   public void testHasAnnotationClassType() throws Exception
   {
      target.addAnnotation(Test.class);
      assertTrue(target.hasAnnotation(Test.class));
   }

   @Test
   public void testHasAnnotationStringType() throws Exception
   {
      target.addAnnotation(Test.class);
      assertTrue(target.hasAnnotation("Test"));
      assertTrue(target.hasAnnotation(Test.class.getName()));
   }

   @Test
   public void testHasAnnotationStringTypeSimple() throws Exception
   {
      target.addAnnotation(Test.class);
      assertNotNull(target.getAnnotation("Test"));
      assertNotNull(target.getAnnotation(Test.class.getSimpleName()));
   }

   @Test
   public void testGetAnnotationClassType() throws Exception
   {
      target.addAnnotation(Test.class);
      assertNotNull(target.getAnnotation(Test.class));
   }

   @Test
   public void testGetAnnotationStringType() throws Exception
   {
      target.addAnnotation(Test.class);
      assertNotNull(target.getAnnotation("org.junit.Test"));
      assertNotNull(target.getAnnotation(Test.class.getName()));
   }

   @Test
   public void testGetAnnotationStringTypeSimple() throws Exception
   {
      target.addAnnotation(Test.class);
      assertTrue(target.hasAnnotation("Test"));
      assertTrue(target.hasAnnotation(Test.class.getSimpleName()));
   }

   @Test
   public void testRemoveAllValues() throws Exception
   {
      target.addAnnotation(Test.class).setLiteralValue("expected", "RuntimeException.class");

      List<Annotation> annotations = target.getAnnotations();
      Annotation annotation = annotations.get(annotations.size() - 1);
      annotation.removeAllValues();

      assertEquals(0, annotation.getValues().size());
   }
}
