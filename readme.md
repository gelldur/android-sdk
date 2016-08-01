[![Build Status](https://travis-ci.org/sensorberg-dev/android-sdk.svg?branch=master)](https://travis-ci.org/sensorberg-dev/android-sdk?branch=master)
[![Join the chat at https://gitter.im/sensorberg-dev/android-sdk](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sensorberg-dev/android-sdk?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

<div class="callout callout-alert">
    <h1><i class="fa fa-exclamation-triangle"></i>Api key caching is 24hrs currently..</h1>
    <p>There were issues found regarding caching of the api key. <br /> In other words if you change the api in the app this would not be sent until after 24 hrs<br /
    We are working on it and a fix will be out shortly.</p>
</div>

##Warning:
You are viewing the sources of version 2.0.3. The SDK is released. Feel free to explore it.

#Last stable release

The last stable release was v1.2.0.  It is not recommended to use this as v2.0.3 is the latest. For now you can read the old [samples page](https://github.com/sensorberg-dev/android-sdk-samples) page for all neccesary information.
Please be aware, we are not currently updating the samples. We are in process of updating the sdk and will release better and updated samples in the future. 
If you have any specific questions, please reach out to us.

#Integration

For Integration instructions please visit our developer hub Android SDK page and [developer.sensorberg.com/android](https://developer.sensorberg.com/android)

#Build,Test,Deploy

##Dependencies:
* JDK 1.7
* Build tools as stated in the build.gradle files
* check the install_android_dependencies.sh file for a list of currently needed packages.

This project is based on a standard Android Gradle setup. Run

```
	./gradlew tasks
```
to see all the tasks.

```
	./gradlew clean connectedAndroidTest
```
will run all the unit tests.

#Release

Set your release name in the root **build.gradle**. If you want a regular release, leave the **project.ext.RC_VERSION** as an empty string.

paste your credentials in the **bintray.properties** file.

run

``` bash
	./gradlew clean cAT android-sdk:bintrayUpload
```

If you want to test the procedure, change the **dryRun** variable in the **bintrayUpload.gradle** file to *true* temporarely. The --info flag will give you details if you need them.

``` bash
	./gradlew clean android-sdk:bintrayUpload --info
	[...]
	Version 'sensorberg/maven/android-sdk/1.0.0-SNAPSHOT' does not exist. Attempting to creating it...
    (Dry run) Created verion 'sensorberg/maven/android-sdk/1.0.0-SNAPSHOT'.
    Uploading to https://api.bintray.com/content/sensorberg/maven/android-sdk/1.0.0-SNAPSHOT/com/sensorberg/sdk/android-sdk/1.0.0-SNAPSHOT/android-sdk-1.0.0-SNAPSHOT-javadoc.jar...
    (Dry run) Uploaded to 'https://api.bintray.com/content/sensorberg/maven/android-sdk/1.0.0-SNAPSHOT/com/sensorberg/sdk/android-sdk/1.0.0-SNAPSHOT/android-sdk-1.0.0-SNAPSHOT-javadoc.jar'.
    Uploading to https://api.bintray.com/content/sensorberg/maven/android-sdk/1.0.0-SNAPSHOT/com/sensorberg/sdk/android-sdk/1.0.0-SNAPSHOT/android-sdk-1.0.0-SNAPSHOT-sources.jar...
    (Dry run) Uploaded to 'https://api.bintray.com/content/sensorberg/maven/android-sdk/1.0.0-SNAPSHOT/com/sensorberg/sdk/android-sdk/1.0.0-SNAPSHOT/android-sdk-1.0.0-SNAPSHOT-sources.jar'.
    Uploading to https://api.bintray.com/content/sensorberg/maven/android-sdk/1.0.0-SNAPSHOT/com/sensorberg/sdk/android-sdk/1.0.0-SNAPSHOT/android-sdk-1.0.0-SNAPSHOT.aar...
    (Dry run) Uploaded to 'https://api.bintray.com/content/sensorberg/maven/android-sdk/1.0.0-SNAPSHOT/com/sensorberg/sdk/android-sdk/1.0.0-SNAPSHOT/android-sdk-1.0.0-SNAPSHOT.aar'.
    Uploading to https://api.bintray.com/content/sensorberg/maven/android-sdk/1.0.0-SNAPSHOT/com/sensorberg/sdk/android-sdk/1.0.0-SNAPSHOT/android-sdk-1.0.0-SNAPSHOT.pom...
    (Dry run) Uploaded to 'https://api.bintray.com/content/sensorberg/maven/android-sdk/1.0.0-SNAPSHOT/com/sensorberg/sdk/android-sdk/1.0.0-SNAPSHOT/android-sdk-1.0.0-SNAPSHOT.pom'.
    (Dry run) Pulished verion 'sensorberg/maven/android-sdk/1.0.0-SNAPSHOT'.
    :android-sdk:bintrayUpload (Thread[Daemon worker Thread 2,5,main]) completed. Took 1.708 secs.

```

#License
-------

	The MIT License (MIT)
	
	Copyright (c) 2016 Sensorberg GmbH
	
	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
