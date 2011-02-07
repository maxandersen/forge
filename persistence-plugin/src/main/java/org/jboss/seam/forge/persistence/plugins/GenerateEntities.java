package org.jboss.seam.forge.persistence.plugins;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import org.hibernate.cfg.JDBCMetaDataConfiguration;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringSettings;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.tool.hbm2x.ArtifactCollector;
import org.hibernate.tool.hbm2x.POJOExporter;
import org.jboss.seam.forge.parser.JavaParser;
import org.jboss.seam.forge.parser.java.Field;
import org.jboss.seam.forge.parser.java.JavaClass;
import org.jboss.seam.forge.parser.java.util.Refactory;
import org.jboss.seam.forge.persistence.PersistenceFacet;
import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.constraints.RequiresFacet;
import org.jboss.seam.forge.project.constraints.RequiresProject;
import org.jboss.seam.forge.project.facets.JavaSourceFacet;
import org.jboss.seam.forge.shell.PromptType;
import org.jboss.seam.forge.shell.Shell;
import org.jboss.seam.forge.shell.plugins.DefaultCommand;
import org.jboss.seam.forge.shell.plugins.Help;
import org.jboss.seam.forge.shell.plugins.Option;
import org.jboss.seam.forge.shell.plugins.Plugin;
import org.jboss.seam.forge.shell.plugins.Topic;

@Singleton
@Named("generate-entities")
@Topic("Project")
@RequiresProject
@RequiresFacet(PersistenceFacet.class)
@Help("Generate entities from a database.")
public class GenerateEntities implements Plugin
{

   private final Instance<Project> projectInstance;

   private final Shell shell;
   private Project lastProject;

   @Inject
   public GenerateEntities(final Instance<Project> projectInstance, final Shell shell)
   {
      this.projectInstance = projectInstance;
      this.shell = shell;
   }

   @DefaultCommand(help = "Generate entities from a datasource")
   public void newEntity(
            @Option(required = false,
                     name = "catalog",
                     description = "Catalog selection", defaultValue="%") final String catalogFilter,
                     @Option(required = false,
                           name = "schema",
                           description = "Schema selection", defaultValue="%") final String schemaFilter,
                     @Option(required = true,
                            name = "table",
                                 description = "Table selection", defaultValue="%") final String tableFilter)
   {
      
      Project project = projectInstance.get();
      PersistenceFacet scaffold = project.getFacet(PersistenceFacet.class);
      JavaSourceFacet javaFacet = project.getFacet(JavaSourceFacet.class);
      String entityPackage = shell.promptCommon(
               "In which package you'd like to create this @Entity, or enter for default:",
               PromptType.JAVA_PACKAGE, scaffold.getEntityPackage());

      
      this.lastProject = project;

      JDBCMetaDataConfiguration jmdc = new JDBCMetaDataConfiguration();
      
      Properties properties = new Properties();
      properties.setProperty("hibernate.connection.driver_class", shell.getProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver"));
      properties.setProperty("hibernate.connection.username", shell.getProperty("hibernate.connection.username", "sa"));
      properties.setProperty("hibernate.connection.password", shell.getProperty("hibernate.connection.password", ""));
      properties.setProperty("hibernate.connection.url", shell.getProperty("hibernate.connection.url", "jdbc:hsqldb:hsql://localhost:1701"));
      
      jmdc.setProperties(properties);
      DefaultReverseEngineeringStrategy defaultStrategy = new DefaultReverseEngineeringStrategy();
      ReverseEngineeringStrategy strategy = defaultStrategy;
      
      ReverseEngineeringSettings revengsettings = 
         new ReverseEngineeringSettings(strategy).setDefaultPackageName(entityPackage)
        // .setDetectManyToMany( detectManyToMany )
        // .setDetectOneToOne( detectOneToOne )
        // .setDetectOptimisticLock( detectOptimisticLock );
         ;
   
      defaultStrategy.setSettings(revengsettings);
      strategy.setSettings(revengsettings);
      
      jmdc.setReverseEngineeringStrategy(strategy);
        
      jmdc.readFromJDBC(); 
      
      Iterator iter = jmdc.getTableMappings();
      int count = 0;
      while(iter.hasNext()) {
         count++;
         iter.next();
      }
     
      shell.println("Found " + count + " tables in datasource");
      
      POJOExporter pj = new POJOExporter(jmdc, javaFacet.getSourceFolder());
      
      ArtifactCollector artifacts = new ArtifactCollector() {
         @Override
         public void addFile(File file, String type)
         {
            shell.println("Generated " + type + " at " + file.getPath());
            super.addFile(file, type);
         }
      };
      pj.setArtifactCollector(artifacts);
      
      pj.start();
      
      shell.println("Generated " + artifacts.getFileCount("java") + " java files.");
     }
}
