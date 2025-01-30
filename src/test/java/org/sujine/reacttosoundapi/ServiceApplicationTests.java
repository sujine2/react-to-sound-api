package org.sujine.reacttosoundapi;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
public class ServiceApplicationTests {
    @Test
    public void contextLoads() {
        var am = ApplicationModules.of(ReactToSoundApiApplication.class);
        am.verify();
        System.out.println(am);

        new Documenter(am).writeDocumentation();
    }
}
