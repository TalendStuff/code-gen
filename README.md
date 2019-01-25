talend-codegen
==============

Command line code generation (job export) plugin for talend open studio 7.1.1

Building/Installation
---------------------

 * mvn clean package
 * Copy generated .jar in target directory to plugins directory of Talend

Usage
-----

Invoke talend with the following mandatory command line arguments:
 * -projectDir - the project directory where the project can be found
 * -jobName - name of the job to be exported
 * -targetDir - the directory where the exported job will be placed

Eclipse application arguments
 * -application au.org.emii.talend.codegen.Generator - run the code generation plugin 
 * -nosplash stops the display of the gui splash window
 * --launcher.suppressErrors stops errors being displayed in message boxes - output to stderr instead
 * -data specifies the talend workspace used for building the project - created automatically if it doesn't exist (recommended to ensure a clean build)
 * --clean_component_cache tells TOS to reload external components and rebuild the cache
 
Some optional command line arguments you can have:
 * -version - version of job to be exported
 * -componentDir - location of any custom components used in the job
 * -needLauncher - include launcher script (true/false)
 * -needSystemRoutine - include system outines (true/false)
 * -needUserRoutine - and so on..
 * -needTalendLibraries
 * -needJobItem
 * -needSourceCode
 * -needDependencies
 * -needJobScript
 * -needContext
 * -applyToChildren

Example
-------

This example is taken from our Jenkins build process - $WORKSPACE is the location of the talend project

TOS_DI-linux-gtk-x86_64 -nosplash --launcher.suppressErrors -data $WORKSPACE/../.talend-workspace --clean_component_cache -application au.org.emii.talend.codegen.Generator -jobName ThreddsExample -projectDir $WORKSPACE -targetDir $WORKSPACE/.talend-build -componentDir /par2/git-repos/talend_components
 
 
