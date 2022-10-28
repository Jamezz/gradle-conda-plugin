package io.logbee.gradle.conda.conda.actions;

import io.logbee.gradle.conda.conda.CondaPluginExtension;
import io.logbee.gradle.conda.conda.MinicondaExtension;
import io.logbee.gradle.conda.plugin.CondaPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.process.internal.ExecAction;
import org.gradle.process.internal.ExecActionFactory;
import org.slf4j.*;
import org.gradle.internal.os.OperatingSystem;

import javax.inject.Inject;
import java.io.File;

public class BootstrapMinicondaAction implements Action<Project> {

    private final ExecActionFactory execActionFactory;
    private final Logger log = LoggerFactory.getLogger(BootstrapMinicondaAction.class);

    @Inject
    public BootstrapMinicondaAction(ExecActionFactory execActionFactory) {
        this.execActionFactory = execActionFactory;
    }

    @Override
    public void execute(Project project) {
        final Configuration configuration = project.getConfigurations().getByName(CondaPlugin.MINICONDA_INSTALLER_CONFIGURATION_NAME);
        final MinicondaExtension miniconda = project.getExtensions().getByType(MinicondaExtension.class);
        final File installationDir = miniconda.getInstallationDir();

        if (!installationDir.exists()) {
            installMiniconda(miniconda, configuration);
            installConda(miniconda);
        }
    }

    private void installMiniconda(MinicondaExtension miniconda, Configuration configuration) {
        final ExecAction action = execActionFactory.newExecAction();
        final File installationDir = miniconda.getInstallationDir();
        final OperatingSystem os = OperatingSystem.current();

        if (os.isWindows()) {
            action.executable(configuration.getSingleFile());
            log.info("Installing miniconda to directory: {}", installationDir);
            action.args("/S", String.format("/D=%s", installationDir));
        } else {
            action.executable("bash");
            action.args(configuration.getSingleFile());
            action.args("-b", "-p", installationDir);
        }

        action.execute();
    }

    private void installConda(MinicondaExtension miniconda) {
        final ExecAction action = execActionFactory.newExecAction();

        action.executable(miniconda.getCondaExecutable());
        action.args("install", "--yes", "conda");

        action.execute();
    }
}
