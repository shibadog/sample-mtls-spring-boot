Feature: Request it for the time being

    Background:
        * configure ssl = { keyStore: 'classpath:server-truststore.p12', keyStorePassowrd: 'changeit', keyStoreType: 'pkcs12', trustStore: 'classpath:server-truststore.p12', trustStorePassword: 'changeit', trustStoreType: 'pkcs12' }
        * url "https://localhost:9443"

    Scenario: Check runnning
        When method get
        Then status 200
        * assert response == "OK"
        * print response
