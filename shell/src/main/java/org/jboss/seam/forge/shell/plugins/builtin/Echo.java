/*
 * JBoss, by Red Hat.
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

package org.jboss.seam.forge.shell.plugins.builtin;

import javax.inject.Inject;

import org.jboss.seam.forge.shell.Shell;
import org.jboss.seam.forge.shell.plugins.*;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Help("Writes input to output.")
@Topic("Shell Environment")
public class Echo implements Plugin
{
   @Inject
   Shell shell;

   @DefaultCommand
   public void run(@Option(help = "The text to be echoed") final String... tokens)
   {
      String input = null;
      if (tokens != null)
      {
         input = "";
         for (String token : tokens)
         {
            input = input + " " + token;
         }
         input = input.trim();
      }

      if (input == null)
      {
         input = shell.prompt();
      }
      shell.println(input);
   }
}
