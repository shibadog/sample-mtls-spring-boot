package server;

import com.intuit.karate.junit5.Karate;

class ServerTest {
    @Karate.Test
    Karate test() {
        return Karate.run("test").relativeTo(getClass());
    }
}
