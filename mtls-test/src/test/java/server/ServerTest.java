package server;

import com.intuit.karate.junit5.Karate;

import util.KarateTest;

@KarateTest
class ServerTest {
    @Karate.Test
    Karate test() {
        return Karate.run("test").relativeTo(getClass());
    }
}
