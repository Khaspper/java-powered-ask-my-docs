package com.khaspper.quickstart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


// ./mvnw clean compile
// mvn [options] [<goal(s)>] [<phase(s)>]
// maven has 3 phases clean, default and site
// a phase is a lifecycle
// clean is used to remove temporary directories and files
	// pre-clean hook for before cleaning
	// clean does the actual cleaning
	// post-clean hook for after cleaning

// default is where the most useful goals live
	// compile compiles your code into bytecode
	// test runs unit tests
	// package creates a jar or war file
	// verify runs checks and integration tests
// this is in order... so if you just want to run test you have to do <./mvnw test> and it will run compile and test
// apply that same logic with verify

// site is where the documentation is generated
@SpringBootTest
class QuickstartApplicationTests {

	@Test
	void contextLoads() {
	}

}
