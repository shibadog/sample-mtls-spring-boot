package client.test;

import org.junit.jupiter.api.DisplayName;

import com.intuit.karate.junit5.Karate;

import util.KarateTest;

@DisplayName("Clientテスト")
@KarateTest
public class ClientTest {

    @DisplayName("疎通のためのテスト")
    @Karate.Test
    Karate test() {
        return Karate.run("test").relativeTo(getClass());
    }
}
