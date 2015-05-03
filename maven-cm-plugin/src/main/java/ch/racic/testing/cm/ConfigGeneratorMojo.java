package ch.racic.testing.cm;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * @phase process-sources
 */
@Mojo(
        name = "generate",
        requiresProject = true,
        defaultPhase = LifecyclePhase.NONE
)
@Execute(phase = LifecyclePhase.GENERATE_SOURCES)
public class ConfigGeneratorMojo extends AbstractMojo {

    @Component
    protected MavenProject project;

    @Component
    private Settings settings;

    @Parameter(
            required = true,
            readonly = true,
            defaultValue = "${project.build.directory}/generated-sources/config",
            property = "outputDir"
    )
    private File outputDir;

    @Parameter(
            required = true,
            readonly = true,
            defaultValue = "${project.groupId}.gen.config",
            property = "basePackage"
    )
    private String basePackage;

    @Parameter(
            required = true,
            readonly = true,
            defaultValue = "false",
            property = "testSourceOnly"
    )
    private boolean testSourceOnly;

    public void execute() throws MojoExecutionException {
        // TODO generate something
        List<Resource> resources = project.getTestResources();
        resources.addAll(project.getResources());
        // getting root resource folders and check if there is a config/global folder to determine if there are any interesting files to parse?
        List<File> configDirs = filterResources(resources);
        // parse global config properties
        //TODO generate a class for each property file
        //TODO generate a class G which contains a field for each property file class

        // find class folders
        List<File> classConfigDirs = filterClassResources(configDirs);
        // TODO Generate a class C for each class config, lats package name segment corresponding to the class name



        // After all is generated, let's add it to the corresponding source root
        if (testSourceOnly) loadGeneratedTestSources();
        else loadGeneratedSources();

    }

    private List<File> filterClassResources(List<File> configDirs) {
        List<File> classConfigFolders = new ArrayList<File>();
        for (File cDir : configDirs) {
            File classConfig = new File(cDir, ConfigProvider.CONFIG_CLASS_FOLDER);
            if (classConfig.exists() && classConfig.isDirectory()) {
                classConfigFolders.add(classConfig);
            }
        }
        return classConfigFolders;
    }

    private List<File> filterResources(List<Resource> resources) {
        List<File> configFolders = new ArrayList<File>();
        // iterate over the resource folders and see which contain any config folder
        for (Resource res : resources) {
            File root = new File(res.getDirectory());
            File config = new File(root, ConfigProvider.CONFIG_BASE_FOLDER);
            File global = new File(config, ConfigProvider.CONFIG_GLOBAL_BASE_FOLDER);
            if (global.exists() && global.isDirectory()) {
                // Found a global config folder
                configFolders.add(global);
            }
        }
        return configFolders;
    }

    private void loadGeneratedTestSources() {
        if (!settings.isInteractiveMode()) {
            getLog().info(String.format("Adding generated configuration enums from %s to test compile source root",
                    outputDir.getAbsolutePath()));
        }
        project.addTestCompileSourceRoot(outputDir.getAbsolutePath());
    }

    private void loadGeneratedSources() {
        if (!settings.isInteractiveMode()) {
            getLog().info(String.format("Adding generated configuration enums from %s to compile source root",
                    outputDir.getAbsolutePath()));
        }
        project.addTestCompileSourceRoot(outputDir.getAbsolutePath());
    }
}