package au.org.emii.talend.codegen;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.RepositoryFactoryProvider;
import org.talend.core.repository.utils.LoginTaskRegistryReader;
import org.talend.core.runtime.services.IMavenUIService;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.components.ui.IComponentPreferenceConstant;
import org.talend.designer.codegen.exception.CodeGeneratorException;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.designer.codegen.model.CodeGeneratorEmittersPoolFactory;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.login.ILoginTask;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;

// Eclipse application plugin used to build a talend project from the command line
//
// The required code to connect to the workspace, import the project, log onto the project, initialise the code
// generation engine and build the project has been copied from the TOS startup code which normally triggers these
// actions.  This command line application is called in place of the TOS application which normally triggers these actions:
//
// https://github.com/Talend/tcommon-studio-se/blob/release/7.1.1/main/plugins/org.talend.rcp/src/main/java/org/talend/rcp/intro/Application.java

public class Generator implements IApplication {
    private static Logger log = Logger.getLogger(Generator.class);

    private ProxyRepositoryFactory repository;

    private Project project;

    public Object start(IApplicationContext context) throws Exception {

        // Parse command line arguments for the code generator

        // project/job/version to build
        String jobName = Params.getMandatoryStringOption("-jobName");
        String projectDir = Params.getMandatoryStringOption("-projectDir");
        String version = Params.getStringOption("-version", "Latest");

        // output directory
        String targetDir = Params.getMandatoryStringOption("-targetDir");

        // user components directory
        String componentDir = Params.getStringOption("-componentDir", "");

        // Context Name
        String contextName = Params.getStringOption("-contextName", "Default");

        // job build options

        Map<ExportChoice, Object> exportChoiceMap = getExportOptions();

        // Let talend services know we are running in headless mode
        // so they don't use ui stuff like messageboxes for exceptions
        CommonsPlugin.setHeadless(true);

        // Set the user components folder the code generator should use
        // https://github.com/Talend/tdi-studio-se/blob/master/main/plugins/org.talend.designer.codegen/src/main/java/org/talend/designer/codegen/components/model/UserComponentsProvider.java#L56

        final IPreferenceStore store = CodeGeneratorActivator.getDefault().getPreferenceStore();
        store.setValue(IComponentPreferenceConstant.USER_COMPONENTS_FOLDER, componentDir);

        // Initialise the maven resolver
        // https://github.com/Talend/tcommon-studio-se/blob/release/7.1.1/main/plugins/org.talend.rcp/src/main/java/org/talend/rcp/intro/Application.java#L121

        if (GlobalServiceRegister.getDefault().isServiceRegistered(IMavenUIService.class)) {
            IMavenUIService mavenUIService =
                (IMavenUIService) GlobalServiceRegister.getDefault().getService(IMavenUIService.class);
            if (mavenUIService != null) {
                mavenUIService.checkUserSettings(new NullProgressMonitor());
                mavenUIService.updateMavenResolver(false);
            }
        }

        // Initialise connection to the local repository (the workspace)
        repository = connectToRepository();

        // Import project into workspace
        project = ProjectUtils.importProject(projectDir);

        log.info("Logging onto " + project.getLabel() + "...");

        repository.logOnProject(project, new NullProgressMonitor());

        // Run login tasks to initialise code generation
        // https://github.com/Talend/tcommon-studio-se/blob/release/7.1.1/main/plugins/org.talend.rcp/src/main/java/org/talend/rcp/intro/ApplicationWorkbenchAdvisor.java#L105
        log.info("Running login tasks...");

        LoginTaskRegistryReader loginTaskRegistryReader = new LoginTaskRegistryReader();
        ILoginTask[] allLoginTasks = loginTaskRegistryReader.getAllTaskListInstance();

        for (ILoginTask toBeRun : allLoginTasks) {
            try {
                toBeRun.run(new NullProgressMonitor());
            } catch (Exception e) {
                log.error("Error while execution a login task.", e); //$NON-NLS-1$
            }
        }

        // Wait until code generator has been initialised before trying to build any code
        // https://github.com/Talend/tdi-studio-se/blob/release/7.1.1/main/plugins/org.talend.designer.codegen/src/main/java/org/talend/designer/codegen/CodeGenerator.java#L201
        log.info("Waiting for code generator to be initialised");

        long startTimer = System.currentTimeMillis();
        long endTimer = startTimer;

        try {
            while(!CodeGeneratorEmittersPoolFactory.isInitialized() && endTimer - startTimer < 900000L) {
                Thread.sleep(1000L);
                endTimer = System.currentTimeMillis();
            }
        } catch (InterruptedException var14) {
            log.error(var14.getMessage(), var14);
            throw new CodeGeneratorException(var14);
        }

        if (endTimer - startTimer > 900000L) {
            throw new CodeGeneratorException(Messages.getString("CodeGenerator.JET.TimeOut"));
        }

        // Export the job
        exportJob(project, jobName, targetDir, version, contextName, exportChoiceMap);

        // Log off the project
        log.info("Logging off " + project.getLabel() + "...");

        repository.logOffProject();

        // All good
        return EXIT_OK;
    }

    public void stop() {
        // TODO Auto-generated method stub
    }

    // Build export file
    private void exportJob(Project project, String jobName, String targetDir, String version, String contextName,
                           Map<ExportChoice, Object> exportChoiceMap) throws Exception {

        log.info("Building " + jobName + "...");

        // Get job to build
        ProcessItem job = getJob(jobName, version);

        String destinationPath = targetDir + "/" + job.getProperty().getLabel() + "_" + version + ".zip";

        BuildJobManager.getInstance().buildJob(destinationPath, job, version, contextName, exportChoiceMap, JobExportType.POJO, true, new NullProgressMonitor());
    }

    // Find specified version of job
    private ProcessItem getJob(String jobName, String version)
            throws PersistenceException {
        List<IRepositoryViewObject> processObjects = repository.getAll(
                project, ERepositoryObjectType.PROCESS, false, false);

        for (IRepositoryViewObject processObject : processObjects) {
            if (processObject.getLabel().equals(jobName)) {
                return ItemCacheManager.getProcessItem(processObject.getId(), version);
            }
        }

        throw new RuntimeException("Job " + jobName + " not found");
    }

    // Initialise and connect to the local repository (workspace)
    private ProxyRepositoryFactory connectToRepository()
            throws PersistenceException {

        ProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        repositoryFactory.setRepositoryFactoryFromProvider(RepositoryFactoryProvider.getRepositoriyById("local")); //$NON-NLS-1$
        repositoryFactory.initialize();

        RepositoryContext repositoryContext = new RepositoryContext();
        repositoryContext.setUser(createUser());
        HashMap<String, String> fields = new HashMap<String, String>();
        repositoryContext.setFields(fields);

        Context ctx = CorePlugin.getContext();
        ctx.putProperty(Context.REPOSITORY_CONTEXT_KEY, repositoryContext);

        return repositoryFactory;
    }

    // Read export options from the command line
    private Map<ExportChoice, Object> getExportOptions() {
        Map<ExportChoice, Object> exportChoiceMap = new EnumMap<ExportChoice, Object>(ExportChoice.class);

        exportChoiceMap.put(ExportChoice.needLauncher, Params.getBooleanOption("-needLauncher", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.launcherName, Params.getStringOption("-launcherName", "Unix"));
        exportChoiceMap.put(ExportChoice.needSystemRoutine, Params.getBooleanOption("-needSystemRoutine", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.needUserRoutine, Params.getBooleanOption("-needUserRoutine", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.needTalendLibraries, Params.getBooleanOption("-needTalendLibraries", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.needJobItem, Params.getBooleanOption("-needJobItem", Boolean.FALSE));
        exportChoiceMap.put(ExportChoice.needJobScript, Params.getBooleanOption("-needJobScript", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.needSourceCode, Params.getBooleanOption("-needSourceCode", Boolean.FALSE));
        exportChoiceMap.put(ExportChoice.includeLibs, Params.getBooleanOption("-includeLibs", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.includeTestSource, Params.getBooleanOption("-includeTestSource", Boolean.FALSE));
        exportChoiceMap.put(ExportChoice.executeTests, Params.getBooleanOption("-executeTests", Boolean.FALSE));
        exportChoiceMap.put(ExportChoice.binaries, Params.getBooleanOption("-binaries", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.needContext, Params.getBooleanOption("-needContext", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.contextName, Params.getStringOption("-contextName", "Default"));
        exportChoiceMap.put(ExportChoice.applyToChildren, Params.getBooleanOption("-applyToChildren", Boolean.FALSE));
        exportChoiceMap.put(ExportChoice.needLog4jLevel, Params.getBooleanOption("-needLog4jLevel", Boolean.FALSE));
        exportChoiceMap.put(ExportChoice.log4jLevel, Params.getStringOption("-log4jLevel", null));
        exportChoiceMap.put(ExportChoice.addStatistics, Params.getBooleanOption("-addStatistics", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.needDependencies, Params.getBooleanOption("-needDependencies", Boolean.TRUE));
        exportChoiceMap.put(ExportChoice.needParameterValues, Params.getBooleanOption("-needParameterValues", Boolean.FALSE));

        return exportChoiceMap;
    }

    // Create a new user
   private User createUser() {
        User user = PropertiesFactory.eINSTANCE.createUser();
        user.setLogin("user@talend.com"); //$NON-NLS-1$
        return user;
    }

}
