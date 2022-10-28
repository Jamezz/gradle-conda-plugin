package io.logbee.gradle.conda.conda.actions;

import io.logbee.gradle.conda.conda.CondaPluginExtension;
import io.logbee.gradle.conda.conda.MinicondaExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.internal.os.OperatingSystem;

import javax.inject.Inject;
import java.io.File;

public class CreateCondaEnvironmentAction implements Action<Project> {

    private final ExecActionFactory execActionFactory;
    private final String CONDA_ENVIRONMENT_NAME = "gradle-conda-plugin-env";

    @Inject
    public CreateCondaEnvironmentAction(ExecActionFactory execActionFactory) {
        this.execActionFactory = execActionFactory;
    }

    @Override
    public void execute(Project project) {
        final MinicondaExtension minicondaExtension = project.getExtensions().getByType(MinicondaExtension.class);
        final CondaPluginExtension condaPluginExtension = project.getExtensions().getByType(CondaPluginExtension.class);
        final ExecAction action = execActionFactory.newExecAction();
        final File dir = condaPluginExtension.getEnvironmentDir();
        final OperatingSystem os = OperatingSystem.current();

        if (os.isWindows()) {
            action.executable("cmd");
            action.args("/C", minicondaExtension.getCondaExecutable(), "create", "--prefix", dir);
        } else {
            action.executable(minicondaExtension.getCondaExecutable());
            action.args("create", "--prefix", dir);
        }

        if (!dir.exists()) {
            try {
                action.execute();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create conda-environment in: " + dir, e);
            }
        }

        //activateEnvironment(dir);
    }

    // private void activateEnvironment(String environmentName) {
    //     final MinicondaExtension minicondaExtension = project.getExtensions().getByType(MinicondaExtension.class);
    //     final ExecAction action = execActionFactory.newExecAction();

    //     action.executable(minicondaExtension.getCondaExecutable());
    //     action.args("activate", CONDA_ENVIRONMENT_NAME);
    // }
}
