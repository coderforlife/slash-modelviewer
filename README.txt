The GWT environment for using three.js is a bit complicated to get started with
since GWT does not include native bindings for WebGL or any of the popular
libraries.

To use the three.js you need to use the threejs module from Blimster. There are
a bunch of steps to get it setup. These steps do not cover basic GWT stuff.


===== Step 1: Install Prerequisites =====
Using the Eclipse marketplace you need to install the following packages:
  * Xtext
  * Eclipse Xtend
  * A Git Team provider (such as EGit)
Go to Window > Preferences > Xtend > Compiler. Set the Directory to "src-gen".


===== Step 2: Add WDLs =====
The first thing you must do is add the three.js WDL and UI WDL. These are
needed to be able to compile the threejs module since it uses some fancy
template system to generate most of the necessary classes.

Copy the two JAR files from the threejs-wdl folder to the dropins folder in
your Eclipse install directory (or plugins folder before Ganymede [v3.4]).
Restart Eclipse.

----- Step 2-alt: Compiling WDLs Yourself -----
You may need to compile the WDLs yourself at some point. In that case you will
probably want to fork the Git repository so you can commit changes.
  1. In Eclipse choose File > Import...
  2. Choose to add a Git project
  3. Use https://github.com/coderforlife/three4g.wdl.git
  4. Once the two projects are loaded into Eclipse you need to deploy it:
     a. Choose File > Export...
     b. Choose Plug-In Development > Deployable plug-ins and fragments
     c. Click "Next >"
     d. Check net.blimster.gwt.threejs.wdl and net.blimster.gwt.threejs.wdl.ui
     e. Enter the destination directory as <Eclipse install directory>\dropins
     f. Click "Finish"
  5. Restart Eclipse


===== Step 3: Get threejs GWT module =====
The threejs module mostly uses templates to generate most of the classes. With
the above WDLs in place you can edit these templates and the classes will
automatically update.

The templates are in "src-threejs" while pure Java sources as in "src". If you
plan on modifying it you should probably fork the Git project so you can commit
changes.

  1. In Eclipse choose File > Import...
  2. Choose to add a Git project
  3. Use https://github.com/coderforlife/net.blimster.gwt.threejs.git
  4. Once the project is loaded it will update itself
  5. Make the threejs module a dependency of ModelView:
     a. Right click the ModelView project and choose Properties
     b. Goto "Java Build Path"
     c. On the "Projects" tab click "Add"
     d. Check net.blimster.gwt.threejs and click OK
     e. Goto "Project References"
     f. Check net.blimster.gwt.threejs and click OK

The GWT reference is done in the *.gwt.xml file with the following line:
  <inherits name='net.blimster.gwt.threejs'/>
Which is already in the ModelView module but will be needed in new modules.
