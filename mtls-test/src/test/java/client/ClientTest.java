package client;

import com.intuit.karate.junit5.Karate;

public class ClientTest {
    @Karate.Test
    Karate test() {
        return Karate.run("test").relativeTo(getClass());
    }
}
