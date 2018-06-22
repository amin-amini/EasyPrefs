
# EasyPrefs - Simplest Android Shared Preferences

EasyPrefs is an Android library based on Google's Gson fully compatible with Dagger that makes read/write on shared preferences so easy and enables you to store any kind of objects on shared preferences.
It allows you to create facade layers for different shared preferences with few lines of code. Because it generates most of the code for you, you probability won't be able to use wrong names for your keys (which is mostly caused by copy/pasting) :smile:

# Installation
* Add jitpack.io to your root gradle file (project level) :
	```gradle
  allprojects {
  		repositories {
  			...
  			maven { url 'https://jitpack.io' }
  		}
  	}
	```

* Add the dependency in your app build.gradle
	```gradle
  dependencies {
      implementation 'com.github.amin-amini.EasyPrefs:EasyPrefsSchema:1.0.1'
      annotationProcessor 'com.github.amin-amini.EasyPrefs:EasyPrefs:1.0.1'
  }
	```

# Create Easy Prefs Schema
Create a class to describe your objects e.g. `MyPreferencesSchema`,
Returning type of each method indicates your object's type and you can implement your methods to give default values (e.g. when the requested object does not exist in preferences) or leave them unimplemented which cause you to receive `false`, `0` or `null` as default values. It goes without saying that your methods should not contain any arguments.
```java

@EasyPrefsSchema("MyPreferences")
public abstract class MyPreferencesSchema {
    abstract Map<String, ArrayList<Test> > hugeMap();
    double testDouble(){return -1;}
}

```

# That's it! :wink:
Above class will create a class called `MyPreferences` containing 6 main methods (3 methods each) for read/write/delete and a constructor which needs a context, and if you're interested, name of your XML file on disk will also be MyPreferences e.g:

```java
//GENERATED
public class MyPreferences extends MyPreferencesSchema {
...
	public Map<String, ArrayList<Test>> getHugeMap() {...}
	public synchronized void setHugeMap(Map<String, ArrayList<Test>> hugeMap) {...}
	public synchronized boolean deleteHugeMap() {...}
	
    public double getTestDouble() {...}
    public synchronized void setTestDouble(double testDouble) {...}
    public synchronized boolean deleteTestDouble() {}
    ..
}

```

## <b>NOTE: after creation or any changes to your schema you have to rebuild your project so EasyPrefs can regenerate required classes!</b>

# Using generated classes
* **Dagger**
	If you are using a dependency injector (like Dagger) you can create a provider for generated class and use it everywhere e.g. in your `AppModule` you can add the required provider:
	```java
    @Provides @Singleton
    MyPreferences providesSharedPreferences(Application application) {
        return new MyPreferences(application);
    }
	```
	and inject preferences everywhere e.g:
	```java

    public class MainActivity extends AppCompatActivity {
	    @Inject MyPreferences prefs;
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
		    ...
		    MyApplication.getComponent().inject(this);
		    prefs.getHugeMap();
		    prefs.setHugeMap(...);
		    prefs.deleteHugeMap();
		    ...
	    }
	    ...
	}
    
	```
	

* **Static Usage**
	You can also tell EasyPrefs to make singleton object for preferences by following annotation:
	```java

	@EasyPrefsSchema(value = "MyStaticPreferences", useStaticReferences = true)
	public abstract class MyPreferencesSchema {
	    abstract Map<String, ArrayList<Test> > hugeMap();
	    double testDouble(){return -1;}
	}

	```
	And then constructor becomes private and you have to initialize it in your application class:
	```java
    public class MyApplication extends Application application {
	    public void onCreate() {
		    ...
	        MyStaticPreferences.init(this);
	    }
    }
	```
	And in other parts of application use the singleton instance:
		
	```java

    public class MainActivity extends AppCompatActivity {
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
		    ...
		    MyPreferences.i().getHugeMap();
		    MyPreferences.i().setHugeMap(...);
		    MyPreferences.i().deleteHugeMap();
		    ...
	    }
	    ...
	}

* **Normal Usage (NOT RECOMMENDED AT ALL)**
	You can create an instance from generated class (containig normal constructor) and pass it everywhere which is not that much logical :) All you have to do is to create an instance and share it among all objects which require it.
	```java
    public class MyApplication extends Application application {
	    MyPreferences prefs;
	    public void onCreate() {
		    ...
	        prefs = new MyPreferences(this);
	    }
	    public MyPreferences getPrefs(){ 
		    return prefs;
		}
    }
	```
	
	```java

    public class MainActivity extends AppCompatActivity {
	    MyPreferences prefs;
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
		    ...
		    prefs = (MyApplication)getApplication().getPrefs(); //Bad idea of course
		    prefs.getHugeMap();
		    prefs.setHugeMap(...);
		    prefs.deleteHugeMap();
		    ...
	    }
	    ...
	}
    
	```

## <b>NOTE: Once again I'm going to tell you that after any changes to your schema you have to rebuild your project (Build -> Rebuild Project) so EasyPrefs can regenerate required classes!</b>

<b>Special thanks to @yehiahd for his great [FastSave-Android](https://github.com/yehiahd/FastSave-Android) which put the idea of developing EasyPrefs in my brain :smiley:</b>

