java-seed
=========

Java boilerplate collection.

Goal of this project is to create testable wrappers around standard java APIs
and popular libraries.

## Why bother?

Many libraries including standard one have simple and beautiful APIs based on
static functions. Those are very easy to use and discover. 

_Let's see what's wrong with it..._

```java
Path createFile(String fileName) throws IOException {
	return Files.createFile(Paths.get(fileName)); // Very simple to use and read, right?
}
```

_OK, let's write a test for that function_
   
```java
@Test
public void testShouldCreateFile() throws Exception {
	// ... wait, what are we going to check?
	// We cannot mock static functions :(
	// Maybe we can just hit the filesystem then?
	// OK, that seems reasonable but in that case we'll have write proper tearDown()
	// function to make sure we leave no garbage in the FS and that may fail anyway. 
	// Smells bad...
	// Besides what if we are going to test network IO? Database killer-sized queries?
}
```

_Not good at all... We need a coffee break here_

In my biased perfect world all libraries would have an API-object so user could mock
them out for testing.

_I'd like to see something like this_

```java
public class MyClass {
	private final Fs fs; // Purists could make this dependency package-visible and test
	                     // the public constructor
	
	public MyClass() {
		this(Fs.instance());
	}
	
	// We're going to use this constructor for testing
	MyClass(Fs fs) {
		this.fs = fs;
	}
	
	Path createFile(String fileName) throws IOException {
		return fs.createFile(Paths.get(fileName));
	}
}

// Test class
public class MyClassTest {
	@Mock private Fs mockFs;
	@InjectMocks private MyClass mc;
	
	@Before
	public void setUp() {
		MockitoAnnotations.init(this);
	}
	
	@Test
	public void testShouldCreateFile() throws Exception {
		mc.createFile("foo");
		verify(fs).createFile(Paths.get("foo));
	}
}
```

So this project is about such wrappers and a little more.

_Detailed description to come_
